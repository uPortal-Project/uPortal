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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.IStylesheetUserPreferencesService;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetParameterDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.xml.xpath.XPathExpressionCallback;
import org.jasig.portal.xml.xpath.XPathOperations;
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
public class SingleTabUrlNodeSyntaxHelper implements IUrlNodeSyntaxHelper {
    public static final char PORTLET_PATH_ELEMENT_SEPERATOR = '.';
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private String defaultLayoutNodeIdExpression = "/layout/folder/folder[@type='regular' and @hidden!='true'][$defaultTab]/@ID";
    private String tabIdExpression = "/layout/folder/folder[@ID=$nodeId or descendant::node()[@ID=$nodeId]]/@ID";
    private String defaultTabParameter = "defaultTab";
    
    private IUserInstanceManager userInstanceManager;
    private XPathOperations xpathOperations;
    private IStylesheetUserPreferencesService stylesheetUserPreferencesService;
    private IStylesheetDescriptorDao stylesheetDescriptorDao;
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
    public void setStylesheetDescriptorDao(IStylesheetDescriptorDao stylesheetDescriptorDao) {
        this.stylesheetDescriptorDao = stylesheetDescriptorDao;
    }

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setXpathOperations(XPathOperations xpathOperations) {
        this.xpathOperations = xpathOperations;
    }
    
    @Autowired
    public void setStylesheetUserPreferencesService(IStylesheetUserPreferencesService stylesheetUserPreferencesService) {
        this.stylesheetUserPreferencesService = stylesheetUserPreferencesService;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlNodeSyntaxHelper#getDefaultLayoutNodeId(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public String getDefaultLayoutNodeId(HttpServletRequest httpServletRequest) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(httpServletRequest);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();

        final IUserLayout userLayout = userLayoutManager.getUserLayout();
        
        //This logic is specific to tab/column layouts
        final String defaultTabIndex = this.getDefaultTabIndex(httpServletRequest);
        
        final String defaultTabId = this.getTabId(userLayout, defaultTabIndex);
        if (StringUtils.isNotEmpty(defaultTabId)) {
            return defaultTabId;
        }
    
        if (this.logger.isWarnEnabled()) {
            this.logger.warn("Failed to find default tab id for " + userInstance.getPerson().getUserName() + " with default tab index " + defaultTabIndex + ". Index 1 will be tried as a fall-back.");
        }
        return getTabId(userLayout, "1");
    }

    protected String getTabId(final IUserLayout userLayout, final String tabIndex) {
        return this.xpathOperations.doWithExpression(
            defaultLayoutNodeIdExpression, 
            Collections.singletonMap("defaultTab", tabIndex), 
            new XPathExpressionCallback<String>() {
                @Override
                public String doWithExpression(XPathExpression xPathExpression) throws XPathExpressionException {
                    return userLayout.findNodeId(xPathExpression);
                }
            });
    }

    /**
     * Get the index of the default tab for the user
     */
    protected String getDefaultTabIndex(HttpServletRequest httpServletRequest) {
        final IStylesheetUserPreferences structureStylesheetUserPreferences = this.stylesheetUserPreferencesService.getStructureStylesheetUserPreferences(httpServletRequest);
        final String defaultTab = structureStylesheetUserPreferences.getStylesheetParameter(defaultTabParameter);
        if (defaultTab != null) {
            return defaultTab;
        }
        
        final long stylesheetDescriptorId = structureStylesheetUserPreferences.getStylesheetDescriptorId();
        final IStylesheetDescriptor stylesheetDescriptor = this.stylesheetDescriptorDao.getStylesheetDescriptor(stylesheetDescriptorId);
        final IStylesheetParameterDescriptor defaultTabParameterDescriptor = stylesheetDescriptor.getStylesheetParameterDescriptor(defaultTabParameter);

        // TODO: temporary fix to support our mobile theme
        if (defaultTabParameterDescriptor != null) {
            return defaultTabParameterDescriptor.getDefaultValue();
        } else {
            return "1";
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlNodeSyntaxHelper#getFolderNamesForLayoutNode(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    @Override
    public List<String> getFolderNamesForLayoutNode(HttpServletRequest request, String layoutNodeId) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final IUserLayout userLayout = userLayoutManager.getUserLayout();
        
        final String tabId = this.xpathOperations.doWithExpression(
                tabIdExpression, 
                Collections.singletonMap("nodeId", layoutNodeId), 
                new XPathExpressionCallback<String>() {
                    @Override
                    public String doWithExpression(XPathExpression xPathExpression) throws XPathExpressionException {
                        return userLayout.findNodeId(xPathExpression);
                    }
                });
        
        if (StringUtils.isEmpty(tabId)) {
            return Collections.emptyList();
        }
        
        return Arrays.asList(tabId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlNodeSyntaxHelper#getLayoutNodeForFolderNames(javax.servlet.http.HttpServletRequest, java.util.List)
     */
    @Override
    public String getLayoutNodeForFolderNames(HttpServletRequest request, List<String> folderNames) {
        if (folderNames == null || folderNames.isEmpty()) {
            return null;
        }
        
        final String layoutNodeId = folderNames.get(0);
        
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        
        final IUserLayoutNodeDescription node;
        try {
            node = userLayoutManager.getNode(layoutNodeId);
        }
        catch (PortalException e) {
            //No layout node exists for the id, return null
            return null;
        }
        
        if (node == null) {
            return null;
        }
        
        return node.getId();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlNodeSyntaxHelper#getFolderNameForPortlet(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    @Override
    public String getFolderNameForPortlet(HttpServletRequest request, IPortletWindowId portletWindowId) {
        final IPortletEntity portletEntity = this.portletWindowRegistry.getParentPortletEntity(request, portletWindowId);
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        final IPortletDefinition portletDefinition = this.portletEntityRegistry.getParentPortletDefinition(portletEntityId);
        
        final String fname = portletDefinition.getFName();
        final String channelSubscribeId = portletEntity.getChannelSubscribeId();
        
        //Build the targeted portlet string (fname + subscribeId)
        return fname + PORTLET_PATH_ELEMENT_SEPERATOR + channelSubscribeId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlNodeSyntaxHelper#getPortletForFolderName(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    @Override
    public IPortletWindowId getPortletForFolderName(HttpServletRequest request, String folderName) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        
        final String fname;
        final IPortletEntity portletEntity;
        
        final int seperatorIndex = folderName.indexOf(PORTLET_PATH_ELEMENT_SEPERATOR);
        if (seperatorIndex <= 0 || seperatorIndex + 1 == folderName.length()) {
            fname = folderName;
            portletEntity = this.portletEntityRegistry.getOrCreatePortletEntityByFname(userInstance, fname);
        }
        else {
            fname = folderName.substring(0, seperatorIndex);
            final String subscribeId = folderName.substring(seperatorIndex + 1);
            
            portletEntity = this.portletEntityRegistry.getOrCreatePortletEntityByFname(userInstance, fname, subscribeId);
        }
        
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindow(request, portletEntityId);
        return portletWindow.getPortletWindowId();
    }

}
