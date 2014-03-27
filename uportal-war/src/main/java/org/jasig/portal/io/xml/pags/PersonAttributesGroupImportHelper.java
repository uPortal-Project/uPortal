/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.io.xml.pags;

import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.pags.dao.IPersonAttributesGroupDefinitionDao;
import org.jasig.portal.pags.dao.IPersonAttributesGroupStoreDefinitionDao;
import org.jasig.portal.pags.dao.IPersonAttributesGroupTestDefinitionDao;
import org.jasig.portal.pags.dao.IPersonAttributesGroupTestGroupDefinitionDao;
import org.jasig.portal.pags.om.IPersonAttributesGroupDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupStoreDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupTestDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupTestGroupDefinition;

/**
 * Helper class called from crn to handle the object creation and updates during PAGS import
 * 
 * @author Shawn Connolly, sconnolly@unicon.net
 */
public class PersonAttributesGroupImportHelper {
    private IPersonAttributesGroupStoreDefinitionDao personAttributesGroupStoreDefinitionDao;
    private IPersonAttributesGroupDefinitionDao personAttributesGroupDefinitionDao;
    private IPersonAttributesGroupTestDefinitionDao personAttributesGroupTestDefinitionDao;
    private IPersonAttributesGroupTestGroupDefinitionDao personAttributesGroupTestGroupDefinitionDao;

    public PersonAttributesGroupImportHelper(
            IPersonAttributesGroupStoreDefinitionDao personAttributesGroupStoreDefinitionDao,
            IPersonAttributesGroupDefinitionDao personAttributesGroupDefinitionDao,
            IPersonAttributesGroupTestDefinitionDao personAttributesGroupTestDefinitionDao,
            IPersonAttributesGroupTestGroupDefinitionDao personAttributesGroupTestGroupDefinitionDao) {
        super();
        this.personAttributesGroupStoreDefinitionDao = personAttributesGroupStoreDefinitionDao;
        this.personAttributesGroupDefinitionDao = personAttributesGroupDefinitionDao;
        this.personAttributesGroupTestDefinitionDao = personAttributesGroupTestDefinitionDao;
        this.personAttributesGroupTestGroupDefinitionDao = personAttributesGroupTestGroupDefinitionDao;
    }

    public IPersonAttributesGroupDefinition addGroup(String name, String description) {
        IPersonAttributesGroupStoreDefinition store = getOrCreateStore();
        return getOrCreateGroup(store, name, description);
    }

    private IPersonAttributesGroupStoreDefinition getOrCreateStore() {
        List<IPersonAttributesGroupStoreDefinition> stores = personAttributesGroupStoreDefinitionDao.getPersonAttributesGroupStoreDefinitions();
        if (stores.size() == 0) {
            return personAttributesGroupStoreDefinitionDao.createPersonAttributesGroupStoreDefinition("Default", "Default Person Attribute Group Store");
        } else {
            return stores.get(0);
        }
    }
    
    private IPersonAttributesGroupDefinition getOrCreateGroup(IPersonAttributesGroupStoreDefinition store, String name, String description) {
        List<IPersonAttributesGroupDefinition> groups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(name);
        if (groups.size() == 0) {
            return personAttributesGroupDefinitionDao.createPersonAttributesGroupDefinition(store, name, description);
        } else {
            groups.get(0).setDescription(description);
            personAttributesGroupDefinitionDao.updatePersonAttributesGroupDefinition(groups.get(0));
            return groups.get(0);
        }
    }
    
    public IPersonAttributesGroupTestGroupDefinition addTestGroup(String groupName, String name, String description) {
        List<IPersonAttributesGroupDefinition> groups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(groupName);
        List<IPersonAttributesGroupTestGroupDefinition> testGroups = personAttributesGroupTestGroupDefinitionDao.getPersonAttributesGroupTestGroupDefinitionByName(name);
        if (testGroups.size() == 0) {
            return personAttributesGroupTestGroupDefinitionDao.createPersonAttributesGroupTestGroupDefinition(groups.get(0), name, description);
        } else {
            testGroups.get(0).setDescription(description);
            personAttributesGroupTestGroupDefinitionDao.updatePersonAttributesGroupTestGroupDefinition(testGroups.get(0));
            return testGroups.get(0);
        }
    }
    
    public void addTest(IPersonAttributesGroupTestGroupDefinition testGroup,
                             String name,
                             String description,
                             String attributeName,
                             String testerClass,
                             String testValue) {

        List<IPersonAttributesGroupTestDefinition> tests = personAttributesGroupTestDefinitionDao.getPersonAttributesGroupTestDefinitionByName(name);
        if (tests.size() == 0) {
            personAttributesGroupTestDefinitionDao.createPersonAttributesGroupTestDefinition(testGroup, name, description, attributeName, testerClass, testValue);
        } else {
            IPersonAttributesGroupTestDefinition test = tests.get(0);
            test.setDescription(description);
            test.setAttributeName(attributeName);
            test.setTesterClassName(testerClass);
            test.setTestValue(testValue);
            personAttributesGroupTestDefinitionDao.updatePersonAttributesGroupTestDefinition(test);
        }
    }
    
    public void addGroupMember(String groupName, String member) {
        List<IPersonAttributesGroupDefinition> groups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(groupName);
        List<IPersonAttributesGroupDefinition> attemptingToAddMembers = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(member);
        if (groups.isEmpty() || attemptingToAddMembers.isEmpty()) {
            throw new RuntimeException("Group: " + groupName + " or member: " + member + " does not exist");
        } else {
            IPersonAttributesGroupDefinition group = groups.get(0);
            IPersonAttributesGroupDefinition attemptingToAddMember = attemptingToAddMembers.get(0);
            List<IPersonAttributesGroupDefinition> groupMembers = group.getMembers();
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
        List<IPersonAttributesGroupDefinition> groups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(groupName);
        IPersonAttributesGroupDefinition group = groups.get(0);
        group.setMembers(new ArrayList<IPersonAttributesGroupDefinition>(0));
        personAttributesGroupDefinitionDao.updatePersonAttributesGroupDefinition(group);
    }
    
    public void dropTestGroupsAndTests(String groupName) {
        List<IPersonAttributesGroupDefinition> groups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(groupName);
        IPersonAttributesGroupDefinition group = groups.get(0);
        List<IPersonAttributesGroupTestGroupDefinition> testGroups = group.getTestGroups();
        // Disconnect the test groups
        group.setTestGroups(new ArrayList<IPersonAttributesGroupTestGroupDefinition>());
        personAttributesGroupDefinitionDao.updatePersonAttributesGroupDefinition(group);
        // Cascade the test group delete to the tests
        for (IPersonAttributesGroupTestGroupDefinition testGroup : testGroups) {
            personAttributesGroupTestGroupDefinitionDao.deletePersonAttributesGroupTestGroupDefinition(testGroup);
        }
    }
}
