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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.rendering.IPortletRenderer;

/**
 * Base for portlet execution dispatching. Tracks the target, request, response objects as well as
 * submitted, started and completed timestamps.
 */
abstract class PortletExecutionWorker<V> implements IPortletExecutionWorker<V> {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
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
            HttpServletRequest request, HttpServletResponse response, IPortletWindow portletWindow, long timeout /*IPortletWindowId portletWindowId, String portletFname*/) {

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
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionContext#getPortletFname()
     */
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

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker#submit()
     */
    @Override
    public final void submit() {
        if (this.submitted > 0) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " for " + this.getPortletWindowId() + " has already been submitted.");
        }
        
        this.submitted = System.currentTimeMillis();
        
        //Run pre-submit interceptors
        for (final IPortletExecutionInterceptor interceptor : this.interceptors) {
            interceptor.preSubmit(request, response, this);
        }
        
        /*
         * Time to prepare a Callable for the executorService;  choose whether 
         * to create a normal Callable (that invokes the portlet code), or a 
         * special (dummy) Callable that throws an Exception indicating the 
         * portlet has too many errant worker threads in the hungWorker queue. 
         */
        Callable<V> callable = null;
        if (this.portletRenderer.getHungWorkerAnalyzer().allowWorkerThreadAllocationForPortlet(portletFname)) {
            // All is well -- proceed as usual
            callable = new PortletExecutionCallable<V>(new Callable<V>() {
                /* (non-Javadoc)
                 * @see java.util.concurrent.Callable#call()
                 */
                @Override
                public V call() throws Exception {
                    //grab the current thread
                    workerThread = Thread.currentThread();
                    
                    //signal any threads waiting for the worker to start
                    started = System.currentTimeMillis();
                    startLatch.countDown();
                    
                    try {
                        //Run pre-execution interceptors
                        for (final IPortletExecutionInterceptor interceptor : PortletExecutionWorker.this.interceptors) {
                            interceptor.preExecution(request, response, PortletExecutionWorker.this);
                        }
                        
                        final V result = callInternal();
                        doPostExecution(null);
                        return result;
                    }
                    catch (Exception e) {
                        logger.warn("Portlet '" + portletWindowId + "' failed with an exception", e);
                        doPostExecution(e);
                        throw e;
                    }
                    finally {
                        complete = System.currentTimeMillis();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Execution complete on portlet " + portletWindowId + " in " + getDuration() + "ms");
                        }
                        
                        workerThread = null;
                    }
                }
            }, this);
        } else {
            // All is NOT well -- replace the Callable with one that throws a meaningful Exception
            callable = new PortletExecutionCallable<V>(new Callable<V>() {
                @Override
                public V call() throws Exception {
                    Exception e = new RuntimeException("Portlet '" + portletFname + "' was not allocated a worker thread because it already has too many workers in a hung state.");
                    logger.warn("Portlet '" + portletWindowId + "' failed with an exception", e);
                    doPostExecution(e);
                    throw e;
                }
            }, this);
        }
        
        this.future = this.executorService.submit(callable);
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
                logger.error("HandlerInterceptor.postExecution threw exception", ex2);
            }
        }
    }
    
    /**
     * @see Callable#call()
     */
    protected abstract V callInternal() throws Exception;
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker#isStarted()
     */
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
        return this.complete > 0 || (this.future != null && this.future.isDone());
    }
    
    @Override
    public boolean isRetrieved() {
        return this.retrieved;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker#waitForStart()
     */
    @Override
    public final long waitForStart(long timeout) throws InterruptedException {
        //Wait for start Callable to start
        this.startLatch.await(timeout, TimeUnit.MILLISECONDS);
        return this.started;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker#get(long)
     */
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
            this.logger.warn("Execution interrupted on portlet window " + this.portletWindowId, e);
            throw e;
        }
        catch (ExecutionException e) {
            this.logger.warn("Execution failed on portlet window " + this.portletWindowId, e);
            throw e;
        }
        catch (TimeoutException e) {
            final StringBuilder errorBuilder = new StringBuilder("Execution timed out on portlet window ");
            errorBuilder.append(this.portletWindowId);
            
            final Thread localWorkerThread = workerThread;
            if (localWorkerThread != null) {
                final State state = localWorkerThread.getState();
                final StackTraceElement[] stackTrace = localWorkerThread.getStackTrace();
                
                errorBuilder.append("\n\tPortlet Thread State: ").append(state).append("\n");
                errorBuilder.append("\tPortlet Thread Stack Trace: \n");
                
                for (final StackTraceElement stackTraceElement : stackTrace) {
                    errorBuilder.append("\t\tat ").append(stackTraceElement).append("\n");
                }
                
                errorBuilder.append("Portal Stack Trace:");
            }
            
            this.logger.warn(errorBuilder, e);
            
            throw e;
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker#cancel()
     */
    @Override
    public final void cancel() {
        if (this.future == null) {
            throw new IllegalStateException("submit() must be called before cancel() can be called");
        }
        
        //Mark worker as retrieved
        this.retrieved = true;

        //Notify the guarding req/res wrappers that cancel has been called
        this.canceled.set(true);
        
        //Cancel the future, interuppting the thread
        this.future.cancel(true);
        
        //Track the number of times cancel has been called
        this.cancelCount.incrementAndGet();
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker#getCancelCount()
     */
    @Override
    public final int getCancelCount() {
        return this.cancelCount.get();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker#getSubmitted()
     */
    @Override
    public final long getSubmittedTime() {
        return this.submitted;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker#getStarted()
     */
    @Override
    public final long getStartedTime() {
        return this.started;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker#getComplete()
     */
    @Override
    public final long getCompleteTime() {
        return this.complete;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker#getWait()
     */
    @Override
    public final long getWait() {
        return this.started - this.submitted;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker#getDuration()
     */
    @Override
    public final long getDuration() {
        return this.complete - this.started;
    }
    

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker#toString()
     */
    @Override
    public String toString() {
        return "PortletExecutionWorker [" +
                    "portletWindowId=" + this.portletWindowId + ", " +
                    "started=" + this.started + ", " +
                    "submitted=" + this.submitted + ", " +
                    "complete=" + this.complete + ", " +
                    "retrieved=" + this.retrieved + ", " +
                    "canceled=" + this.canceled + ", " +
                    "cancelCount=" + this.cancelCount + ", " +
                    "wait=" + this.getWait() + ", " +
                    "duration=" + this.getDuration() + "]";
    }
}