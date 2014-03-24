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
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PersonalAttributeGroup;
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PersonalAttributeGroup.PersonalAttributeGroupMembers;
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PersonalAttributeGroup.PersonalAttributeGroupTestGroups;
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PersonalAttributeGroup.PersonalAttributeGroupTestGroups.PersonalAttributeGroupTestGroup;
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PersonalAttributeGroup.PersonalAttributeGroupTestGroups.PersonalAttributeGroupTestGroup.PersonalAttributeGroupTest;
import org.jasig.portal.pags.dao.IPersonAttributeGroupDefinitionDao;
import org.jasig.portal.pags.dao.IPersonAttributeGroupStoreDefinitionDao;
import org.jasig.portal.pags.dao.IPersonAttributeGroupTestDefinitionDao;
import org.jasig.portal.pags.dao.IPersonAttributeGroupTestGroupDefinitionDao;
import org.jasig.portal.pags.dao.jpa.PersonAttributeGroupDefinitionImpl;
import org.jasig.portal.pags.dao.jpa.PersonAttributeGroupStoreDefinitionImpl;
import org.jasig.portal.pags.dao.jpa.PersonAttributeGroupTestDefinitionImpl;
import org.jasig.portal.pags.dao.jpa.PersonAttributeGroupTestGroupDefinitionImpl;
import org.jasig.portal.pags.om.IPersonAttributeGroupDefinition;
import org.jasig.portal.pags.om.IPersonAttributeGroupStoreDefinition;
import org.jasig.portal.pags.om.IPersonAttributeGroupTestGroupDefinition;
import org.jasig.portal.utils.SafeFilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private boolean errorOnChannel = true;

    @Value("${org.jasig.portal.io.errorOnChannel}")
    public void setErrorOnChannel(boolean errorOnChannel) {
        this.errorOnChannel = errorOnChannel;
    }

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
        final List<PersonAttributeGroupStoreDefinitionImpl> pagStores = this.personAttributeGroupStoreDefinitionDao.getPersonAttributeGroupStoreDefinitions();
        
        return Lists.transform(pagStores, new Function<PersonAttributeGroupStoreDefinitionImpl, IPortalData>() {
            /* (non-Javadoc)
             * @see com.google.common.base.Function#apply(java.lang.Object)
             */
            @Override
            public IPortalData apply(PersonAttributeGroupStoreDefinitionImpl pagStores) {
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
        final List<PersonAttributeGroupStoreDefinitionImpl> pagStores = this.personAttributeGroupStoreDefinitionDao.getPersonAttributeGroupStoreDefinitions();
        for(PersonAttributeGroupStoreDefinitionImpl store : pagStores) {
            personAttributeGroupStoreDefinitionDao.deletePersonAttributeGroupStoreDefinition(store);
        }
        String name = groupStoreRep.getStoreName();
        String description = groupStoreRep.getStoreDescription();
        logStoreInfo(name, description);
        IPersonAttributeGroupStoreDefinition personAttributeGroupStoreDefinition = 
                personAttributeGroupStoreDefinitionDao.createPersonAttributeGroupStoreDefinition(name, description);
        List<PersonalAttributeGroup> groups = groupStoreRep.getPersonalAttributeGroups();
        importGroups(personAttributeGroupStoreDefinition, groups);
        // Circle back and populate the members of groups, now that all the groups have been created
        importGroupMembers(groups);
    }

    private void importGroups(IPersonAttributeGroupStoreDefinition personAttributeGroupStoreDefinition, List<PersonalAttributeGroup> groups) {
        for(PersonalAttributeGroup group: groups) {
            String groupName = group.getGroupName();
            String groupDescription = group.getGroupDescription();
            logGroupsInfo(group, groupName);
            IPersonAttributeGroupDefinition personAttributeGroupDefinition = 
                    personAttributeGroupDefinitionDao.createPersonAttributeGroupDefinition((PersonAttributeGroupStoreDefinitionImpl)personAttributeGroupStoreDefinition, 
                                                                                            groupName, 
                                                                                            groupDescription);
            PersonalAttributeGroupTestGroups selectionTest = group.getPersonalAttributeGroupTestGroups();
            if (null != selectionTest) {
                logger.trace("------- Test Groups -------");
                importTestGroups(personAttributeGroupDefinition, selectionTest);
            }
        }
    }

    private void importTestGroups(IPersonAttributeGroupDefinition personAttributeGroupDefinition, PersonalAttributeGroupTestGroups personalAttributeGroupTestGroups) {
        List<PersonalAttributeGroupTestGroup> testGroups = personalAttributeGroupTestGroups.getPersonalAttributeGroupTestGroups();
        for(PersonalAttributeGroupTestGroup testGroup: testGroups) {
            String testGroupName = testGroup.getTestGroupName();
            String testGroupDescription = testGroup.getTestGroupDescription();
            logTestGroupsInfo(testGroupName, testGroupDescription);
            IPersonAttributeGroupTestGroupDefinition createdTestGroup =
                    personAttributeGroupTestGroupDefinitionDao.createPersonAttributeGroupTestGroupDefinition((PersonAttributeGroupDefinitionImpl) personAttributeGroupDefinition,
                                                                                                             testGroupName,
                                                                                                             testGroupDescription);
            List<PersonalAttributeGroupTest> tests = testGroup.getPersonalAttributeGroupTests();
            importTests(createdTestGroup, tests);
        }
    }

    private void importTests(IPersonAttributeGroupTestGroupDefinition createdTestGroup, List<PersonalAttributeGroupTest> tests) {
        for(PersonalAttributeGroupTest test: tests) {
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
    
    private void importGroupMembers(List<PersonalAttributeGroup> groups) {
        for(PersonalAttributeGroup group: groups) {
            PersonalAttributeGroupMembers members = group.getPersonalAttributeGroupMembers();
            if (null != members) {
                List<PersonAttributeGroupDefinitionImpl> parentPag =
                        personAttributeGroupDefinitionDao.getPersonAttributeGroupDefinitionByName(group.getGroupName());
                List<PersonAttributeGroupDefinitionImpl> membersToSave = new ArrayList<PersonAttributeGroupDefinitionImpl>();
                List<String> memberKeys = members.getMemberKeies();
                logger.trace("------ Members -------");
                for(String memberKey: memberKeys) {
                    logger.trace("Member key: {}", memberKey);
                    List<PersonAttributeGroupDefinitionImpl> memberPag =
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
    
    private void logGroupsInfo(PersonalAttributeGroup group, String groupKey) {
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
        final List<PersonAttributeGroupStoreDefinitionImpl> personAttributeGroupStoreDefinitionImpl = this.personAttributeGroupStoreDefinitionDao.getPersonAttributeGroupStoreDefinitionByName(name);
        if (personAttributeGroupStoreDefinitionImpl != null && personAttributeGroupStoreDefinitionImpl.size() > 0) {
            return convert(personAttributeGroupStoreDefinitionImpl.get(0));
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
    
    protected ExternalGroupStoreDefinition convert(final PersonAttributeGroupStoreDefinitionImpl pags) {
        if (pags == null) {
            return null;
        }
        ExternalGroupStoreDefinition externalGroupStoreDefinition = new ExternalGroupStoreDefinition();
        externalGroupStoreDefinition.setStoreName(pags.getName());
        externalGroupStoreDefinition.setStoreDescription(pags.getDescription());
        
        addExternalGroups(pags, externalGroupStoreDefinition);
        
        return externalGroupStoreDefinition;
    }

    private void addExternalGroups(final PersonAttributeGroupStoreDefinitionImpl pags,
                                   ExternalGroupStoreDefinition externalGroupStoreDefinition) {
        List<PersonalAttributeGroup> externalPags = externalGroupStoreDefinition.getPersonalAttributeGroups();
        List<PersonAttributeGroupDefinitionImpl> pagsGroups = pags.getGroups();
        for(PersonAttributeGroupDefinitionImpl pag : pagsGroups) {
            PersonalAttributeGroup externalPag = new PersonalAttributeGroup();
            externalPag.setGroupName(pag.getName());
            externalPag.setGroupKey(pag.getName());
            externalPag.setGroupDescription(pag.getDescription());
            
            List<PersonAttributeGroupDefinitionImpl> members = pag.getMembers();
            if (members.size() > 0) {
                addExternalGroupMembers(externalPag, members);
            }
            
            List<PersonAttributeGroupTestGroupDefinitionImpl> testGroups = pag.getTestGroups();
            if (testGroups.size() > 0) {
                addExternalTestGroupsWrapper(externalPag, testGroups);
            }
            externalPags.add(externalPag);
        }
    }

    private void addExternalGroupMembers(PersonalAttributeGroup externalPag, List<PersonAttributeGroupDefinitionImpl> members) {
        PersonalAttributeGroupMembers pagMembers = new PersonalAttributeGroupMembers();
        List<String> externalMembers = pagMembers.getMemberKeies();
        for(PersonAttributeGroupDefinitionImpl member: members) {
            externalMembers.add(member.getName());
        }
        externalPag.setPersonalAttributeGroupMembers(pagMembers);
    }

    private void addExternalTestGroupsWrapper(PersonalAttributeGroup externalPag, List<PersonAttributeGroupTestGroupDefinitionImpl> testGroups) {
        PersonalAttributeGroupTestGroups externalPagTestGroups = new PersonalAttributeGroupTestGroups();
        List<PersonalAttributeGroupTestGroup> externalPagTestGroupsHolder = externalPagTestGroups.getPersonalAttributeGroupTestGroups();
        addExternalTestGroupObjects(testGroups, externalPagTestGroupsHolder);
        externalPag.setPersonalAttributeGroupTestGroups(externalPagTestGroups);
    }

    private void addExternalTestGroupObjects(List<PersonAttributeGroupTestGroupDefinitionImpl> testGroups,
                                             List<PersonalAttributeGroupTestGroup> externalPagTestGroupsHolder) {
        for(PersonAttributeGroupTestGroupDefinitionImpl testGroup: testGroups) {
            PersonalAttributeGroupTestGroup externalPagTestGroup = new PersonalAttributeGroupTestGroup();
            externalPagTestGroup.setTestGroupName(testGroup.getName());
            externalPagTestGroup.setTestGroupDescription(testGroup.getDescription());
            List<PersonAttributeGroupTestDefinitionImpl> pagTests = testGroup.getTests();
            List<PersonalAttributeGroupTest> personalAttributeGroupTests = externalPagTestGroup.getPersonalAttributeGroupTests();
            addExternalTestObjects(pagTests, personalAttributeGroupTests);
            externalPagTestGroupsHolder.add(externalPagTestGroup);
        }
    }

    private void addExternalTestObjects(List<PersonAttributeGroupTestDefinitionImpl> pagTests,
                                        List<PersonalAttributeGroupTest> personalAttributeGroupTests) {
        for(PersonAttributeGroupTestDefinitionImpl pagTest: pagTests) {
            PersonalAttributeGroupTest externalPagTest = new PersonalAttributeGroupTest();
            String testName = (pagTest.getName() == null) ? "" : pagTest.getName();
            String testDescription = (pagTest.getDescription() == null) ? "" : pagTest.getDescription();
            externalPagTest.setTestName(testName);
            externalPagTest.setTestDescription(testDescription);
            externalPagTest.setAttributeName(pagTest.getAttributeName());
            externalPagTest.setTesterClass(pagTest.getTesterClassName());
            externalPagTest.setTestValue(pagTest.getTestValue());
            personalAttributeGroupTests.add(externalPagTest);
        }
    }

}
