/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
 * Responsible for removing some membership-related cache entries when a user
 * authenticates so they can be evaluated afresh.
 *
 * @author drewwills
 */
@Component
public class GroupsCacheAuthenticationListener implements IAuthenticationListener {

    @Autowired
    @Qualifier(value = "org.apereo.portal.groups.GroupMemberImpl.parentGroups")
    private Cache parentGroupsCache;

    @Autowired
    @Qualifier(value = "org.apereo.portal.groups.EntityGroupImpl.children")
    private Cache childrenCache;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void userAuthenticateed(IPerson user) {

        final long timestamp = System.currentTimeMillis();

        /*
         * Group/member relationships are cached 2 ways:  child-to-parents and
         * parent-to-children.  We need to flush both.
         */
        final EntityIdentifier ei = user.getEntityIdentifier();
        final Element parentGroupsElement = parentGroupsCache.get(ei);
        if (parentGroupsElement != null) {
            // We have some flushing work to do...
            int numPurged = 1;
            final Set<IEntityGroup> parentGroups = (Set<IEntityGroup>) parentGroupsElement.getObjectValue();
            for (IEntityGroup group : parentGroups) {
                final EntityIdentifier uei = group.getUnderlyingEntityIdentifier();
                if (childrenCache.remove(uei)) {
                    ++numPurged;
                }
            }
            parentGroupsCache.remove(ei);
            logger.debug("Purged {} local group cache entries for authenticated user '{}' in {}ms",
                    numPurged, user.getUserName(), Long.toBinaryString(System.currentTimeMillis() - timestamp));
        }

    }

}
