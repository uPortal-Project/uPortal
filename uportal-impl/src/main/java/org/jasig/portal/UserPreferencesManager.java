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
import org.jasig.portal.jndi.IJndiManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutManagerFactory;
import org.jasig.portal.layout.UserLayoutStoreFactory;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.utils.PropsMatcher;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;


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
                    final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
                    final IJndiManager jndiManager = (IJndiManager) applicationContext.getBean("jndiManager", IJndiManager.class);
                    
                    // Initialize the JNDI context for this user
                    final String userId = Integer.toString(this.person.getID());
                    final String layoutId = Integer.toString(userProfile.getLayoutId());
                    final Document userLayoutDom = userLayoutManager.getUserLayoutDOM();
                    jndiManager.initializeSessionContext(session, userId, layoutId, userLayoutDom);
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



