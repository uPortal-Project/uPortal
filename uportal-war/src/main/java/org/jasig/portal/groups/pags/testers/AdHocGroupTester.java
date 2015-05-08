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

import org.apache.commons.lang.Validate;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.pags.IPersonTester;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
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
 * Immutable PAGS Tester for inclusive/exclusive membership in sets of groups.
 *
 * @author  Benito J. Gonzalez <bgonzalez@unicon.net>
 * @see     org.jasig.portal.groups.pags.IPersonTester
 * @since   4.3
 */
public final class AdHocGroupTester implements IPersonTester {

    private static final Logger logger = LoggerFactory.getLogger(AdHocGroupTester.class);
    private static final Set<String> currentPersons = new ConcurrentSkipListSet<>();
    private final Set<IEntityGroup> includes = new HashSet<>();
    private final Set<IEntityGroup> excludes = new HashSet<>();
    private final String hashcode;

    public AdHocGroupTester(IPersonAttributesGroupTestDefinition definition) {
        Validate.notNull(definition.getIncludes());
        Validate.notNull(definition.getExcludes());
        collectGroups(definition.getIncludes(), this.includes, true);
        collectGroups(definition.getExcludes(), this.excludes, false);
        hashcode = calcHashcode();
    }

    /**
     * Given a set of group names, find the entity groups and add them to the <code>groups</code> collection.
     * If a group is not found, either log a warning or throw an <code>IllegalStateException</code> based on
     * the <code>throwOnFail</code> parameter.
     *
     * @param groupNames    Set of group names
     * @param groups        Set to add named groups from <code>GroupService</code>
     * @param throwOnFail   flag to indicate whether to log or throw exception if a group is not found
     */
    private void collectGroups(Set<String> groupNames, Set<IEntityGroup> groups, boolean throwOnFail) {
        for (String groupName : groupNames) {
            IEntityGroup entityGroup = findGroupByName(groupName);
            if (entityGroup != null) {
                groups.add(entityGroup);
            } else {
                if (throwOnFail) {
                    logger.error("Could not find group named {}", groupName);
                    throw new IllegalArgumentException("Could not find group named " + groupName);
                } else {
                    logger.warn("Could not find group named {}", groupName);
                }
            }
        }
    }

    /**
     * Create a hashcode based on the includes/excludes groups.
     *
     * @return String hascode for this instance
     */
    private String calcHashcode() {
        StringBuilder hash = new StringBuilder("__");
        for (IEntityGroup group : includes) {
            hash.append(group.getKey());
        }
        hash.append("^");
        for (IEntityGroup group : excludes) {
            hash.append(group.getKey());
        }
        hash.append("_#");
        return hash.toString();
    }

    @Override
    public boolean test(IPerson person) {
        String personHash = person.getEntityIdentifier().getKey() + hashcode + Thread.currentThread().getId();
        logger.debug(personHash);
        if (currentPersons.contains(personHash))
            return false; // stop recursing
        currentPersons.add(personHash);

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

        currentPersons.remove(personHash);
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
        return GroupService.getEntity(personKey, IPerson.class);
    }
}
