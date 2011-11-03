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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jasig.portal.portlet.dao.jpa.BaseJpaDaoTest;
import org.jasig.portal.test.ThreadGroupRunner;
import org.jasig.portal.utils.threading.ThrowingRunnable;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaClusterLockDaoTestContext.xml")
public class JpaClusterLockDaoTest extends BaseJpaDaoTest {
    @Autowired
    private IClusterLockDao clusterLockDao;
    private TransactionTemplate transactionTemplate;
    
    @Autowired
    public void setPlatformTransactionManager(PlatformTransactionManager platformTransactionManager) {
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
        this.transactionTemplate.afterPropertiesSet();
    }
    
    @Test
    public void testConcurrentCreation() throws InterruptedException  {
        final ThreadGroupRunner threadGroupRunner = new ThreadGroupRunner("JpaClusterLockDaoTest-", true);
        
        final AtomicInteger diveCounter = new AtomicInteger(); 
        
        threadGroupRunner.addTask(10, new ThrowingRunnable() {
            @Override
            public void runWithException() throws Throwable {
                execute(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        return transactionTemplate.execute(new TransactionCallback<Object>() {
                            @Override
                            public Object doInTransaction(TransactionStatus status) {
                                try {
                                    final String mutexName = "testConcurrentCreation";
                                    
                                    threadGroupRunner.tick(1);
                                    ClusterMutex mutex = clusterLockDao.getClusterMutex(mutexName);
                                    assertNull(mutex);
                                    
                                    threadGroupRunner.tick(2);
                                    try {
                                        clusterLockDao.createClusterMutex(mutexName);
                                    }
                                    catch (DataIntegrityViolationException e) {
                                        diveCounter.incrementAndGet();
                                    }
                                    
                                    threadGroupRunner.tick(3);
                                    mutex = clusterLockDao.getClusterMutex(mutexName);
                                    assertNotNull(mutex);
                                    
                                    return null;
                                }
                                catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    }
                });
            }
        });
        
        threadGroupRunner.start();
        threadGroupRunner.join();
        
        assertEquals(9, diveCounter.intValue());
    }
    
    /**
     * Can only be enabled if testing against Oracle or Postgres due to DB limitations
     */
    @Test
    @Ignore
    public void testConcurrentLocking() throws InterruptedException  {
        final String mutexName = "testConcurrentLocking";

        execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                ClusterMutex mutex = clusterLockDao.getClusterMutex(mutexName);
                assertNull(mutex);
                
                clusterLockDao.createClusterMutex(mutexName);
                
                mutex = clusterLockDao.getClusterMutex(mutexName);
                assertNotNull(mutex);
                
                return null;
            }
        });
        
        
        final ThreadGroupRunner threadGroupRunner = new ThreadGroupRunner("JpaClusterLockDaoTest-", true);
        
        final AtomicInteger lockCounter = new AtomicInteger(); 
        
        threadGroupRunner.addTask(3, new ThrowingRunnable() {
            @Override
            public void runWithException() throws Throwable {
                execute(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        return transactionTemplate.execute(new TransactionCallback<Object>() {
                            @Override
                            public Object doInTransaction(TransactionStatus status) {
                                try {
                                    threadGroupRunner.tick(1);
                                    final ClusterMutex mutex = clusterLockDao.getClusterMutex(mutexName);
                                    assertNotNull(mutex);
                                    
                                    threadGroupRunner.tick(2);
                                    try {
                                        final boolean locked = clusterLockDao.tryLock(mutex, 0, TimeUnit.MILLISECONDS);
                                        if (locked) {
                                            lockCounter.incrementAndGet();
                                        }
                                    }
                                    finally {
                                        threadGroupRunner.tick(3);
                                    }
                                }
                                catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                
                                return null;
                            }
                            
                        });
                    }
                });
            }
        });
        
        threadGroupRunner.start();
        threadGroupRunner.join();
        
        assertEquals(1, lockCounter.intValue());
    }
}
