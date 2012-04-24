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
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.portlet.rendering.ResourcePortletOutputHandler;

class PortletResourceExecutionWorker extends PortletExecutionWorker<Long> implements IPortletResourceExecutionWorker {

    public PortletResourceExecutionWorker(
            ExecutorService executorService, List<IPortletExecutionInterceptor> interceptors, IPortletRenderer portletRenderer, 
            HttpServletRequest request, HttpServletResponse response, IPortletWindow portletWindow) {
        
        super(executorService, interceptors, portletRenderer, request, response, portletWindow, 
                portletWindow.getPortletEntity().getPortletDefinition().getResourceTimeout() != null
                        ? portletWindow.getPortletEntity().getPortletDefinition().getResourceTimeout()
                        : portletWindow.getPortletEntity().getPortletDefinition().getTimeout());
    }

    @Override
    public ExecutionType getExecutionType() {
        return ExecutionType.RESOURCE;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.PortletExecutionManager.PortletExecutionWorker#callInternal()
     */
    @Override
    protected Long callInternal() throws Exception {
        return portletRenderer.doServeResource(portletWindowId, request, response, new ResourcePortletOutputHandler(response));
    }
    
}