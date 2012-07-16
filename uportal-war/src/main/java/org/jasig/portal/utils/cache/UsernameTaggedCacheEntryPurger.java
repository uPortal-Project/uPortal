package org.jasig.portal.utils.cache;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Resource;

import org.jasig.portal.events.LoginEvent;
import org.jasig.portal.events.LogoutEvent;
import org.jasig.portal.events.PortalEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Purges cache entries tagged for a specific user when they login or logout
 * 
 * @author Eric Dalquist
 */
@Component
public class UsernameTaggedCacheEntryPurger implements ApplicationListener<PortalEvent> {
    public static final String TAG_TYPE = "username";
    
    public static CacheEntryTag createCacheEntryTag(String username) {
        return new SimpleCacheEntryTag(TAG_TYPE, username);
    }
    
    private TaggedCacheEntryPurger taggedCacheEntryPurger;
    private Set<String> ignoredUserNames = Collections.emptySet();
    
    @Resource(name="UsernameCacheTagPurger_IgnoredUsernames")
    public void setIgnoredUserNames(Set<String> ignoredUserNames) {
        this.ignoredUserNames = ignoredUserNames;
    }

    @Autowired
    public void setTaggedCacheEntryPurger(TaggedCacheEntryPurger taggedCacheEntryPurger) {
        this.taggedCacheEntryPurger = taggedCacheEntryPurger;
    }

    @Override
    public void onApplicationEvent(PortalEvent event) {
        if (event instanceof LoginEvent || event instanceof LogoutEvent) {
            final String userName = event.getUserName();
            if (!ignoredUserNames.contains(userName)) {
                final CacheEntryTag tag = createCacheEntryTag(userName);
                this.taggedCacheEntryPurger.purgeCacheEntries(tag);
            }
        }
    }
}
