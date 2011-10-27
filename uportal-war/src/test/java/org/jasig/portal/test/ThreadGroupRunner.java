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

package org.jasig.portal.test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import junit.framework.AssertionFailedError;

import org.jasig.portal.utils.ConcurrentMapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for running several threads in a test
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class ThreadGroupRunner {
    private enum State {
        SETUP,
        RUNNING,
        COMPLETE;
    }
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final List<Thread> threads = new LinkedList<Thread>();
    private final Map<String, Throwable> uncaughtExceptions = new ConcurrentHashMap<String, Throwable>();
    
    private final String namePrefix;
    private final boolean daemon;
    private volatile State running = State.SETUP;
    
    public ThreadGroupRunner(String namePrefix, boolean daemon) {
        this.namePrefix = namePrefix;
        this.daemon = daemon;
    }

    /**
     * Add a Runnable that will be executed in its own thread.
     */
    public synchronized void addTask(Runnable r) {
        if (running != State.SETUP) {
            throw new IllegalStateException("Can't be called after start() has been called");
        }
        
        final Thread t = new Thread(r , this.namePrefix + threads.size());
        t.setDaemon(this.daemon);
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logger.debug("Uncaught Exception", e);
                uncaughtExceptions.put(t.getName(), e);
            }
        });
        
        threads.add(t);
    }
    

    public synchronized void addTask(int threadCount, Runnable r) {
        if (running != State.SETUP) {
            throw new IllegalStateException("Can't be called after start() has been called");
        }
        
        for (int index = 0; index < threadCount; index++) {
            this.addTask(r);
        }
    }
    
    /**
     * Start all threads (start is in order of added runnable)
     */
    public synchronized void start() {
        if (running != State.SETUP) {
            throw new IllegalStateException("Can only be called once");
        }
        running = State.RUNNING;
        
        for (final Thread t : this.threads) {
            t.start();
        }
    }
    
    /**
     * Join on all threads (join is in order of added runnable)
     */
    public synchronized void join() throws InterruptedException {
        if (running != State.RUNNING) {
            throw new IllegalStateException("Can only be called after start()");
        }
        
        for (final Thread t : this.threads) {
            t.join();
        }
        running = State.COMPLETE;
        
        Map.Entry<String, Throwable> exception = null;
        for (final Map.Entry<String, Throwable> exceptionEntry : uncaughtExceptions.entrySet()) {
            if (exception == null) {
                exception = exceptionEntry;
            }
            logger.error("Thread " + exceptionEntry.getKey() + " failed with an exception", exceptionEntry.getValue());
        }
        
        if (exception != null) {
            final AssertionFailedError assertionError = new AssertionFailedError("Thread " + exception.getKey() + " failed with an exception");
            assertionError.initCause(exception.getValue());
            throw assertionError;
        }
    }
    
    private final ConcurrentMap<Integer, CountDownLatch> latchMap = new ConcurrentHashMap<Integer, CountDownLatch>(); 
    
    /**
     * Effectively a count down latch where all threads in the group must reach the specified
     * tick before any are allowed to proceed  
     */
    public void tick(int index) throws InterruptedException {
        tick(index, false);
    }
    
    /**
     * Effectively a count down latch where all threads in the group must reach the specified
     * tick before any are allowed to proceed.
     * 
     * @param includeMainThread If true all threads in the group AND the main thread must call tick
     */
    public void tick(int index, boolean includeMainThread) throws InterruptedException {
        if (running != State.RUNNING) {
            throw new IllegalStateException("Can only be called after start() and before join() returns");
        }
        
        CountDownLatch latch = latchMap.get(index);
        if (latch == null) {
            final int latchCount = this.threads.size() + (includeMainThread ? 1 : 0);
            final CountDownLatch newLatch = new CountDownLatch(latchCount);
            latch = ConcurrentMapUtils.putIfAbsent(latchMap, index, newLatch);
            
            if (newLatch == latch) {
                logger.debug("created tick({}) = {}", index, latchCount);
            }
        }
        
        latch.countDown();
        logger.debug("tick({}) = {}", index, latch.getCount());
        latch.await();
    }
}
