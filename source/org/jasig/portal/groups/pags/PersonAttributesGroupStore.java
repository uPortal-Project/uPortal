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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
   private Map groupDefinitions;
   private Map groups;
   private Map containingGroups;
      
   public PersonAttributesGroupStore() {
      Document config = null;
      try {
        config = ResourceLoader.getResourceAsDocument(this.getClass(), "/properties/groups/PAGSGroupStoreConfig.xml");
      } catch(Exception rme){
         throw new RuntimeException("PersonAttributesGroupStore: Unable to find configuration document");
      }
      init(config);
      LogService.log(LogService.DEBUG, "PersonAttributeGroupStore: initialized with "+groupDefinitions.size()+" groups");
   }
   
   public PersonAttributesGroupStore(Document config) {
      init(config);
   }
   
   /**
    * Read the XML configuration and create a Map of GroupDefinition objects.
    * 
    * @param config xml config document
    */
   private void init(Document config) {
      groupDefinitions = new HashMap();
      groups = new HashMap();
      containingGroups = new HashMap();
      config.normalize();
      Element groupStoreElement = config.getDocumentElement();
      NodeList groupElements = groupStoreElement.getChildNodes();
      for (int i = 0; i < groupElements.getLength(); i++) {
         if (groupElements.item(i) instanceof Element) {
            initGroupDef((Element)groupElements.item(i));
         }
      }
      try { 
         initGroups(); 
      } catch ( GroupsException ge ) {
         String errorMsg = "PersonAttributeGroupStore.init(): " + "Problem initializing groups: " + ge.getMessage();
         LogService.log(LogService.ERROR, errorMsg);
         throw new RuntimeException(errorMsg);
      }
   }

   /**
    * Iterates over the groupDefinitions Collection and creates the
    * corresponding groups.  Then, caches parents for each child group.
    */
   private void initGroups() throws GroupsException {
       Iterator i = null;
       Collection groupDefs = groupDefinitions.values();
       
       for ( i=groupDefs.iterator(); i.hasNext(); )
       {
           GroupDefinition groupDef = (GroupDefinition) i.next();
           IEntityGroup group = new EntityGroupImpl(groupDef.getKey(), IPerson.class);
           group.setName(groupDef.getName());
           group.setDescription(groupDef.getDescription());
           cachePut(group);
       }
       cacheContainingGroupsForGroups();
   }
   
   private void initGroupDef(Element groupElement) {
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
            } else if (tagName.equals("group-description")) {
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
      groupDefinitions.put(groupDef.getKey(), groupDef);
   }
   
   private void addMemberKeys(GroupDefinition groupDef, Element members) {
      NodeList children = members.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
         Node node = children.item(i);
         if (node instanceof Element && node.getNodeName().equals("member-key")) {
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
   
   private IEntityGroup cacheGet(String key) {
        return (IEntityGroup) groups.get(key);
    }
   private void cachePut(IEntityGroup group) {
       groups.put(group.getLocalKey(), group);
   }
   
   public boolean contains(IEntityGroup group, IGroupMember member) 
   throws GroupsException {
      GroupDefinition groupDef = (GroupDefinition)groupDefinitions.get(group.getLocalKey());
      if (member.isGroup()) {
         String key = ((IEntityGroup)member).getLocalKey();
         return groupDef.hasMember(key);
      } else {
         if (member.getEntityType() == IPerson.class) {
            // get the IPerson for the member and test
            IPerson person = PersonDirectory.getRestrictedPerson(member.getKey());
            // return groupDef.test(person);
            return testRecursively(groupDef, person);
         } else {
            return false;
         }
      }
   }

   public void delete(IEntityGroup group) throws GroupsException {
      throw new UnsupportedOperationException("PersonAttributesGroupStore: Method delete() not supported.");
   }

   public IEntityGroup find(String key) throws GroupsException {
      return (IEntityGroup)groups.get(key);
   }
   
   private void cacheContainingGroupsForGroups() throws GroupsException
   {
       Iterator i = null;
       // Find potential parent groups, those whose GroupDefinitions have members.
       List parentGroupsList = new ArrayList();
       for (i=groupDefinitions.values().iterator(); i.hasNext();)
       {
           GroupDefinition groupDef = (GroupDefinition) i.next();
           if (! groupDef.members.isEmpty())
               { parentGroupsList.add(cacheGet(groupDef.getKey())); }
       }
       IEntityGroup[] parentGroupsArray = (IEntityGroup[]) 
         parentGroupsList.toArray(new IEntityGroup[parentGroupsList.size()]);
         
       // Check each group for its parents and cache the references.
       for (i=groups.values().iterator(); i.hasNext();)
       {
           IEntityGroup childGroup = (IEntityGroup) i.next();
           parentGroupsList = new ArrayList(5);
           for (int idx=0; idx<parentGroupsArray.length; idx++)
           {
               if ( contains(parentGroupsArray[idx], childGroup) )
                   { parentGroupsList.add(parentGroupsArray[idx]); } 
           }
           containingGroups.put(childGroup.getLocalKey(), parentGroupsList);
       }
   }

   private boolean testRecursively(GroupDefinition groupDef, IPerson person)
   throws GroupsException {
       if ( ! groupDef.test(person) )
           { return false;}
       else
       {
           IEntityGroup group = cacheGet(groupDef.getKey());
           Set allParents = primGetAllContainingGroups(group, new HashSet());
           boolean testPassed = true;
           for (Iterator i=allParents.iterator(); i.hasNext() && testPassed;)
           {
               IEntityGroup parentGroup = (IEntityGroup) i.next();
               GroupDefinition parentGroupDef = 
                 (GroupDefinition) groupDefinitions.get(parentGroup.getLocalKey());
               testPassed = parentGroupDef.test(person);               
           }
           return testPassed;
       }
   }
   private java.util.Set primGetAllContainingGroups(IEntityGroup group, Set s)
   throws GroupsException
   {
       Iterator i = findContainingGroups(group);
       while ( i.hasNext() )
       {
           IEntityGroup parentGroup = (IEntityGroup) i.next();
           s.add(parentGroup);
           primGetAllContainingGroups(parentGroup, s);
       }
       return s;
   }

   public Iterator findContainingGroups(IGroupMember member) 
   throws GroupsException 
   {
      return (member.isEntity()) 
        ? findContainingGroupsForEntity((IEntity)member)
        : findContainingGroupsForGroup((IEntityGroup)member);
   }
   
   private Iterator findContainingGroupsForGroup(IEntityGroup group)
   {
       List parents = (List)containingGroups.get(group.getLocalKey());
       return (parents !=null)
         ? parents.iterator()
         : Collections.EMPTY_LIST.iterator();
   }
   private Iterator findContainingGroupsForEntity(IEntity member)
   throws GroupsException {
       List results = new ArrayList();
       for (Iterator i = groups.values().iterator(); i.hasNext(); ) {
          IEntityGroup group = (IEntityGroup)i.next();
          if ( contains(group, member)) 
              { results.add(group); }
       }
       return results.iterator();
   }

   public Iterator findEntitiesForGroup(IEntityGroup group) throws GroupsException {
      return Collections.EMPTY_LIST.iterator();
   }

   public ILockableEntityGroup findLockable(String key) throws GroupsException {
      throw new UnsupportedOperationException("PersonAttributesGroupStore: Method findLockable() not supported");
   }

   public String[] findMemberGroupKeys(IEntityGroup group) throws GroupsException {
      List keys = new ArrayList();
      GroupDefinition groupDef = (GroupDefinition) groupDefinitions.get(group.getLocalKey());
      if (groupDef != null)
      {
          for (Iterator i = groupDef.members.iterator(); i.hasNext(); ) 
              { keys.add((String) i.next()); }
      }
      return (String [])keys.toArray(new String[]{});
   }

   public Iterator findMemberGroups(IEntityGroup group) throws GroupsException {
      String[] keys = findMemberGroupKeys(group);
      List results = new ArrayList();
      for (int i = 0; i < keys.length; i++) {
         results.add(cacheGet(keys[i]));
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
               IEntityGroup g = (IEntityGroup)i.next();
               if (g.getName().equals(query)) {
                  results.add(g.getEntityIdentifier());
               }
            }
            break;
         case STARTS_WITH:
            for (Iterator i = groups.values().iterator(); i.hasNext(); ) {
               IEntityGroup g = (IEntityGroup)i.next();
               if (g.getName().startsWith(query)) {
                  results.add(g.getEntityIdentifier());
               }
            }
            break;
         case ENDS_WITH:
            for (Iterator i = groups.values().iterator(); i.hasNext(); ) {
               IEntityGroup g = (IEntityGroup)i.next();
               if (g.getName().endsWith(query)) {
                  results.add(g.getEntityIdentifier());
              }
            }
            break;
         case CONTAINS:
            for (Iterator i = groups.values().iterator(); i.hasNext(); ) {
               IEntityGroup g = (IEntityGroup)i.next();
               if (g.getName().indexOf(query) != -1) {
                  results.add(g.getEntityIdentifier());
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
         if (testGroups.isEmpty())
             return true;
         for (Iterator i = testGroups.iterator(); i.hasNext(); ) {
            TestGroup testGroup = (TestGroup)i.next();
            if (testGroup.test(person)) {
               return true;
            }
         }
         return false;
      }
      public String toString() {
          return "GroupDefinition " + key + " (" + name + ")";
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