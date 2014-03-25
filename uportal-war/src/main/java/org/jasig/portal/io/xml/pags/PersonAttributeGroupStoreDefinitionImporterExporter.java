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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jasig.portal.io.xml.AbstractJaxbDataHandler;
import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;
import org.jasig.portal.io.xml.SimpleStringPortalData;
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PersonAttributeGroup;
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PersonAttributeGroup.PersonAttributeGroupMembers;
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PersonAttributeGroup.PersonAttributeGroupTestGroups;
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PersonAttributeGroup.PersonAttributeGroupTestGroups.PersonAttributeGroupTestGroup;
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PersonAttributeGroup.PersonAttributeGroupTestGroups.PersonAttributeGroupTestGroup.PersonAttributeGroupTest;
import org.jasig.portal.pags.dao.IPersonAttributeGroupDefinitionDao;
import org.jasig.portal.pags.dao.IPersonAttributeGroupStoreDefinitionDao;
import org.jasig.portal.pags.dao.IPersonAttributeGroupTestDefinitionDao;
import org.jasig.portal.pags.dao.IPersonAttributeGroupTestGroupDefinitionDao;
import org.jasig.portal.pags.dao.jpa.PersonAttributeGroupDefinitionImpl;
import org.jasig.portal.pags.dao.jpa.PersonAttributeGroupStoreDefinitionImpl;
import org.jasig.portal.pags.dao.jpa.PersonAttributeGroupTestGroupDefinitionImpl;
import org.jasig.portal.pags.om.IPersonAttributeGroupDefinition;
import org.jasig.portal.pags.om.IPersonAttributeGroupStoreDefinition;
import org.jasig.portal.pags.om.IPersonAttributeGroupTestDefinition;
import org.jasig.portal.pags.om.IPersonAttributeGroupTestGroupDefinition;
import org.jasig.portal.utils.SafeFilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * @author Shawn Connolly, sconnolly@unicon.net
 */
public class PersonAttributeGroupStoreDefinitionImporterExporter 
        extends AbstractJaxbDataHandler<ExternalGroupStoreDefinition> {

    private PersonAttributeGroupStorePortalDataType personAttributeGroupStorePortalDataType;
    private IPersonAttributeGroupStoreDefinitionDao personAttributeGroupStoreDefinitionDao;
    private IPersonAttributeGroupDefinitionDao personAttributeGroupDefinitionDao;
    private IPersonAttributeGroupTestDefinitionDao personAttributeGroupTestDefinitionDao;
    private IPersonAttributeGroupTestGroupDefinitionDao personAttributeGroupTestGroupDefinitionDao;
    private Logger logger = LoggerFactory.getLogger(PersonAttributeGroupStoreDefinitionImporterExporter.class);

    @Autowired
    public void setpersonAttributeGroupStorePortalDataType(PersonAttributeGroupStorePortalDataType personAttributeGroupStorePortalDataType) {
        this.personAttributeGroupStorePortalDataType = personAttributeGroupStorePortalDataType;
    }

    @Autowired
    public void setPersonAttributeGroupStoreDefinitionDao(IPersonAttributeGroupStoreDefinitionDao personAttributeGroupStoreDefinitionDao) {
        this.personAttributeGroupStoreDefinitionDao = personAttributeGroupStoreDefinitionDao;
    }

    @Autowired
    public void setPersonAttributeGroupDefinitionDao(IPersonAttributeGroupDefinitionDao personAttributeGroupDefinitionDao) {
        this.personAttributeGroupDefinitionDao = personAttributeGroupDefinitionDao;
    }

    @Autowired
    public void setPersonAttributeGroupTestDefinitionDao(IPersonAttributeGroupTestDefinitionDao personAttributeGroupTestDefinitionDao) {
        this.personAttributeGroupTestDefinitionDao = personAttributeGroupTestDefinitionDao;
    }

    @Autowired
    public void setPersonAttributeGroupTestGroupDefinitionDao(IPersonAttributeGroupTestGroupDefinitionDao personAttributeGroupTestGroupDefinitionDao) {
        this.personAttributeGroupTestGroupDefinitionDao = personAttributeGroupTestGroupDefinitionDao;
    }

    @Override
    public Set<PortalDataKey> getImportDataKeys() {
        return new HashSet<PortalDataKey>(personAttributeGroupStorePortalDataType.getDataKeyImportOrder());
    }

    @Override
    public Iterable<? extends IPortalData> getPortalData() {
        final List<IPersonAttributeGroupStoreDefinition> pagStores = this.personAttributeGroupStoreDefinitionDao.getPersonAttributeGroupStoreDefinitions();
        
        return Lists.transform(pagStores, new Function<IPersonAttributeGroupStoreDefinition, IPortalData>() {
            /* (non-Javadoc)
             * @see com.google.common.base.Function#apply(java.lang.Object)
             */
            @Override
            public IPortalData apply(IPersonAttributeGroupStoreDefinition pagStores) {
                return new SimpleStringPortalData(
                        pagStores.getName(),
                        pagStores.getName(),
                        pagStores.getDescription());
            }
        });
    }

    /**
     * Entry point for importing of data
     */
    @Transactional
    @Override
    public void importData(ExternalGroupStoreDefinition groupStoreRep) {
        final List<IPersonAttributeGroupStoreDefinition> pagStores = this.personAttributeGroupStoreDefinitionDao.getPersonAttributeGroupStoreDefinitions();
        for(IPersonAttributeGroupStoreDefinition store : pagStores) {
            personAttributeGroupStoreDefinitionDao.deletePersonAttributeGroupStoreDefinition(store);
        }
        String name = groupStoreRep.getStoreName();
        String description = groupStoreRep.getStoreDescription();
        logStoreInfo(name, description);
        IPersonAttributeGroupStoreDefinition personAttributeGroupStoreDefinition = 
                personAttributeGroupStoreDefinitionDao.createPersonAttributeGroupStoreDefinition(name, description);
        List<PersonAttributeGroup> groups = groupStoreRep.getPersonAttributeGroups();
        importGroups(personAttributeGroupStoreDefinition, groups);
        // Circle back and populate the members of groups, now that all the groups have been created
        importGroupMembers(groups);
    }

    private void importGroups(IPersonAttributeGroupStoreDefinition personAttributeGroupStoreDefinition, List<PersonAttributeGroup> groups) {
        for(PersonAttributeGroup group: groups) {
            String groupName = group.getGroupName();
            String groupDescription = group.getGroupDescription();
            logGroupsInfo(group, groupName);
            IPersonAttributeGroupDefinition personAttributeGroupDefinition = 
                    personAttributeGroupDefinitionDao.createPersonAttributeGroupDefinition((PersonAttributeGroupStoreDefinitionImpl)personAttributeGroupStoreDefinition, 
                                                                                            groupName, 
                                                                                            groupDescription);
            PersonAttributeGroupTestGroups selectionTest = group.getPersonAttributeGroupTestGroups();
            if (null != selectionTest) {
                logger.trace("------- Test Groups -------");
                importTestGroups(personAttributeGroupDefinition, selectionTest);
            }
        }
    }

    private void importTestGroups(IPersonAttributeGroupDefinition personAttributeGroupDefinition, PersonAttributeGroupTestGroups PersonAttributeGroupTestGroups) {
        List<PersonAttributeGroupTestGroup> testGroups = PersonAttributeGroupTestGroups.getPersonAttributeGroupTestGroups();
        for(PersonAttributeGroupTestGroup testGroup: testGroups) {
            String testGroupName = testGroup.getTestGroupName();
            String testGroupDescription = testGroup.getTestGroupDescription();
            logTestGroupsInfo(testGroupName, testGroupDescription);
            IPersonAttributeGroupTestGroupDefinition createdTestGroup =
                    personAttributeGroupTestGroupDefinitionDao.createPersonAttributeGroupTestGroupDefinition((PersonAttributeGroupDefinitionImpl) personAttributeGroupDefinition,
                                                                                                             testGroupName,
                                                                                                             testGroupDescription);
            List<PersonAttributeGroupTest> tests = testGroup.getPersonAttributeGroupTests();
            importTests(createdTestGroup, tests);
        }
    }

    private void importTests(IPersonAttributeGroupTestGroupDefinition createdTestGroup, List<PersonAttributeGroupTest> tests) {
        for(PersonAttributeGroupTest test: tests) {
            String attributeName = test.getAttributeName();
            String testerClass = test.getTesterClass();
            String testValue = test.getTestValue();
            logTestInfo(attributeName, testerClass, testValue);
            personAttributeGroupTestDefinitionDao.createPersonAttributeGroupTestDefinition((PersonAttributeGroupTestGroupDefinitionImpl)createdTestGroup,
                                                                                           attributeName,
                                                                                           testerClass,
                                                                                           testValue);
        }
    }
    
    private void importGroupMembers(List<PersonAttributeGroup> groups) {
        for(PersonAttributeGroup group: groups) {
            PersonAttributeGroupMembers members = group.getPersonAttributeGroupMembers();
            if (null != members) {
                List<IPersonAttributeGroupDefinition> parentPag =
                        personAttributeGroupDefinitionDao.getPersonAttributeGroupDefinitionByName(group.getGroupName());
                List<IPersonAttributeGroupDefinition> membersToSave = new ArrayList<IPersonAttributeGroupDefinition>();
                List<String> memberKeys = members.getMemberKeies();
                logger.trace("------ Members -------");
                for(String memberKey: memberKeys) {
                    logger.trace("Member key: {}", memberKey);
                    List<IPersonAttributeGroupDefinition> memberPag =
                            personAttributeGroupDefinitionDao.getPersonAttributeGroupDefinitionByName(memberKey);
                    membersToSave.add(memberPag.get(0));
                }
                parentPag.get(0).setMembers(membersToSave);
                personAttributeGroupDefinitionDao.updatePersonAttributeGroupDefinition(parentPag.get(0));
            }
        }
        
    }
    
    private void logStoreInfo(String name, String description) {
        logger.trace("Store Name: {}", name);
        logger.trace("Store Description: {}", description);
    }
    
    private void logGroupsInfo(PersonAttributeGroup group, String groupKey) {
        logger.trace("------ Group ------");
        logger.trace("Group Key: {}", groupKey);
        logger.trace("Group Name: {}", group.getGroupName());
        logger.trace("Group Description: {}", group.getGroupDescription());
    }
    
    private void logTestGroupsInfo(String testGroupName, String testGroupDescription) {
        logger.trace("Test Group Name: {}", testGroupName);
        logger.trace("Test Group Description: {}", testGroupDescription);
    }
    
    private void logTestInfo(String attributeName, String testerClass, String testValue) {
        logger.trace("Test Attribute Name: {}", attributeName);
        logger.trace("Tester Class: {}", testerClass);
        logger.trace("Test Value: {}", testValue);
    }
    
    @Override
    public IPortalDataType getPortalDataType() {
        return personAttributeGroupStorePortalDataType;
    }

    @Override
    public ExternalGroupStoreDefinition exportData(String name) {
        final List<IPersonAttributeGroupStoreDefinition> personAttributeGroupStoreDefinition = this.personAttributeGroupStoreDefinitionDao.getPersonAttributeGroupStoreDefinitionByName(name);
        if (personAttributeGroupStoreDefinition != null && personAttributeGroupStoreDefinition.size() > 0) {
            return convert(personAttributeGroupStoreDefinition.get(0));
        } else {
            return null;
        }
    }

    @Override
    public String getFileName(ExternalGroupStoreDefinition data) {
        return SafeFilenameUtils.makeSafeFilename(data.getStoreName());
    }

    @Override
    public ExternalGroupStoreDefinition deleteData(String id) {
        throw new UnsupportedOperationException();
    }
    
    protected ExternalGroupStoreDefinition convert(final IPersonAttributeGroupStoreDefinition pags) {
        if (pags == null) {
            return null;
        }
        ExternalGroupStoreDefinition externalGroupStoreDefinition = new ExternalGroupStoreDefinition();
        externalGroupStoreDefinition.setStoreName(pags.getName());
        externalGroupStoreDefinition.setStoreDescription(pags.getDescription());
        
        addExternalGroups(pags, externalGroupStoreDefinition);
        
        return externalGroupStoreDefinition;
    }

    private void addExternalGroups(final IPersonAttributeGroupStoreDefinition pags,
                                   ExternalGroupStoreDefinition externalGroupStoreDefinition) {
        List<PersonAttributeGroup> externalPags = externalGroupStoreDefinition.getPersonAttributeGroups();
        List<IPersonAttributeGroupDefinition> pagsGroups = pags.getGroups();
        for(IPersonAttributeGroupDefinition pag : pagsGroups) {
            PersonAttributeGroup externalPag = new PersonAttributeGroup();
            externalPag.setGroupName(pag.getName());
            externalPag.setGroupKey(pag.getName());
            externalPag.setGroupDescription(pag.getDescription());
            
            List<IPersonAttributeGroupDefinition> members = pag.getMembers();
            if (members.size() > 0) {
                addExternalGroupMembers(externalPag, members);
            }
            
            List<IPersonAttributeGroupTestGroupDefinition> testGroups = pag.getTestGroups();
            if (testGroups.size() > 0) {
                addExternalTestGroupsWrapper(externalPag, testGroups);
            }
            externalPags.add(externalPag);
        }
    }

    private void addExternalGroupMembers(PersonAttributeGroup externalPag, List<IPersonAttributeGroupDefinition> members) {
        PersonAttributeGroupMembers pagMembers = new PersonAttributeGroupMembers();
        List<String> externalMembers = pagMembers.getMemberKeies();
        for(IPersonAttributeGroupDefinition member: members) {
            externalMembers.add(member.getName());
        }
        externalPag.setPersonAttributeGroupMembers(pagMembers);
    }

    private void addExternalTestGroupsWrapper(PersonAttributeGroup externalPag, List<IPersonAttributeGroupTestGroupDefinition> testGroups) {
        PersonAttributeGroupTestGroups externalPagTestGroups = new PersonAttributeGroupTestGroups();
        List<PersonAttributeGroupTestGroup> externalPagTestGroupsHolder = externalPagTestGroups.getPersonAttributeGroupTestGroups();
        addExternalTestGroupObjects(testGroups, externalPagTestGroupsHolder);
        externalPag.setPersonAttributeGroupTestGroups(externalPagTestGroups);
    }

    private void addExternalTestGroupObjects(List<IPersonAttributeGroupTestGroupDefinition> testGroups,
                                             List<PersonAttributeGroupTestGroup> externalPagTestGroupsHolder) {
        for(IPersonAttributeGroupTestGroupDefinition testGroup: testGroups) {
            PersonAttributeGroupTestGroup externalPagTestGroup = new PersonAttributeGroupTestGroup();
            externalPagTestGroup.setTestGroupName(testGroup.getName());
            externalPagTestGroup.setTestGroupDescription(testGroup.getDescription());
            List<IPersonAttributeGroupTestDefinition> pagTests = testGroup.getTests();
            List<PersonAttributeGroupTest> PersonAttributeGroupTests = externalPagTestGroup.getPersonAttributeGroupTests();
            addExternalTestObjects(pagTests, PersonAttributeGroupTests);
            externalPagTestGroupsHolder.add(externalPagTestGroup);
        }
    }

    private void addExternalTestObjects(List<IPersonAttributeGroupTestDefinition> pagTests,
                                        List<PersonAttributeGroupTest> PersonAttributeGroupTests) {
        for(IPersonAttributeGroupTestDefinition pagTest: pagTests) {
            PersonAttributeGroupTest externalPagTest = new PersonAttributeGroupTest();
            String testName = (pagTest.getName() == null) ? "" : pagTest.getName();
            String testDescription = (pagTest.getDescription() == null) ? "" : pagTest.getDescription();
            externalPagTest.setTestName(testName);
            externalPagTest.setTestDescription(testDescription);
            externalPagTest.setAttributeName(pagTest.getAttributeName());
            externalPagTest.setTesterClass(pagTest.getTesterClassName());
            externalPagTest.setTestValue(pagTest.getTestValue());
            PersonAttributeGroupTests.add(externalPagTest);
        }
    }

}
