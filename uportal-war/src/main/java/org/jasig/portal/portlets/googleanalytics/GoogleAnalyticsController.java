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
package org.jasig.portal.portlets.googleanalytics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.portlets.PortletPreferencesJsonDao;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.fasterxml.jackson.databind.JsonNode;

@Controller
@RequestMapping("VIEW")
public class GoogleAnalyticsController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private PortletPreferencesJsonDao portletPreferencesJsonDao;

    @Value("${org.jasig.portal.security.PersonFactory.guest_user_name:guest}")
    private String guestUserName;

    @Autowired
    public void setPortletPreferencesJsonDao(PortletPreferencesJsonDao portletPreferencesJsonDao) {
        this.portletPreferencesJsonDao = portletPreferencesJsonDao;
    }
    
    @RenderMapping
    public String renderAnalyticsHeader(RenderRequest request, ModelMap model) throws IOException {

        // For which user account are we logging portal activity?
        String remoteUser = request.getRemoteUser();
        if (remoteUser == null) {
            // User is not authenticated;  log for the guest user
            remoteUser = guestUserName;
        }

        final IGroupMember groupMember = GroupService.getGroupMember(remoteUser, IPerson.class);
        final Map<String, Boolean> isMemberCache = new HashMap<String, Boolean>();
        
        final PortletPreferences preferences = request.getPreferences();
        final JsonNode config = this.portletPreferencesJsonDao.getJsonNode(preferences, GoogleAnalyticsConfigController.CONFIG_PREF_NAME);
        
        final JsonNode propertyConfig = config.get("defaultConfig");
        this.filterAnalyticsGroups(groupMember, propertyConfig, isMemberCache);
        
        final JsonNode hosts = config.get("hosts");
        if (hosts != null) {
            for (final Iterator<JsonNode> hostsItr = hosts.elements(); hostsItr.hasNext(); ) {
                final JsonNode institution = hostsItr.next();
                this.filterAnalyticsGroups(groupMember, institution, isMemberCache);
            }
        }
        
        model.put("data", config);
        
        if (propertyConfig == null || propertyConfig.get("propertyId") == null) {
            return "jsp/GoogleAnalytics/noop";
        }
        else {
            return "jsp/GoogleAnalytics/init";
        }
    }
    
    /**
     * Remove groups from the AnalyticsConfig that the current user is not a member of
     */
    protected void filterAnalyticsGroups(IGroupMember groupMember, JsonNode config, Map<String, Boolean> isMemberCache) {
        if (config == null) {
            return;
        }
        
        final JsonNode dimensionGroups = config.get("dimensionGroups");
        if (dimensionGroups == null) {
            return;
        }
        
        for (final Iterator<JsonNode> groupItr = dimensionGroups.elements(); groupItr.hasNext(); ) {
            final JsonNode group = groupItr.next();
            
            final JsonNode valueNode = group.get("value");
            if (valueNode == null) {
                continue;
            }
            
            final String groupName = valueNode.asText();
        
            Boolean isMember = isMemberCache.get(groupName);
            if (isMember == null) {
                isMember = isMember(groupMember, groupName);
                isMemberCache.put(groupName, isMember);
            }
            
            if (!isMember) {
                groupItr.remove();
            }
        }
    }
    
    /**
     * Check if the user is a member of the specified group name
     */
    protected boolean isMember(IGroupMember groupMember, String groupName) {
        try {
            IGroupMember group = GroupService.findGroup(groupName);
            if (group != null) {
                return groupMember.isDeepMemberOf(group);
            }
    
            
            final EntityIdentifier[] results = GroupService.searchForGroups(groupName, GroupService.IS, IPerson.class);
            if (results == null || results.length == 0) {
                this.logger.warn("No portal group found for '{}' no users will be placed in that group for analytics", groupName);
                return false;
            }
            
            if (results.length > 1) {
                this.logger.warn("{} groups were found for groupName '{}'. The first result will be used.", results.length, groupName);
            }
            
            group = GroupService.getGroupMember(results[0]);
            return groupMember.isDeepMemberOf(group);
        }
        catch (Exception e) {
            this.logger.warn("Failed to determine if {} is a member of {}, returning false", groupMember, groupName, e);
            return false;
        }
    }
}
