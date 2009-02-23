/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.user;

import java.util.Map;

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.support.UserLoggedOutPortalEvent;
import org.jasig.portal.events.support.UserSessionDestroyedPortalEvent;
import org.jasig.portal.security.IPerson;
import org.jasig.services.persondir.support.CachingPersonAttributeDaoImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * On configuration this bean will load all beans of type {@link CachingPersonAttributeDaoImpl} and store references to
 * them. When a {@link UserLoggedOutPortalEvent} or {@link UserSessionDestroyedPortalEvent} are recieved this code will
 * iterate over all caching DAOs and call the {@link CachingPersonAttributeDaoImpl#removeUserAttributes(String)} method
 * to clear out the caches.
 * 
 * @author Eric Dalquist
 * @version $Revision: 1.1 $
 */
public class UserAttributesCacheCleaner implements ApplicationListener, ApplicationContextAware, InitializingBean {
    private ApplicationContext applicationContext;
    private Map<String, CachingPersonAttributeDaoImpl> cachingAttributeDaos;

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() throws Exception {
        this.cachingAttributeDaos = this.applicationContext.getBeansOfType(CachingPersonAttributeDaoImpl.class);
    }


    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event) {
        if (!(event instanceof UserLoggedOutPortalEvent || event instanceof UserSessionDestroyedPortalEvent)) {
            return;
        }
        
        final PortalEvent portalEvent = (PortalEvent)event;
        final IPerson person = portalEvent.getPerson();
        final String userName = person.getUserName();

        for (final CachingPersonAttributeDaoImpl cachingAttributeDao : this.cachingAttributeDaos.values()) {
            cachingAttributeDao.removeUserAttributes(userName);
        }
    }
}
