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

package org.jasig.portal;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.events.EventPublisherLocator;
import org.jasig.portal.events.support.UserSessionCreatedPortalEvent;
import org.jasig.portal.events.support.UserSessionDestroyedPortalEvent;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.user.IUserInstance;
import org.springframework.context.ApplicationEventPublisher;

/**
 * A multithreaded version of a UserInstance.
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
 * @version $Revision$
 */
public class GuestUserInstance implements IUserInstance {
    private static final AtomicInteger guestSessions = new AtomicInteger();
    
    public static long getGuestSessions() {
        return guestSessions.longValue();
    }
    
    
    protected final Log log = LogFactory.getLog(this.getClass());

    // manages locale
    private final LocaleManager localeManager;

    // lock preventing concurrent rendering
    private final Object renderingLock;

    private final IPerson person;

    // manages layout and preferences
    private final GuestUserPreferencesManagerWrapper userPreferencesManager;

    public GuestUserInstance(IPerson person, GuestUserPreferencesManager preferencesManager, HttpServletRequest request) {
        this.person = person;
        
        // instantiate locale manager (uPortal i18n)
        final String acceptLanguage = request.getHeader("Accept-Language");
        this.localeManager = new LocaleManager(person, acceptLanguage);

        //Use the shared user preferences manager
        final HttpSession session = request.getSession(false);
        final String sessionId = session.getId();
        
        preferencesManager.setLocaleManager(this.localeManager);
        preferencesManager.registerSession(request);
        
        if (preferencesManager.isUserAgentUnmapped(sessionId)) {
            this.log.warn("A Mapping User-Agent could not be found for the GuestUserPreferencesManagerWrapper");
        }
        
        this.userPreferencesManager = new GuestUserPreferencesManagerWrapper(preferencesManager, sessionId);
        
        //Create the rendering lock for the user
        this.renderingLock = new Object();
        
        if (log.isDebugEnabled()) {
            log.debug("instance bound to a new session '" + request.getSession().getId() + "'");
        }

        // Record the creation of the session
        final ApplicationEventPublisher applicationEventPublisher = EventPublisherLocator.getApplicationEventPublisher();
        applicationEventPublisher.publishEvent(new UserSessionCreatedPortalEvent(this, person));
        
        guestSessions.incrementAndGet();
    }
    
    

    /* (non-Javadoc)
     * @see org.jasig.portal.IUserInstance#getLocaleManager()
     */
    public LocaleManager getLocaleManager() {
        return this.localeManager;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IUserInstance#getPerson()
     */
    public IPerson getPerson() {
        return this.person;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IUserInstance#getPreferencesManager()
     */
    public IUserPreferencesManager getPreferencesManager() {
        return this.userPreferencesManager;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.IUserInstance#getRenderingLock()
     */
    public Object getRenderingLock() {
        return this.renderingLock;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.user.IUserInstance#destroySession(javax.servlet.http.HttpSession)
     */
    public void destroySession(HttpSession session) {
        if (log.isDebugEnabled()) {
            log.debug("unbinding session '" + session.getId() + "'");
        }
        
        this.userPreferencesManager.finishedSession(session);

        // Record the destruction of the session
        final ApplicationEventPublisher applicationEventPublisher = EventPublisherLocator.getApplicationEventPublisher();
        applicationEventPublisher.publishEvent(new UserSessionDestroyedPortalEvent(this, person));
        
        guestSessions.decrementAndGet();
    }
}



