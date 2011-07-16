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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Interceptor used to add behavior before or after portlet execution. {@link #postExecution(HttpServletRequest, HttpServletResponse, IPortletWindowId, Exception)}
 * will always be called within a finally block.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletExecutionInterceptor {
    /**
     * Called immediately before the worker is submitted to run. Runs in the same thread as the thread that calls
     * {@link IPortletExecutionWorker#submit()}
     * 
     * @param request The request used by the worker, this is a SHARED object that is not scoped to the worker so be careful.
     * @param response The response used by the worker, this is a SHARED object that is not scoped to the worker so be careful.
     * @param context The portlet execution context 
     */
    public void preSubmit(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context);
    
    /**
     * Called immediately before the worker is executed. Runs in the same thread as the thread that executes to worker 
     * 
     * @param request The request used by the worker, this is a SHARED object that is not scoped to the worker so be careful.
     * @param response The response used by the worker, this is a SHARED object that is not scoped to the worker so be careful.
     * @param context The portlet execution context 
     */
    public void preExecution(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context);
    
    /**
     * Called immediately after the worker is executed. Runs in the same thread as the thread that executes to worker
     * 
     * @param request The request used by the worker, this is a SHARED object that is not scoped to the worker so be careful.
     * @param response The response used by the worker, this is a SHARED object that is not scoped to the worker so be careful.
     * @param context The portlet execution context
     * @param e Exception thrown during execution, null if no exception was thrown
     */
    public void postExecution(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context, Exception e);
}
