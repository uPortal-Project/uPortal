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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    protected final IPortletRenderer portletRenderer;
    protected final IPortletWindowId portletWindowId;
    protected final HttpServletRequest request;
    protected final HttpServletResponse response;
    
    private volatile Future<V> future;
    private volatile long submitted = 0;
    private volatile long started = 0;
    private volatile long complete = 0;
    
    public PortletExecutionWorker(
            ExecutorService executorService, List<IPortletExecutionInterceptor> interceptors, IPortletRenderer portletRenderer, 
            HttpServletRequest request, HttpServletResponse response, IPortletWindowId portletWindowId) {

        this.executorService = executorService;
        this.interceptors = interceptors;
        this.portletRenderer = portletRenderer;
        this.request = request;
        this.response = response;
        this.portletWindowId = portletWindowId;
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
        
        this.future = this.executorService.submit(new Callable<V>() {
            /* (non-Javadoc)
             * @see java.util.concurrent.Callable#call()
             */
            @Override
            public V call() throws Exception {
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
                }
            }
        });
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
        return this.complete > 0;
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
        
        try {
            final long startTime = this.waitForStart(timeout);
            return this.future.get(timeout - (System.currentTimeMillis() - startTime), TimeUnit.MILLISECONDS);
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
            this.logger.warn("Execution timed out on portlet window " + this.portletWindowId, e);
            throw e;
        }
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
                    "wait=" + this.getWait() + ", " +
                    "duration=" + this.getDuration() + "]";
    }
    
}