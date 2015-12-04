/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlet.rendering.worker;

import java.lang.Thread.State;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.util.concurrent.Futures;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for portlet execution dispatching. Tracks the target, request, response objects as well as
 * submitted, started and completed timestamps.
 */
abstract class PortletExecutionWorker<V> implements IPortletExecutionWorker<V> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final Map<String, Object> executionAttributes = new ConcurrentHashMap<String, Object>();
    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final ExecutorService executorService;
    private final List<IPortletExecutionInterceptor> interceptors;
    final IPortletRenderer portletRenderer;
    final IPortletWindowId portletWindowId;
    final String portletFname;
    final long timeout;
    final HttpServletRequest request;
    final HttpServletResponse response;
    
    private volatile Future<V> future;
    private volatile Thread workerThread;
    private volatile long submitted = 0;
    private volatile long started = 0;
    private volatile long complete = 0;
    private final AtomicInteger cancelCount = new AtomicInteger();
    private final AtomicBoolean canceled = new AtomicBoolean();
    private volatile boolean retrieved = false;

    public PortletExecutionWorker(
            ExecutorService executorService, List<IPortletExecutionInterceptor> interceptors, IPortletRenderer portletRenderer, 
            HttpServletRequest request, HttpServletResponse response, IPortletWindow portletWindow, long timeout) {

        this.executorService = executorService;
        this.interceptors = interceptors;
        this.portletRenderer = portletRenderer;
        this.request = new GuardingHttpServletRequest(request, canceled);
        this.response = new GuardingHttpServletResponse(response, canceled);
        this.portletWindowId = portletWindow.getPortletWindowId();
        this.portletFname = portletWindow.getPortletEntity().getPortletDefinition().getFName();
        this.timeout = timeout;
    }

    @Override
    public Object setExecutionAttribute(String name, Object value) {
        if (value == null) {
            return executionAttributes.remove(name);
        }
        return executionAttributes.put(name, value);
    }

    @Override
    public Object getExecutionAttribute(String name) {
        return executionAttributes.get(name);
    }

    @Override
    public IPortletWindowId getPortletWindowId() {
        return this.portletWindowId;
    }
    
    @Override
    public String getPortletFname() {
        return this.portletFname;
    }

    /**
     * @return The timeout setting for the operation in process
     */
    @Override
    public long getApplicableTimeout() {
        return this.timeout;
    }

    @Override
    public final void submit() {
        if (this.submitted > 0) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " for " + this.getPortletWindowId() + " has already been submitted.");
        }
        
        this.submitted = System.currentTimeMillis();
        
        try {
            //Run pre-submit interceptors
            for (final IPortletExecutionInterceptor interceptor : this.interceptors) {
                interceptor.preSubmit(request, response, this);
            }
            
            final Callable<V> callable = new PortletExecutionCallable<V>(this, new ExecutionLifecycleCallable<V>(new Callable<V>() {
                    @Override
                    public V call() throws Exception {
                        return callInternal();
                    }
                })
            );
            
            this.future = this.executorService.submit(callable);
        }
        catch (final Exception e) {
            //All is not well do the basic portlet execution lifecycle and then, return a Future that simply rethrows the exception
            
            final Callable<Future<V>> callable = new ExecutionLifecycleCallable<Future<V>>(new Callable<Future<V>>() {
                @Override
                public Future<V> call() throws Exception {
                    return Futures.immediateFailedFuture(e);
                }
            });
            
            try {
                this.future = callable.call();
            }
            catch (Exception e1) {
                //We know this will never throw
            }
        }
    }
    
    private final class ExecutionLifecycleCallable<V1> implements Callable<V1> {
        private final Callable<V1> callable;
        
        public ExecutionLifecycleCallable(Callable<V1> callable) {
            this.callable = callable;
        }

        @Override
        public V1 call() throws Exception {
            startExecution();
            
            try {
                runPreExecutionInterceptors();
                
                final V1 result = this.callable.call();
                doPostExecution(null);
                return result;
            }
            catch (Exception e) {
                doPostExecution(e);
                throw e;
            }
            finally {
                executionComplete();
            }
        }
    }
    
    private void startExecution() {
        //grab the current thread
        workerThread = Thread.currentThread();
        
        //signal any threads waiting for the worker to start
        started = System.currentTimeMillis();
        startLatch.countDown();
    }
    
    private void runPreExecutionInterceptors() {

        for (final IPortletExecutionInterceptor interceptor : this.interceptors) {
            interceptor.preExecution(request, response, this);
        }
    }
    
    private void executionComplete() {
        complete = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug("Execution complete on portlet " + portletWindowId + " in " + getDuration() + "ms");
        }
        
        workerThread = null;
    }
    
    private void doPostExecution(Exception e) {
        //Iterate over handlers in reverse for post execution
        final ListIterator<IPortletExecutionInterceptor> listIterator = this.interceptors.listIterator(this.interceptors.size());
        while (listIterator.hasPrevious()) {
            final IPortletExecutionInterceptor interceptor = listIterator.previous();
            try {
                interceptor.postExecution(request, response, this, e);
            }
            catch (Throwable ex2) {
                logger.error("HandlerInterceptor.postExecution threw exception for {}", this, ex2);
            }
        }
    }
    
    /**
     * @see Callable#call()
     */
    protected abstract V callInternal() throws Exception;
    
    @Override
    public final boolean isStarted() {
        return this.started > 0;
    }
    
    @Override
    public boolean isSubmitted() {
        return this.submitted > 0;
    }

    @Override
    public boolean isComplete() {
        return this.complete > 0 || this.workerThread == null || this.workerThread.getState() == State.TERMINATED;
    }
    
    @Override
    public boolean isRetrieved() {
        return this.retrieved;
    }
    
    @Override
    public final long waitForStart(long timeout) throws InterruptedException {
        //Wait for start Callable to start
        this.startLatch.await(timeout, TimeUnit.MILLISECONDS);
        return this.started;
    }
    
    @Override
    public V get(long timeout) throws Exception {
        if (this.future == null) {
            throw new IllegalStateException("submit() must be called before get(long) can be called");
        }
        
        this.retrieved = true;
        
        try {
            final long startTime = this.waitForStart(timeout);
            final long waitTime = Math.max(0, timeout - (System.currentTimeMillis() - startTime));
            return this.future.get(waitTime, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
            this.logger.warn("Execution interrupted on portlet {}", this, e);
            throw e;
        }
        catch (ExecutionException e) {
            this.logger.warn("Execution failed on portlet {}", this, e);
            throw e;
        }
        catch (TimeoutException e) {
            final StringBuilder errorBuilder = new StringBuilder("Execution timed out on portlet ");

            errorBuilder.append(this.toString());

            final Thread localWorkerThread = workerThread;
            if (localWorkerThread != null) {
                final State state = localWorkerThread.getState();
                final StackTraceElement[] stackTrace = localWorkerThread.getStackTrace();

                errorBuilder.append("\n\tPortlet Thread State: ").append(state).append("\n");
                errorBuilder.append("\tPortlet Thread Stack Trace: \n");

                for (final StackTraceElement stackTraceElement : stackTrace) {
                    errorBuilder.append("\t\tat ").append(stackTraceElement).append("\n");
                }
            }

            this.logger.warn(errorBuilder.toString());

            throw e;
        }
    }
    
    @Override
    public final void cancel() {
        if (this.future == null) {
            throw new IllegalStateException("submit() must be called before cancel() can be called");
        }
        
        if (this.isComplete()) {
            return;
        }
        
        //Mark worker as retrieved
        this.retrieved = true;

        //Notify the guarding req/res wrappers that cancel has been called
        this.canceled.set(true);
        
        //Cancel the future, interrupting the thread
        this.future.cancel(true);
        
        //Track the number of times cancel has been called
        final int count = this.cancelCount.getAndIncrement();
        if (count > 0) {
            //Since Future.cancel only interrupts the thread on the first call interrupt the thread directly
            final Thread thread = this.workerThread;
            if (thread != null) {
                thread.interrupt();
            }
        }
    }
    
    @Override
    public final int getCancelCount() {
        return this.cancelCount.get();
    }

    @Override
    public final long getSubmittedTime() {
        return this.submitted;
    }

    @Override
    public final long getStartedTime() {
        return this.started;
    }

    @Override
    public final long getCompleteTime() {
        return this.complete;
    }
    
    @Override
    public final long getWait() {
        return this.started - this.submitted;
    }
    
    @Override
    public final long getDuration() {
        if (this.complete > 0) {
            return this.complete - this.submitted;
        } else {
            return System.currentTimeMillis()- this.submitted;
        }
    }

    @Override
    public String toString() {
        return "PortletExecutionWorker [" +
                    "portletFname=" + this.portletFname + ", " +
                    "timeout=" + this.timeout + ", " +
                    "portletWindowId=" + this.portletWindowId + ", " +
                    "started=" + this.started + ", " +
                    "submitted=" + this.submitted + ", " +
                    "complete=" + this.complete + ", " +
                    "retrieved=" + this.retrieved + ", " +
                    "canceled=" + this.canceled + ", " +
                    "cancelCount=" + this.cancelCount + ", " +
                    "wait=" + this.getWait() + ", " +
                    "duration=" + this.getDuration() + "ms]";
    }
}