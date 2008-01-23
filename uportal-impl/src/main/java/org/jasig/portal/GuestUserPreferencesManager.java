/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.jndi.IJndiManager;
import org.jasig.portal.layout.IUserLayoutManager;
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
 * Multithreaded version of {@link UserPreferencesManager}.
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
 * @version $Revision$
 * @see UserPreferencesManager
 */
public class GuestUserPreferencesManager extends UserPreferencesManager  {
    
    private static final Log log = LogFactory.getLog(GuestUserPreferencesManager.class);
    
    private class MState {
        private ThemeStylesheetDescription tsd;
        private StructureStylesheetDescription ssd;
        private boolean unmapped_user_agent;
        private UserPreferences complete_up;
        private IUserLayoutManager ulm;
        public MState() {
            tsd=null; ssd=null; complete_up=null;
            unmapped_user_agent=false;
        }
    }

    Map<String, MState> stateTable;

    // tables keeping user layouts and clean user preferences for various profiles
    Hashtable<Integer, IUserLayoutManager> sp_layouts;
    Hashtable<Integer, IUserLayoutManager> up_layouts;

    Hashtable<Integer, UserPreferences> sp_cleanUPs;
    Hashtable<Integer, UserPreferences> up_cleanUPs;
    Hashtable<Integer, ThemeStylesheetDescription> ts_descripts;
    Hashtable<Integer, StructureStylesheetDescription> ss_descripts;
    Hashtable<String, UserProfile> cached_profiles;

    IPerson m_person;
    LocaleManager localeManager;

    final static boolean SAVE_PROFILE_GUESSES=PropertiesManager.getPropertyAsBoolean("org.jasig.portal.GuestUserPreferencesManager.save_profile_guesses");

    /**
     * Initializing constructor.
     * @param person object
     */
    public GuestUserPreferencesManager (IPerson person) {
        super(person);
        stateTable=Collections.synchronizedMap(new HashMap<String, MState>());
        up_cleanUPs=new Hashtable<Integer, UserPreferences>();
        sp_cleanUPs=new Hashtable<Integer, UserPreferences>();
        sp_layouts=new Hashtable<Integer, IUserLayoutManager>();
        up_layouts=new Hashtable<Integer, IUserLayoutManager>();
        cached_profiles=new Hashtable<String, UserProfile>();
        ts_descripts=new Hashtable<Integer, ThemeStylesheetDescription>();
        ss_descripts=new Hashtable<Integer, StructureStylesheetDescription>();
        m_person = person;
        userLayoutStore = UserLayoutStoreFactory.getUserLayoutStoreImpl();
    }


    /**
     * Unbinds a registered session.
     * @param sessionId a <code>String</code> value
     * @deprecated use {@link #finishedSession(HttpSessionBindingEvent)} instead.
     */
    public void unbindSession(String sessionId) {
        stateTable.remove(sessionId);
    }

    /**
     * Register arrival of a new session.
     * Create and populate new state entry.
     * @param req a <code>HttpServletRequest</code> value
     * @exception PortalException if an error occurs
     */
    public void registerSession(HttpServletRequest req) throws PortalException {
        MState newState=new MState();
        try {
            // load user preferences
            // determine user profile
            String userAgent = req.getHeader("User-Agent");
            if(userAgent==null || userAgent.equals("")) { 
                userAgent=MediaManager.NULL_USER_AGENT; 
            }
            UserProfile upl;
            // see if the profile was cached
            if((upl=cached_profiles.get(userAgent))==null) {
                synchronized(cached_profiles) {
                    upl= userLayoutStore.getUserProfile(m_person, userAgent);
                    if (upl == null) {
                        upl = userLayoutStore.getSystemProfile(userAgent);
                    }
                    if(upl!=null) {
                        cached_profiles.put(userAgent,upl);
                    }
                }
            }

            if(upl==null) {
                // try guessing the profile through pattern matching

                final PropsMatcher userAgentMatcher = getUserAgentMatcher();

                if(userAgentMatcher!=null) {
                    // try matching
                    String profileId=userAgentMatcher.match(userAgent);
                    if(profileId!=null) {
                        // user agent has been matched
                        if (log.isDebugEnabled())
                            log.debug("GuestUserPreferencesManager::GuestUserPreferencesManager() : " +
                                    "userAgent \"" + userAgent + "\" has matched to a profile " + profileId);
                        upl=userLayoutStore.getSystemProfileById(Integer.parseInt(profileId));
                        // save mapping
                        if(SAVE_PROFILE_GUESSES) {
                            userLayoutStore.setSystemBrowserMapping(userAgent,upl.getProfileId());
                        }
                    } else {
                        if (log.isDebugEnabled())
                            log.debug("GuestUserPreferencesManager::GuestUserPreferencesManager() : " +
                                    "userAgent \"" + userAgent + "\" has not matched any profile.");
                    }
                }
            }

            if (upl != null) {
                // see if the user layout xml has been cached
                if(upl.isSystemProfile()) {
                    newState.ulm=sp_layouts.get(new Integer(upl.getProfileId()));
                } else {
                    newState.ulm=up_layouts.get(new Integer(upl.getProfileId()));
                }
                if(newState.ulm==null) {
                    try {
			upl.setLocaleManager(localeManager);
                        newState.ulm=UserLayoutManagerFactory.immutableUserLayoutManager(UserLayoutManagerFactory.getUserLayoutManager(m_person,upl));
                        if(upl.isSystemProfile()) {
                            sp_layouts.put(new Integer(upl.getProfileId()),newState.ulm);
                        } else {
                            up_layouts.put(new Integer(upl.getProfileId()),newState.ulm);
                        }
                    } catch (PortalException pe) {
                        throw pe;
                    } catch (Exception e) {
                        throw new PortalException("GuestUserPreferencesManager::registerSession() : caught an exception while trying to retreive a userLayout for user=\"" +m_person.getID()+ "\", profile=\"" + upl.getProfileName() + "\".",e);
                    }
                }

                /*
                // modify the entire profile to be unremovable and immutable
                // mark all of the folders
                NodeList folderList=newState.uLayoutXML.getElementsByTagName("folder");
                for(int i=0;i<folderList.getLength();i++) {
                    Element e=(Element)folderList.item(i);
                    e.setAttribute("immutable","true");
                    e.setAttribute("unremovable","true");                        
                }
                // mark all of the channels
                NodeList channelList=newState.uLayoutXML.getElementsByTagName("channel");
                for(int i=0;i<channelList.getLength();i++) {
                    Element e=(Element)channelList.item(i);
                    e.setAttribute("immutable","true");
                    e.setAttribute("unremovable","true");                        
                }
                */


                // see if the user preferences for this profile are cached
                UserPreferences cleanUP;
                if(upl.isSystemProfile()) {
                    cleanUP=sp_cleanUPs.get(new Integer(upl.getProfileId()));
                } else {
                    cleanUP=up_cleanUPs.get(new Integer(upl.getProfileId()));
                }
                if(cleanUP==null) {
                    try {
                        cleanUP=userLayoutStore.getUserPreferences(m_person, upl);
                        if(cleanUP!=null) {
                            if(upl.isSystemProfile()) {
                                sp_cleanUPs.put(new Integer(upl.getProfileId()),cleanUP);
                            } else {
                                up_cleanUPs.put(new Integer(upl.getProfileId()),cleanUP);
                            }
                        }
                    } catch (Exception e) {
                        log.error("GuestUserPreferencesManager::registerSession() : " +
                        		"unable to find UP for a profile \""+upl.getProfileName()+"\"",e);
                        cleanUP=new UserPreferences(upl);
                    }
                }

                if(cleanUP!=null) {
                    newState.complete_up=new UserPreferences(cleanUP);
                } else {
                    log.error("GuestUserPreferencesManager::registerSession() : " +
                    		"unable to find UP for a profile \""+upl.getProfileName()+"\"");
                    newState.complete_up=new UserPreferences(upl);
                }

                final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
                final IJndiManager jndiManager = (IJndiManager) applicationContext.getBean("jndiManager", IJndiManager.class);
                
                // Initialize the JNDI context for this user
                final HttpSession session = req.getSession();
                final String userId = Integer.toString(m_person.getID());
                final String layoutId = Integer.toString(upl.getLayoutId());
                final Document userLayoutDom = newState.ulm.getUserLayoutDOM();
                jndiManager.initializeSessionContext(session, userId, layoutId, userLayoutDom);
            } else {
                // there is no user-defined mapping for this particular browser.
                // user should be redirected to a browser-registration page.
                newState.unmapped_user_agent = true;
                if (log.isDebugEnabled())
                    log.debug("GuestUserPreferencesManager::registerSession() : " +
                            "unable to find a profile for user \"" + m_person.getID() + 
                            "\" and userAgent=\"" + userAgent + "\".");
            }
        } catch (PortalException pe) {
            throw pe;
        } catch (Throwable t) {
            throw new PortalException(t);
        }
        stateTable.put(req.getSession(false).getId(),newState);
    }

    /**
     * Returns a global channel Id given a channel instance Id
     * @return Channel's global Id
     */
    protected String getChannelGlobalId (String channelSubscribeId, String sessionId) throws PortalException {
        // Get the channel node from the user's layout
        IUserLayoutChannelDescription channel=(IUserLayoutChannelDescription) getUserLayoutManager(sessionId).getNode(channelSubscribeId);
        if(channel!=null) {
            return channel.getChannelPublishId();
        } else {
            return null;
        }
    }

    public boolean isUserAgentUnmapped (String sessionId) {
        MState state=stateTable.get(sessionId);
        if(state==null) {
        	throw new IllegalStateException("Trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
        }
        return  state.unmapped_user_agent;
    }

    public boolean isUserAgentUnmapped () {
        throw new UnsupportedOperationException();
    }

    public UserPreferences getUserPreferences (String sessionId) {
        MState state=stateTable.get(sessionId);
        if(state==null) {
        	throw new IllegalStateException("Trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
        }
        return  state.complete_up;
    }

    public UserPreferences getUserPreferences () {
        throw new UnsupportedOperationException();
    }

    /*
     * Guest users can not (by definition) save any preferences. Method does nothing.
     */
    public void setNewUserLayoutAndUserPreferences (IUserLayoutManager newLayout, UserPreferences newPreferences,String sessionId) throws PortalException {
        // not implemented yet
    }

    public void setNewUserLayoutAndUserPreferences (IUserLayoutManager newLayout, UserPreferences newPreferences) throws PortalException {
        throw new UnsupportedOperationException();
    }



    public UserPreferences getUserPreferencesCopy (String sessionId) {
        return  new UserPreferences(this.getUserPreferences(sessionId));
    }

    public UserPreferences getUserPreferencesCopy () {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns current profile
     * @return UserProfile
     */
    public UserProfile getCurrentProfile (String sessionId) {
        return  this.getUserPreferences(sessionId).getProfile();
    }

    public UserProfile getCurrentProfile () {
        throw new UnsupportedOperationException();
    }

    public ThemeStylesheetDescription getThemeStylesheetDescription (String sessionId) throws Exception {
        MState state=stateTable.get(sessionId);
        if(state==null) {
        	throw new IllegalStateException("Trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
        }
        if (state.tsd == null) {
            int sid=state.complete_up.getProfile().getThemeStylesheetId();
            state.tsd=ts_descripts.get(new Integer(sid));
            if(state.tsd==null) {
                state.tsd = userLayoutStore.getThemeStylesheetDescription(sid);
                ts_descripts.put(new Integer(sid),state.tsd);
            }
        }
        return  state.tsd;
    }

    public ThemeStylesheetDescription getThemeStylesheetDescription () {
        throw new UnsupportedOperationException();
    }

    public StructureStylesheetDescription getStructureStylesheetDescription (String sessionId) throws Exception{
        MState state=stateTable.get(sessionId);
        if(state==null) {
        	throw new IllegalStateException("Trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
        }
        if (state.ssd == null) {
            int sid=state.complete_up.getProfile().getStructureStylesheetId();
            state.ssd=ss_descripts.get(new Integer(sid));
            if(state.ssd==null) {
                state.ssd = userLayoutStore.getStructureStylesheetDescription(sid);
                ss_descripts.put(new Integer(sid),state.ssd);
            }
        }
        return  state.ssd;
    }

    public StructureStylesheetDescription getStructureStylesheetDescription () {
        throw new UnsupportedOperationException();
    }

    public IUserLayoutManager getUserLayoutManager(String sessionId) {
        MState state=stateTable.get(sessionId);
        if(state==null) {
        	throw new IllegalStateException("Trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
        }
        return  state.ulm;
    }

    public IUserLayoutManager getUserLayoutManager() {
        throw new UnsupportedOperationException();
    }

    public void finishedSession(HttpSession session, String sessionId) {
        // remove session state info
        stateTable.remove(sessionId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.UserPreferencesManager#finishedSession(javax.servlet.http.HttpSession)
     */
    @Override
    public void finishedSession(HttpSession session) {
        final String sessionId = session.getId();
        this.finishedSession(session, sessionId);
    }

    public void setLocaleManager(LocaleManager lm) {
	localeManager = lm;
    }

}



