/**
 * Copyright (c) 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.groups.pags;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.groups.EntityGroupImpl;
import org.jasig.portal.groups.EntityImpl;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntitySearcher;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.PersonDirectory;
import org.jasig.portal.utils.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * The Person Attributes Group Store uses attributes stored in the IPerson object to determine
 * group membership.  It can use attributes from any data source supported by the PersonDirectory
 * service.
 * 
 * @author Al Wold
 * @version $Revision$
 */
public class PersonAttributesGroupStore implements IEntityGroupStore, IEntityStore, IEntitySearcher {
   private Map groups;
   
   public PersonAttributesGroupStore() {
      Document config = null;
      try {
        config = ResourceLoader.getResourceAsDocument(this.getClass(), "/properties/groups/PAGSGroupStoreConfig.xml");
      } catch(Exception rme){
         throw new RuntimeException("PersonAttributesGroupStore: Unable to find configuration configuration document");
      }
      init(config);
      LogService.log(LogService.DEBUG, "PersonAttributeGroupStore: initialized with "+groups.size()+" groups");
   }
   
   public PersonAttributesGroupStore(Document config) {
      init(config);
   }
   
   /**
    * Read the XML configuration and create a Map of GroupDefinition objects
    * 
    * @param config xml config document
    */
   private void init(Document config) {
      groups = new Hashtable();
      config.normalize();
      Element groupStoreElement = config.getDocumentElement();
      NodeList groupElements = groupStoreElement.getChildNodes();
      for (int i = 0; i < groupElements.getLength(); i++) {
         if (groupElements.item(i) instanceof Element) {
            initGroup((Element)groupElements.item(i));
         }
      }
   }
   
   private void initGroup(Element groupElement) {
      GroupDefinition groupDef = new GroupDefinition();
      NodeList children = groupElement.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
         if (children.item(i) instanceof Element) {
            Element element = (Element)children.item(i);
            String tagName = element.getTagName();
            element.normalize();
            String text = null;
            if (element.getFirstChild() instanceof Text) {
               text = ((Text)element.getFirstChild()).getData();
            }
            if (tagName.equals("group-key")) {
               groupDef.setKey(text);
            } else if (tagName.equals("group-name")) {
               groupDef.setName(text);
            } else if (tagName.equals("description")) {
               groupDef.setDescription(text);
            } else if (tagName.equals("selection-test")) {
               NodeList testGroups = element.getChildNodes();
               for (int j = 0; j < testGroups.getLength(); j++) {
                  Node testGroup = testGroups.item(j);
                  if (testGroup instanceof Element && ((Element)testGroup).getTagName().equals("test-group")) {
                     TestGroup tg = new TestGroup();
                     NodeList tests = testGroup.getChildNodes();
                     for (int k = 0; k < tests.getLength(); k++) {
                        Node test = tests.item(k);
                        if (test instanceof Element && ((Element)test).getTagName().equals("test")) {
                           String attribute = null;
                           String tester = null;
                           String value = null;
                           NodeList parameters = test.getChildNodes();
                           for (int l = 0; l < parameters.getLength(); l++) {
                              Node parameter = parameters.item(l);
                              text = null;
                              String nodeName = parameter.getNodeName();
                              if (parameter.getFirstChild() != null &&
                                  parameter.getFirstChild() instanceof Text) {
                                     text = ((Text)parameter.getFirstChild()).getData();
                              }
                              if (nodeName.equals("attribute-name")) {
                                 attribute = text;
                              } else if (nodeName.equals("tester-class")) {
                                 tester = text;
                              } else if (nodeName.equals("test-value")) {
                                 value = text;
                              }
                           }
                           IPersonTester testerInst = initializeTester(tester, attribute, value);
                           tg.addTest(testerInst);
                        }
                        groupDef.addTestGroup(tg);
                    }
                  }
               }
            } else if (tagName.equals("members")) {
               addMemberKeys(groupDef, element);
            }
         }
      }
      groups.put(groupDef.getKey(), groupDef);
   }
   
   private void addMemberKeys(GroupDefinition groupDef, Element members) {
      NodeList children = members.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
         Node node = children.item(i);
         if (node instanceof Element && node.getNodeName().equals("member")) {
            Element member = (Element)node;
            member.normalize();
            if (member.getFirstChild() instanceof Text) {
               groupDef.addMember(((Text)member.getFirstChild()).getData()); 
            }
         }
      }
   }
   
   private IPersonTester initializeTester(String tester, String attribute, String value) {
      try {
         Class testerClass = Class.forName(tester);
         Constructor c = testerClass.getConstructor(new Class[]{String.class, String.class});
         Object o = c.newInstance(new Object[]{attribute, value});
         return (IPersonTester)o;
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public boolean contains(IEntityGroup group, IGroupMember member) throws GroupsException {
      GroupDefinition groupDef = (GroupDefinition)groups.get(group.getKey());
      if (member.isGroup()) {
         return groupDef.hasMember(member.getKey());
      } else {
         // get the IPerson for the member and test
         IPerson person = PersonDirectory.getRestrictedPerson(member.getKey());
         return groupDef.test(person);
      }
   }

   public void delete(IEntityGroup group) throws GroupsException {
      throw new UnsupportedOperationException("PersonAttributesGroupStore: Method delete() not supported.");
   }

   public IEntityGroup find(String key) throws GroupsException {
      GroupDefinition groupDef = (GroupDefinition)groups.get(key);
      IEntityGroup group = new EntityGroupImpl(groupDef.getKey(), IPerson.class);
      group.setName(groupDef.getName());
      group.setDescription(groupDef.getDescription());
      return group;
   }

   public Iterator findContainingGroups(IGroupMember gm) throws GroupsException {
      List results = new ArrayList();
      for (Iterator i = groups.values().iterator(); i.hasNext(); ) {
         GroupDefinition groupDef = (GroupDefinition)i.next();
         if (gm.isGroup()) {
            if (groupDef.hasMember(gm.getKey())) {
               results.add(new EntityGroupImpl(groupDef.getKey(), IPerson.class));
            }
         } else {
            IPerson person = PersonDirectory.getRestrictedPerson(gm.getKey());
            if (groupDef.test(person)) {
               results.add(new EntityGroupImpl(groupDef.getKey(), IPerson.class));
            }
         }
      }
      return results.iterator();
   }

   public Iterator findEntitiesForGroup(IEntityGroup group) throws GroupsException {
      return new ArrayList().iterator();
   }

   public ILockableEntityGroup findLockable(String key) throws GroupsException {
      throw new UnsupportedOperationException("PersonAttributesGroupStore: Method findLockable() not supported");
   }

   public String[] findMemberGroupKeys(IEntityGroup group) throws GroupsException {
      List keys = new ArrayList();
      for (Iterator i = groups.values().iterator(); i.hasNext(); ) {
         GroupDefinition groupDef = (GroupDefinition)i.next();
         if (groupDef.hasMember(group.getKey())) {
            keys.add(groupDef.getKey());
         }
      }
      return (String [])keys.toArray(new String[]{});
   }

   public Iterator findMemberGroups(IEntityGroup group) throws GroupsException {
      String[] keys = findMemberGroupKeys(group);
      List results = new ArrayList();
      for (int i = 0; i < keys.length; i++) {
         results.add(new EntityGroupImpl(keys[i], IPerson.class));
      }
      return results.iterator();
   }

   public IEntityGroup newInstance(Class entityType) throws GroupsException {
      throw new UnsupportedOperationException("PersonAttributesGroupStore: Method newInstance() not supported");
   }

   public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype) throws GroupsException {
      List results = new ArrayList();
      switch (method) {
         case IS:
            for (Iterator i = groups.values().iterator(); i.hasNext(); ) {
               GroupDefinition groupDef = (GroupDefinition)i.next();
               if (groupDef.getName().equals(query)) {
                  results.add(new EntityIdentifier(groupDef.getKey(), IEntityGroup.class));
               }
            }
            break;
         case STARTS_WITH:
            for (Iterator i = groups.values().iterator(); i.hasNext(); ) {
               GroupDefinition groupDef = (GroupDefinition)i.next();
               if (groupDef.getName().startsWith(query)) {
                  results.add(new EntityIdentifier(groupDef.getKey(), IEntityGroup.class));
               }
            }
            break;
         case ENDS_WITH:
            for (Iterator i = groups.values().iterator(); i.hasNext(); ) {
               GroupDefinition groupDef = (GroupDefinition)i.next();
               if (groupDef.getName().endsWith(query)) {
                  results.add(new EntityIdentifier(groupDef.getKey(), IEntityGroup.class));
              }
            }
            break;
         case CONTAINS:
            for (Iterator i = groups.values().iterator(); i.hasNext(); ) {
               GroupDefinition groupDef = (GroupDefinition)i.next();
               if (groupDef.getName().indexOf(query) != -1) {
                  results.add(new EntityIdentifier(groupDef.getKey(), IEntityGroup.class));
              }
            }
            break;
      }
      return (EntityIdentifier [])results.toArray(new EntityIdentifier[]{});
   }

   public void update(IEntityGroup group) throws GroupsException {
      throw new UnsupportedOperationException("PersonAttributesGroupStore: Method update() not supported.");
   }

   public void updateMembers(IEntityGroup group) throws GroupsException {
      throw new UnsupportedOperationException("PersonAttributesGroupStore: Method updateMembers() not supported.");
   }
   
   private class GroupDefinition {
      private String key;
      private String name;
      private String description;
      private List members;
      private List testGroups;
      
      public GroupDefinition() {
         members = new Vector();
         testGroups = new Vector();
      }
      
      public void setKey(String key) {
         this.key = key;
      }
      public String getKey() {
         return key;
      }
      
      public void setName(String name) {
         this.name = name;
      }
      public String getName() {
         return name;
      }
      
      public void setDescription(String description) {
         this.description = description;
      }
      public String getDescription() {
         return description;
      }
      public void addMember(String key) {
         members.add(key);
      }
      public boolean hasMember(String key) {
         return members.contains(key);
      }
      public void addTestGroup(TestGroup testGroup) {
         testGroups.add(testGroup);
      }
      public boolean test(IPerson person) {
         for (Iterator i = testGroups.iterator(); i.hasNext(); ) {
            TestGroup testGroup = (TestGroup)i.next();
            if (testGroup.test(person)) {
               return true;
            }
         }
         return false;
      }
   }
   
   private class TestGroup {
      private List tests;
      
      public TestGroup() {
         tests = new Vector();
      }
      
      public void addTest(IPersonTester test) {
         tests.add(test);
      }
      
      public boolean test(IPerson person) {
         for (Iterator i = tests.iterator(); i.hasNext(); ) {
            IPersonTester tester = (IPersonTester)i.next();
            if (!tester.test(person)) {
               return false;
            }
         }
         return true;
      }
   }

   public IEntity newInstance(String key, Class type) throws GroupsException {
      if (EntityTypes.getEntityTypeID(type) == null) {
         throw new GroupsException("Invalid entity type: "+type.getName());
      }
      return new EntityImpl(key, type);
   }

   public IEntity newInstance(String key) throws GroupsException {
      return new EntityImpl(key, null);
   }

   public EntityIdentifier[] searchForEntities(String query, int method, Class type) throws GroupsException {
      List results = new ArrayList();
      return (EntityIdentifier [])results.toArray(new EntityIdentifier[]{});
   }

}