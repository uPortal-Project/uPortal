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
package org.apereo.portal.url.xml;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.portlet.PortletUtils;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.url.IPortalUrlBuilder;
import org.apereo.portal.url.IPortalUrlProvider;
import org.apereo.portal.url.IPortletUrlBuilder;
import org.apereo.portal.url.IUrlBuilder;
import org.apereo.portal.url.UrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Wrapper class for {@link IPortalUrlProvider} that makes use easier in XSL */
@Service("xslPortalUrlProvider")
public class XsltPortalUrlProvider {
    public static final String XSLT_PORTAL_URL_PROVIDER = "XSLT_PORTAL_URL_PROVIDER";
    public static final String CURRENT_REQUEST = "CURRENT_REQUEST";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static XsltPortalUrlProvider getUrlProvider(Object urlProvider) {
        return (XsltPortalUrlProvider) urlProvider;
    }

    public static HttpServletRequest getHttpServletRequest(Object request) {
        return (HttpServletRequest) request;
    }

    /** Needed due to compile-time type checking limitations of the XSLTC compiler */
    public static void addParameter(IUrlBuilder urlBuilder, String name, String value) {
        urlBuilder.addParameter(name, value);
    }

    private IPortalUrlProvider portalUrlProvider;
    private IPortletWindowRegistry portletWindowRegistry;

    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider urlProvider) {
        this.portalUrlProvider = urlProvider;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    /** Create a portal URL builder for the specified fname or layoutId (fname takes precedence) */
    public IPortalUrlBuilder getPortalUrlBuilder(
            HttpServletRequest request, String fname, String layoutId, String type) {
        try {
            final UrlType urlType;
            if (StringUtils.isEmpty(type)) {
                urlType = UrlType.RENDER;
            } else {
                urlType = UrlType.valueOfIgnoreCase(type);
            }

            if (StringUtils.isNotEmpty(fname)) {
                final IPortalUrlBuilder portalUrlBuilderByPortletFName =
                        this.portalUrlProvider.getPortalUrlBuilderByPortletFName(
                                request, fname, urlType);
                return portalUrlBuilderByPortletFName;
            }

            if (StringUtils.isNotEmpty(layoutId)) {
                final IPortalUrlBuilder portalUrlBuilderByLayoutNode =
                        this.portalUrlProvider.getPortalUrlBuilderByLayoutNode(
                                request, layoutId, urlType);
                return portalUrlBuilderByLayoutNode;
            }

            return this.portalUrlProvider.getDefaultUrl(request);
        } catch (Exception e) {
            this.logger.error(
                    "Failed to create IPortalUrlBuilder for fname='"
                            + fname
                            + "', layoutId='"
                            + layoutId
                            + "', type='"
                            + type
                            + "'. # will be returned instead.",
                    e);
            return new FailSafePortalUrlBuilder();
        }
    }

    /**
     * Get the portlet URL builder for the specified fname or layoutId (fname takes precedence)
     * @param request
     * @param portalUrlBuilder
     * @param fname
     * @param layoutId
     * @param state
     * @param mode
     * @param copyCurrentRenderParameters
     * @return
     * @deprecated As of uPortal release 4.1, replaced by
     *     {@link #getPortletUrlBuilder(HttpServletRequest request, IPortalUrlBuilder portalUrlBuilder, String fname, String layoutId, String state, String mode, String copyCurrentRenderParameters, String resourceId))
     */
    @Deprecated
    public IPortletUrlBuilder getPortletUrlBuilder(
            HttpServletRequest request,
            IPortalUrlBuilder portalUrlBuilder,
            String fname,
            String layoutId,
            String state,
            String mode,
            String copyCurrentRenderParameters) {
        return this.getPortletUrlBuilder(
                request,
                portalUrlBuilder,
                fname,
                layoutId,
                state,
                mode,
                copyCurrentRenderParameters,
                null);
    }

    /**
     * Get the portlet URL builder for the specified fname or layoutId (fname takes precedence)
     *
     * @param request
     * @param portalUrlBuilder
     * @param fname - can be empty string
     * @param layoutId - can by empty string
     * @param state - can be empty string
     * @param mode - can be empty string
     * @param copyCurrentRenderParameters
     * @param resourceId - can be empty string
     * @return IPortletUrlBuilder
     * @since 4.1
     */
    public IPortletUrlBuilder getPortletUrlBuilder(
            HttpServletRequest request,
            IPortalUrlBuilder portalUrlBuilder,
            String fname,
            String layoutId,
            String state,
            String mode,
            String copyCurrentRenderParameters,
            String resourceId) {
        final IPortletUrlBuilder portletUrlBuilder;

        if (StringUtils.isNotEmpty(fname)) {
            final IPortletWindow portletWindow =
                    this.portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(
                            request, fname);
            final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
            portletUrlBuilder = portalUrlBuilder.getPortletUrlBuilder(portletWindowId);
        } else if (StringUtils.isNotEmpty(layoutId)) {
            final IPortletWindow portletWindow =
                    this.portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(
                            request, layoutId);
            final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
            portletUrlBuilder = portalUrlBuilder.getPortletUrlBuilder(portletWindowId);
        } else {
            final IPortletWindowId targetPortletWindowId =
                    portalUrlBuilder.getTargetPortletWindowId();
            if (targetPortletWindowId == null) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.warn(
                            "Can only target the default portlet if the root portal-url targets a portlet.",
                            new Throwable());
                } else {
                    this.logger.warn(
                            "Can only target the default portlet if the root portal-url targets a portlet. Enable debug for stack trace.");
                }
                return new FailSafePortletUrlBuilder(null, portalUrlBuilder);
            }

            portletUrlBuilder = portalUrlBuilder.getTargetedPortletUrlBuilder();
        }

        portletUrlBuilder.setCopyCurrentRenderParameters(
                Boolean.parseBoolean(copyCurrentRenderParameters));

        if (StringUtils.isNotEmpty(state)) {
            portletUrlBuilder.setWindowState(PortletUtils.getWindowState(state));
        }

        if (StringUtils.isNotEmpty(mode)) {
            portletUrlBuilder.setPortletMode(PortletUtils.getPortletMode(mode));
        }

        if (StringUtils.isNotEmpty(resourceId)
                && portletUrlBuilder.getPortalUrlBuilder().getUrlType() == UrlType.RESOURCE) {
            portletUrlBuilder.setResourceId(resourceId);
        }

        return portletUrlBuilder;
    }
}
