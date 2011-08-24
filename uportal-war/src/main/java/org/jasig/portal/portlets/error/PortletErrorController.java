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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.ParameterMap;
import org.jasig.portal.url.UrlType;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Portlet intended to replace the output for a portlet that failed
 * to render due to an {@link Exception}.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
@Controller
public class PortletErrorController {
    public static final String REQUEST_ATTRIBUTE__CURRENT_FAILED_PORTLET_WINDOW_ID = PortletErrorController.class.getName() + ".CURRENT_FAILED_PORTLET_WINDOW_ID";
    public static final String REQUEST_ATTRIBUTE__CURRENT_EXCEPTION_CAUSE = PortletErrorController.class.getName() + ".CURRENT_EXCEPTION_CAUSE";

	protected static final String ERROR_OWNER = "UP_ERROR_CHAN";
	protected static final String ERROR_ACTIVITY = "VIEW";
	protected static final String ERROR_TARGET = "DETAILS";
	private IUserInstanceManager userInstanceManager;
	private IPortalRequestUtils portalRequestUtils;
	private IPortletWindowRegistry portletWindowRegistry;
	private IPortletRenderer portletRenderer;
	private IPortalUrlProvider portalUrlProvider;
	
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
	 * @param portletRenderer the portletRenderer to set
	 */
	@Autowired
	public void setPortletRenderer(IPortletRenderer portletRenderer) {
		this.portletRenderer = portletRenderer;
	}
	/**
	 * @param portalUrlProvider the portalUrlProvider to set
	 */
	@Autowired
	public void setPortalUrlProvider(IPortalUrlProvider portalUrlProvider) {
		this.portalUrlProvider = portalUrlProvider;
	}
	
	/**
	 * Render the error portlet view.
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return the name of the view to display
	 * @throws Exception
	 */
	@RequestMapping("VIEW")
	public String renderError(RenderRequest request,
			RenderResponse response, ModelMap model) throws Exception {
		
		HttpServletRequest httpRequest = this.portalRequestUtils.getPortletHttpRequest(request);
		IPortletWindowId currentFailedPortletWindowId = (IPortletWindowId) request.getAttribute(REQUEST_ATTRIBUTE__CURRENT_FAILED_PORTLET_WINDOW_ID);
		model.addAttribute("portletWindowId", currentFailedPortletWindowId);
		Exception cause = (Exception) request.getAttribute(REQUEST_ATTRIBUTE__CURRENT_EXCEPTION_CAUSE);
		model.addAttribute("exception", cause);
		final String rootCauseMessage = ExceptionUtils.getRootCauseMessage(cause);
		model.addAttribute("rootCauseMessage", rootCauseMessage);
		
		IUserInstance userInstance = this.userInstanceManager.getUserInstance(httpRequest);
		if(hasAdminPrivileges(userInstance)) {
			IPortletWindow window = this.portletWindowRegistry.getPortletWindow(httpRequest, currentFailedPortletWindowId);
			window.setRenderParameters(new ParameterMap());
			IPortalUrlBuilder adminRetryUrl = this.portalUrlProvider.getPortalUrlBuilderByPortletWindow(httpRequest, currentFailedPortletWindowId, UrlType.RENDER);
			model.addAttribute("adminRetryUrl", adminRetryUrl.getUrlString());
			
			final IPortletWindow portletWindow = portletWindowRegistry.getPortletWindow(httpRequest, currentFailedPortletWindowId);
            final IPortletEntity parentPortletEntity = portletWindow.getPortletEntity();
            final IPortletDefinition parentPortletDefinition = parentPortletEntity.getPortletDefinition();
            model.addAttribute("channelDefinition", parentPortletDefinition);
            
			StringWriter stackTraceWriter = new StringWriter();
			cause.printStackTrace(new PrintWriter(stackTraceWriter));
			
			model.addAttribute("stackTrace", stackTraceWriter.toString());
			
			return "/jsp/PortletError/detailed";
		} 
		// no admin privileges, return generic view
		return "/jsp/PortletError/generic";
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("VIEW")
	public void executeReset(ActionRequest request,
			ActionResponse response) throws Exception {
		final String windowId = request.getParameter("failedPortletWindowId");
		if(StringUtils.isNotBlank(windowId)) {
			HttpServletRequest httpRequest = this.portalRequestUtils.getPortletHttpRequest(request);
			IPortletWindowId portletWindowId = this.portletWindowRegistry.getPortletWindowId(httpRequest, windowId);

			HttpServletResponse httpResponse = this.portalRequestUtils.getOriginalPortalResponse(request);
			this.portletRenderer.doReset(portletWindowId, httpRequest, httpResponse);

			IPortalUrlBuilder builder = this.portalUrlProvider.getPortalUrlBuilderByPortletWindow(httpRequest, portletWindowId, UrlType.RENDER);

			response.sendRedirect(builder.getUrlString());
		}
	}

	/**
	 * 
	 * @return true if the userInstance argument has administrative privileges regarding viewing error details
	 */
	protected boolean hasAdminPrivileges(IUserInstance userInstance) {
		EntityIdentifier ei = userInstance.getPerson().getEntityIdentifier();
	    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
	    return ap.hasPermission(ERROR_OWNER, ERROR_ACTIVITY, ERROR_TARGET);
	}
}
