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
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PagsGroup;
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PagsGroup.Members;
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PagsGroup.SelectionTest;
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PagsGroup.SelectionTest.TestGroup;
import org.jasig.portal.io.xml.pags.ExternalGroupStoreDefinition.PagsGroup.SelectionTest.TestGroup.Test;
import org.jasig.portal.pags.dao.IPersonAttributesGroupDefinitionDao;
import org.jasig.portal.pags.dao.IPersonAttributesGroupStoreDefinitionDao;
import org.jasig.portal.pags.dao.IPersonAttributesGroupTestDefinitionDao;
import org.jasig.portal.pags.dao.IPersonAttributesGroupTestGroupDefinitionDao;
import org.jasig.portal.pags.dao.jpa.PersonAttributesGroupDefinitionImpl;
import org.jasig.portal.pags.dao.jpa.PersonAttributesGroupStoreDefinitionImpl;
import org.jasig.portal.pags.dao.jpa.PersonAttributesGroupTestGroupDefinitionImpl;
import org.jasig.portal.pags.om.IPersonAttributesGroupDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupStoreDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupTestDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupTestGroupDefinition;
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
public class PersonAttributesGroupStoreDefinitionImporterExporter 
        extends AbstractJaxbDataHandler<ExternalGroupStoreDefinition> {

    private PersonAttributesGroupStorePortalDataType personAttributesGroupStorePortalDataType;
    private IPersonAttributesGroupStoreDefinitionDao personAttributesGroupStoreDefinitionDao;
    private IPersonAttributesGroupDefinitionDao personAttributesGroupDefinitionDao;
    private IPersonAttributesGroupTestDefinitionDao personAttributesGroupTestDefinitionDao;
    private IPersonAttributesGroupTestGroupDefinitionDao personAttributesGroupTestGroupDefinitionDao;
    private Logger logger = LoggerFactory.getLogger(PersonAttributesGroupStoreDefinitionImporterExporter.class);

    @Autowired
    public void setpersonAttributesGroupStorePortalDataType(PersonAttributesGroupStorePortalDataType personAttributesGroupStorePortalDataType) {
        this.personAttributesGroupStorePortalDataType = personAttributesGroupStorePortalDataType;
    }

    @Autowired
    public void setPersonAttributesGroupStoreDefinitionDao(IPersonAttributesGroupStoreDefinitionDao personAttributesGroupStoreDefinitionDao) {
        this.personAttributesGroupStoreDefinitionDao = personAttributesGroupStoreDefinitionDao;
    }

    @Autowired
    public void setPersonAttributesGroupDefinitionDao(IPersonAttributesGroupDefinitionDao personAttributesGroupDefinitionDao) {
        this.personAttributesGroupDefinitionDao = personAttributesGroupDefinitionDao;
    }

    @Autowired
    public void setPersonAttributesGroupTestDefinitionDao(IPersonAttributesGroupTestDefinitionDao personAttributesGroupTestDefinitionDao) {
        this.personAttributesGroupTestDefinitionDao = personAttributesGroupTestDefinitionDao;
    }

    @Autowired
    public void setPersonAttributesGroupTestGroupDefinitionDao(IPersonAttributesGroupTestGroupDefinitionDao personAttributesGroupTestGroupDefinitionDao) {
        this.personAttributesGroupTestGroupDefinitionDao = personAttributesGroupTestGroupDefinitionDao;
    }

    @Override
    public Set<PortalDataKey> getImportDataKeys() {
        return new HashSet<PortalDataKey>(personAttributesGroupStorePortalDataType.getDataKeyImportOrder());
    }

    @Override
    public Iterable<? extends IPortalData> getPortalData() {
        final List<IPersonAttributesGroupStoreDefinition> pagStores = this.personAttributesGroupStoreDefinitionDao.getPersonAttributesGroupStoreDefinitions();
        
        return Lists.transform(pagStores, new Function<IPersonAttributesGroupStoreDefinition, IPortalData>() {
            /* (non-Javadoc)
             * @see com.google.common.base.Function#apply(java.lang.Object)
             */
            @Override
            public IPortalData apply(IPersonAttributesGroupStoreDefinition pagStores) {
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
        final List<IPersonAttributesGroupStoreDefinition> pagStores = this.personAttributesGroupStoreDefinitionDao.getPersonAttributesGroupStoreDefinitions();
        for(IPersonAttributesGroupStoreDefinition store : pagStores) {
            personAttributesGroupStoreDefinitionDao.deletePersonAttributesGroupStoreDefinition(store);
        }
        String name = groupStoreRep.getName();
        String description = groupStoreRep.getDescription();
        logStoreInfo(name, description);
        IPersonAttributesGroupStoreDefinition personAttributesGroupStoreDefinition = 
                personAttributesGroupStoreDefinitionDao.createPersonAttributesGroupStoreDefinition(name, description);
        List<PagsGroup> groups = groupStoreRep.getPagsGroups();
        importGroups(personAttributesGroupStoreDefinition, groups);
        // Circle back and populate the members of groups, now that all the groups have been created
        importGroupMembers(groups);
    }

    private void importGroups(IPersonAttributesGroupStoreDefinition personAttributesGroupStoreDefinition, List<PagsGroup> groups) {
        for(PagsGroup group: groups) {
            String groupName = group.getName();
            String groupDescription = group.getDescription();
            logGroupsInfo(group, groupName);
            IPersonAttributesGroupDefinition personAttributesGroupDefinition = 
                    personAttributesGroupDefinitionDao.createPersonAttributesGroupDefinition((PersonAttributesGroupStoreDefinitionImpl)personAttributesGroupStoreDefinition, 
                                                                                            groupName, 
                                                                                            groupDescription);
            SelectionTest selectionTest = group.getSelectionTest();
            if (null != selectionTest) {
                logger.trace("------- Test Groups -------");
                importTestGroups(personAttributesGroupDefinition, selectionTest);
            }
        }
    }

    private void importTestGroups(IPersonAttributesGroupDefinition personAttributesGroupDefinition, SelectionTest PersonAttributesGroupTestGroups) {
        List<TestGroup> testGroups = PersonAttributesGroupTestGroups.getTestGroups();
        for(TestGroup testGroup: testGroups) {
            String testGroupName = testGroup.getName();
            String testGroupDescription = testGroup.getDescription();
            logTestGroupsInfo(testGroupName, testGroupDescription);
            IPersonAttributesGroupTestGroupDefinition createdTestGroup =
                    personAttributesGroupTestGroupDefinitionDao.createPersonAttributesGroupTestGroupDefinition((PersonAttributesGroupDefinitionImpl) personAttributesGroupDefinition,
                                                                                                             testGroupName,
                                                                                                             testGroupDescription);
            List<Test> tests = testGroup.getTests();
            importTests(createdTestGroup, tests);
        }
    }

    private void importTests(IPersonAttributesGroupTestGroupDefinition createdTestGroup, List<Test> tests) {
        for(Test test: tests) {
            String attributeName = test.getAttributeName();
            String testerClass = test.getTesterClass();
            String testValue = test.getTestValue();
            logTestInfo(attributeName, testerClass, testValue);
            personAttributesGroupTestDefinitionDao.createPersonAttributesGroupTestDefinition((PersonAttributesGroupTestGroupDefinitionImpl)createdTestGroup,
                                                                                           attributeName,
                                                                                           testerClass,
                                                                                           testValue);
        }
    }
    
    private void importGroupMembers(List<PagsGroup> groups) {
        for(PagsGroup group: groups) {
            Members members = group.getMembers();
            if (null != members) {
                List<IPersonAttributesGroupDefinition> parentPag =
                        personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(group.getName());
                List<IPersonAttributesGroupDefinition> membersToSave = new ArrayList<IPersonAttributesGroupDefinition>();
                List<String> memberKeys = members.getMemberKeies();
                logger.trace("------ Members -------");
                for(String memberKey: memberKeys) {
                    logger.trace("Member key: {}", memberKey);
                    List<IPersonAttributesGroupDefinition> memberPag =
                            personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(memberKey);
                    membersToSave.add(memberPag.get(0));
                }
                parentPag.get(0).setMembers(membersToSave);
                personAttributesGroupDefinitionDao.updatePersonAttributesGroupDefinition(parentPag.get(0));
            }
        }
        
    }
    
    private void logStoreInfo(String name, String description) {
        logger.trace("Store Name: {}", name);
        logger.trace("Store Description: {}", description);
    }
    
    private void logGroupsInfo(PagsGroup group, String groupKey) {
        logger.trace("------ Group ------");
        logger.trace("Group Key: {}", groupKey);
        logger.trace("Group Name: {}", group.getName());
        logger.trace("Group Description: {}", group.getDescription());
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
        return personAttributesGroupStorePortalDataType;
    }

    @Override
    public ExternalGroupStoreDefinition exportData(String name) {
        final List<IPersonAttributesGroupStoreDefinition> personAttributesGroupStoreDefinition = this.personAttributesGroupStoreDefinitionDao.getPersonAttributesGroupStoreDefinitionByName(name);
        if (personAttributesGroupStoreDefinition != null && personAttributesGroupStoreDefinition.size() > 0) {
            return convert(personAttributesGroupStoreDefinition.get(0));
        } else {
            return null;
        }
    }

    @Override
    public String getFileName(ExternalGroupStoreDefinition data) {
        return SafeFilenameUtils.makeSafeFilename(data.getName());
    }

    @Override
    public ExternalGroupStoreDefinition deleteData(String id) {
        throw new UnsupportedOperationException();
    }
    
    protected ExternalGroupStoreDefinition convert(final IPersonAttributesGroupStoreDefinition pags) {
        if (pags == null) {
            return null;
        }
        ExternalGroupStoreDefinition externalGroupStoreDefinition = new ExternalGroupStoreDefinition();
        externalGroupStoreDefinition.setName(pags.getName());
        externalGroupStoreDefinition.setDescription(pags.getDescription());
        
        addExternalGroups(pags, externalGroupStoreDefinition);
        
        return externalGroupStoreDefinition;
    }

    private void addExternalGroups(final IPersonAttributesGroupStoreDefinition pags,
                                   ExternalGroupStoreDefinition externalGroupStoreDefinition) {
        List<PagsGroup> externalPags = externalGroupStoreDefinition.getPagsGroups();
        List<IPersonAttributesGroupDefinition> pagsGroups = pags.getGroups();
        for(IPersonAttributesGroupDefinition pag : pagsGroups) {
            PagsGroup externalPag = new PagsGroup();
            externalPag.setName(pag.getName());
            externalPag.setDescription(pag.getDescription());
            
            List<IPersonAttributesGroupDefinition> members = pag.getMembers();
            if (members.size() > 0) {
                addExternalGroupMembers(externalPag, members);
            }
            
            List<IPersonAttributesGroupTestGroupDefinition> testGroups = pag.getTestGroups();
            if (testGroups.size() > 0) {
                addExternalTestGroupsWrapper(externalPag, testGroups);
            }
            externalPags.add(externalPag);
        }
    }

    private void addExternalGroupMembers(PagsGroup externalPag, List<IPersonAttributesGroupDefinition> members) {
        Members pagMembers = new Members();
        List<String> externalMembers = pagMembers.getMemberKeies();
        for(IPersonAttributesGroupDefinition member: members) {
            externalMembers.add(member.getName());
        }
        externalPag.setMembers(pagMembers);
    }

    private void addExternalTestGroupsWrapper(PagsGroup externalPag, List<IPersonAttributesGroupTestGroupDefinition> testGroups) {
        SelectionTest externalPagTestGroups = new SelectionTest();
        List<TestGroup> externalPagTestGroupsHolder = externalPagTestGroups.getTestGroups();
        addExternalTestGroupObjects(testGroups, externalPagTestGroupsHolder);
        externalPag.setSelectionTest(externalPagTestGroups);
    }

    private void addExternalTestGroupObjects(List<IPersonAttributesGroupTestGroupDefinition> testGroups,
                                             List<TestGroup> externalPagTestGroupsHolder) {
        for(IPersonAttributesGroupTestGroupDefinition testGroup: testGroups) {
            TestGroup externalPagTestGroup = new TestGroup();
            externalPagTestGroup.setName(testGroup.getName());
            externalPagTestGroup.setDescription(testGroup.getDescription());
            List<IPersonAttributesGroupTestDefinition> pagTests = testGroup.getTests();
            List<Test> PersonAttributesGroupTests = externalPagTestGroup.getTests();
            addExternalTestObjects(pagTests, PersonAttributesGroupTests);
            externalPagTestGroupsHolder.add(externalPagTestGroup);
        }
    }

    private void addExternalTestObjects(List<IPersonAttributesGroupTestDefinition> pagTests,
                                        List<Test> PersonAttributesGroupTests) {
        for(IPersonAttributesGroupTestDefinition pagTest: pagTests) {
            Test externalPagTest = new Test();
            String testName = (pagTest.getName() == null) ? "" : pagTest.getName();
            String testDescription = (pagTest.getDescription() == null) ? "" : pagTest.getDescription();
            externalPagTest.setName(testName);
            externalPagTest.setDescription(testDescription);
            externalPagTest.setAttributeName(pagTest.getAttributeName());
            externalPagTest.setTesterClass(pagTest.getTesterClassName());
            externalPagTest.setTestValue(pagTest.getTestValue());
            PersonAttributesGroupTests.add(externalPagTest);
        }
    }

}
