package org.jasig.portal.portlets.googleanalytics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.codehaus.jackson.JsonNode;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.portlets.PortletPreferencesJsonDao;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

@Controller
@RequestMapping("VIEW")
public class GoogleAnalyticsController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private PortletPreferencesJsonDao portletPreferencesJsonDao;

    @Autowired
    public void setPortletPreferencesJsonDao(PortletPreferencesJsonDao portletPreferencesJsonDao) {
        this.portletPreferencesJsonDao = portletPreferencesJsonDao;
    }
    
    @RenderMapping
    public String renderAnalyticsHeader(PortletRequest portletRequest, ModelMap model) throws IOException {
        final String remoteUser = portletRequest.getRemoteUser();
        final IGroupMember groupMember = GroupService.getGroupMember(remoteUser, IPerson.class);
        final Map<String, Boolean> isMemberCache = new HashMap<String, Boolean>();
        
        final PortletPreferences preferences = portletRequest.getPreferences();
        final JsonNode config = this.portletPreferencesJsonDao.getJsonNode(preferences, GoogleAnalyticsConfigController.CONFIG_PREF_NAME);
        
        final JsonNode propertyConfig = config.get("defaultConfig");
        this.filterAnalyticsGroups(groupMember, propertyConfig, isMemberCache);
        
        final JsonNode institutions = config.get("institutions");
        if (institutions != null) {
            for (final Iterator<JsonNode> institutionsItr = institutions.getElements(); institutionsItr.hasNext(); ) {
                final JsonNode institution = institutionsItr.next();
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
        
        for (final Iterator<JsonNode> groupItr = dimensionGroups.getElements(); groupItr.hasNext(); ) {
            final JsonNode group = groupItr.next();
            
            final JsonNode valueNode = group.get("value");
            if (valueNode == null) {
                continue;
            }
            
            final String groupName = valueNode.getTextValue();
        
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
}
