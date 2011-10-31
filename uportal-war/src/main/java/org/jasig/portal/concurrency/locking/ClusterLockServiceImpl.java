/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.concurrency.locking;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.hibernate.LockOptions;
import org.hibernate.TransactionException;
import org.jasig.portal.utils.ConcurrentMapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.MapMaker;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class ClusterLockServiceImpl implements IClusterLockService, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final ConcurrentMap<String, ReentrantLock> localLocks = new MapMaker().weakValues().makeMap();

    private final Cache<Integer, TransactionOperations> transactionOperations = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(new CacheLoader<Integer, TransactionOperations>() {
                @Override
                public TransactionOperations load(Integer key) throws Exception {
                    int timeout = key.intValue();
                    if (timeout >= 0) {
                        //There is setup/teardown time involved in the db based locking, add 1 second to the lock timeout to avoid over-eager failures
                        timeout = timeout + 1;
                    }
                    
                    if (platformTransactionManager == null) {
                        throw new IllegalStateException("Cannot use transactionOperations cache until after setPlatformTransactionManager has been called");
                    }
                    
                    final TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
                    transactionTemplate.setTimeout(timeout);
                    //All locking related work MUST happen in its own transaction
                    transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    transactionTemplate.afterPropertiesSet();
                    return transactionTemplate;
                }
            });
    
    
    
    private IClusterLockDao clusterLockDao;
    private PlatformTransactionManager platformTransactionManager;
    private TransactionOperations noTimeoutTransactionOperations;
    private TransactionOperations noWaitTransactionOperations;

    @Autowired
    public void setClusterLockDao(IClusterLockDao clusterLockDao) {
        this.clusterLockDao = clusterLockDao;
    }

    @Autowired
    public void setPlatformTransactionManager(@Qualifier("PortalDb") PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.noTimeoutTransactionOperations = this.transactionOperations.getUnchecked(TransactionDefinition.TIMEOUT_DEFAULT);
        this.noWaitTransactionOperations = this.transactionOperations.getUnchecked(LockOptions.NO_WAIT);
    }
    
    protected TransactionOperations getTransactionTemplate(int timeout) {
        if (timeout == TransactionDefinition.TIMEOUT_DEFAULT) {
            return this.noTimeoutTransactionOperations;
        }
        if (timeout == LockOptions.NO_WAIT) {
            return this.noWaitTransactionOperations;
        }
        
        return this.transactionOperations.getUnchecked(timeout);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.locking.IClusterLockService#doInLock(java.lang.String, com.google.common.base.Function)
     */
    @Override
    public <T> T doInLock(final String mutexName, final Function<String, T> lockFunction) {
        this.logger.trace("doInLock({})", mutexName);
        
        final ReentrantLock lock = getLocalLock(mutexName);
        lock.lock();
        try {
            this.logger.trace("doInLock({}) - aquired local lock", mutexName);
            
            return this.noTimeoutTransactionOperations.execute(new TransactionCallback<T>() {
                @Override
                public T doInTransaction(TransactionStatus status) {
                    final ClusterMutex mutex = getClusterMutex(mutexName);
                    
                    logger.trace("doInLock({}) - found {}", mutexName, mutex);
                    
                    clusterLockDao.lock(mutex);
                    
                    logger.trace("doInLock({}) - db locked {}", mutexName, mutex);
                    
                    return lockFunction.apply(mutexName);
                }
            });
        }
        finally {
            logger.trace("doInLock({}) - db unlocked", mutexName);
            lock.unlock();
            this.logger.trace("doInLock({}) - released local lock", mutexName);
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.locking.IClusterLockService#doInTryLock(java.lang.String, com.google.common.base.Function)
     */
    @Override
    public <T> TryLockFunctionResult<T> doInTryLock(String mutexName, Function<String, T> lockFunction) {
        return this.doInTryLock(mutexName, LockOptions.NO_WAIT, TimeUnit.MILLISECONDS, lockFunction);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.locking.IClusterLockService#doInTryLock(java.lang.String, long, java.util.concurrent.TimeUnit, com.google.common.base.Function)
     */
    @Override
    public <T> TryLockFunctionResult<T> doInTryLock(final String mutexName, final long time,
            final TimeUnit unit, final Function<String, T> lockFunction) {
        //Capture start time since we may have to deal with two different lock timeouts
        final long startTime = System.currentTimeMillis();
        final long timeout = unit.toMillis(time);
        
        this.logger.trace("doInTryLock({}, {})", mutexName, timeout);
        
        final ReentrantLock lock = getLocalLock(mutexName);
        
        //Try to get the local lock and determine the tx operations to use based on the lock wait duration
        final TransactionOperations transactionOperations;
        final boolean localLocked;
        if (time == LockOptions.NO_WAIT) {
            transactionOperations = this.noWaitTransactionOperations;
            localLocked = lock.tryLock();
        }
        else {
            transactionOperations = this.getTransactionTemplate((int)unit.toSeconds(time));
            localLocked = lock.tryLock();
        }
        
        //If the local lock failed return immediately
        if (!localLocked) {
            this.logger.trace("doInTryLock({}, {}) - failed to aquire local lock, returning notExecuted result", mutexName, timeout);
            return TryLockFunctionResult.getNotExecutedInstance();
        }
        
        try {
            this.logger.trace("doInTryLock({}, {}) - aquired local lock", mutexName, timeout);
            
            return transactionOperations.execute(new TransactionCallback<TryLockFunctionResult<T>>() {
                @Override
                public TryLockFunctionResult<T> doInTransaction(TransactionStatus status) {

                    final ClusterMutex mutex = getClusterMutex(mutexName);
                    
                    logger.trace("doInTryLock({}, {}) - found {}", new Object[] { mutexName, timeout, mutex });
                    
                    //Since there may have been time spent waiting for the local lock recalculate the
                    //time to spend waiting for a DB lock, if the recalculated time is less than 0 
                    //return immediately
                    final long lockTime;
                    if (time == LockOptions.NO_WAIT) {
                        lockTime = LockOptions.NO_WAIT;
                    }
                    else {
                        lockTime = timeout - (startTime - System.currentTimeMillis());
                        if (lockTime < 0) {
                            logger.trace("doInTryLock({}, {}) - timeout passed before attempting aquisition of {}", new Object[] { mutexName, timeout, mutex });
                            return TryLockFunctionResult.getNotExecutedInstance();
                        }
                    }
                    
                    //Try to acquire the DB side lock
                    final boolean dbLocked = clusterLockDao.tryLock(mutex, lockTime, TimeUnit.MILLISECONDS);
                    if (!dbLocked) {
                        logger.trace("doInTryLock({}, {}) - failed to aquire {}", new Object[] { mutexName, timeout, mutex });
                        return TryLockFunctionResult.getNotExecutedInstance();
                    }
                    
                    logger.trace("doInTryLock({}, {}) - db locked {}", new Object[] { mutexName, timeout, mutex });
                    
                    //Locked! run the function and immediately return the result
                    return new TryLockFunctionResult<T>(lockFunction.apply(mutexName));
                }
            });
        }
        catch (TransactionException e) {
            logger.trace("doInTryLock({}, {}) - failed to aquire cluster mutex", mutexName, timeout);
            return TryLockFunctionResult.getNotExecutedInstance();
        }
        catch (TransactionTimedOutException e) {
            logger.trace("doInTryLock({}, {}) - failed to aquire cluster mutex", mutexName, timeout);
            return TryLockFunctionResult.getNotExecutedInstance();
        }
        finally {
            logger.trace("doInTryLock({}, {}) - db unlocked {}", mutexName, timeout);
            lock.unlock();
            this.logger.trace("doInTryLock({}, {}) - released local lock", mutexName, timeout);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.locking.IClusterLockService#isLockOwner(java.lang.String)
     */
    @Override
    public boolean isLockOwner(String mutexName) {
        final ReentrantLock lock = getLocalLock(mutexName);
        return lock.isHeldByCurrentThread();
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.locking.IClusterLockService#isLocked(java.lang.String)
     */
    @Override
    public boolean isLocked(String mutexName) {
        final ReentrantLock lock = getLocalLock(mutexName);
        if (lock.isLocked()) {
            return true;
        }
        
        final TryLockFunctionResult<String> result = this.doInTryLock(mutexName, 0, TimeUnit.MILLISECONDS, Functions.<String>identity());
        
        return !result.isExecuted();
    }

    private ClusterMutex getClusterMutex(String mutexName) {
        ClusterMutex mutex = this.clusterLockDao.getClusterMutex(mutexName);
        if (mutex == null) {
            this.clusterLockDao.createClusterMutex(mutexName);
            mutex = this.clusterLockDao.getClusterMutex(mutexName);
        }
        return mutex;
    }

    ReentrantLock getLocalLock(String mutexName) {
        final ReentrantLock lock = localLocks.get(mutexName);
        if (lock != null) {
            return lock;
        }
        return ConcurrentMapUtils.putIfAbsent(this.localLocks, mutexName, new ReentrantLock(true));
    }
}
