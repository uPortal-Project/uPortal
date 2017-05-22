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
package org.apereo.portal.io.xml.pags;

import java.util.HashSet;
import java.util.Set;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinitionDao;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestDefinitionDao;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinitionDao;

/**
 * Helper class called from crn to handle the object creation and updates during PAGS import
 *
 */
public class PersonAttributesGroupImportHelper {
    private IPersonAttributesGroupDefinitionDao personAttributesGroupDefinitionDao;
    private IPersonAttributesGroupTestDefinitionDao personAttributesGroupTestDefinitionDao;
    private IPersonAttributesGroupTestGroupDefinitionDao
            personAttributesGroupTestGroupDefinitionDao;

    public PersonAttributesGroupImportHelper(
            IPersonAttributesGroupDefinitionDao personAttributesGroupDefinitionDao,
            IPersonAttributesGroupTestDefinitionDao personAttributesGroupTestDefinitionDao,
            IPersonAttributesGroupTestGroupDefinitionDao
                    personAttributesGroupTestGroupDefinitionDao) {
        super();
        this.personAttributesGroupDefinitionDao = personAttributesGroupDefinitionDao;
        this.personAttributesGroupTestDefinitionDao = personAttributesGroupTestDefinitionDao;
        this.personAttributesGroupTestGroupDefinitionDao =
                personAttributesGroupTestGroupDefinitionDao;
    }

    public IPersonAttributesGroupDefinition addGroup(String name, String description) {
        return getOrCreateGroup(name, description);
    }

    private IPersonAttributesGroupDefinition getOrCreateGroup(String name, String description) {
        Set<IPersonAttributesGroupDefinition> groups =
                personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(name);
        if (groups.size() == 0) {
            return personAttributesGroupDefinitionDao.createPersonAttributesGroupDefinition(
                    name, description);
        } else {
            IPersonAttributesGroupDefinition group = groups.iterator().next();
            group.setDescription(description);
            return personAttributesGroupDefinitionDao.updatePersonAttributesGroupDefinition(group);
        }
    }

    public IPersonAttributesGroupTestGroupDefinition addTestGroup(String groupName) {
        Set<IPersonAttributesGroupDefinition> groups =
                personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(
                        groupName);
        return personAttributesGroupTestGroupDefinitionDao
                .createPersonAttributesGroupTestGroupDefinition(groups.iterator().next());
    }

    public void addTest(
            IPersonAttributesGroupTestGroupDefinition testGroup,
            String attributeName,
            String testerClass,
            String testValue) {

        /*
         * For version 5.0, all uPortal sources were repackaged from 'org.jasig.portal'
         * to 'org.apereo.portal'.  *.pags-group.xml files exported from earlier
         * versions of uPortal will contain the old tester-class name.  We can detect that
         * and intervene here.
         */
        testerClass = testerClass.replace("org.jasig.portal", "org.apereo.portal");

        personAttributesGroupTestDefinitionDao.createPersonAttributesGroupTestDefinition(
                testGroup, attributeName, testerClass, testValue);
    }

    public void addGroupMember(String groupName, String member) {
        Set<IPersonAttributesGroupDefinition> groups =
                personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(
                        groupName);
        Set<IPersonAttributesGroupDefinition> attemptingToAddMembers =
                personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(member);
        if (groups.isEmpty() || attemptingToAddMembers.isEmpty()) {
            throw new RuntimeException(
                    "Group: " + groupName + " or member: " + member + " does not exist");
        } else {
            IPersonAttributesGroupDefinition group = groups.iterator().next();
            IPersonAttributesGroupDefinition attemptingToAddMember =
                    attemptingToAddMembers.iterator().next();
            Set<IPersonAttributesGroupDefinition> groupMembers = group.getMembers();
            for (IPersonAttributesGroupDefinition groupMember : groupMembers) {
                if (groupMember.getName().equalsIgnoreCase(attemptingToAddMember.getName())) {
                    return;
                }
            }
            groupMembers.add(attemptingToAddMember);
            group.setMembers(groupMembers);
            personAttributesGroupDefinitionDao.updatePersonAttributesGroupDefinition(group);
        }
    }

    public void dropGroupMembers(String groupName) {
        Set<IPersonAttributesGroupDefinition> groups =
                personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(
                        groupName);
        IPersonAttributesGroupDefinition group = groups.iterator().next();
        group.setMembers(new HashSet<IPersonAttributesGroupDefinition>(0));
        personAttributesGroupDefinitionDao.updatePersonAttributesGroupDefinition(group);
    }

    public void dropTestGroupsAndTests(String groupName) {
        Set<IPersonAttributesGroupDefinition> groups =
                personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(
                        groupName);
        IPersonAttributesGroupDefinition group = groups.iterator().next();
        Set<IPersonAttributesGroupTestGroupDefinition> testGroups = group.getTestGroups();
        // Disconnect the test groups
        group.setTestGroups(new HashSet<IPersonAttributesGroupTestGroupDefinition>());
        personAttributesGroupDefinitionDao.updatePersonAttributesGroupDefinition(group);
        // Cascade the test group delete to the tests
        for (IPersonAttributesGroupTestGroupDefinition testGroup : testGroups) {
            personAttributesGroupTestGroupDefinitionDao
                    .deletePersonAttributesGroupTestGroupDefinition(testGroup);
        }
    }
}
