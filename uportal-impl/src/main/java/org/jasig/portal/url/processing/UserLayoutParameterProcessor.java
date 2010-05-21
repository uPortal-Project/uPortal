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

package org.jasig.portal.url.processing;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelManager;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IWritableHttpServletRequest;
import org.jasig.portal.url.UrlState;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This helper class processes HttpServletRequests for parameters relating to
 * user layout actions, propagating appropriate events to the user's layout
 * manager, preferences, and channel manager.
 * 
 * This class results from factoring the processUserLayoutParameters method out
 * of UserInstance in an effort to make UserInstance smaller and more literate.
 */
@Service("userLayoutRequestParameterProcessor")
public class UserLayoutParameterProcessor implements IRequestParameterProcessor {
    protected final Log logger = LogFactory.getLog(getClass());
    
    private IUserInstanceManager userInstanceManager;
    private IPortalUrlProvider portalUrlProvider;
    
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }
    
    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider portalUrlProvider) {
        this.portalUrlProvider = portalUrlProvider;
    }

    public boolean processParameters(IWritableHttpServletRequest request, HttpServletResponse response) {
        final IPortalRequestInfo portalRequestInfo = this.portalUrlProvider.getPortalRequestInfo(request);
        
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final ChannelManager channelManager = userInstance.getChannelManager();
        
        final IPerson person = userInstance.getPerson();
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final UserPreferences userPreferences = preferencesManager.getUserPreferences();

        
        final StructureStylesheetUserPreferences structureStylesheetUserPreferences = userPreferences.getStructureStylesheetUserPreferences();
        final ThemeStylesheetUserPreferences themeStylesheetUserPreferences = userPreferences.getThemeStylesheetUserPreferences();
        
        
        portalRequestInfo.getTargetedLayoutNodeId();
        final UrlState urlState = portalRequestInfo.getUrlState();
        switch (urlState) {
            case MAX:
                final String targetedChannelSubscribeId = portalRequestInfo.getTargetedChannelSubscribeId();
                //TODO can this ever be null if urlState is MAX?
                structureStylesheetUserPreferences.putParameterValue("userLayoutRoot", targetedChannelSubscribeId);
            break;
                
            case NORMAL:
            default:
                final String tabId = portalRequestInfo.getTargetedLayoutNodeId();
                structureStylesheetUserPreferences.putParameterValue("activeTabNodeId", tabId);
                structureStylesheetUserPreferences.putParameterValue("userLayoutRoot", IUserLayout.ROOT_NODE_NAME);
            break;
        }
        
        //TODO after portlet processing is complete set minimized theme flags by subscribeId

        userLayoutManager.processLayoutParameters(person, userPreferences, request);

        return true;
    }

    /**
     * Passes the specified event to all channel IDs specified by the parameter.
     */
    protected void parseMultiTargetEvent(IWritableHttpServletRequest request, HttpServletResponse response, String parameterName, PortalEvent event, ChannelManager channelManager) {
        final String[] channelIds = request.getParameterValues(parameterName);
        if (channelIds != null) {
            for (final String channelId : channelIds) {
                channelManager.passPortalEvent(request, response, channelId, event);
            }
        }
    }
    
    /**
     * Passes the specified event to the first channel ID specified by the parameter.
     */
    protected void parseSingleTargetEvent(IWritableHttpServletRequest request, HttpServletResponse response, String parameterName, PortalEvent event, ChannelManager channelManager) {
        final String channelId = request.getParameter(parameterName);
        if (channelId != null) {
            channelManager.passPortalEvent(request, response, channelId, event);
        }
    }
}
