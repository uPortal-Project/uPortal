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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.pags.IPersonTester;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.spring.locator.ApplicationContextLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Immutable PAGS Tester for inclusive/exclusive membership in sets of groups. To avoid infinite recursion,
 * calls to {@code test()} are tracked by parameters plus thread ID.
 * <p/>
 * {@code Attribute} should be either {@code group-member} or {@code not-group-member}.
 * <p/>
 * {@code Value} should be the group name as expected by {@code GroupService.searchForGroups}. {@code GroupService}
 * is searched every test since groups can be added without restarting uPortal.
 *
 * @author  Benito J. Gonzalez <bgonzalez@unicon.net>
 * @see     org.jasig.portal.groups.pags.IPersonTester
 * @see     org.jasig.portal.groups.pags.dao.EntityPersonAttributesGroupStore
 * @see     org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition
 * @see     org.jasig.portal.services.GroupService
 * @since   4.3
 */
public final class AdHocGroupTester implements IPersonTester {

    public static final String MEMBER_OF = "group-member";
    public static final String NOT_MEMBER_OF = "not-group-member";

    private final Cache currentTests;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String groupName;
    private final boolean isNotTest;
    private final String groupHash;

    public AdHocGroupTester(IPersonAttributesGroupTestDefinition definition) {
        assert(definition.getAttributeName().equals(MEMBER_OF) || definition.getAttributeName().equals(NOT_MEMBER_OF));
        this.isNotTest = (definition.getAttributeName().equals(NOT_MEMBER_OF));
        this.groupName = definition.getTestValue();
        this.groupHash = calcGroupHash(groupName, isNotTest);
        ApplicationContext context = ApplicationContextLocator.getApplicationContext();
        CacheManager cacheManager = context.getBean("cacheManager", CacheManager.class);
        this.currentTests = cacheManager.getCache("org.jasig.portal.groups.pags.testers.AdHocGroupTester");
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
        String personHash = person.getEntityIdentifier().getKey() + groupHash + Thread.currentThread().getId();
        logger.debug("Entering test() for {}", personHash);
        IEntityGroup entityGroup = findGroupByName(groupName);
        if (entityGroup == null) {
            logger.error("Group named '{}' in ad hoc group tester definition not found!!", groupName);
            return false;
        }
        IGroupMember gmPerson = findPersonAsGroupMember(person);
        if (currentTests.getQuiet(personHash) != null) {
            logger.debug("Returning from test() for {} due to recusion for person = {}", personHash, person.toString());
            return false; // stop recursing
        }
        Element cacheEl = new Element(personHash, personHash);
        currentTests.put(cacheEl);
        // method that potentially recurs
        boolean isPersonGroupMember = gmPerson.isDeepMemberOf(entityGroup);
        currentTests.remove(personHash);
        logger.debug("Returning from test() for {}", personHash);
        return isPersonGroupMember ^ isNotTest;
    }

    /**
     * Create a hash based on the group name and member-of/not-member-of test. This will be part of the call hash key
     * used to detect recursive calls to the same test (although this may be a different instance).
     * <p/>
     * Format for member-of test: _+{@code groupName}_#
     * Format for not-member-of test: _^{@code groupName}_#
     * Example for member-of Students: _+Students_#
     *
     * @param groupName     group name to hash
     * @param isNotTest     whether the test is for not-member-of
     * @return              hash for this test based on group name and test type parameters
     */
    private static String calcGroupHash(String groupName, boolean isNotTest) {
        return ( isNotTest ? "_^" : "_+" ) + groupName + "_#";
    }

    /**
     * Find {@link IEntityGroup} from group name.
     *
     * @param groupName     name of group to search from {@code GroupService}
     * @return              {@code IEntityGroup} with given name or null if no group with given name found
     * @see                 org.jasig.portal.services.GroupService#searchForEntities(String, int, Class)
     * @see                 org.jasig.portal.services.GroupService#findGroup(String)
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
     * @see             org.jasig.portal.services.GroupService#getEntity(String, Class)
     */
    private static IGroupMember findPersonAsGroupMember(IPerson person) {
        String personKey = person.getEntityIdentifier().getKey();
        return GroupService.getEntity(personKey, IPerson.class);
    }
}
