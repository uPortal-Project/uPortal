/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.groups.EntityImpl;
import org.jasig.portal.groups.EntityTestingGroupImpl;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntitySearcher;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.provider.RestrictedPerson;
import org.jasig.portal.services.PersonDirectory;
import org.jasig.portal.services.persondir.IPersonAttributeDao;

/**
 * The Person Attributes Group Store uses attributes stored in the IPerson object to determine
 * group membership.  It can use attributes from any data source supported by the PersonDirectory
 * service.
 * 
 * @author Al Wold
 * @version $Revision$
 */
public class PersonAttributesGroupStore implements IEntityGroupStore, IEntityStore, IEntitySearcher {
    private static final Log log = LogFactory.getLog(PersonAttributesGroupStore.class);
   private Properties props;
   private Map groupDefinitions;
   private Map groups;
   private Map containingGroups;
      
   public PersonAttributesGroupStore() {
      groups = new HashMap();
      containingGroups = new HashMap();
      try {
         props = new Properties();
         props.load(PersonAttributesGroupStore.class.getResourceAsStream("/properties/groups/pags.properties"));
         IPersonAttributesConfiguration config = getConfig(props.getProperty("org.jasig.portal.groups.pags.PersonAttributesGroupStore.configurationClass"));
         groupDefinitions = config.getConfig();
         initGroups(); 
      } catch ( Exception e ) {
         String errorMsg = "PersonAttributeGroupStore.init(): " + "Problem initializing groups: " + e.getMessage();
         log.error("Problem initializing groups.", e);
         throw new RuntimeException(errorMsg);
      }
   }
   
   private IPersonAttributesConfiguration getConfig(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
      Class configClass = Class.forName(className);
      Object o = configClass.newInstance();
      return (IPersonAttributesConfiguration)o;
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
           IEntityGroup group = new EntityTestingGroupImpl(groupDef.getKey(), IPerson.class);
           group.setName(groupDef.getName());
           group.setDescription(groupDef.getDescription());
           cachePut(group);
       }
       cacheContainingGroupsForGroups();
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
   throws GroupsException 
   {
      GroupDefinition groupDef = (GroupDefinition)groupDefinitions.get(group.getLocalKey());
      if (member.isGroup()) 
      {
         String key = ((IEntityGroup)member).getLocalKey();
         return groupDef.hasMember(key);
      } 
      else 
      {
         if (member.getEntityType() != IPerson.class) 
             { return false; }
         IPerson person = null;
         try {
             IPersonAttributeDao pa = PersonDirectory.getPersonAttributeDao();
             Map attrs = pa.getUserAttributes(member.getKey());
             RestrictedPerson rp = PersonFactory.createRestrictedPerson();
             rp.setAttributes(attrs);
             
             person = rp;
         }
         catch (Exception ex)
             { return false; }
         return testRecursively(groupDef, person);
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
       if ( ! groupDef.contains(person) )
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
               if (g.getName().equalsIgnoreCase(query)) {
                  results.add(g.getEntityIdentifier());
               }
            }
            break;
         case STARTS_WITH:
            for (Iterator i = groups.values().iterator(); i.hasNext(); ) {
               IEntityGroup g = (IEntityGroup)i.next();
               if (g.getName().toUpperCase().startsWith(query.toUpperCase())) {
                  results.add(g.getEntityIdentifier());
               }
            }
            break;
         case ENDS_WITH:
            for (Iterator i = groups.values().iterator(); i.hasNext(); ) {
               IEntityGroup g = (IEntityGroup)i.next();
               if (g.getName().toUpperCase().endsWith(query.toUpperCase())) {
                  results.add(g.getEntityIdentifier());
              }
            }
            break;
         case CONTAINS:
            for (Iterator i = groups.values().iterator(); i.hasNext(); ) {
               IEntityGroup g = (IEntityGroup)i.next();
               if (g.getName().toUpperCase().indexOf(query.toUpperCase()) != -1) {
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
   
   public static class GroupDefinition {
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
      public boolean contains(IPerson person) {
         return ( testGroups.isEmpty() ) ? false : test(person);
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
   
   public static class TestGroup {
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