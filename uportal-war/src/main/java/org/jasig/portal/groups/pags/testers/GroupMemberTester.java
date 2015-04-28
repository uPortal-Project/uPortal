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

import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.pags.IPersonTester;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * PAG Tester for membership in another group.
 *
 * @author Benito J. Gonzalez
 * @version $Revision$
 */
public class GroupMemberTester implements IPersonTester {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    /* public since they are final and set in the constructor. */
    public final String groupName;
    public final boolean exclude;

    public GroupMemberTester(String attribute, String value) {
        logger.debug("constructor attribute: ", attribute);
        logger.debug("constructor value: ", value);
        String attributeLower = attribute.toLowerCase();
        this.exclude = "non-member".equals(attributeLower);
        if (!this.exclude && !"member".equals(attributeLower))
            logger.warn("Group member test attribute should be either 'member' or 'non-member'");
        //if (groupExists(value))
            this.groupName = value;
        //else
        //    throw new IllegalArgumentException("Unknown group name specified in PAG Group Member Tester: " + value);
    }

    @Override
    public boolean test(IPerson person) {
        Set<String> personGroups = getPersonGroupMembership(person);
        logger.warn(personGroups.toString());
        return personGroups.contains(groupName) ^ exclude;
    }

    @SuppressWarnings("unchecked")
    protected Set<String> getPersonGroupMembership(IPerson person) {
        String personKey = person.getEntityIdentifier().getKey();
        logger.warn(personKey);
        IGroupMember member = GroupService.getEntity(personKey, IPerson.class);
        logger.warn(member.toString());
        Iterator<IGroupMember> iterator = (Iterator<IGroupMember>) member.getAllContainingGroups();
        Set<String> groupNames = new HashSet<String>();
        while (iterator.hasNext()) {
            String groupKey = iterator.next().getKey();
            logger.debug("Person {}/{} is a member of group {}", person.getUserName(), personKey, groupKey);
            groupNames.add(groupKey);
        }
        return groupNames;

    }

    protected boolean groupExists(String groupName) {
        return (GroupService.getDistinguishedGroup(groupName) != null);
    }
}
