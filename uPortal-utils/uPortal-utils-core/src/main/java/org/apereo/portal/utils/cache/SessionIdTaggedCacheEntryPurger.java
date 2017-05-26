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

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Component;

/**
 * Purges cache entries tagged for a specific session when the session is destroyed
 *
 */
@Component
public class SessionIdTaggedCacheEntryPurger
        implements ApplicationListener<HttpSessionDestroyedEvent> {
    public static final String TAG_TYPE = "httpSessionId";

    public static CacheEntryTag createCacheEntryTag(String sessionId) {
        return new SimpleCacheEntryTag<String>(TAG_TYPE, sessionId);
    }

    private TaggedCacheEntryPurger taggedCacheEntryPurger;

    @Autowired
    public void setTaggedCacheEntryPurger(TaggedCacheEntryPurger taggedCacheEntryPurger) {
        this.taggedCacheEntryPurger = taggedCacheEntryPurger;
    }

    @Override
    public void onApplicationEvent(HttpSessionDestroyedEvent event) {
        final HttpSession session = event.getSession();
        purgeTaggedCacheEntries(session.getId());
    }

    public void purgeTaggedCacheEntries(final String sessionId) {
        final CacheEntryTag tag = createCacheEntryTag(sessionId);
        this.taggedCacheEntryPurger.purgeCacheEntries(tag);
    }
}
