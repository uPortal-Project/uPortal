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
package org.apereo.portal.groups;

import java.util.Set;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.IAuthenticationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Responsible for removing membership-related cache entries within the 'local' (database) groups
 * strategy when a user authenticates. (Other {@link IAuthenticationListener} beans may perform a
 * similar function for other strategies). This purge is necessary so they can be evaluated afresh;
 * depending on authentication parameters, there could be a different result.
 *
 * @since 5.0
 */
@Component
public class LocalGroupsCacheAuthenticationListener implements IAuthenticationListener {

    private Cache parentGroupsCache;

    private Cache childrenCache;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier(value = "org.apereo.portal.groups.GroupMemberImpl.parentGroups")
    public void setParentGroupsCache(Cache parentGroupsCache) {
        this.parentGroupsCache = parentGroupsCache;
    }

    @Autowired
    @Qualifier(value = "org.apereo.portal.groups.EntityGroupImpl.children")
    public void setChildrenCache(Cache childrenCache) {
        this.childrenCache = childrenCache;
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
         * Group/member relationships are cached 2 ways:  child-to-parents and
         * parent-to-children.  We need to flush both.
         */
        final EntityIdentifier ei = user.getEntityIdentifier();
        final Element parentGroupsElement = parentGroupsCache.get(ei);
        if (parentGroupsElement != null) {
            // We have some flushing work to do...
            final Set<IEntityGroup> parentGroups =
                    (Set<IEntityGroup>) parentGroupsElement.getObjectValue();
            for (IEntityGroup group : parentGroups) {
                final EntityIdentifier uei = group.getUnderlyingEntityIdentifier();
                childrenCache.remove(uei);
            }
            parentGroupsCache.remove(ei);
            logger.debug(
                    "Purged the following local group cache entries for authenticated user '{}' in {}ms:  {}",
                    user.getUserName(),
                    Long.toBinaryString(System.currentTimeMillis() - timestamp),
                    parentGroups);
        }
    }
}
