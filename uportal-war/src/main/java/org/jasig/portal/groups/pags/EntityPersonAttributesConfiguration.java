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

package org.jasig.portal.groups.pags;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.portal.groups.pags.PersonAttributesGroupStore.GroupDefinition;
import org.jasig.portal.groups.pags.PersonAttributesGroupStore.TestGroup;
import org.jasig.portal.pags.dao.IPersonAttributesGroupStoreDefinitionDao;
import org.jasig.portal.pags.om.IPersonAttributesGroupDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupStoreDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupTestDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupTestGroupDefinition;
import org.jasig.portal.spring.locator.ApplicationContextLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Get Store information from imported PAGS Store
 * 
 * @since 4.1
 * @author Shawn Connolly, sconnolly@unicon.net
 */
public class EntityPersonAttributesConfiguration implements IPersonAttributesConfiguration {

    private IPersonAttributesGroupStoreDefinitionDao personAttributesGroupStoreDefinitionDao;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final ApplicationContext applicationContext;

    public EntityPersonAttributesConfiguration() {
        super();
        this.applicationContext = ApplicationContextLocator.getApplicationContext();
        this.personAttributesGroupStoreDefinitionDao = applicationContext.getBean(IPersonAttributesGroupStoreDefinitionDao.class);
    }

   public Map<String, GroupDefinition> getConfig() {
      List<IPersonAttributesGroupStoreDefinition> stores = personAttributesGroupStoreDefinitionDao.getPersonAttributesGroupStoreDefinitions();
      Map<String, GroupDefinition> groupDefinitions = new HashMap<String, GroupDefinition>();
      if(stores == null || stores.size() != 1) {
          throw new RuntimeException("PersonAttributesGroupStore: Unable to get imported PAGS Store information");
      }
      IPersonAttributesGroupStoreDefinition store = stores.get(0);
      List<IPersonAttributesGroupDefinition> groups = store.getGroups();
      for (IPersonAttributesGroupDefinition group : groups) {
          GroupDefinition groupDef = initGroupDef(group);
          groupDefinitions.put(groupDef.getKey(), groupDef);
      }
      return groupDefinitions;
   }
   
   private GroupDefinition initGroupDef(IPersonAttributesGroupDefinition group) {
      GroupDefinition groupDef = new GroupDefinition();
      groupDef.setKey(group.getName());
      groupDef.setName(group.getName());
      groupDef.setDescription(group.getDescription());
      addMemberKeys(groupDef, group.getMembers());
      List<IPersonAttributesGroupTestGroupDefinition> testGroups = group.getTestGroups();
      for(IPersonAttributesGroupTestGroupDefinition testGroup : testGroups) {
          TestGroup tg = new TestGroup();
          List<IPersonAttributesGroupTestDefinition> tests = testGroup.getTests();
          for(IPersonAttributesGroupTestDefinition test : tests) {
              IPersonTester testerInst = initializeTester(test.getTesterClassName(), test.getAttributeName(), test.getTestValue());
              tg.addTest(testerInst);
          }
          groupDef.addTestGroup(tg);
      }
      return groupDef;
   }
   private void addMemberKeys(GroupDefinition groupDef, List<IPersonAttributesGroupDefinition> members) {
       for(IPersonAttributesGroupDefinition member: members) {
           groupDef.addMember(member.getName());
       }
   }
   private IPersonTester initializeTester(String tester, String attribute, String value) {
         try {
            Class<?> testerClass = Class.forName(tester);
            Constructor<?> c = testerClass.getConstructor(new Class[]{String.class, String.class});
            Object o = c.newInstance(new Object[]{attribute, value});
            return (IPersonTester)o;
         } catch (Exception e) {
            logger.error("Error in initializing tester class: {}", tester, e);
            return null;
         }
      }
}
