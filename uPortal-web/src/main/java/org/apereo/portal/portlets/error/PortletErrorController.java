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
package org.apereo.portal.portlets.error;

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
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.portlet.rendering.IPortletRenderer;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.services.AuthorizationServiceFacade;
import org.apereo.portal.url.IPortalRequestUtils;
import org.apereo.portal.url.IPortalUrlBuilder;
import org.apereo.portal.url.IPortalUrlProvider;
import org.apereo.portal.url.ParameterMap;
import org.apereo.portal.url.UrlType;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Portlet intended to replace the output for a portlet that failed to render due to an {@link
 * Exception}.
 */
@Controller
public class PortletErrorController {
    public static final String REQUEST_ATTRIBUTE__CURRENT_FAILED_PORTLET_WINDOW_ID =
            PortletErrorController.class.getName() + ".CURRENT_FAILED_PORTLET_WINDOW_ID";
    public static final String REQUEST_ATTRIBUTE__CURRENT_EXCEPTION_CAUSE =
            PortletErrorController.class.getName() + ".CURRENT_EXCEPTION_CAUSE";

    private IUserInstanceManager userInstanceManager;
    private IPortalRequestUtils portalRequestUtils;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletRenderer portletRenderer;
    private IPortalUrlProvider portalUrlProvider;

    /** @param userInstanceManager the userInstanceManager to set */
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }
    /** @param portalRequestUtils the portalRequestUtils to set */
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    /** @param portletWindowRegistry the portletWindowRegistry to set */
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    /** @param portletRenderer the portletRenderer to set */
    @Autowired
    public void setPortletRenderer(IPortletRenderer portletRenderer) {
        this.portletRenderer = portletRenderer;
    }
    /** @param portalUrlProvider the portalUrlProvider to set */
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
    public String renderError(RenderRequest request, RenderResponse response, ModelMap model) {

        HttpServletRequest httpRequest = this.portalRequestUtils.getPortletHttpRequest(request);
        IPortletWindowId currentFailedPortletWindowId =
                (IPortletWindowId)
                        request.getAttribute(REQUEST_ATTRIBUTE__CURRENT_FAILED_PORTLET_WINDOW_ID);
        model.addAttribute("portletWindowId", currentFailedPortletWindowId);
        Exception cause =
                (Exception) request.getAttribute(REQUEST_ATTRIBUTE__CURRENT_EXCEPTION_CAUSE);
        model.addAttribute("exception", cause);
        final String rootCauseMessage = ExceptionUtils.getRootCauseMessage(cause);
        model.addAttribute("rootCauseMessage", rootCauseMessage);

        // Maintenance Mode?
        if (cause != null && cause instanceof MaintenanceModeException) {
            model.addAttribute(
                    "customMaintenanceMessage",
                    ((MaintenanceModeException) cause).getCustomMaintenanceMessage());
            return "/jsp/PortletError/maintenance";
        }

        IUserInstance userInstance = this.userInstanceManager.getUserInstance(httpRequest);
        if (hasAdminPrivileges(userInstance)) {
            IPortletWindow window =
                    this.portletWindowRegistry.getPortletWindow(
                            httpRequest, currentFailedPortletWindowId);
            window.setRenderParameters(new ParameterMap());
            IPortalUrlBuilder adminRetryUrl =
                    this.portalUrlProvider.getPortalUrlBuilderByPortletWindow(
                            httpRequest, currentFailedPortletWindowId, UrlType.RENDER);
            model.addAttribute("adminRetryUrl", adminRetryUrl.getUrlString());

            final IPortletWindow portletWindow =
                    portletWindowRegistry.getPortletWindow(
                            httpRequest, currentFailedPortletWindowId);
            final IPortletEntity parentPortletEntity = portletWindow.getPortletEntity();
            final IPortletDefinition parentPortletDefinition =
                    parentPortletEntity.getPortletDefinition();
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
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("VIEW")
    public void executeReset(ActionRequest request, ActionResponse response) throws Exception {
        final String windowId = request.getParameter("failedPortletWindowId");
        if (StringUtils.isNotBlank(windowId)) {
            HttpServletRequest httpRequest = this.portalRequestUtils.getPortletHttpRequest(request);
            IPortletWindowId portletWindowId =
                    this.portletWindowRegistry.getPortletWindowId(httpRequest, windowId);

            HttpServletResponse httpResponse =
                    this.portalRequestUtils.getOriginalPortalResponse(request);
            this.portletRenderer.doReset(portletWindowId, httpRequest, httpResponse);

            IPortalUrlBuilder builder =
                    this.portalUrlProvider.getPortalUrlBuilderByPortletWindow(
                            httpRequest, portletWindowId, UrlType.RENDER);

            response.sendRedirect(builder.getUrlString());
        }
    }

    /**
     * @return true if the userInstance argument has administrative privileges regarding viewing
     *     error details
     */
    protected boolean hasAdminPrivileges(IUserInstance userInstance) {
        EntityIdentifier ei = userInstance.getPerson().getEntityIdentifier();
        IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());
        return ap.hasPermission(
                IPermission.ERROR_PORTLET, IPermission.VIEW_ACTIVITY, IPermission.DETAILS_TARGET);
    }
}
