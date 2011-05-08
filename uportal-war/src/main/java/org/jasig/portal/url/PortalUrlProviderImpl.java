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

package org.jasig.portal.url;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Generates {@link IPortalUrlBuilder} objects based on various layout and portlet targets.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class PortalUrlProviderImpl implements IPortalUrlProvider {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private IUrlSyntaxProvider urlSyntaxProvider;
    private IUserInstanceManager userInstanceManager;
    private IUrlNodeSyntaxHelperRegistry urlNodeSyntaxHelperRegistry;
    private IPortletWindowRegistry portletWindowRegistry;
    
    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setUrlNodeSyntaxHelperRegistry(IUrlNodeSyntaxHelperRegistry urlNodeSyntaxHelperRegistry) {
        this.urlNodeSyntaxHelperRegistry = urlNodeSyntaxHelperRegistry;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getDefaultUrl(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public IPortalUrlBuilder getDefaultUrl(HttpServletRequest request) {
        final IUrlNodeSyntaxHelper urlNodeSyntaxHelper = this.urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request);
        final String defaultLayoutNodeId = urlNodeSyntaxHelper.getDefaultLayoutNodeId(request);
        return this.getPortalUrlBuilderByLayoutNode(request, defaultLayoutNodeId, UrlType.RENDER);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortalUrlBuilderByLayoutNode(javax.servlet.http.HttpServletRequest, java.lang.String, org.jasig.portal.url.UrlType)
     */
    @Override
    public IPortalUrlBuilder getPortalUrlBuilderByLayoutNode(HttpServletRequest request, String layoutNodeId, UrlType urlType) {
        final IPortletWindowId portletWindowId;
        if (layoutNodeId != null) {
            final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowBySubscribeId(request, layoutNodeId);
            if (portletWindow == null) {
                //No window so make sure the node is even in the layout
                this.verifyLayoutNodeId(request, layoutNodeId);
                portletWindowId = null;
            }
            else {
                portletWindowId = portletWindow.getPortletWindowId();
            }
        }
        else {
            portletWindowId = null;
        }
        
        return new PortalUrlBuilder(this.urlSyntaxProvider, request, layoutNodeId, portletWindowId, urlType);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortalUrlBuilderByPortletWindow(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId, org.jasig.portal.url.UrlType)
     */
    @Override
    public IPortalUrlBuilder getPortalUrlBuilderByPortletWindow(HttpServletRequest request, IPortletWindowId portletWindowId, UrlType urlType) {
        final String layoutNodeId = this.verifyPortletWindowId(request, portletWindowId);
        return new PortalUrlBuilder(this.urlSyntaxProvider, request, layoutNodeId, portletWindowId, urlType);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortalUrlBuilderByPortletFName(javax.servlet.http.HttpServletRequest, java.lang.String, org.jasig.portal.url.UrlType)
     */
    @Override
    public IPortalUrlBuilder getPortalUrlBuilderByPortletFName(HttpServletRequest request, String portletFName, UrlType urlType) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(request, portletFName);

        return this.getPortalUrlBuilderByPortletWindow(request, portletWindow.getPortletWindowId(), urlType);
    }
    
    /**
     * Verify the requested portlet window corresponds to a node in the user's layout and return the
     * corresponding layout node id
     */
    protected String verifyPortletWindowId(HttpServletRequest request, IPortletWindowId portletWindowId) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final String channelSubscribeId = portletEntity.getLayoutNodeId();
        final IUserLayoutNodeDescription node = userLayoutManager.getNode(channelSubscribeId);
        if (node == null) {
            throw new IllegalArgumentException("No layout node exists for id " + channelSubscribeId + " of window " + portletWindowId);
        }
        
        return node.getId();
    }
    
    /**
     * Verify the requested node exists in the user's layout. Also if the node exists see if it
     * is a portlet node and if it is return the {@link IPortletWindowId} of the corresponding
     * portlet.
     */
    protected void verifyLayoutNodeId(HttpServletRequest request, String folderNodeId) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final IUserLayoutNodeDescription node = userLayoutManager.getNode(folderNodeId);
        
        if (node == null) {
            throw new IllegalArgumentException("No layout node exists for id: " + folderNodeId);
        }
    }
}
