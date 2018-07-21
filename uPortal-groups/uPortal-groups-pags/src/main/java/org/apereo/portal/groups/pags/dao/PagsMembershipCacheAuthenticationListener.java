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
package org.apereo.portal.groups.pags.dao;

import java.util.List;
import javax.annotation.PostConstruct;
import net.sf.ehcache.Cache;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Result;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.IAuthenticationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Responsible for flushing PAGS membership-related cache entries when a user authenticates so they
 * can be evaluated afresh. Users may have different attributes -- and therefore different PAGS
 * affiliations -- based on information passed to the authentication process.
 *
 * @since 5.0
 */
@Component
public class PagsMembershipCacheAuthenticationListener implements IAuthenticationListener {

    private static final String SEARCH_ATTRIBUTE_NAME = "memberId";

    @Autowired
    @Qualifier(
            value = "org.apereo.portal.groups.pags.dao.EntityPersonAttributesGroupStore.membership")
    private Cache membershipCache;

    private Attribute<String> usernameSearchAttribute;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        usernameSearchAttribute = membershipCache.getSearchAttribute(SEARCH_ATTRIBUTE_NAME);
    }

    @Override
    public void userAuthenticated(IPerson user) {

        /*
         * Used to log the time it takes to complete this operation;  the author
         * has some anxiety about running time with large numbers of elements in
         * the cache.
         */
        final long timestamp = System.currentTimeMillis();

        /*
         * Query the membershipCache (ehcache) for elements that
         * reference the specified user and remove them.
         */
        final Query query =
                membershipCache
                        .createQuery()
                        .includeKeys()
                        .addCriteria(
                                usernameSearchAttribute.eq(user.getEntityIdentifier().getKey()))
                        .end();
        final List<Result> queryResults = query.execute().all();
        for (Result r : queryResults) {
            membershipCache.remove(r.getKey());
        }

        logger.debug(
                "Purged {} PAGS membership cache entries for authenticated user '{}' in {}ms",
                queryResults.size(),
                user.getUserName(),
                Long.toBinaryString(System.currentTimeMillis() - timestamp));
    }
}
