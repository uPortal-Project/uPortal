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

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.joda.time.Duration;
import org.joda.time.ReadableDuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class ClusterLockServiceImpl implements IClusterLockService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @SuppressWarnings("deprecation")
    private final ConcurrentMap<String, ReentrantLock> localLocks = new MapMaker().weakValues().makeComputingMap(
            new Function<String, ReentrantLock>() {
                @Override
                public ReentrantLock apply(String input) {
                    return new ReentrantLock(true);
                }
            });

    private ExecutorService lockMonitorExecutorService;
    private IClusterLockDao clusterLockDao;
    private ReadableDuration updateLockRate = Duration.millis(500);
    private ReadableDuration maximumLockDuration = Duration.standardMinutes(15);

    @Autowired
    public void setClusterLockDao(IClusterLockDao clusterLockDao) {
        this.clusterLockDao = clusterLockDao;
    }

    @Autowired
    public void setLockMonitorExecutorService(@Qualifier("uPortalLockExecutor") ExecutorService lockMonitorExecutorService) {
        this.lockMonitorExecutorService = lockMonitorExecutorService;
    }
    /**
     * Rate at which {@link IClusterLockDao#updateLock(String)} is called while a mutex is locked, defaults to 500ms
     */
    @Value("${org.jasig.portal.concurrency.locking.ClusterLockDao.updateLockRate:PT0.500S}")
    public void setUpdateLockRate(ReadableDuration updateLockRate) {
        this.updateLockRate = updateLockRate;
    }

    /**
     * Maximum duration that a lock can be held, functionally longest duration that the lockFunction can take to execute.
     * Defaults to 15 minutes
     */
    @Value("${org.jasig.portal.concurrency.locking.ClusterLockDao.maximumLockDuration:PT900S}")
    public void setMaximumLockDuration(ReadableDuration maximumLockDuration) {
        this.maximumLockDuration = maximumLockDuration;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.locking.IClusterLockService#doInTryLock(java.lang.String, com.google.common.base.Function)
     */
    @Override
    public <T> TryLockFunctionResult<T> doInTryLock(final String mutexName, Function<String, T> lockFunction) throws InterruptedException {
        /*
         * locking strategy requires 2 threads
         * the caller thread is the 'work thread', it executes the lockFunction
         * an additional 'lock thread' is used to acquire and maintain the database lock
         */
        
        this.logger.trace("doInLock({})", mutexName);
        
        //Thread coordination objects
        final CountDownLatch dbLockLatch = new CountDownLatch(1);
        final CountDownLatch workCompleteLatch = new CountDownLatch(1);
        final AtomicBoolean dbLocked = new AtomicBoolean(false);
        
        
        Future<Boolean> lockFuture = null;
        
        final ReentrantLock lock = getLocalLock(mutexName);
        final boolean lockedLocally = lock.tryLock();
        if (!lockedLocally) {
            this.logger.trace("local lock already held for {}", mutexName);
            return TryLockFunctionResultImpl.getNotExecutedInstance();
        }
        try {
            this.logger.trace("acquired local lock for {}", mutexName);
            
            final DatabaseLockWorker databaseLockWorker = new DatabaseLockWorker(dbLocked, mutexName, dbLockLatch, workCompleteLatch);
            lockFuture = this.lockMonitorExecutorService.submit(databaseLockWorker);
            
            //Wait for DB lock acquisition
            dbLockLatch.await();
            
            if (!dbLocked.get()) {
                //Failed to get DB lock, stop now
                this.logger.trace("failed to aquire database lock, returning notExecuted result for: {}", mutexName);
                return TryLockFunctionResultImpl.getNotExecutedInstance();
            }
            
            //Execute the lockFunction
            return new TryLockFunctionResultImpl<T>(lockFunction.apply(mutexName));
        }
        finally {
            //Signal db lock worker to release the lock
            workCompleteLatch.countDown();
            
            if (lockFuture != null) {
                //Wait for the db lock worker to complete 
                try {
                    lockFuture.get();
                }
                catch (ExecutionException e) {
                    this.logger.warn("Lock manager thread for " + mutexName + " failed with an exception. Everything is cleaned up but this could indicate a problem with cluster locking", e.getCause());
                }
            }
            
            //Release the local lock
            lock.unlock();
            this.logger.trace("released local lock for: {}", mutexName);
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
        
        final ClusterMutex clusterMutex = this.clusterLockDao.getClusterMutex(mutexName);
        
        return clusterMutex.isLocked();
    }

    /**
     * The local Lock for the specified mutex
     */
    protected ReentrantLock getLocalLock(final String mutexName) {
        return this.localLocks.get(mutexName);
    }

    /**
     * Callable that acquires, maintains, and releases a database lock
     */
    private final class DatabaseLockWorker implements Callable<Boolean> {
        private final AtomicBoolean dbLocked;
        private final String mutexName;
        private final CountDownLatch dbLockLatch;
        private final CountDownLatch workCompleteLatch;

        private DatabaseLockWorker(AtomicBoolean dbLocked, String mutexName, CountDownLatch dbLockLatch,
                CountDownLatch workCompleteLatch) {
            this.dbLocked = dbLocked;
            this.mutexName = mutexName;
            this.dbLockLatch = dbLockLatch;
            this.workCompleteLatch = workCompleteLatch;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                final long lockTimeout = System.currentTimeMillis() + maximumLockDuration.getMillis();
                try {
                    //Try to acquire the lock, set the success to the dbLocked holder
                    this.dbLocked.set(clusterLockDao.getLock(this.mutexName));
                }
                finally {
                    //Signal the work thread that we've attempted to get the DB lock, done in finally so
                    //the work thread won't hang if something goes wrong during acquisition
                    this.dbLockLatch.countDown();
                    logger.trace("Signaled dbLockLatch for: {}", this.mutexName);
                }
                
                //If acquisition failed return immediately
                if (!this.dbLocked.get()) {
                    logger.trace("failed to acquire db lock for: {}", this.mutexName);
                    return false;
                }
                logger.trace("acquired db lock for: {}", this.mutexName);
            
                //wait for the work to complete using the updateLockRate as the wait duration, if the wait time
                //passes without the work thread signaling completion update the mutex (signal we still have the lock)
                //and wait again
                while (!this.workCompleteLatch.await(updateLockRate.getMillis(), TimeUnit.MILLISECONDS)) {
                    clusterLockDao.updateLock(this.mutexName);
                    
                    if (lockTimeout < System.currentTimeMillis()) {
                        //TODO is there some way to force the work thread to stop now that the lock thread is giving up?
                        //TODO better exception type?
                        throw new RuntimeException("The database lock has been held for more than " + maximumLockDuration + ", giving up and releasing the DB lock.");
                    }
                }
            }
            catch (Exception e) {
                logger.warn("DB Lock Worker failed for " + mutexName + " due to an exception.", e);
                throw e;
            }
            finally {
                //If the db lock was acquired release it
                if (this.dbLocked.get()) {
                    clusterLockDao.releaseLock(this.mutexName);
                    logger.trace("released db lock for: {}", this.mutexName);
                }
            }
            
            logger.trace("DB lock worker returning true: {}", this.mutexName);
            return true;
        }
    }
    
    public static class TryLockFunctionResultImpl<T> implements TryLockFunctionResult<T> {
        private static final TryLockFunctionResult<?> NOT_EXECUTED_INSTANCE = new TryLockFunctionResultImpl<Object>();
        
        @SuppressWarnings("unchecked")
        static <T> TryLockFunctionResult<T> getNotExecutedInstance() {
            return (TryLockFunctionResult<T>)NOT_EXECUTED_INSTANCE;
        }
        
        private final boolean executed;
        private final T result;

        private TryLockFunctionResultImpl() {
            this.executed = false;
            this.result = null;
        }
        TryLockFunctionResultImpl(T result) {
            this.executed = true;
            this.result = result;
        }

        @Override
        public boolean isExecuted() {
            return executed;
        }

        @Override
        public T getResult() {
            return result;
        }

        @Override
        public String toString() {
            return "LockFunctionResult [executed=" + executed + ", result=" + result + "]";
        }
    }
}
