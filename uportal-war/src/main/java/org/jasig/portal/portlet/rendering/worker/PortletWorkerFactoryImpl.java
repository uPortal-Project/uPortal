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

import javax.portlet.Event;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Factory for creating {@link IPortletExecutionWorker}s that handle the asynchronous execution of
 * portlet requests
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class PortletWorkerFactoryImpl implements IPortletWorkerFactory {
    public static final String DEFAULT_ERROR_PORTLET_FNAME = "error";
    
    private ExecutorService portletThreadPool;
    private IPortletRenderer portletRenderer;
    private List<IPortletExecutionInterceptor> executionInterceptors;
    private String errorPortletFName = DEFAULT_ERROR_PORTLET_FNAME;
    private IUserInstanceManager userInstanceManager;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletWindowRegistry portletWindowRegistry;
    
    public void setErrorPortletFName(String errorPortletFName) {
        this.errorPortletFName = errorPortletFName;
    }

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Autowired
    public void setPortletThreadPool(@Qualifier("portletThreadPool") ExecutorService portletThreadPool) {
        this.portletThreadPool = portletThreadPool;
    }

    @Autowired
    public void setPortletRenderer(IPortletRenderer portletRenderer) {
        this.portletRenderer = portletRenderer;
    }
    
    @javax.annotation.Resource(name="portletExecutionInterceptors")
    public void setExecutionInterceptors(List<IPortletExecutionInterceptor> executionInterceptors) {
        this.executionInterceptors = executionInterceptors;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletWorkerFactory#getActionWorker()
     */
    @Override
    public IPortletActionExecutionWorker createActionWorker(HttpServletRequest request, HttpServletResponse response, IPortletWindowId portletWindowId) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        return new PortletActionExecutionWorker(portletThreadPool, executionInterceptors, portletRenderer, request, response, portletWindow);
    }
    

    @Override
    public IPortletEventExecutionWorker createEventWorker(HttpServletRequest request, HttpServletResponse response, IPortletWindowId portletWindowId, Event event) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        return new PortletEventExecutionWorker(portletThreadPool, executionInterceptors, portletRenderer, request, response, portletWindow, event);
    }


    
    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.rendering.worker.IPortletWorkerFactory#createRenderHeaderWorker(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.jasig.portal.portlet.om.IPortletWindowId)
	 */
	@Override
	public IPortletRenderExecutionWorker createRenderHeaderWorker(
			HttpServletRequest request, HttpServletResponse response,
			IPortletWindowId portletWindowId) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
		return new PortletRenderHeaderExecutionWorker(portletThreadPool,executionInterceptors, portletRenderer, request, response, portletWindow);
	}

	@Override
    public IPortletRenderExecutionWorker createRenderWorker(HttpServletRequest request, HttpServletResponse response, IPortletWindowId portletWindowId) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        return new PortletRenderExecutionWorker(portletThreadPool, executionInterceptors, portletRenderer, request, response, portletWindow);
    }

    @Override
    public IPortletResourceExecutionWorker createResourceWorker(HttpServletRequest request, HttpServletResponse response, IPortletWindowId portletWindowId) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        return new PortletResourceExecutionWorker(portletThreadPool, executionInterceptors, portletRenderer, request, response, portletWindow);
    }
    
    @Override
    public IPortletFailureExecutionWorker createFailureWorker(HttpServletRequest request, HttpServletResponse response, IPortletWindowId failedPortletWindowId, Exception cause) {
        final IPortletWindowId errorPortletWindowId = this.getErrorPortletWindowId(request, this.errorPortletFName);
        return new PortletFailureExecutionWorker(portletRenderer, executionInterceptors, request, response, errorPortletWindowId, failedPortletWindowId, this.errorPortletFName, cause);
        /*
        HttpSession session = request.getSession();
        Map<IPortletWindowId, Exception> portletFailureMap = safeRetrieveErrorMapFromSession(session);
        portletFailureMap.put(failedPortletWindowId, cause);
        
        // once we've grabbed the output, remove the throwable from the session map
        portletFailureMap.remove(failedPortletWindowId);
         */
    }
    
    protected IPortletWindowId getErrorPortletWindowId(HttpServletRequest request, String fname) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final String errorPortletSubscribeId = userLayoutManager.getSubscribeId(fname);
        final IPortletEntity errorPortletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(request, userInstance, errorPortletSubscribeId);
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindow(request, errorPortletEntity.getPortletEntityId());
        return portletWindow.getPortletWindowId();
    }
    
    protected String getPortletFname(HttpServletRequest request, IPortletWindowId portletWindowId) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        return portletDefinition.getFName();
    }
}
