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

import javax.portlet.Event;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Create workers used to execute portlet actions. The workers take care of submitting to
 * a thread pool for execution and tracking the execution and result.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletWorkerFactory {
    /**
     * Create a worker that will execute an action request on the specified portlet window.
     */
    public IPortletActionExecutionWorker createActionWorker(HttpServletRequest request, HttpServletResponse response, IPortletWindowId portletWindowId);
    /**
     * Create a worker that will execute an event request on the specified portlet window 
     */
    public IPortletEventExecutionWorker createEventWorker(HttpServletRequest request, HttpServletResponse response, IPortletWindowId portletWindowId, Event event);
    /**
     * Create a worker that will execute a render request on the specified portlet window
     */
    public IPortletRenderExecutionWorker createRenderHeaderWorker(HttpServletRequest request, HttpServletResponse response, IPortletWindowId portletWindowId);
    /**
     * Create a worker that will execute a render request on the specified portlet window
     */
    public IPortletRenderExecutionWorker createRenderWorker(HttpServletRequest request, HttpServletResponse response, IPortletWindowId portletWindowId);
    /**
     * Create a worker that will execute a resource request on the specified portlet window
     */
    public IPortletResourceExecutionWorker createResourceWorker(HttpServletRequest request, HttpServletResponse response, IPortletWindowId portletWindowId);
    /**
     * Create a worker that will execute the failure handler on the specified portlet window that threw an exception
     */
    public IPortletFailureExecutionWorker createFailureWorker(HttpServletRequest request, HttpServletResponse response, IPortletWindowId failedPortletWindowId, Exception cause);
}
