/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout.dlm;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.UserProfile;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.support.UserLoggedOutPortalEvent;
import org.jasig.portal.events.support.UserSessionDestroyedPortalEvent;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutStoreFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.Tuple;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.w3c.dom.Document;

/**
 * Provides API for layout caching service
 */
public class LayoutCachingService implements ApplicationListener, ILayoutCachingService {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private Map<Serializable, Document> layoutCache;
    
    /**
     * @return the layoutCache
     */
    public Map<Serializable, Document> getLayoutCache() {
        return layoutCache;
    }
    /**
     * @param layoutCache the layoutCache to set
     */
    public void setLayoutCache(Map<Serializable, Document> layoutCache) {
        this.layoutCache = layoutCache;
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof UserLoggedOutPortalEvent || event instanceof UserSessionDestroyedPortalEvent) {
            final PortalEvent portalEvent = (PortalEvent)event;
            final IPerson person = portalEvent.getPerson();
            //We don't want to clear out the guest layout
            if (person.isGuest()) {
                return;
            }
            
            //Try invalidating just the layout associated with the current user and profile
            final UserProfile currentUserProfile = (UserProfile)person.getAttribute(UserProfile.USER_PROFILE);
            if (currentUserProfile != null) {
                this.removeCachedLayout(person, currentUserProfile);
                return;
            }
            
            //No provided profile, invalidate all layouts for the user
            final IUserLayoutStore userLayoutStore = UserLayoutStoreFactory.getUserLayoutStoreImpl();
            final Hashtable<Integer, UserProfile> userProfiles;
            try {
                userProfiles = userLayoutStore.getUserProfileList(person);
            }
            catch (Exception e) {
                this.logger.warn("Failed to load all UserProfiles for '" + person.getUserName() + "'. The user's layouts will not be explicitly removed from the layout cache.", e);
                return;
            }
            
            for (final UserProfile userProfile : userProfiles.values()) {
                this.removeCachedLayout(person, userProfile);
            }
        }
    }

    public void cacheLayout(IPerson owner, UserProfile profile, Document layout) {
        final Serializable cacheKey = this.getCacheKey(owner, profile);
        this.layoutCache.put(cacheKey, layout);
    }
    
    public Document getCachedLayout(IPerson owner, UserProfile profile) {
        final Serializable cacheKey = this.getCacheKey(owner, profile);
        return this.layoutCache.get(cacheKey);
    }
    
    public void removeCachedLayout(IPerson owner, UserProfile profile) {
        final Serializable cacheKey = this.getCacheKey(owner, profile);
        this.layoutCache.remove(cacheKey);
    }
    protected Serializable getCacheKey(IPerson owner, UserProfile profile) {
        return new Tuple<String, Integer>(owner.getUserName(), profile.getLayoutId());
    }
}
