package org.jasig.portal.url.processing;

import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelManager;
import org.jasig.portal.Constants;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
import org.jasig.portal.layout.UserLayoutStoreFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IWritableHttpServletRequest;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Required;

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
    
    private IUserInstanceManager userInstanceManager;
    
    /**
     * @return the userInstanceManager
     */
    public IUserInstanceManager getUserInstanceManager() {
        return this.userInstanceManager;
    }
    /**
     * @param userInstanceManager the userInstanceManager to set
     */
    @Required
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        Validate.notNull(userInstanceManager);
        this.userInstanceManager = userInstanceManager;
    }

    public boolean processParameters(IWritableHttpServletRequest request, HttpServletResponse response) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final ChannelManager channelManager = userInstance.getChannelManager();
        
        this.parseMultiTargetEvent(request, response, "uP_help_target", PortalEvent.HELP_BUTTON, channelManager);
        this.parseMultiTargetEvent(request, response, "uP_about_target", PortalEvent.ABOUT_BUTTON, channelManager);
        this.parseMultiTargetEvent(request, response, "uP_edit_target", PortalEvent.EDIT_BUTTON, channelManager);

        this.parseSingleTargetEvent(request, response, "uP_detach_target", PortalEvent.DETACH_BUTTON, channelManager);


        final IPerson person = userInstance.getPerson();
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final UserPreferences userPreferences = preferencesManager.getUserPreferences();

        
        final StructureStylesheetUserPreferences structureStylesheetUserPreferences = userPreferences.getStructureStylesheetUserPreferences();
        final ThemeStylesheetUserPreferences themeStylesheetUserPreferences = userPreferences.getThemeStylesheetUserPreferences();
        
        String root = request.getParameter("uP_root");
        if (root != null) {
            // If a channel specifies "me" as its root, set the root
            // to the channel's subscribe Id
            if (root.equals("me")) {
                // get uPFile spec and search for "channel" clause
                //TODO get UPFileSpec for original request and store it as a request attribute
                final UPFileSpec upfs = new UPFileSpec(request);
                root = upfs.getTargetNodeId();
            }

            if (IUserLayout.ROOT_NODE_NAME.equals(root)) {
                final String oldChannelId = structureStylesheetUserPreferences.getParameterValue("userLayoutRoot");
                
                if (oldChannelId != null && !IUserLayout.ROOT_NODE_NAME.equals(oldChannelId)) {
                    //Tell the previously maximized channel it is going back to normal
                    if (logger.isDebugEnabled()) {
                        logger.debug("Sending window state event '" + PortalEvent.NORMAL + "' to '" + root + "'.");
                    }
                    channelManager.passPortalEvent(request, response, oldChannelId, PortalEvent.NORMAL);
                }
            }
            else {
                //Make sure that the focused channel isn't rendered minimized
                themeStylesheetUserPreferences.setChannelAttributeValue(root, "minimized", "false");
                
                //Tell the channel it is being maximized
                if (logger.isDebugEnabled()) {
                    logger.debug("Sending window state event '" + PortalEvent.MAXIMIZE + "' to '" + root + "'.");
                }
                channelManager.passPortalEvent(request, response, root, PortalEvent.MAXIMIZE);
            }
            
            structureStylesheetUserPreferences.putParameterValue("userLayoutRoot", root);
        }
        
        // fname and root are mutually exclusive and
        // should not be used in the same request,
        // as an fname is treated as the root target.
        final String fname = request.getParameter(Constants.FNAME_PARAM);
        if (fname != null) {
            // get a subscribe id for the fname
            String subId = null;
            try {
                subId = userLayoutManager.getSubscribeId(fname);
            }
            catch (PortalException pe) {
                logger.warn("Unable to get subscribe ID for fname='" + fname + "'", pe);
            }

            if (subId != null) {
                if (userLayoutManager instanceof TransientUserLayoutManagerWrapper) {
                    // get wrapper implementation for focusing
                    final TransientUserLayoutManagerWrapper transientUserLayoutManagerWrapper = (TransientUserLayoutManagerWrapper) userLayoutManager;
                    // .. and now set it as the focused id
                    transientUserLayoutManagerWrapper.setFocusedId(subId);
                }
    
                structureStylesheetUserPreferences.putParameterValue("userLayoutRoot", subId);
                if (logger.isDebugEnabled()) {
                    logger.debug("setting structure preference 'userLayoutRoot'='" + subId + "'");
                }
            }
        }
        
        // other params
        final String[] structParamNames = request.getParameterValues("uP_sparam");
        if (structParamNames != null) {
            for (final String structParamName : structParamNames) {
                final String structParamValue = request.getParameter(structParamName);
                
                structureStylesheetUserPreferences.putParameterValue(structParamName, structParamValue);
                if (logger.isDebugEnabled()) {
                    logger.debug("set structure parameter '" + structParamName + "'='" + structParamValue + "'.");
                }
            }
        }

        final String[] themeParamNames = request.getParameterValues("uP_tparam");
        if (themeParamNames != null) {
            for (final String themeParamName : themeParamNames) {
                final String themeParamValue = request.getParameter(themeParamName);
                
                themeStylesheetUserPreferences.putParameterValue(themeParamName, themeParamValue);
                if (logger.isDebugEnabled()) {
                    logger.debug("set theme parameter '" + themeParamName + "'='" + themeParamValue + "'.");
                }
            }
        }
        
        // attribute processing
        // structure transformation
        final String[] structFolderAttrNames = request.getParameterValues("uP_sfattr");
        if (structFolderAttrNames != null) {
            for (final String structFolderAttrName : structFolderAttrNames) {
                final String[] folderIds = request.getParameterValues(structFolderAttrName + "_folderId");
                
                if (folderIds != null) {
                    for (final String folderId : folderIds) {
                        final String structFolderAttrValue = request.getParameter(structFolderAttrName + "_" + folderId + "_value");
                        structureStylesheetUserPreferences.setFolderAttributeValue(folderId, structFolderAttrName, structFolderAttrValue);
                        
                        if (logger.isDebugEnabled()) {
                            logger.debug("set structure folder attribute '" + structFolderAttrName + "'='" + structFolderAttrValue + "' on folder '" + folderId + "'.");
                        }
                    }
                }
            }
        }
            
        final String[] structChannelAttrNames = request.getParameterValues("uP_scattr");
        if (structChannelAttrNames != null) {
            for (final String structChannelAttrName : structChannelAttrNames) {
                final String[] channelIds = request.getParameterValues(structChannelAttrName + "_channelId");
                
                if (channelIds != null) {
                    for (final String channelId : channelIds) {
                        final String structChannelAttrValue = request.getParameter(structChannelAttrName + "_" + channelId + "_value");
                        structureStylesheetUserPreferences.setChannelAttributeValue(channelId, structChannelAttrName, structChannelAttrValue);
                        
                        if (logger.isDebugEnabled()) {
                            logger.debug("set structure channel attribute '" + structChannelAttrName + "'='" + structChannelAttrValue + "' on folder '" + channelId + "'.");
                        }
                    }
                }
            }
        }
        
        // theme stylesheet attributes
        final String[] themeChannelAttrNames = request.getParameterValues("uP_tcattr");
        if (themeChannelAttrNames != null) {
            for (final String themeChannelAttrName : themeChannelAttrNames) {
                final String[] channelIds = request.getParameterValues(themeChannelAttrName + "_channelId");
                
                if (channelIds != null) {
                    for (final String channelId : channelIds) {
                        final String themeChannelAttrValue = request.getParameter(themeChannelAttrName + "_" + channelId + "_value");
                        themeStylesheetUserPreferences.setChannelAttributeValue(channelId, themeChannelAttrName, themeChannelAttrValue);
                        
                        if (logger.isDebugEnabled()) {
                            logger.debug("set theme channel attribute '" + themeChannelAttrName + "'='" + themeChannelAttrValue + "' on folder '" + channelId + "'.");
                        }
                        
                        if ("minimized".equals(themeChannelAttrName)) {
                            final PortalEvent event;
                            if ("true".equals(themeChannelAttrValue)) {
                                event = PortalEvent.MINIMIZE;
                            }
                            else {
                                event = PortalEvent.NORMAL;
                            }
        
                            channelManager.passPortalEvent(request, response, channelId, event);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Sent window state event '" + event + "' to '" + channelId + "'.");
                            }
                        }
                    }
                }
            }
        }
        
        //Processing that only applies to authenticated users
        if (!person.isGuest()) {
            // Request to change the locale
            final String localesString = request.getParameter(Constants.LOCALES_PARAM);
            if (localesString != null) {
                final UserProfile profile = userPreferences.getProfile();
                final LocaleManager localeManager = profile.getLocaleManager();

                final Locale[] locales = LocaleManager.parseLocales(localesString);
                localeManager.setSessionLocales(locales);
            }
            
            // save processing handled at end to provide persisting of changes
            // if desired.
            final String saveWhat = request.getParameter("uP_save");
            if (saveWhat != null) {
//TODO Make UserLayoutStoreFactory a shell and spring inject this
                final IUserLayoutStore userLayoutStore = UserLayoutStoreFactory.getUserLayoutStoreImpl();
                
                try {
                    if (saveWhat.equals("preferences")) {
                        userLayoutStore.putUserPreferences(person, userPreferences);
                    }
                    else if (saveWhat.equals("layout")) {
                        userLayoutManager.saveUserLayout();
                    }
                    else if (saveWhat.equals("all")) {
                        userLayoutStore.putUserPreferences(person, userPreferences);
                        userLayoutManager.saveUserLayout();
                    }
                    
                    if (logger.isDebugEnabled()) {
                        logger.debug("persisted " + saveWhat + " changes.");
                    }

                }
                catch (Exception e) {
                    logger.error("unable to persist " + saveWhat + " changes. ", e);
                }
            }
        }
        
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
