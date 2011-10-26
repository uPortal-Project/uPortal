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

package org.jasig.portal.user;

import java.io.Serializable;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserInstance;
import org.jasig.portal.UserPreferencesManager;
import org.jasig.portal.i18n.ILocaleStore;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.IProfileMapper;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutManagerFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Determines which user instance object to use for a given user.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version $Revision 1.1$
 */
@Service("userInstanceManager")
public class UserInstanceManagerImpl implements IUserInstanceManager {
    private static final String KEY = UserInstanceManagerImpl.class.getName() + ".USER_INSTANCE";
    
    protected final Log logger = LogFactory.getLog(UserInstanceManagerImpl.class);
    
    private ILocaleStore localeStore;
    private IUserLayoutStore userLayoutStore;
    private IPersonManager personManager;
    private IPortalRequestUtils portalRequestUtils;
    private IProfileMapper profileMapper;
    private UserLayoutManagerFactory userLayoutManagerFactory;

    @Autowired
    public void setUserLayoutManagerFactory(UserLayoutManagerFactory userLayoutManagerFactory) {
        this.userLayoutManagerFactory = userLayoutManagerFactory;
    }

    @Autowired
    public void setLocaleStore(ILocaleStore localeStore) {
        this.localeStore = localeStore;
    }

    @Autowired
    public void setUserLayoutStore(IUserLayoutStore userLayoutStore) {
        this.userLayoutStore = userLayoutStore;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
    
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    
    @Autowired
    public void setProfileMapper(IProfileMapper profileMapper) {
        this.profileMapper = profileMapper;
    }

    /**
     * Returns the UserInstance object that is associated with the given request.
     * @param request Incoming HttpServletRequest
     * @return UserInstance object associated with the given request
     */
    @Override
    public IUserInstance getUserInstance(HttpServletRequest request) throws PortalException {
        try {
            request = this.portalRequestUtils.getOriginalPortalRequest(request);
        }
        catch (IllegalArgumentException iae) {
            //ignore, just means that this isn't a wrapped request
        }
        
        //Use request attributes first for the fastest possible retrieval
        IUserInstance userInstance = (IUserInstance)request.getAttribute(KEY);
        if (userInstance != null) {
            return userInstance;
        }
        
        final IPerson person;
        try {
            // Retrieve the person object that is associated with the request
            person = this.personManager.getPerson(request);
        }
        catch (Exception e) {
            logger.error("Exception while retrieving IPerson!", e);
            throw new PortalSecurityException("Could not retrieve IPerson", e);
        }
        
        if (person == null) {
            throw new PortalSecurityException("PersonManager returned null person for this request.  With no user, there's no UserInstance.  Is PersonManager misconfigured?  RDBMS access misconfigured?");
        }

        final HttpSession session = request.getSession();
        if (session == null) {
            throw new IllegalStateException("HttpServletRequest.getSession() returned a null session for request: " + request);
        }

        // Return the UserInstance object if it's in the session
        UserInstanceHolder userInstanceHolder = getUserInstanceHolder(session);
        if (userInstanceHolder != null) {
            userInstance = userInstanceHolder.getUserInstance();
            
            if (userInstance != null) {
                return userInstance;
            }
        }

        // Create either a UserInstance or a GuestUserInstance
        final LocaleManager localeManager = this.getLocaleManager(request, person);
        final String userAgent = this.getUserAgent(request);
        final IUserProfile userProfile = this.getUserProfile(request, person, localeManager, userAgent);

        //Create the user layout manager and user instance object
        IUserLayoutManager userLayoutManager = userLayoutManagerFactory.getUserLayoutManager(person, userProfile);
        if (person.isGuest()) {
            userLayoutManager = userLayoutManagerFactory.immutableUserLayoutManager(userLayoutManager);
        }
        
        final UserPreferencesManager userPreferencesManager = new UserPreferencesManager(person, userProfile, userLayoutManager);
        userInstance = new UserInstance(person, userPreferencesManager, localeManager);
        

        //Ensure the newly created UserInstance is cached in the session
        if (userInstanceHolder == null) {
            userInstanceHolder = new UserInstanceHolder();
        }
        userInstanceHolder.setUserInstance(userInstance);
        session.setAttribute(KEY, userInstanceHolder);
        request.setAttribute(KEY, userInstance);

        // Return the new UserInstance
        return userInstance;
    }

    protected IUserProfile getUserProfile(HttpServletRequest request, IPerson person, LocaleManager localeManager, String userAgent) {
        final String profileFname = profileMapper.getProfileFname(person, request);
        IUserProfile userProfile = userLayoutStore.getUserProfileByFname(person, profileFname);

        if (userProfile == null) {
            userProfile = userLayoutStore.getSystemProfileByFname(profileFname);
        }
        
        if (localeManager != null && LocaleManager.isLocaleAware()) {
            userProfile.setLocaleManager(localeManager);
        }
        
        return userProfile;
    }
    
    protected LocaleManager getLocaleManager(HttpServletRequest request, IPerson person) {
        final String acceptLanguage = request.getHeader("Accept-Language");
        final Locale[] userLocales = localeStore.getUserLocales(person);
        return new LocaleManager(person, userLocales, acceptLanguage);
    }

    protected String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if(StringUtils.isEmpty(userAgent)) {
            userAgent="null";
        }
        return userAgent;
    }

    protected UserInstanceHolder getUserInstanceHolder(final HttpSession session) {
        return (UserInstanceHolder) session.getAttribute(KEY);
    }

    /**
     * <p>Serializable wrapper class so the UserInstance object can
     * be indirectly stored in the session. The manager can deal with
     * this class returning a null value and its field is transient
     * so the session can be serialized successfully with the
     * UserInstance object in it.</p>
     * <p>Implements HttpSessionBindingListener and delegates those methods to
     * the wrapped UserInstance, if present.</p>
     */
    private static class UserInstanceHolder implements Serializable {
        private static final long serialVersionUID = 1L;

        private transient IUserInstance ui = null;

        /**
         * @return Returns the userInstance.
         */
        protected IUserInstance getUserInstance() {
            return this.ui;
        }

        /**
         * @param userInstance The userInstance to set.
         */
        protected void setUserInstance(IUserInstance userInstance) {
            this.ui = userInstance;
        }
    }
}
