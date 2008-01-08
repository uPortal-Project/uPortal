/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.jndi.JNDIManager;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
import org.jasig.portal.layout.UserLayoutManagerFactory;
import org.jasig.portal.layout.UserLayoutStoreFactory;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.PropsMatcher;


/**
 * UserPreferencesManager is responsible for keeping: user id, user layout, user preferences
 * and stylesheet descriptions.
 * For method descriptions please see {@link IUserPreferencesManager}.
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
 * @version $Revision$
 */
public class UserPreferencesManager implements IUserPreferencesManager {
    private static final String BROWSER_MAPPINGS_PROPERTIES = "/properties/browser.mappings";

    private static final String USER_PREFERENCES_KEY = UserPreferencesManager.class.getName();
    
    /**
     * Default value for saveUserPreferencesAtLogout.
     * This value will be used when the corresponding property cannot be loaded.
     */
    private static final boolean DEFAULT_SAVE_USER_PREFERENCES_AT_LOGOUT = false;

    private static final boolean saveUserPreferencesAtLogout = PropertiesManager.getPropertyAsBoolean(UserPreferencesManager.class.getName() + ".save_UserPreferences_at_logout", DEFAULT_SAVE_USER_PREFERENCES_AT_LOGOUT);

    // user agent mapper for guessing the profile
    private static PropsMatcher userAgentMatcher;

    protected final static Log logger = LogFactory.getLog(UserPreferencesManager.class);
    
    
    private IUserLayoutManager userLayoutManager;

    private UserPreferences completeUserPreferences;
    // caching of stylesheet descriptions is recommended
    // if they'll take up too much space, we can take them
    // out, but cache stylesheet URIs, mime type and serializer name.
    // Those are used in every rendering cycle.
    private ThemeStylesheetDescription themeStylesheetDescription;
    private StructureStylesheetDescription structureStylesheetDescription;
    private boolean unmappedUserAgent = false;
    private final IPerson person;
    IUserLayoutStore userLayoutStore = null;
    
    
    /**
     * @return lazily initialized access to the UserAgentMatcher
     * TODO this should be re-written & tested in a static initializer or injected
     */
    static synchronized PropsMatcher getUserAgentMatcher() {
        if (userAgentMatcher == null) {
            InputStream userAgentMatcherStream = null;
            try {
                userAgentMatcherStream = UserPreferencesManager.class.getResourceAsStream(BROWSER_MAPPINGS_PROPERTIES);
                userAgentMatcher = new PropsMatcher(userAgentMatcherStream);
            }
            catch (IOException ioe) {
                logger.error("Failed to load browser mapping file: '" + BROWSER_MAPPINGS_PROPERTIES + "'", ioe);
            }
            finally {
                IOUtils.closeQuietly(userAgentMatcherStream);
            }
        }

        return userAgentMatcher;
    }



    /**
     * Constructor does the following
     *  1. Read layout.properties
     *  2. read userLayout from the database
     *  @param req the servlet request object
     *  @param person the person object
     */
    public UserPreferencesManager (HttpServletRequest req, IPerson person) throws PortalException {
        this(req, person, null);
    }

    /**
     * Constructor does the following
     *  1. Read layout.properties
     *  2. read userLayout from the database
     *  @param req the servlet request object
     *  @param person the person object
     *  @param localeManager the locale manager
     */
    public UserPreferencesManager (HttpServletRequest req, IPerson person, LocaleManager localeManager) throws PortalException {
        this.userLayoutManager=null;
        try {
            this.person = person;
            
            
            // load user preferences
            // Should obtain implementation in a different way!!
            this.userLayoutStore = UserLayoutStoreFactory.getUserLayoutStoreImpl();
            
            
            // determine user profile
            String userAgent = req.getHeader("User-Agent");
            if(StringUtils.isEmpty(userAgent)) {
                userAgent=MediaManager.NULL_USER_AGENT;
            }
            
            
            UserProfile userProfile = this.userLayoutStore.getUserProfile(this.person, userAgent);
            if (userProfile == null) {
                userProfile = this.userLayoutStore.getSystemProfile(userAgent);
            }
            
            // try guessing the profile through pattern matching
            if(userProfile==null) {
                final PropsMatcher userAgentMatcher = getUserAgentMatcher();
                if (userAgentMatcher != null) {
                    // try matching
                    final String profileId = userAgentMatcher.match(userAgent);
                    
                    // user agent has been matched
                    if (profileId != null) {
                        userProfile = userLayoutStore.getSystemProfileById(Integer.parseInt(profileId));
                    }
                }

            }

            if (userProfile != null) {
                if (localeManager != null && LocaleManager.isLocaleAware()) {
                    userProfile.setLocaleManager(localeManager);
                }
                userLayoutManager=UserLayoutManagerFactory.getUserLayoutManager(this.person,userProfile);

                final HttpSession session = req.getSession(true);
                try {
                    if (session != null) {
                        completeUserPreferences = (UserPreferences)session.getAttribute(USER_PREFERENCES_KEY);
                    }

                    if (completeUserPreferences == null) {
                        completeUserPreferences=userLayoutStore.getUserPreferences(this.person, userProfile);
                    }
                    else {
                        logger.debug("Found UserPreferences in session, using it instead of creating new UserPreferences");
                    }
                }
                catch (Exception e) {
                    logger.error( "UserPreferencesManager(): caught an exception trying to retreive user preferences for user=\"" + this.person.getID() + "\", profile=\"" + userProfile.getProfileName() + "\".", e);
                    completeUserPreferences=new UserPreferences(userProfile);
                }

                if (completeUserPreferences != null) {
                    session.setAttribute(USER_PREFERENCES_KEY, completeUserPreferences);
                }

                try {
                    // Initialize the JNDI context for this user
                    JNDIManager.initializeSessionContext(session,Integer.toString(this.person.getID()),Integer.toString(userProfile.getLayoutId()),userLayoutManager.getUserLayoutDOM());
                }
                catch(PortalException ipe) {
                  logger.error( "UserPreferencesManager(): Could not properly initialize user context", ipe);
                }
            }
            else {
                // there is no user-defined mapping for this particular browser.
                // user should be redirected to a browser-registration page.
                unmappedUserAgent = true;
                if (logger.isDebugEnabled())
                    logger.debug("UserPreferencesManager::UserPreferencesManager() : unable to find a profile for user \"" + this.person.getID()+"\" and userAgent=\""+ userAgent + "\".");
            }
        }
        catch (PortalException pe) {
            throw pe;
        }
        catch (Exception e) {
            final String msg = "Exception constructing UserPreferencesManager on request " + req + " for user " + this.person;
            logger.error(msg, e);
            throw new PortalException(msg, e);
        }
    }

    /**
     * A simpler constructor, that only initialises the person object.
     * Needed for ancestors.
     * @param person an <code>IPerson</code> object.
     */
    UserPreferencesManager(IPerson person) {
        this.person = person;
    }

    /* This function processes request parameters related to
     * setting Structure/Theme stylesheet parameters and attributes.
     * (uP_sparam, uP_tparam, uP_sfattr, uP_scattr
     * uP_tcattr)
     * It also processes layout root requests (uP_root) and
     * (up_fname)
     * @param req current <code>HttpServletRequest</code>
     */
    public void processUserPreferencesParameters(HttpServletRequest req) {
        // layout root setting
        String root;
        if ((root = req.getParameter("uP_root")) != null) {
            // If a channel specifies "me" as its root, set the root
            // to the channel's subscribe Id
            if (root.equals("me")) {
                // get uPFile spec and search for "channel" clause
                UPFileSpec upfs=new UPFileSpec(req);
                root=upfs.getTargetNodeId();
            }
            if(root!=null) {
                completeUserPreferences.getStructureStylesheetUserPreferences().putParameterValue("userLayoutRoot", root);
                
                //If going to focused make sure we aren't minimzed
                if (!root.equals(IUserLayout.ROOT_NODE_NAME)) {
                    completeUserPreferences.getThemeStylesheetUserPreferences().setChannelAttributeValue(root, "minimized", "false");
                }
            } else {
                logger.error( "UserPreferencesManager::processUserPreferencesParameters() : unable to extract channel ID. servletPath=\""+req.getServletPath()+"\".");
            }
        }

        // fname and root are mutually exclusive and
        // should not be used in the same request,
        // as an fname is treated as the root target.
        String fname = req.getParameter( Constants.FNAME_PARAM );
        if (fname != null) {
            // get a subscribe id for the fname
            String subId = null;
            try {
             subId = userLayoutManager.getSubscribeId(fname);
            } catch ( PortalException pe ) {
               logger.error( "UserPreferencesManager::processUserPreferencesParameters(): Unable to get subscribe ID for fname="+fname, pe);
              }
            if ( userLayoutManager instanceof TransientUserLayoutManagerWrapper ){
                // get wrapper implementation for focusing
                TransientUserLayoutManagerWrapper iulm =
                    (TransientUserLayoutManagerWrapper) userLayoutManager;
                // .. and now set it as the focused id
                iulm.setFocusedId(subId);
            }

            completeUserPreferences.getStructureStylesheetUserPreferences().putParameterValue("userLayoutRoot",
                                                                                  subId);
            if (logger.isDebugEnabled())
                logger.debug(
                           "UserPreferencesManager::processUserPreferencesParameters() : " +
                           "setting sfname \" userLayoutRoot" + "\"=\"" + subId + "\".");
        }

        // Request to change the locale
        String localesString = req.getParameter(Constants.LOCALES_PARAM);
        if (localesString != null) {
            LocaleManager localeManager = completeUserPreferences.getProfile().getLocaleManager();
            localeManager.setSessionLocales(LocaleManager.parseLocales(localesString));
        }

        // other params
        String[] sparams = req.getParameterValues("uP_sparam");
        if (sparams != null) {
            for (int i = 0; i < sparams.length; i++) {
                String pValue = req.getParameter(sparams[i]);
                completeUserPreferences.getStructureStylesheetUserPreferences().putParameterValue(sparams[i], pValue);
                if (logger.isDebugEnabled())
                    logger.debug("UserPreferencesManager::processUserPreferencesParameters() : setting sparam \"" + sparams[i] + "\"=\"" + pValue + "\".");
            }
        }
        String[] tparams = req.getParameterValues("uP_tparam");
        if (tparams != null) {
            for (int i = 0; i < tparams.length; i++) {
                String pValue = req.getParameter(tparams[i]);
                completeUserPreferences.getThemeStylesheetUserPreferences().putParameterValue(tparams[i], pValue);
                if (logger.isDebugEnabled())
                    logger.debug("UserPreferencesManager::processUserPreferencesParameters() : setting tparam \"" + tparams[i]+ "\"=\"" + pValue + "\".");
            }
        }
        // attribute processing
        // structure transformation
        String[] sfattrs = req.getParameterValues("uP_sfattr");
        if (sfattrs != null) {
            for (int i = 0; i < sfattrs.length; i++) {
                String aName = sfattrs[i];
                String[] aNode = req.getParameterValues(aName + "_folderId");
                if (aNode != null && aNode.length > 0) {
                    for (int j = 0; j < aNode.length; j++) {
                        String aValue = req.getParameter(aName + "_" + aNode[j] + "_value");
                        completeUserPreferences.getStructureStylesheetUserPreferences().setFolderAttributeValue(aNode[j], aName, aValue);
                        if (logger.isDebugEnabled())
                            logger.debug("UserPreferencesManager::processUserPreferencesParameters() : setting sfattr \"" + aName + "\" of \"" + aNode[j] + "\" to \"" + aValue + "\".");
                    }
                }
            }
        }
        String[] scattrs = req.getParameterValues("uP_scattr");
        if (scattrs != null) {
            for (int i = 0; i < scattrs.length; i++) {
                String aName = scattrs[i];
                String[] aNode = req.getParameterValues(aName + "_channelId");
                if (aNode != null && aNode.length > 0) {
                    for (int j = 0; j < aNode.length; j++) {
                        String aValue = req.getParameter(aName + "_" + aNode[j] + "_value");
                        completeUserPreferences.getStructureStylesheetUserPreferences().setChannelAttributeValue(aNode[j], aName, aValue);
                        if (logger.isDebugEnabled())
                            logger.debug("UserPreferencesManager::processUserPreferencesParameters() : setting scattr \"" + aName + "\" of \"" + aNode[j] + "\" to \"" + aValue + "\".");
                    }
                }
            }
        }
        // theme stylesheet attributes
        String[] tcattrs = req.getParameterValues("uP_tcattr");
        if (tcattrs != null) {
            for (int i = 0; i < tcattrs.length; i++) {
                String aName = tcattrs[i];
                String[] aNode = req.getParameterValues(aName + "_channelId");
                if (aNode != null && aNode.length > 0) {
                    for (int j = 0; j < aNode.length; j++) {
                        String aValue = req.getParameter(aName + "_" + aNode[j] + "_value");
                        completeUserPreferences.getThemeStylesheetUserPreferences().setChannelAttributeValue(aNode[j], aName, aValue);
                        if (logger.isDebugEnabled())
                            logger.debug("UserPreferencesManager::processUserPreferencesParameters() : setting tcattr \"" + aName + "\" of \"" + aNode[j] + "\" to \"" + aValue + "\".");
                    }
                }
            }
        }

        // save processing handled at end to provide persisting of changes
        // if desired.
        String saveWhat=req.getParameter("uP_save");
        if(saveWhat!=null) {
            try {
                if(saveWhat.equals("preferences")) {
                    userLayoutStore.putUserPreferences(person, completeUserPreferences);
                } else if(saveWhat.equals("layout")) {
                    userLayoutManager.saveUserLayout();
                } else if(saveWhat.equals("all")) {
                    userLayoutStore.putUserPreferences(person, completeUserPreferences);
                    userLayoutManager.saveUserLayout();
                  }
                if (logger.isDebugEnabled())
                    logger.debug("UserPreferencesManager::processUserPreferencesParameters() : persisted "+saveWhat+" changes.");

            } catch (Exception e) {
                logger.error( "UserPreferencesManager::processUserPreferencesParameters() : unable to persist "+saveWhat+" changes. ",e);
            }
        }
    }

    /**
     * Returns current person object
     * @return current <code>IPerson</code>
     */
    public IPerson getPerson () {
        return  (person);
    }

    /**
     * Returns a global channel Id given a channel instance Id
     * @param channelSubscribeId subscribe id of a channel
     * @return channel global id
     */
    protected String getChannelPublishId(String channelSubscribeId) throws PortalException {
        // Get the channel node from the user's layout
        IUserLayoutChannelDescription channel=(IUserLayoutChannelDescription) getUserLayoutManager().getNode(channelSubscribeId);
        if(channel!=null) {
            return channel.getChannelPublishId();
        }
        
        return null;
    }

    public boolean isUserAgentUnmapped() {
        return  unmappedUserAgent;
    }

    /*
     * Resets both user layout and user preferences.
     * Note that if any of the two are "null", old values will be used.
     */
    public void setNewUserLayoutAndUserPreferences(IUserLayoutManager newUlm, UserPreferences newPreferences) throws PortalException {
      try {
        if (newPreferences != null) {
            // see if the profile has changed
            if(completeUserPreferences.getProfile().getProfileId()!=newPreferences.getProfile().getProfileId() || completeUserPreferences.getProfile().isSystemProfile()!=newPreferences.getProfile().isSystemProfile()) {
                // see if a layout was passed
                if(newUlm !=null && newUlm.getLayoutId()==newPreferences.getProfile().getLayoutId()) {
                    // just use a new layout
                    this.userLayoutManager=newUlm;
                } else {
                    // construct a new user layout manager, for a new profile
                    userLayoutManager=UserLayoutManagerFactory.getUserLayoutManager(person,newPreferences.getProfile());
                }
            }
            userLayoutStore.putUserPreferences(person, newPreferences);
            completeUserPreferences=newPreferences;

        }
      } catch (Exception e) {
        logger.error("Exception setting new user layout manager " + newUlm + 
                " and/or new prefererences " + newPreferences, e);
        throw  new GeneralRenderingException(e);
      }
    }

    public IUserLayoutManager getUserLayoutManager() {
        return userLayoutManager;
    }

    public void finishedSession(HttpSessionBindingEvent bindingEvent) {
        // persist the layout and user preferences
        try {
            if(saveUserPreferencesAtLogout) {
                userLayoutStore.putUserPreferences(person, completeUserPreferences);
                userLayoutManager.saveUserLayout();
            }
        } catch (Exception e) {
            logger.error("UserPreferencesManager::finishedSession() : unable to persist layout upon session termination !", e);
        }
    }

    public UserPreferences getUserPreferencesCopy() {
        return  new UserPreferences(this.getUserPreferences());
    }


    public UserProfile getCurrentProfile() {
        return  this.getUserPreferences().getProfile();
    }

    public ThemeStylesheetDescription getThemeStylesheetDescription() throws Exception {
        if (this.themeStylesheetDescription == null) {
           themeStylesheetDescription = userLayoutStore.getThemeStylesheetDescription(this.getCurrentProfile().getThemeStylesheetId());
        }
        return  themeStylesheetDescription;
    }

    public StructureStylesheetDescription getStructureStylesheetDescription() throws Exception {
        if (this.structureStylesheetDescription == null) {
            structureStylesheetDescription = userLayoutStore.getStructureStylesheetDescription(this.getCurrentProfile().getStructureStylesheetId());
        }
        return  structureStylesheetDescription;
    }

    public UserPreferences getUserPreferences() {
        return completeUserPreferences;
    }
}



