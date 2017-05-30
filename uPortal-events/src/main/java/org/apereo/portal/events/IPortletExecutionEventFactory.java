/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.events;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.rendering.worker.IPortletExecutionWorker;
import org.apereo.portal.url.IPortalRequestInfo;

/**
 * Publishes events related to portlet execution
 *
 */
public interface IPortletExecutionEventFactory {

    //********** Portlet Hung Events **********//

    void publishPortletHungEvent(
            HttpServletRequest request, Object source, IPortletExecutionWorker<?> worker);

    void publishPortletHungCompleteEvent(Object source, IPortletExecutionWorker<?> worker);

    //********** Portlet Execution Events **********//

    void publishPortletActionExecutionEvent(
            HttpServletRequest request,
            Object source,
            IPortletWindowId portletWindowId,
            long executionTime);

    void publishPortletEventExecutionEvent(
            HttpServletRequest request,
            Object source,
            IPortletWindowId portletWindowId,
            long executionTime,
            QName eventName);

    void publishPortletRenderHeaderExecutionEvent(
            HttpServletRequest request,
            Object source,
            IPortletWindowId portletWindowId,
            long executionTime,
            boolean targeted,
            boolean cached);

    void publishPortletRenderExecutionEvent(
            HttpServletRequest request,
            Object source,
            IPortletWindowId portletWindowId,
            long executionTime,
            boolean targeted,
            boolean cached);

    void publishPortletResourceExecutionEvent(
            HttpServletRequest request,
            Object source,
            IPortletWindowId portletWindowId,
            long executionTime,
            boolean usedBrowserCache,
            boolean usedPortalCache);

    //********** Portal Rendering Pipeline Events **********//

    void publishPortalRenderEvent(
            HttpServletRequest request,
            Object source,
            String requestPathInfo,
            long executionTime,
            IPortalRequestInfo portalRequestInfo);
}
