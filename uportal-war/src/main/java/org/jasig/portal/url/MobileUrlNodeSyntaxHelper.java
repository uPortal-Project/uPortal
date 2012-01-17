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

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.concurrency.caching.RequestCache;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Maps tabs and portlets to folder names and back. Handles a single set of tabs and uses tab IDs for folder names.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class MobileUrlNodeSyntaxHelper implements IUrlNodeSyntaxHelper {
    public static final char PORTLET_PATH_ELEMENT_SEPERATOR = '.';
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private IUserInstanceManager userInstanceManager;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }
    
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlNodeSyntaxHelper#getDefaultLayoutNodeId(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public String getDefaultLayoutNodeId(HttpServletRequest httpServletRequest) {
        //Mobile view never targets a node for the default url
        return null;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlNodeSyntaxHelper#getFolderNamesForLayoutNode(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    @Override
    public List<String> getFolderNamesForLayoutNode(HttpServletRequest request, String layoutNodeId) {
        //layout node folders are never part of the mobile url
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlNodeSyntaxHelper#getLayoutNodeForFolderNames(javax.servlet.http.HttpServletRequest, java.util.List)
     */
    @Override
    public String getLayoutNodeForFolderNames(HttpServletRequest request, List<String> folderNames) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlNodeSyntaxHelper#getFolderNameForPortlet(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    @RequestCache
    @Override
    public String getFolderNameForPortlet(HttpServletRequest request, IPortletWindowId portletWindowId) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        
        final String fname = portletDefinition.getFName();
        final String channelSubscribeId = portletEntity.getLayoutNodeId();
        
        //Build the targeted portlet string (fname + subscribeId)
        return fname + PORTLET_PATH_ELEMENT_SEPERATOR + channelSubscribeId;
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.url.IUrlNodeSyntaxHelper#getPortletForFolderName(javax.servlet.http.HttpServletRequest, java.lang.String, java.lang.String)
	 */
    @RequestCache
	@Override
	public IPortletWindowId getPortletForFolderName(HttpServletRequest request, String targetedLayoutNodeId, String folderName) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        
        final String fname;
        final IPortletEntity portletEntity;
        
        final int seperatorIndex = folderName.indexOf(PORTLET_PATH_ELEMENT_SEPERATOR);
        if (seperatorIndex <= 0 || seperatorIndex + 1 == folderName.length()) {
            fname = folderName;
            portletEntity = this.portletEntityRegistry.getOrCreatePortletEntityByFname(request, userInstance, fname);
        }
        else {
            fname = folderName.substring(0, seperatorIndex);
            final String subscribeId = folderName.substring(seperatorIndex + 1);
            
            portletEntity = this.portletEntityRegistry.getOrCreatePortletEntityByFname(request, userInstance, fname, subscribeId);
        }
        
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindow(request, portletEntityId);
        return portletWindow.getPortletWindowId();
    }

}
