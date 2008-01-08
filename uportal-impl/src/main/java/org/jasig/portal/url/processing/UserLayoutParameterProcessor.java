package org.jasig.portal.url.processing;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelManager;
import org.jasig.portal.IUserInstance;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.UserInstanceManager;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IWritableHttpServletRequest;

/**
 * This helper class processes HttpServletRequests for parameters relating to
 * user layout actions, propagating appropriate events to the user's layout
 * manager, preferences, and channel manager.
 * 
 * This class results from factoring the processUserLayoutParameters method out
 * of UserInstance in an effort to make UserInstance smaller and more literate.
 */
public class UserLayoutParameterProcessor implements IRequestParameterProcessor {
    protected final Log logger = LogFactory.getLog(getClass());
    
    public boolean processParameters(IWritableHttpServletRequest request, HttpServletResponse response) {
        final IUserInstance userInstance = UserInstanceManager.getUserInstance(request);
        final ChannelManager channelManager = userInstance.getChannelManager();

        this.parseMultiTargetEvent(request, response, "uP_help_target", PortalEvent.HELP_BUTTON, channelManager);
        this.parseMultiTargetEvent(request, response, "uP_about_target", PortalEvent.ABOUT_BUTTON, channelManager);
        this.parseMultiTargetEvent(request, response, "uP_edit_target", PortalEvent.EDIT_BUTTON, channelManager);

        this.parseSingleTargetEvent(request, response, "uP_detach_target", PortalEvent.DETACH_BUTTON, channelManager);

        this.parseMinMaxEvents(request, response, channelManager);
        
        final IPerson person = userInstance.getPerson();
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final UserPreferences userPreferences = preferencesManager.getUserPreferences();
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

    /**
     * Walks down the theme channel attribute parameter tree to notify channels of min/max events
     */
    protected void parseMinMaxEvents(IWritableHttpServletRequest request, HttpServletResponse response, final ChannelManager channelManager) {
        final String[] themeChannelAttributes = request.getParameterValues("uP_tcattr");
        if (themeChannelAttributes == null) {
            return;
        }

        for (final String themeChannelAttributeName : themeChannelAttributes) {
            if ("minimized".equals(themeChannelAttributeName)) {
                final String[] channelIds = request.getParameterValues(themeChannelAttributeName + "_channelId");
                if (channelIds != null) { 
                    for (final String channelId : channelIds) {
                        final String attributeValue = request.getParameter(themeChannelAttributeName + "_" + channelId + "_value");
    
                        final PortalEvent event;
                        if ("true".equals(attributeValue)) {
                            event = PortalEvent.MINIMIZE;
                        }
                        else {
                            event = PortalEvent.MAXIMIZE;
                        }
    
                        channelManager.passPortalEvent(request, response, channelId, event);
    
                        if (logger.isDebugEnabled()) {
                            logger.debug("Sent window state event to '" + themeChannelAttributeName + "' of '" + channelId + "' to '" + attributeValue + "'.");
                        }
                    }
                }
            }
        }
    }
}
