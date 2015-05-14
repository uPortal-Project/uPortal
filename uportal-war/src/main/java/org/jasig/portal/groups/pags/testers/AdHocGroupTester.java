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

import org.apache.commons.lang3.Validate;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.pags.IPersonTester;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Immutable PAGS Tester for inclusive/exclusive membership in sets of groups. To avoid infinite recursion,
 * calls to {@code test()} are tracked by parameters plus thread ID.
 *
 * @author  Benito J. Gonzalez <bgonzalez@unicon.net>
 * @see     org.jasig.portal.groups.pags.IPersonTester
 * @see     org.jasig.portal.groups.pags.dao.EntityPersonAttributesGroupStore
 * @see     org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition
 * @since   4.3
 */
public final class AdHocGroupTester implements IPersonTester {

    private static final Set<String> currentPersons = new ConcurrentSkipListSet<>();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Set<IEntityGroup> includes = new HashSet<>();
    private final Set<IEntityGroup> excludes = new HashSet<>();
    private final String groupsHash;

    public AdHocGroupTester(IPersonAttributesGroupTestDefinition definition) {
        Validate.notNull(definition.getIncludes());
        Validate.notNull(definition.getExcludes());
        this.includes.addAll(collectGroups(definition.getIncludes(), true));
        this.excludes.addAll(collectGroups(definition.getExcludes(), false));
        groupsHash = calcGroupsHash(definition.getIncludes(), definition.getExcludes());
    }

    /*
     * At some point, a person is being tested for group membership. During that test, the thread hits an ad hoc group
     * tester. When that tester calls isDeepMemberOf, a test for group membership is triggered. During this call stack,
     * the second call the the ad hoc group tester returns false. Assuming the group hierarchy is not itself recursive
     * for the group containing the ad hoc group test, the test returns a usable value.
     *
     * If there is no caching and the second person object only exists for the recursive call, then the implementation
     * works.
     *
     * Also, if the person object is cached and used twice, then the group key with the ad hoc tester is not added to
     * the containing group keys during the recursion but is added (or not) after the test call returns positive.
     */
    @Override
    public boolean test(IPerson person) {
        String personHash = person.getEntityIdentifier().getKey() + groupsHash + Thread.currentThread().getId();
        logger.debug("Entering test() for {}", personHash);
        if (currentPersons.contains(personHash)) {
            logger.warn("Returning from test() for {} due to recusion for person = {}", personHash, person.toString());
            return false; // stop recursing
        }
        currentPersons.add(personHash);

        IGroupMember gmPerson = findPersonAsGroupMember(person);
        boolean pass = true;

        Iterator<IEntityGroup> groups = includes.iterator();
        while (pass && groups.hasNext()) {
            IEntityGroup group = groups.next();
            logger.debug("Checking if {} is a member of {}", person.getUserName(), group.getName());
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
            logger.debug("Checking if {} is NOT a member of {}", person.getUserName(), group.getName());
            if (gmPerson.isDeepMemberOf(group)) {
                logger.debug("!! {} is a member of {}", person.getUserName(), group.getDescription());
                pass = false;
            } else {
                logger.debug("{} is not a member of {}", person.getUserName(), group.getDescription());
            }
        }

        currentPersons.remove(personHash);
        logger.debug("Returning from test() for {}", personHash);
        return pass;
    }

    /**
     * Given a set of group names, find the entity groups and add them to the {@code groups} collection to be returned.
     * If a group is not found, either log a warning or throw an {@code IllegalArgumentException} based on
     * the {@code throwOnFail} parameter.
     *
     * @param groupNames    set of group names to find
     * @param throwOnFail   flag to indicate whether to log or throw exception if a group is not found
     * @return              set of named groups from {@link GroupService}
     */
    private Set<IEntityGroup> collectGroups(Set<String> groupNames, boolean throwOnFail) {
        Set<IEntityGroup> groups = new HashSet<>();
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
        return groups;
    }

    /**
     * Create a hash based on the includes/excludes groups. This will be part of the call hash key
     * used to detect recursive calls to the same test (although this may be a different instance).
     * For consistency, the sets are sorted before the hash string is built.
     * <p/>
     * Format: _(_[include_group_names])*(^[exclude_group_names])*_#
     * Example: for includes = "Students"+"Active" and excludes = "Hackers", "__Active_Students^Hackers_#"
     *
     * @param includes      {@String} collection of group names for member of tests
     * @param excludes      {@String} collection of group names for NOT member of tests
     * @return              hash for this test based on groups parameters
     */
    private static String calcGroupsHash(Set<String> includes, Set<String> excludes) {
        Set<String> includesSorted = new TreeSet<>(includes);
        Set<String> excludesSorted = new TreeSet<>(excludes);
        StringBuilder hash = new StringBuilder("_");
        for (String group : includesSorted) {
            hash.append("_");
            hash.append(group);
        }
        for (String group : excludesSorted) {
            hash.append("^");
            hash.append(group);
        }
        hash.append("_#");
        return hash.toString();
    }

    /**
     * Find {@link IEntityGroup} from group name.
     *
     * @param groupName     name of group to search from {@code GroupService}
     * @return              {@code IEntityGroup} with given name or null if no group with given name found
     * @see                 org.jasig.portal.services.GroupService
     */
    private static IEntityGroup findGroupByName(String groupName) {
        EntityIdentifier[] identifiers = GroupService.searchForGroups(groupName, GroupService.IS, IPerson.class);
        for (EntityIdentifier entityIdentifier : identifiers) {
            if (entityIdentifier.getType().equals(IEntityGroup.class)) {
                return GroupService.findGroup(entityIdentifier.getKey());
            }
        }
        return null;
    }

    /**
     * Find {@link IPerson} as {@link IGroupMember}.
     *
     * @param person    {@code IPerson} with entity identifier key to look up
     * @return          person as {@code IGroupMember}
     * @see             org.jasig.portal.services.GroupService
     */
    private static IGroupMember findPersonAsGroupMember(IPerson person) {
        String personKey = person.getEntityIdentifier().getKey();
        IGroupMember personGM =  GroupService.getEntity(personKey, IPerson.class);
        return personGM;
    }
}
