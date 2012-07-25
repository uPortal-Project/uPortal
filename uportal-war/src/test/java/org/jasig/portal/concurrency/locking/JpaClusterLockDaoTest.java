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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Assert;

import org.jasig.portal.IPortalInfoProvider;
import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.test.BasePortalJpaDaoTest;
import org.jasig.portal.test.ThreadGroupRunner;
import org.jasig.portal.utils.threading.ThrowingRunnable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaClusterLockDaoTestContext.xml")
public class JpaClusterLockDaoTest extends BasePortalJpaDaoTest {
    @Autowired
    private IClusterLockDao clusterLockDao;
    @Autowired
    private IPortalInfoProvider portalInfoProvider;

    @Test
    public void testConcurrentCreation() throws InterruptedException  {
        reset(portalInfoProvider);
        when(portalInfoProvider.getUniqueServerName()).thenReturn("ServerA");
        
        final ThreadGroupRunner threadGroupRunner = new ThreadGroupRunner("JpaClusterLockDaoTest-", true);
        
        threadGroupRunner.addTask(3, new ThrowingRunnable() {
            @Override
            public void runWithException() throws Throwable {
                executeInTransaction(new CallableWithoutResult() {
                    @Override
                    protected void callWithoutResult() {
                        try {
                            final String mutexName = "testConcurrentCreation";
                            
                            threadGroupRunner.tick(1);
                            ClusterMutex mutex = clusterLockDao.getClusterMutex(mutexName);
                            assertNotNull(mutex);
                        }
                        catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        });
        
        threadGroupRunner.start();
        threadGroupRunner.join();
    }
    
    @Test
    public void testConcurrentLocking() throws InterruptedException  {
        reset(portalInfoProvider);
        when(portalInfoProvider.getUniqueServerName()).thenReturn("ServerA");

        final String mutexName = "testConcurrentLocking";

        execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final ClusterMutex mutex = clusterLockDao.getClusterMutex(mutexName);
                assertNotNull(mutex);
            }
        });
        
        
        final ThreadGroupRunner threadGroupRunner = new ThreadGroupRunner("JpaClusterLockDaoTest-", true);
        
        final AtomicInteger lockCounter = new AtomicInteger(); 
        
        threadGroupRunner.addTask(3, new ThrowingRunnable() {
            @Override
            public void runWithException() throws Throwable {
                executeInTransaction(new CallableWithoutResult() {
                    @Override
                    protected void callWithoutResult() {
                        try {
                            threadGroupRunner.tick(1);
                            try {
                                final ClusterMutex mutex = clusterLockDao.getLock(mutexName);
                                if (mutex != null) {
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
                    }
                });
            }
        });
        
        threadGroupRunner.start();
        threadGroupRunner.join();
        
        assertEquals(1, lockCounter.intValue());
        
        ClusterMutex mutex = clusterLockDao.getClusterMutex(mutexName);
        assertTrue(mutex.isLocked());
        
        clusterLockDao.releaseLock(mutexName);
        
        mutex = clusterLockDao.getClusterMutex(mutexName);
        assertFalse(mutex.isLocked());
    }
    

    @Test
    public void testConcurrentCreateLocking() throws InterruptedException  {
        reset(portalInfoProvider);
        when(portalInfoProvider.getUniqueServerName()).thenReturn("ServerA");

        final String mutexName = "testConcurrentLocking";
        final ThreadGroupRunner threadGroupRunner = new ThreadGroupRunner("JpaClusterLockDaoTest-", true);
        
        final AtomicInteger lockCounter = new AtomicInteger(); 
        
        threadGroupRunner.addTask(3, new ThrowingRunnable() {
            @Override
            public void runWithException() throws Throwable {
                executeInTransaction(new CallableWithoutResult() {
                    @Override
                    protected void callWithoutResult() {
                        try {
                            threadGroupRunner.tick(1);
                            try {
                                final ClusterMutex mutex = clusterLockDao.getLock(mutexName);
                                if (mutex != null) {
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
                    }
                });
            }
        });
        
        threadGroupRunner.start();
        threadGroupRunner.join();
        
        assertEquals(1, lockCounter.intValue());
        
        ClusterMutex mutex = clusterLockDao.getClusterMutex(mutexName);
        assertTrue(mutex.isLocked());
        
        clusterLockDao.releaseLock(mutexName);
        
        mutex = clusterLockDao.getClusterMutex(mutexName);
        assertFalse(mutex.isLocked());
    }
    
    @Test
    public void testNotAbandoned() throws Exception  {
        //Used to make a 'mutable string'
        final AtomicReference<String> currentServer = new AtomicReference<String>("ServerA");
        final String mutexName = "testNotAbandoned";
        
        reset(portalInfoProvider);
        when(portalInfoProvider.getUniqueServerName()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return currentServer.get();
            }
        });

        //get/create the mutex
        execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final ClusterMutex mutex = clusterLockDao.getClusterMutex(mutexName);
                assertNotNull(mutex);
            }
        });
        
        //lock serverA
        currentServer.set("ServerA");
        final ClusterMutex lockedMutex = execute(new Callable<ClusterMutex>() {
            @Override
            public ClusterMutex call() throws Exception {
                final ClusterMutex mutex = clusterLockDao.getLock(mutexName);
                assertNotNull(mutex);
                return mutex;
            }
        }); 
        
        //test context configures a 100ms abandoned lock timeout, spin in tryLock/updateLock for 110ms
        while (lockedMutex.getLockStart() + 110 > System.currentTimeMillis()) {
            //try lock ServerB
            currentServer.set("ServerB");
            execute(new CallableWithoutResult() {
                @Override
                protected void callWithoutResult() {
                    final ClusterMutex mutex = clusterLockDao.getLock(mutexName);
                    assertNull(mutex);
                }
            });
            TimeUnit.MILLISECONDS.sleep(1);
            //ServerA update ping
            currentServer.set("ServerA");
            execute(new CallableWithoutResult() {
                @Override
                protected void callWithoutResult() {
                    clusterLockDao.updateLock(mutexName);
                }
            });
            TimeUnit.MILLISECONDS.sleep(1);
        }

        currentServer.set("ServerA");
        
        ClusterMutex mutex = clusterLockDao.getClusterMutex(mutexName);
        assertTrue(mutex.isLocked());
        
        clusterLockDao.releaseLock(mutexName);
        
        mutex = clusterLockDao.getClusterMutex(mutexName);
        assertFalse(mutex.isLocked());
    }
    
    @Test
    public void testAbandoned() throws Exception  {
        //Used to make a 'mutable string'
        final AtomicReference<String> currentServer = new AtomicReference<String>("ServerA");
        final String mutexName = "testNotAbandoned";
        
        reset(portalInfoProvider);
        when(portalInfoProvider.getUniqueServerName()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return currentServer.get();
            }
        });

        //get/create the mutex
        execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final ClusterMutex mutex = clusterLockDao.getClusterMutex(mutexName);
                Assert.assertNotNull(mutex);
            }
        });
        
        //lock serverA
        currentServer.set("ServerA");
        execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final ClusterMutex mutex = clusterLockDao.getLock(mutexName);
                Assert.assertNotNull(mutex);
            }
        }); 
        
        final AtomicInteger lockFailCount = new AtomicInteger(0);
        final AtomicBoolean serverBLocked = new AtomicBoolean(false);
        
        currentServer.set("ServerB");
        
        //test context configures a 100ms abandoned lock timeout, wait 110 between tests
        for (int i = 0; i < 5 && !serverBLocked.get(); i++) {
            //try lock ServerB
            execute(new CallableWithoutResult() {
                @Override
                protected void callWithoutResult() {
                    final ClusterMutex mutex = clusterLockDao.getLock(mutexName);
                    if (mutex == null) {
                        lockFailCount.incrementAndGet();
                    }
                    else {
                        serverBLocked.set(true);
                    }
                }
            });
            
            TimeUnit.MILLISECONDS.sleep(110);
        }
        
        assertTrue(serverBLocked.get());
        assertEquals(1, lockFailCount.get());

        currentServer.set("ServerB");
        
        ClusterMutex mutex = clusterLockDao.getClusterMutex(mutexName);
        assertTrue(mutex.isLocked());
        
        clusterLockDao.releaseLock(mutexName);
        
        mutex = clusterLockDao.getClusterMutex(mutexName);
        assertFalse(mutex.isLocked());
    }
    
    @Test(expected=IllegalMonitorStateException.class)
    public void testUnlockedUpdate() throws Exception  {
        //Used to make a 'mutable string'
        final AtomicReference<String> currentServer = new AtomicReference<String>("ServerA");
        final String mutexName = "testUnlockedUpdate";
        
        reset(portalInfoProvider);
        when(portalInfoProvider.getUniqueServerName()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return currentServer.get();
            }
        });

        //get/create the mutex
        execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                clusterLockDao.updateLock(mutexName);
            }
        });
    }
    
    @Test(expected=IllegalMonitorStateException.class)
    public void testUnlockedRelease() throws Exception  {
        //Used to make a 'mutable string'
        final AtomicReference<String> currentServer = new AtomicReference<String>("ServerA");
        final String mutexName = "testUnlockedRelease";
        
        reset(portalInfoProvider);
        when(portalInfoProvider.getUniqueServerName()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return currentServer.get();
            }
        });

        //get/create the mutex
        execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                clusterLockDao.releaseLock(mutexName);
            }
        });
    }
    
    @Test(expected=IllegalMonitorStateException.class)
    public void testWrongServerUpdate() throws Exception  {
        //Used to make a 'mutable string'
        final AtomicReference<String> currentServer = new AtomicReference<String>("ServerA");
        final String mutexName = "testUnlockedUpdate";
        
        reset(portalInfoProvider);
        when(portalInfoProvider.getUniqueServerName()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return currentServer.get();
            }
        });

        //get/create the mutex
        execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                clusterLockDao.getLock(mutexName);
            }
        });

        currentServer.set("ServerB");
        //get/create the mutex
        execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                clusterLockDao.updateLock(mutexName);
            }
        });
    }
    
    @Test(expected=IllegalMonitorStateException.class)
    public void testWrongServerRelease() throws Exception  {
        //Used to make a 'mutable string'
        final AtomicReference<String> currentServer = new AtomicReference<String>("ServerA");
        final String mutexName = "testUnlockedRelease";
        
        reset(portalInfoProvider);
        when(portalInfoProvider.getUniqueServerName()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return currentServer.get();
            }
        });

        //get/create the mutex
        execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                clusterLockDao.getLock(mutexName);
            }
        });

        currentServer.set("ServerB");

        //get/create the mutex
        execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                clusterLockDao.releaseLock(mutexName);
            }
        });
    }
}
