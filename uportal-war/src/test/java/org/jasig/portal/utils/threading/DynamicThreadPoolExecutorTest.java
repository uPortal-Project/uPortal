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
package org.jasig.portal.utils.threading;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for {@link ThreadPoolExecutor} that verifies threads are only created on demand
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Ignore
public class DynamicThreadPoolExecutorTest {
    
    @Test
    public void testExecutorsNewCachedThreadPool() throws Exception {
        //See Executors.newCachedThreadPool();
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, 2, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                //This happens after Future.get() returns so it reduces the chance for timing issues in the test
                final LatchFutureTask lr = (LatchFutureTask)r;
                lr.done();
            }

            @SuppressWarnings("unchecked")
            @Override
            protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
                if (runnable instanceof RunnableFuture) {
                    return (RunnableFuture<T>)runnable;
                }
                return super.newTaskFor(runnable, value);
            }
            
        };
    
        testThreadPoolExecutor(threadPoolExecutor, false);
    }

    protected void testThreadPoolExecutor(final ThreadPoolExecutor threadPoolExecutor, boolean queuesAdditional) throws InterruptedException,
            ExecutionException {
        final ExecutorStats executorStats = new ExecutorStats();
        
        //Nothing going on in a new pool
        executorStats.verify(threadPoolExecutor);
        
        //****************************************//
        
        //Schedule task 1
        final LatchFutureTask lr1 = LatchFutureTask.create();
        threadPoolExecutor.submit(lr1);
        
        //Verify 1 is running
        lr1.waitForStart();
        executorStats.activeCount++;
        executorStats.largestPoolSize++;
        executorStats.poolSize++;
        executorStats.verify(threadPoolExecutor);
        
        //Verify 1 is stopped
        lr1.waitForDone();
        //Sadly need to sleep just long enough to let the completed thread get back into the pool 
        Thread.sleep(5);
        executorStats.activeCount--;
        executorStats.completedTaskCount++;
        executorStats.verify(threadPoolExecutor);
        
        //****************************************//
        
        //Schedule task 2
        final LatchFutureTask lr2 = LatchFutureTask.create();
        threadPoolExecutor.submit(lr2);
        
        //Verify 2 is running
        lr2.waitForStart();
        executorStats.activeCount++;
        executorStats.verify(threadPoolExecutor);
        
        //Verify 2 is stopped
        lr2.waitForDone();
        //Sadly need to sleep just long enough to let the completed thread get back into the pool 
        Thread.sleep(5);
        executorStats.activeCount--;
        executorStats.completedTaskCount++;
        executorStats.verify(threadPoolExecutor);
        
        //****************************************//
        
        //Schedule task 3
        final LatchFutureTask lr3 = LatchFutureTask.create();
        threadPoolExecutor.submit(lr3);

        //Verify 3 is running
        lr3.waitForStart();
        executorStats.activeCount++;
        executorStats.verify(threadPoolExecutor);
        
        //Schedule task 4 concurrently
        final LatchFutureTask lr4 = LatchFutureTask.create();
        threadPoolExecutor.submit(lr4);

        //Verify 4 is running
        lr4.waitForStart();
        executorStats.activeCount++;
        executorStats.poolSize++;
        executorStats.largestPoolSize++;
        executorStats.verify(threadPoolExecutor);
        
        //Verify 3 is stopped
        lr3.waitForDone();
        executorStats.activeCount--;
        executorStats.completedTaskCount++;
        executorStats.verify(threadPoolExecutor);
        
        //Verify 4 is stopped
        lr4.waitForDone();
        //Sadly need to sleep just long enough to let the completed thread get back into the pool 
        Thread.sleep(5);
        executorStats.activeCount--;
        executorStats.completedTaskCount++;
        executorStats.verify(threadPoolExecutor);
        
        //****************************************//
        
        //Schedule task 5
        final LatchFutureTask lr5 = LatchFutureTask.create();
        threadPoolExecutor.submit(lr5);
        
        //Verify 5 is running
        lr5.waitForStart();
        executorStats.activeCount++;
        executorStats.verify(threadPoolExecutor);
        
        //Verify 5 is stopped
        lr5.waitForDone();
        //Sadly need to sleep just long enough to let the completed thread get back into the pool 
        Thread.sleep(5);
        executorStats.activeCount--;
        executorStats.completedTaskCount++;
        executorStats.verify(threadPoolExecutor);
        
        //****************************************//
        
        //Schedule task 6
        final LatchFutureTask lr6 = LatchFutureTask.create();
        threadPoolExecutor.submit(lr6);

        //Verify 6 is running
        lr6.waitForStart();
        executorStats.activeCount++;
        executorStats.verify(threadPoolExecutor);
        
        //Schedule task 7 concurrently
        final LatchFutureTask lr7 = LatchFutureTask.create();
        threadPoolExecutor.submit(lr7);

        //Verify 7 is running
        lr7.waitForStart();
        executorStats.activeCount++;
        executorStats.verify(threadPoolExecutor);
        
        //Schedule task 8 concurrently
        final LatchFutureTask lr8 = LatchFutureTask.create();
        if (queuesAdditional) {
            threadPoolExecutor.submit(lr8);
            executorStats.verify(threadPoolExecutor);
            
            //Stop 6 to make room in pool for 8
            //Verify 6 is stopped
            lr6.waitForDone();
            executorStats.activeCount--;
            executorStats.completedTaskCount++;
            executorStats.verify(threadPoolExecutor);

            //Verify 8 is running
            lr8.waitForStart();
            executorStats.activeCount++;
            executorStats.verify(threadPoolExecutor);
            
            //Verify 8 is stopped
            lr8.waitForDone();
            executorStats.activeCount--;
            executorStats.completedTaskCount++;
            executorStats.verify(threadPoolExecutor);
            
        }
        else {
            try {
                threadPoolExecutor.submit(lr8);
                fail("submit should have thrown RejectedExecutionException");
            }
            catch (RejectedExecutionException e) {
                //Expected
            }
        
            //Verify 6 is stopped
            lr6.waitForDone();
            executorStats.activeCount--;
            executorStats.completedTaskCount++;
            executorStats.verify(threadPoolExecutor);
        }
        
        //Verify 7 is stopped
        lr7.waitForDone();
        //Sadly need to sleep just long enough to let the completed thread get back into the pool 
        Thread.sleep(5);
        executorStats.activeCount--;
        executorStats.completedTaskCount++;
        executorStats.verify(threadPoolExecutor);
        
        //****************************************//
    }
    
    private static final class ExecutorStats {
        public int activeCount = 0;
        public int completedTaskCount = 0;
        public int corePoolSize = 0;
        public int largestPoolSize = 0;
        public int poolSize = 0;
        
        public void verify(ThreadPoolExecutor executor) {
            assertEquals("Active Thread Counts don't match", activeCount, executor.getActiveCount());
            assertEquals("Completed Task Counts don't match", completedTaskCount, executor.getCompletedTaskCount());
            assertEquals("Core Pool Sizes don't match", corePoolSize, executor.getCorePoolSize());
            assertEquals("Pool Sizes don't match", poolSize, executor.getPoolSize());
            assertEquals("Largest Pool Sizes don't match", largestPoolSize, executor.getLargestPoolSize());
        }
    }
    
    private static final class LatchFutureTask extends FutureTask<Object> {
        private final LatchRunnable latchRunnable;
        
        public static LatchFutureTask create() {
            return new LatchFutureTask(new LatchRunnable());
        }

        private LatchFutureTask(LatchRunnable latchRunnable) {
            super(latchRunnable, null);
            this.latchRunnable = latchRunnable;
        }

        public void waitForStart() throws InterruptedException {
            latchRunnable.waitForStart();
        }

        public void waitForDone() throws InterruptedException, ExecutionException {
            latchRunnable.waitForDone();
            this.get();
        }

        public void done() {
            latchRunnable.done();
        }
    }
    
    private static final class LatchRunnable implements Runnable {
        public final CountDownLatch startLatch = new CountDownLatch(1);
        public final CountDownLatch stopLatch = new CountDownLatch(1);
        public final CountDownLatch doneLatch = new CountDownLatch(1);
        
        public void waitForStart() throws InterruptedException {
            startLatch.await();
        }
        
        public void waitForDone() throws InterruptedException {
            stopLatch.countDown();
            doneLatch.await();
        }
        
        public void done() {
            doneLatch.countDown();
        }

        public void run() {
            startLatch.countDown();
            try {
                System.out.println(Thread.currentThread().getName());
                stopLatch.await();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
