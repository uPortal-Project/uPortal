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
package org.jasig.portal.groups.pags.testers;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.pags.IPersonTester;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * PAGS Tester for inclusive/exclusive membership in sets of groups.
 *
 * @author Benito J. Gonzalez <bgonzalez@unicon.net>
 */
public final class AdHocGroupTester implements IPersonTester {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final Set<String> currentPersons = new ConcurrentSkipListSet<String>();
    private static final Set<IEntityGroup> includes = new HashSet<>();
    private static final Set<IEntityGroup> excludes = new HashSet<>();

    /**
     * Temporary constructor for testing group test before tackling import/export changes.
     *
     * @param attribute not used
     * @param value     group name
     */
    public AdHocGroupTester(String attribute, String value) {
        logger.debug("constructor attribute: {}", attribute);
        logger.debug("constructor value: {}", value);

        // Get IEntityGroup for group value
        IEntityGroup entityGroup = findGroupByName(value);
        if (entityGroup != null) {
            includes.add(entityGroup);
        } else {
            logger.warn("Could not find group {}", value);
        }
    }

    @Override
    public boolean test(IPerson person) {
        logger.debug("ID of Person \"{}\" is {}", person.getUserName(), person.getEntityIdentifier().getKey());
        if (currentPersons.contains(person.getEntityIdentifier().getKey()))
            return false; // stop recursing
        currentPersons.add(person.getEntityIdentifier().getKey());

        IGroupMember gmPerson = findPersonAsGroupMember(person);
        boolean pass = true;

        Iterator<IEntityGroup> groups = includes.iterator();
        while (pass && groups.hasNext()) {
            IEntityGroup group = groups.next();
            if (!gmPerson.isDeepMemberOf(group)) {
                logger.debug("!! {} is not a member of {}", person.getUserName(), group.getDescription());
                pass = false;
            } else {
                logger.debug("{} is a member of {}", person.getUserName(), group.getDescription());
            }
        }
        groups = excludes.iterator();
        while (pass && groups.hasNext()) {
            IEntityGroup group = groups.next();
            if (gmPerson.isDeepMemberOf(group)) {
                logger.debug("!! {} is a member of {}", person.getUserName(), group.getDescription());
                pass = false;
            } else {
                logger.debug("{} is a not member of {}", person.getUserName(), group.getDescription());
            }
        }

        currentPersons.remove(person.getEntityIdentifier().getKey());
        return pass;
    }

    /**
     * Find <code>IEntityGroup</code> from group name.
     *
     * @param groupName name of group
     * @return IEntityGroup group object with given name
     */
    private IEntityGroup findGroupByName(String groupName) {
        EntityIdentifier[] identifiers = GroupService.searchForGroups(groupName, GroupService.IS, EntityEnum.GROUP.getClazz());
        logger.debug("Found {} indentifier(s) for group name {}", identifiers.length, groupName);
        for (EntityIdentifier entityIdentifier : identifiers) {
            logger.debug(entityIdentifier.toString());
            if (entityIdentifier.getType().equals(IEntityGroup.class)) {
                return GroupService.findGroup(entityIdentifier.getKey());
            }
        }
        return null;
    }
    /**
     * Find <code>IPerson</code> as <code>IGroupMember</code>.
     *
     * @param person <code>IPerson</code> to be tested
     * @return IGroupMember person as <code>IGroupMember</code>
     */
    private IGroupMember findPersonAsGroupMember(IPerson person) {
        String personKey = person.getEntityIdentifier().getKey();
        logger.warn(personKey);
        return GroupService.getEntity(personKey, IPerson.class);
    }
}
