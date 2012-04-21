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
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.portlet.rendering.PortletRenderResult;
import org.jasig.portal.portlet.rendering.RenderPortletOutputHandler;
import org.jasig.portal.portlets.error.PortletErrorController;
import org.jasig.portal.utils.web.PortletHttpServletRequestWrapper;

/**
 * Worker used to execute render requests on the error portlet. Does not use
 * any thread-pool code to make sure the error portlet still renders in the event
 * of the thread pool being broken.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
final class PortletFailureExecutionWorker implements IPortletFailureExecutionWorker {
    
    private static final int SIGNAL_THE_OPERATION_DOES_NOT_TIMEOUT = -1;
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final Map<String, Object> executionAttributes = new ConcurrentHashMap<String, Object>();
    
    private final IPortletRenderer portletRenderer;
    private final List<IPortletExecutionInterceptor> interceptors;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final IPortletWindowId errorPortletWindowId;
    private final IPortletWindowId failedPortletWindowId;
    private final String failedPortletFname;
    private final Exception cause;

    private PortletRenderResult portletRenderResult;
    private String output;
    
    private boolean retrieved = false;
    private long submitted = 0;
    private long completed = 0;
    
    public PortletFailureExecutionWorker(
            IPortletRenderer portletRenderer, List<IPortletExecutionInterceptor> interceptors,
            HttpServletRequest request, HttpServletResponse response, IPortletWindowId errorPortletWindowId,
            IPortletWindowId failedPortletWindowId, String failedPortletFname, Exception cause) {
        
        this.portletRenderer = portletRenderer;
        this.interceptors = interceptors;
        this.request = request;
        this.response = response;
        this.errorPortletWindowId = errorPortletWindowId;
        this.failedPortletWindowId = failedPortletWindowId;
        this.failedPortletFname = failedPortletFname;
        this.cause = cause;
    }

    @Override
    public ExecutionType getExecutionType() {
        return ExecutionType.FAILURE;
    }

    @Override
    public void submit() {
        if (this.submitted > 0) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " for " + this.getPortletWindowId() + " has already been submitted.");
        }
        
        this.submitted = System.currentTimeMillis();
        
        //Run pre-submit interceptors
        for (final IPortletExecutionInterceptor interceptor : this.interceptors) {
            interceptor.preSubmit(request, response, this);
        }
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
    
    @Override
    public String getOutput(long timeout) throws Exception {
        this.get(timeout);
        return this.output;
    }

    @Override
    public long waitForStart(long timeout) throws InterruptedException {
        this.renderError(timeout);
        return this.submitted;
    }

    @Override
    public synchronized PortletRenderResult get(long timeout) throws Exception {
        this.retrieved = true;
        this.renderError(timeout);
        return this.portletRenderResult;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker#cancel()
     */
    @Override
    public void cancel() {
        //NOOP
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker#getCancelCount()
     */
    @Override
    public int getCancelCount() {
        return 0;
    }

    protected synchronized void renderError(long timeout) {
        //Make sure the error rendering only happens once
        if (this.completed > 0) {
            return;
        }
        
        //Wrap the request to scope the attributes to just this execution
        final PortletHttpServletRequestWrapper wrappedRequest = new PortletHttpServletRequestWrapper(request);
        wrappedRequest.setAttribute(PortletErrorController.REQUEST_ATTRIBUTE__CURRENT_FAILED_PORTLET_WINDOW_ID, failedPortletWindowId);
        wrappedRequest.setAttribute(PortletErrorController.REQUEST_ATTRIBUTE__CURRENT_EXCEPTION_CAUSE, cause);
        
        //Run pre-execution interceptors
        for (final IPortletExecutionInterceptor interceptor : this.interceptors) {
            interceptor.preExecution(request, response, this);
        }

        //Aggressive exception handling to make sure at least something is written out when an error happens.
        try {
            final String characterEncoding = response.getCharacterEncoding();
            final RenderPortletOutputHandler renderPortletOutputHandler = new RenderPortletOutputHandler(characterEncoding);
            
            this.portletRenderResult = this.portletRenderer.doRenderMarkup(errorPortletWindowId, wrappedRequest, response, renderPortletOutputHandler);
            doPostExecution(null);
            this.output = renderPortletOutputHandler.getOutput();
        }
        catch (Exception e) {
            doPostExecution(e);
            this.logger.error("Exception while dispatching to error handling portlet", e);
            this.output = "Error Portlet Unavailable. Please contact your portal adminstrators.";
        }
        
        this.completed = System.currentTimeMillis();
    }

    @Override
    public Object setExecutionAttribute(String name, Object value) {
        if (value == null) {
            return executionAttributes.remove(name);
        }
        return this.executionAttributes.put(name, value);
    }

    @Override
    public Object getExecutionAttribute(String name) {
        return this.executionAttributes.get(name);
    }

    @Override
    public IPortletWindowId getPortletWindowId() {
        return this.failedPortletWindowId;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionContext#getPortletFname()
     */
    @Override
    public String getPortletFname() {
        return this.failedPortletFname;
    }

    @Override
    public boolean isSubmitted() {
        return this.submitted > 0;
    }

    @Override
    public boolean isStarted() {
        return this.submitted > 0;
    }

    @Override
    public boolean isComplete() {
        return this.completed > 0;
    }
    
    @Override
    public boolean isRetrieved() {
        return this.retrieved;
    }

    @Override
    public long getSubmittedTime() {
        return this.submitted;
    }

    @Override
    public long getStartedTime() {
        return this.submitted;
    }

    @Override
    public long getCompleteTime() {
        return this.completed;
    }

    @Override
    public long getWait() {
        return 0;
    }

    @Override
    public long getDuration() {
        return this.completed - this.submitted;
    }

    @Override
    public long getApplicableTimeout() {
        return SIGNAL_THE_OPERATION_DOES_NOT_TIMEOUT;
    }
}