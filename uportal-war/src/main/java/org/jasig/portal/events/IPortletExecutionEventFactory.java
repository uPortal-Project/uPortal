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
package org.jasig.portal.events;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker;
import org.jasig.portal.url.IPortalRequestInfo;

/**
 * Publishes events related to portlet execution
 * 
 * @author Eric Dalquist
 */
public interface IPortletExecutionEventFactory {
    
    //********** Portlet Hung Events **********//
    
    public void publishPortletHungEvent(HttpServletRequest request, Object source,
            IPortletExecutionWorker<?> worker);
    
    public void publishPortletHungCompleteEvent(Object source,
            IPortletExecutionWorker<?> worker);
    
    //********** Portlet Execution Events **********//
    
    public void publishPortletActionExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters);
    public void publishPortletEventExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters, QName eventName);
    public void publishPortletRenderHeaderExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters, boolean targeted, boolean usedPortalCache);
    public void publishPortletRenderExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters, boolean targeted, boolean usedPortalCache);
    public void publishPortletResourceExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters, String resourceId, boolean usedBrowserCache, boolean usedPortalCache);
    
    //********** Portal Rendering Pipeline Events **********//
    
    public void publishPortalRenderEvent(HttpServletRequest request, Object source, String requestPathInfo, long executionTime,
            IPortalRequestInfo portalRequestInfo);
}