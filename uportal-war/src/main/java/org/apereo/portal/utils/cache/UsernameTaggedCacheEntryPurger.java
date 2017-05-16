/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.utils.cache;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Resource;
import org.apereo.portal.events.LogoutEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Purges cache entries tagged for a specific user when they login or logout
 *
 */
@Component
public class UsernameTaggedCacheEntryPurger implements ApplicationListener<LogoutEvent> {
    public static final String TAG_TYPE = "username";

    public static CacheEntryTag createCacheEntryTag(String username) {
        return new SimpleCacheEntryTag<String>(TAG_TYPE, username);
    }

    private TaggedCacheEntryPurger taggedCacheEntryPurger;
    private Set<String> ignoredUserNames = Collections.emptySet();

    @Resource(name = "UsernameCacheTagPurger_IgnoredUsernames")
    public void setIgnoredUserNames(Set<String> ignoredUserNames) {
        this.ignoredUserNames = ImmutableSet.copyOf(ignoredUserNames);
    }

    @Autowired
    public void setTaggedCacheEntryPurger(TaggedCacheEntryPurger taggedCacheEntryPurger) {
        this.taggedCacheEntryPurger = taggedCacheEntryPurger;
    }

    @Override
    public void onApplicationEvent(LogoutEvent event) {
        final String userName = event.getUserName();
        purgeTaggedCacheEntries(userName);
    }

    public void purgeTaggedCacheEntries(final String username) {
        if (!ignoredUserNames.contains(username)) {
            final CacheEntryTag tag = createCacheEntryTag(username);
            this.taggedCacheEntryPurger.purgeCacheEntries(tag);
        }
    }
}
