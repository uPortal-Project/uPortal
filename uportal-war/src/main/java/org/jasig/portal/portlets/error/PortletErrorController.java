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

package org.jasig.portal.portlets.error;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.PortletExecutionManager;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;

/**
 * Portlet intended to replace the output for a portlet that failed
 * to render due to an {@link Exception}.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
public class PortletErrorController extends AbstractController {
    public static final String REQUEST_ATTRIBUTE__CURRENT_FAILED_PORTLET_WINDOW_ID = PortletExecutionManager.class.getName() + ".CURRENT_FAILED_PORTLET_WINDOW_ID";
    public static final String REQUEST_ATTRIBUTE__CURRENT_EXCEPTION_CAUSE = PortletExecutionManager.class.getName() + ".CURRENT_EXCEPTION_CAUSE";

	protected static final String ERROR_OWNER = "UP_ERROR_CHAN";
	protected static final String ERROR_ACTIVITY = "VIEW";
	protected static final String ERROR_TARGET = "DETAILS";
	private IUserInstanceManager userInstanceManager;
	private IPortalRequestUtils portalRequestUtils;
	private IPortletWindowRegistry portletWindowRegistry;
	private IPortletEntityRegistry portletEntityRegistry;
	
	/**
	 * @param userInstanceManager the userInstanceManager to set
	 */
	@Autowired
	public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
		this.userInstanceManager = userInstanceManager;
	}
	/**
	 * @param portalRequestUtils the portalRequestUtils to set
	 */
	@Autowired
	public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
		this.portalRequestUtils = portalRequestUtils;
	}
	/**
	 * @param portletWindowRegistry the portletWindowRegistry to set
	 */
	@Autowired
	public void setPortletWindowRegistry(
			IPortletWindowRegistry portletWindowRegistry) {
		this.portletWindowRegistry = portletWindowRegistry;
	}
	/**
	 * @param portletEntityRegistry the portletEntityRegistry to set
	 */
	@Autowired
	public void setPortletEntityRegistry(
			IPortletEntityRegistry portletEntityRegistry) {
		this.portletEntityRegistry = portletEntityRegistry;
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.portlet.mvc.AbstractController#handleRenderRequestInternal(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
	 */
	@Override
	protected ModelAndView handleRenderRequestInternal(RenderRequest request,
			RenderResponse response) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		HttpServletRequest httpRequest = this.portalRequestUtils.getOriginalPortalRequest(request);
		IUserInstance userInstance = this.userInstanceManager.getUserInstance(httpRequest);
		if(canSeeErrorDetails(userInstance)) {
			IPortletWindowId currentFailedPortletWindowId = (IPortletWindowId) request.getAttribute(REQUEST_ATTRIBUTE__CURRENT_FAILED_PORTLET_WINDOW_ID);
			model.put("portletWindowId", currentFailedPortletWindowId);
			
			final IPortletEntity parentPortletEntity = portletWindowRegistry.getParentPortletEntity(httpRequest, currentFailedPortletWindowId);
            final IPortletDefinition parentPortletDefinition = portletEntityRegistry.getParentPortletDefinition(parentPortletEntity.getPortletEntityId());
            model.put("channelDefinition", parentPortletDefinition);
            
			Exception cause = (Exception) request.getAttribute(REQUEST_ATTRIBUTE__CURRENT_EXCEPTION_CAUSE);
			model.put("exception", cause);
			StringWriter stackTraceWriter = new StringWriter();
			cause.printStackTrace(new PrintWriter(stackTraceWriter));
			
			model.put("stackTrace", stackTraceWriter.toString());
			
			return new ModelAndView("/jsp/PortletError/detailed", model);
		} else {
			return new ModelAndView("/jsp/PortletError/generic", model);
		}
	}

	/**
	 * 
	 * @return
	 */
	protected boolean canSeeErrorDetails(IUserInstance userInstance) {
		EntityIdentifier ei = userInstance.getPerson().getEntityIdentifier();
	    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
	    return ap.hasPermission(ERROR_OWNER, ERROR_ACTIVITY, ERROR_TARGET);
	}
}
