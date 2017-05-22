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
package org.apereo.portal.url;

import java.util.List;
import java.util.Map;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.IUserPreferencesManager;
import org.apereo.portal.api.portlet.DelegationRequest;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription.LayoutNodeType;
import org.apereo.portal.portlet.delegation.IPortletDelegationManager;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Generates {@link IPortalUrlBuilder} objects based on various layout and portlet targets.
 *
 */
@Service
public class PortalUrlProviderImpl implements IPortalUrlProvider {
    private static final String PORTAL_ACTION_URL_BUILDER =
            PortalUrlProviderImpl.class.getName() + ".PORTAL_ACTION_URL_BUILDER";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IUrlSyntaxProvider urlSyntaxProvider;
    private IUserInstanceManager userInstanceManager;
    private IUrlNodeSyntaxHelperRegistry urlNodeSyntaxHelperRegistry;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletDelegationManager portletDelegationManager;

    @Autowired
    public void setPortletDelegationManager(IPortletDelegationManager portletDelegationManager) {
        this.portletDelegationManager = portletDelegationManager;
    }

    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setUrlNodeSyntaxHelperRegistry(
            IUrlNodeSyntaxHelperRegistry urlNodeSyntaxHelperRegistry) {
        this.urlNodeSyntaxHelperRegistry = urlNodeSyntaxHelperRegistry;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Override
    public IPortalActionUrlBuilder getPortalActionUrlBuilder(HttpServletRequest request) {
        return (IPortalActionUrlBuilder) request.getAttribute(PORTAL_ACTION_URL_BUILDER);
    }

    @Override
    public IPortalActionUrlBuilder convertToPortalActionUrlBuilder(
            HttpServletRequest request, IPortalUrlBuilder portalUrlBuilder) {
        request.setAttribute(PORTAL_ACTION_URL_BUILDER, portalUrlBuilder);
        return (IPortalActionUrlBuilder) portalUrlBuilder;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.url.IPortalUrlProvider#getDefaultUrl(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public IPortalUrlBuilder getDefaultUrl(HttpServletRequest request) {
        final IUrlNodeSyntaxHelper urlNodeSyntaxHelper =
                this.urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request);
        final String defaultLayoutNodeId = urlNodeSyntaxHelper.getDefaultLayoutNodeId(request);
        return this.getPortalUrlBuilderByLayoutNode(request, defaultLayoutNodeId, UrlType.RENDER);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.url.IPortalUrlProvider#getPortalUrlBuilderByLayoutNode(javax.servlet.http.HttpServletRequest, java.lang.String, org.apereo.portal.url.UrlType)
     */
    @Override
    public IPortalUrlBuilder getPortalUrlBuilderByLayoutNode(
            HttpServletRequest request, String layoutNodeId, UrlType urlType) {
        final IPortletWindowId portletWindowId = getPortletWindowId(request, layoutNodeId);

        return new PortalUrlBuilder(
                this.urlSyntaxProvider, request, layoutNodeId, portletWindowId, urlType);
    }

    private IPortletWindowId getPortletWindowId(HttpServletRequest request, String layoutNodeId) {
        if (layoutNodeId == null) {
            return null;
        }

        final LayoutNodeType layoutNodeType = this.getLayoutNodeType(request, layoutNodeId);
        if (layoutNodeType == null) {
            throw new IllegalArgumentException("No layout node exists for id: " + layoutNodeId);
        }

        if (layoutNodeType != LayoutNodeType.PORTLET) {
            return null;
        }

        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(
                        request, layoutNodeId);
        if (portletWindow == null) {
            return null;
        }

        return portletWindow.getPortletWindowId();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.url.IPortalUrlProvider#getPortalUrlBuilderByPortletWindow(javax.servlet.http.HttpServletRequest, org.apereo.portal.portlet.om.IPortletWindowId, org.apereo.portal.url.UrlType)
     */
    @Override
    public IPortalUrlBuilder getPortalUrlBuilderByPortletWindow(
            HttpServletRequest request, IPortletWindowId portletWindowId, UrlType urlType) {
        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        return getPortalUrlBuilderByPortletWindow(request, portletWindow, urlType);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.url.IPortalUrlProvider#getPortalUrlBuilderByPortletFName(javax.servlet.http.HttpServletRequest, java.lang.String, org.apereo.portal.url.UrlType)
     */
    @Override
    public IPortalUrlBuilder getPortalUrlBuilderByPortletFName(
            HttpServletRequest request, String portletFName, UrlType urlType) {
        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(
                        request, portletFName);
        if (portletWindow == null) {
            throw new IllegalArgumentException(
                    "Could not find PortletWindow for fname="
                            + portletFName
                            + " to create IPortalUrlBuilder");
        }
        return this.getPortalUrlBuilderByPortletWindow(request, portletWindow, urlType);
    }

    protected IPortalUrlBuilder getPortalUrlBuilderByPortletWindow(
            HttpServletRequest request, IPortletWindow portletWindow, UrlType urlType) {
        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();

        //See if the targeted portlet is actually a delegate
        final IPortletWindowId parentPortletWindowId = portletWindow.getDelegationParentId();
        if (parentPortletWindowId != null) {
            //Get the portal url builder that targets the parent
            final IPortalUrlBuilder portalUrlBuilder =
                    this.getPortalUrlBuilderByPortletWindow(
                            request, parentPortletWindowId, urlType);

            //See if there is additional delegation request data that needs to be added to the URL
            final DelegationRequest delegationRequest =
                    this.portletDelegationManager.getDelegationRequest(request, portletWindowId);
            if (delegationRequest != null) {
                final IPortletUrlBuilder parentPortletUrlBuilder =
                        portalUrlBuilder.getPortletUrlBuilder(parentPortletWindowId);

                final Map<String, List<String>> parentParameters =
                        delegationRequest.getParentParameters();
                if (parentParameters != null) {
                    parentPortletUrlBuilder.setParameters(parentParameters);
                }

                final PortletMode parentPortletMode = delegationRequest.getParentPortletMode();
                if (parentPortletMode != null) {
                    parentPortletUrlBuilder.setPortletMode(parentPortletMode);
                }

                final WindowState parentWindowState = delegationRequest.getParentWindowState();
                if (parentWindowState != null) {
                    parentPortletUrlBuilder.setWindowState(parentWindowState);
                }
            }

            return portalUrlBuilder;
        }

        //create the portlet url builder
        final String layoutNodeId = this.verifyPortletWindowId(request, portletWindowId);
        return new PortalUrlBuilder(
                this.urlSyntaxProvider, request, layoutNodeId, portletWindowId, urlType);
    }

    /**
     * Verify the requested portlet window corresponds to a node in the user's layout and return the
     * corresponding layout node id
     */
    protected String verifyPortletWindowId(
            HttpServletRequest request, IPortletWindowId portletWindowId) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();

        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        final IPortletWindowId delegationParentWindowId = portletWindow.getDelegationParentId();
        if (delegationParentWindowId != null) {
            return verifyPortletWindowId(request, delegationParentWindowId);
        }

        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final String channelSubscribeId = portletEntity.getLayoutNodeId();
        final IUserLayoutNodeDescription node = userLayoutManager.getNode(channelSubscribeId);
        if (node == null) {
            throw new IllegalArgumentException(
                    "No layout node exists for id "
                            + channelSubscribeId
                            + " of window "
                            + portletWindowId);
        }

        return node.getId();
    }

    /**
     * Verify the requested node exists in the user's layout. Also if the node exists see if it is a
     * portlet node and if it is return the {@link IPortletWindowId} of the corresponding portlet.
     */
    protected LayoutNodeType getLayoutNodeType(HttpServletRequest request, String folderNodeId) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final IUserLayoutNodeDescription node = userLayoutManager.getNode(folderNodeId);

        if (node == null) {
            return null;
        }

        return node.getType();
    }
}
