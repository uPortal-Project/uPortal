/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.events.EventPublisherLocator;
import org.jasig.portal.events.support.UserSessionCreatedPortalEvent;
import org.jasig.portal.events.support.UserSessionDestroyedPortalEvent;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.user.IUserInstance;
import org.springframework.context.ApplicationEventPublisher;


/**
 * A class handling holding all user state information. The class is also reponsible for
 * request processing and orchestrating the entire rendering procedure.
 * (this is a replacement for the good old LayoutBean class)
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version $Revision$
 */
public class UserInstance implements IUserInstance {
    private static final AtomicInteger userSessions = new AtomicInteger();
    
    public static long getUserSessions() {
        return userSessions.longValue();
    }
    
    
    protected final Log log = LogFactory.getLog(this.getClass());

    // manages layout and preferences
    private final IUserPreferencesManager preferencesManager;
    // manages channel instances and channel rendering
    private final ChannelManager channelManager;
    // manages locale
    private final LocaleManager localeManager;

    // lock preventing concurrent rendering
    private final Object renderingLock;

    private final IPerson person;


    public UserInstance(IPerson person, HttpServletRequest request) {
        this.person = person;

        // instantiate locale manager (uPortal i18n)
        final String acceptLanguage = request.getHeader("Accept-Language");
        this.localeManager = new LocaleManager(person, acceptLanguage);

        //Create the UserPreferencesManager
        this.preferencesManager = new UserPreferencesManager(request, this.person, this.localeManager);
        if (preferencesManager.isUserAgentUnmapped()) {
            this.log.warn("A Mapping User-Agent could not be found for the UserPreferencesManager");
        }

        //Initialize the ChannelManager
        final HttpSession session = request.getSession(false);
        this.channelManager = new ChannelManager(this.preferencesManager, session);
        
        //Register the channel manager as a layout event listener
        final IUserLayoutManager userLayoutManager = this.preferencesManager.getUserLayoutManager();
        userLayoutManager.addLayoutEventListener(this.channelManager);
        
        //Create the rendering lock for the user
        this.renderingLock = new Object();
        
        // Record the creation of the session
        final ApplicationEventPublisher applicationEventPublisher = EventPublisherLocator.getApplicationEventPublisher();
        applicationEventPublisher.publishEvent(new UserSessionCreatedPortalEvent(this, this.person));

        userSessions.incrementAndGet();
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
        return this.preferencesManager;
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
     * @see org.jasig.portal.IUserInstance#getRenderingLock()
     */
    public Object getRenderingLock() {
        return this.renderingLock;
    }
    
    /**
     * This notifies UserInstance that it has been unbound from the session.
     * Method triggers cleanup in ChannelManager.
     * 
     * @see org.jasig.portal.user.IUserInstance#destroySession(javax.servlet.http.HttpSession)
     */
    public void destroySession(HttpSession session) {
        if (this.channelManager != null) {
            this.channelManager.finishedSession(session);
            
            if (this.preferencesManager != null) {
                final IUserLayoutManager userLayoutManager = this.preferencesManager.getUserLayoutManager();
                userLayoutManager.removeLayoutEventListener(this.channelManager);
            }
        }
        
        if (this.preferencesManager != null) {
            this.preferencesManager.finishedSession(session);
        }
        
        GroupService.finishedSession(this.person);
        
        // Record the destruction of the session
        final ApplicationEventPublisher applicationEventPublisher = EventPublisherLocator.getApplicationEventPublisher();
        applicationEventPublisher.publishEvent(new UserSessionDestroyedPortalEvent(this, this.person));
        
        userSessions.decrementAndGet();
    }
}
