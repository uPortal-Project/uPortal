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
package org.jasig.portal.io.xml.pags;

import java.util.HashSet;
import java.util.Set;

import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupDefinitionDao;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestDefinitionDao;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinitionDao;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition;

/**
 * Helper class called from crn to handle the object creation and updates during PAGS import
 * 
 * @author Shawn Connolly, sconnolly@unicon.net
 */
public class PersonAttributesGroupImportHelper {
    private IPersonAttributesGroupDefinitionDao personAttributesGroupDefinitionDao;
    private IPersonAttributesGroupTestDefinitionDao personAttributesGroupTestDefinitionDao;
    private IPersonAttributesGroupTestGroupDefinitionDao personAttributesGroupTestGroupDefinitionDao;

    public PersonAttributesGroupImportHelper(
            IPersonAttributesGroupDefinitionDao personAttributesGroupDefinitionDao,
            IPersonAttributesGroupTestDefinitionDao personAttributesGroupTestDefinitionDao,
            IPersonAttributesGroupTestGroupDefinitionDao personAttributesGroupTestGroupDefinitionDao) {
        super();
        this.personAttributesGroupDefinitionDao = personAttributesGroupDefinitionDao;
        this.personAttributesGroupTestDefinitionDao = personAttributesGroupTestDefinitionDao;
        this.personAttributesGroupTestGroupDefinitionDao = personAttributesGroupTestGroupDefinitionDao;
    }

    public IPersonAttributesGroupDefinition addGroup(String name, String description) {
        return getOrCreateGroup(name, description);
    }

    private IPersonAttributesGroupDefinition getOrCreateGroup(String name, String description) {
        Set<IPersonAttributesGroupDefinition> groups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(name);
        if (groups.size() == 0) {
            return personAttributesGroupDefinitionDao.createPersonAttributesGroupDefinition(name, description);
        } else {
            IPersonAttributesGroupDefinition group = groups.iterator().next();
            group.setDescription(description);
            return personAttributesGroupDefinitionDao.updatePersonAttributesGroupDefinition(group);
        }
    }
    
    public IPersonAttributesGroupTestGroupDefinition addTestGroup(String groupName) {
        Set<IPersonAttributesGroupDefinition> groups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(groupName);
        return personAttributesGroupTestGroupDefinitionDao.createPersonAttributesGroupTestGroupDefinition(groups.iterator().next());
    }
    
    public void addTest(IPersonAttributesGroupTestGroupDefinition testGroup,
                             String attributeName,
                             String testerClass,
                             String testValue,
                             Set<String> includes,
                             Set<String> excludes) {
        personAttributesGroupTestDefinitionDao.createPersonAttributesGroupTestDefinition(
                testGroup, attributeName, testerClass, testValue, includes, excludes);
    }
    
    public void addGroupMember(String groupName, String member) {
        Set<IPersonAttributesGroupDefinition> groups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(groupName);
        Set<IPersonAttributesGroupDefinition> attemptingToAddMembers = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(member);
        if (groups.isEmpty() || attemptingToAddMembers.isEmpty()) {
            throw new RuntimeException("Group: " + groupName + " or member: " + member + " does not exist");
        } else {
            IPersonAttributesGroupDefinition group = groups.iterator().next();
            IPersonAttributesGroupDefinition attemptingToAddMember = attemptingToAddMembers.iterator().next();
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
        Set<IPersonAttributesGroupDefinition> groups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(groupName);
        IPersonAttributesGroupDefinition group = groups.iterator().next();
        group.setMembers(new HashSet<IPersonAttributesGroupDefinition>(0));
        personAttributesGroupDefinitionDao.updatePersonAttributesGroupDefinition(group);
    }
    
    public void dropTestGroupsAndTests(String groupName) {
        Set<IPersonAttributesGroupDefinition> groups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(groupName);
        IPersonAttributesGroupDefinition group = groups.iterator().next();
        Set<IPersonAttributesGroupTestGroupDefinition> testGroups = group.getTestGroups();
        // Disconnect the test groups
        group.setTestGroups(new HashSet<IPersonAttributesGroupTestGroupDefinition>());
        personAttributesGroupDefinitionDao.updatePersonAttributesGroupDefinition(group);
        // Cascade the test group delete to the tests
        for (IPersonAttributesGroupTestGroupDefinition testGroup : testGroups) {
            personAttributesGroupTestGroupDefinitionDao.deletePersonAttributesGroupTestGroupDefinition(testGroup);
        }
    }
}
