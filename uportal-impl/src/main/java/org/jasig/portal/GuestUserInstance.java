/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.events.EventPublisherLocator;
import org.jasig.portal.events.support.UserSessionCreatedPortalEvent;
import org.jasig.portal.events.support.UserSessionDestroyedPortalEvent;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.security.IPerson;
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

    // manages channel instances and channel rendering
    private final ChannelManager channelManager;
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
        
        //Initialize the ChannelManager
        this.channelManager = new ChannelManager(this.userPreferencesManager, session);
        
        //Create the rendering lock for the user
        this.renderingLock = new Object();
    }
    
    

    /* (non-Javadoc)
     * @see org.jasig.portal.IUserInstance#getChannelManager()
     */
    public ChannelManager getChannelManager() {
        return this.channelManager;
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
     * @see javax.servlet.http.HttpSessionBindingListener#valueBound(javax.servlet.http.HttpSessionBindingEvent)
     */
    public void valueBound(HttpSessionBindingEvent bindingEvent) {
        if (log.isDebugEnabled()) {
            log.debug("instance bound to a new session '" + bindingEvent.getSession().getId() + "'");
        }

        // Record the creation of the session
        final ApplicationEventPublisher applicationEventPublisher = EventPublisherLocator.getApplicationEventPublisher();
        applicationEventPublisher.publishEvent(new UserSessionCreatedPortalEvent(this, person));
        
        guestSessions.incrementAndGet();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
     */
    public void valueUnbound(HttpSessionBindingEvent bindingEvent) {
        final HttpSession session = bindingEvent.getSession();
        if (log.isDebugEnabled()) {
            log.debug("unbinding session '" + session.getId() + "'");
        }
        
        this.channelManager.finishedSession(session);
        
        this.userPreferencesManager.finishedSession(bindingEvent);

        // Record the destruction of the session
        final ApplicationEventPublisher applicationEventPublisher = EventPublisherLocator.getApplicationEventPublisher();
        applicationEventPublisher.publishEvent(new UserSessionDestroyedPortalEvent(this, person));
        
        guestSessions.decrementAndGet();
    }
}



