package org.jasig.portal.groups;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;
import junit.framework.*;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.concurrency.IBasicEntity;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.security.IPerson;

/**
 * Tests the groups framework (a start).
 * @author: Dan Ellentuck
 */
public class GroupsTester extends TestCase {
    private static Class GROUP_CLASS;
    private static Class IPERSON_CLASS;
    private static Class MINIMAL_ENTITY_CLASS;
    private static String CR = "\n";
    private IEntity[] testEntities;
    private String[] testEntityKeys;
    private int numTestEntities = 0;


    private class MinimalEntity implements IBasicEntity
    {
        private String key;
        private MinimalEntity(String entityKey) {
            super();
            key = entityKey;
        }
        public Class getEntityType() {
            return this.getClass();
        }
        public String getEntityKey() {
            return key;
        }
        public boolean equals(Object o) {
            if ( o == null )
                return false;
            if ( ! (o instanceof IEntity) )
                return false;
            IBasicEntity ent = (IBasicEntity) o;
            return ent.getEntityType() == getEntityType() &&
                   ent.getEntityKey().equals(key);
        }
        public String toString() {
            return "MinimalEntity(" + key + ")";
        }
    }
/**
 * EntityLockTester constructor comment.
 */
public GroupsTester(String name) {
    super(name);
}
/**
 */
protected void addTestEntityType()
{
    try
    {
        Connection conn = org.jasig.portal.RDBMServices.getConnection();
        Statement stmnt = conn.createStatement();
        String sql =  "INSERT INTO UP_ENTITY_TYPE " +
                      "VALUES (99, " + "'" + MINIMAL_ENTITY_CLASS.getName() + "')";
        int rc = stmnt.executeUpdate( sql );
        if ( rc == 1 )
            { print("Test entity type inserted.");}

    }
    catch (Exception ex) { print("EntityCacheTester.addTestEntityType(): " + ex.getMessage());}
 }
/**
 */
protected void deleteTestEntityType()
{
    try
    {
        Connection conn = org.jasig.portal.RDBMServices.getConnection();
        Statement stmnt = conn.createStatement();
        String sql =  "DELETE FROM UP_ENTITY_TYPE WHERE ENTITY_TYPE_NAME = " + "'" +
                      MINIMAL_ENTITY_CLASS.getName() + "'";
        int rc = stmnt.executeUpdate( sql );
        if ( rc == 1 )
            { print("Test entity type deleted.");}

    }
    catch (Exception ex) { print("EntityCacheTester.deleteTestEntityType(): " + ex.getMessage());}
 }
/**
 */
protected void deleteTestGroups()
{
    try
    {
        Connection conn = org.jasig.portal.RDBMServices.getConnection();
        Statement stmnt = conn.createStatement();
        String sql =  "DELETE FROM UP_GROUP WHERE ENTITY_TYPE_ID = " +
                      EntityTypes.getEntityTypeID(MINIMAL_ENTITY_CLASS);
        int rc = stmnt.executeUpdate( sql );
        print("Test group rows deleted: " + rc);

    }
    catch (Exception ex) { print("EntityCacheTester.deleteTestGroups(): " + ex.getMessage());}
 }
/**
 * @return org.jasig.portal.services.GroupService
 */
private Collection getAllGroupMembers(IGroupMember gm) throws GroupsException
{
    Collection list = new ArrayList();
    for( Iterator itr=gm.getAllMembers(); itr.hasNext(); )
        { list.add(itr.next()); }
    return list;
}
/**
 * @return RDBMEntityStore
 */
private IEntityStore getEntityStore() throws GroupsException
{
    return RDBMEntityStore.singleton();
}
/**
 * @return org.jasig.portal.services.GroupService
 */
private Collection getGroupMembers(IGroupMember gm) throws GroupsException
{
    Collection list = new ArrayList();
    for( Iterator itr=gm.getMembers(); itr.hasNext(); )
        { list.add(itr.next()); }
    return list;
}
/**
 * @return RDBMEntityGroupStore
 */
private RDBMEntityGroupStore getGroupStore() throws GroupsException
{
    return RDBMEntityGroupStore.singleton();
}
/**
 * @return org.jasig.portal.groups.IEntity
 */
private IEntity getNewEntity(String key) throws GroupsException
{
    return 	getService().getEntity(key, MINIMAL_ENTITY_CLASS);
}
/**
 * @return org.jasig.portal.groups.IEntityGroup
 */
private IEntityGroup getNewGroup() throws GroupsException
{
    IEntityGroup group = getService().newGroup(MINIMAL_ENTITY_CLASS);
    group.setName("name_" + group.getKey());
    group.setCreatorID("de3");
    return group;
}
/**
*  @return java.lang.String
 * @param length int
 */
private String getRandomString(java.util.Random r, int length) {

    char[] chars = new char[length];

    for(int i=0; i<length; i++)
    {
        int diff = ( r.nextInt(25) );
        int charValue =  (int)'A' + diff;
        chars[i] = (char) charValue;
    }
    return new String(chars);
}
/**
 * @return org.jasig.portal.services.GroupService
 */
private GroupService getService() throws GroupsException
{
    return GroupService.instance();
}
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) throws Exception
{
    String[] mainArgs = {"org.jasig.portal.concurrency.caching.EntityCacheTester"};
    print("START TESTING CACHE");
    printBlankLine();
    junit.swingui.TestRunner.main(mainArgs);
    printBlankLine();
    print("END TESTING CACHE");

}
/**
 * @param msg java.lang.String
 */
private static void print (IEntity[] entities)
{
    for ( int i=0; i<entities.length; i++ )
    {
        print("(" + (i+1) + ") " + entities[i]);
    }
    print("  Total: " + entities.length);
}
/**
 * @param msg java.lang.String
 */
private static void print(String msg)
{
    java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
    System.out.println(ts + " : " + msg);
}
/**
 * @param msg java.lang.String
 */
private static void printBlankLine()
{
    System.out.println("");
}
/**
 */
protected void setUp()
{
    try {
        if ( GROUP_CLASS == null )
            { GROUP_CLASS = Class.forName("org.jasig.portal.groups.IEntityGroup"); }
        if ( IPERSON_CLASS == null )
            { IPERSON_CLASS = Class.forName("org.jasig.portal.security.IPerson"); }
        if ( MINIMAL_ENTITY_CLASS == null )
            { MINIMAL_ENTITY_CLASS = MinimalEntity.class; }

    addTestEntityType();
    numTestEntities = 100;

    // Entities and their keys:
    testEntityKeys = new String[numTestEntities];
    testEntities = new IEntity[numTestEntities];
    java.util.Random random = new java.util.Random();
    for (int i=0; i<numTestEntities; i++)
    {
        testEntityKeys[i] = (getRandomString(random, 3) + i);
        testEntities[i] = getNewEntity(testEntityKeys[i]);
    }


    }
    catch (Exception ex) { print("EntityCacheTester.setUp(): " + ex.getMessage());}
 }
/**
 * @return junit.framework.Test
 */
public static junit.framework.Test suite() {
    TestSuite suite = new TestSuite();

  suite.addTest(new GroupsTester("testAddAndDeleteGroups"));
  suite.addTest(new GroupsTester("testAddAndDeleteMembers"));
  suite.addTest(new GroupsTester("testGroupMemberValidation"));
  suite.addTest(new GroupsTester("testGroupMemberUpdate"));


//  suite.addTest(new EntityCacheTester("testStoreDeleteBefore"));

//	Add more tests here.
//  NB: Order of tests is not guaranteed.

    return suite;
}
/**
 */
protected void tearDown()
{
    try
    {
        testEntityKeys = null;
        testEntities = null;
        deleteTestGroups();
        deleteTestEntityType();

    }
    catch (Exception ex) { print("EntityCacheTester.tearDown(): " + ex.getMessage());}
}
/**
 */
public void testAddAndDeleteGroups() throws Exception
{
    print(CR + "***** ENTERING GroupsTester.testAddAndDeleteGroups() *****" + CR);
    String msg = null;

    msg = "Creating a new IEntityGroup.";
    print(msg);
    IEntityGroup newGroup = getNewGroup();
    assertNotNull(msg, newGroup);

    print("Now updating " + newGroup);
    newGroup.setName("Test");
    newGroup.setCreatorID("de3");
    newGroup.update();

    print("Now retrieving group just created from the store.");
    String key = newGroup.getKey();
    IEntityGroup retrievedGroup = getService().findGroup(key);

    msg = "Testing retrieved group.";
    print(msg);
    assertEquals(msg, newGroup, retrievedGroup);

    print("Now deleting group just created from the store.");
    retrievedGroup.delete();

    print("Attempting to retrieve deleted group from the store.");
    retrievedGroup = getService().findGroup(key);
    assertNull(msg, retrievedGroup);

    print(CR + "***** LEAVING GroupsTester.testAddAndDeleteGroups() *****" + CR);

}
/**
 */
public void testAddAndDeleteMembers() throws Exception
{
    print(CR + "***** ENTERING GroupsTester.testAddAndDeleteMembers() *****" + CR);
    String msg = null;
    Class type = MINIMAL_ENTITY_CLASS;
    int totNumGroups = 3;
    int totNumEntities = 5;
    IEntityGroup[] groups = new IEntityGroup[totNumGroups];
    IEntity[] entities = new IEntity[totNumEntities];
    IGroupMember[] groupMembers = null;
    Iterator itr = null;
    ArrayList list = null;
    int idx = 0;

    msg = "Creating " + totNumGroups + " new groups.";
    print(msg);
    for (idx=0; idx<totNumGroups; idx++)
    {
        groups[idx] = getNewGroup();
        assertNotNull(msg, groups[idx]);
    }
    IEntityGroup rootGroup = groups[0];
    IEntityGroup childGroup = groups[1];


    msg = "Adding " + (totNumGroups - 1) + " to root group.";
    print(msg);
    for(idx=1; idx<totNumGroups; idx++)
        { rootGroup.addMember(groups[idx]); }

    msg = "Retrieving members from root group.";
    print(msg);
    list = new ArrayList();
    for( itr=rootGroup.getMembers(); itr.hasNext(); )
        { list.add(itr.next()); }
    assertEquals(msg, (totNumGroups - 1), list.size());

    msg = "Adding " + (totNumEntities - 2) + " to root group.";
    print(msg);
    for(idx=0; idx<(totNumEntities - 2) ; idx++)
        { rootGroup.addMember(testEntities[idx]); }

    msg = "Retrieving members from root group.";
    print(msg);
    list = new ArrayList();
    for( itr=rootGroup.getMembers(); itr.hasNext(); )
        { list.add(itr.next()); }
    assertEquals(msg, (totNumGroups - 1 + totNumEntities - 2), list.size());

    msg = "Adding 2 entities to child group.";
    print(msg);
    childGroup.addMember(testEntities[totNumEntities - 1]);
    childGroup.addMember(testEntities[totNumEntities]);

    msg = "Retrieving ALL members from root group.";
    print(msg);
    list = new ArrayList();
    for( itr=rootGroup.getAllMembers(); itr.hasNext(); )
        { list.add(itr.next()); }
    assertEquals(msg, (totNumGroups - 1 + totNumEntities), list.size());

    msg = "Deleting child group from root group.";
    print(msg);
    rootGroup.removeMember(childGroup);

    msg = "Retrieving ALL members from root group.";
    print(msg);
    list = new ArrayList();
    for( itr=rootGroup.getAllMembers(); itr.hasNext(); )
        { list.add(itr.next()); }
    assertEquals(msg, (totNumGroups - 2 + totNumEntities - 2 ), list.size());


    print(CR + "***** LEAVING GroupsTester.testAddAndDeleteMembers() *****" + CR);

}
/**
 */
public void testGroupMemberUpdate() throws Exception
{
    print(CR + "***** ENTERING GroupsTester.testGroupMemberUpdate() *****" + CR);
    String msg = null;

    Iterator itr;
    Collection list;
    int idx = 0;
    Exception e = null;

    int numAddedEntities = 10;
    int numDeletedEntities = 5;

    print("Creating 2 new groups.");

    IEntityGroup parent = getNewGroup(); parent.setName("parent"); parent.setCreatorID("de3");
    String parentKey = parent.getKey();
    IEntityGroup child  = getNewGroup(); child.setName("child");   child.setCreatorID("de3");
    String childKey = child.getKey();

    print("Adding " + child + " to " + parent);
    parent.addMember(child);

    print("Adding " + numAddedEntities + " members to " + child);
    for(idx=0; idx<numAddedEntities; idx++)
        { child.addMember(testEntities[idx]); }

    msg = "Retrieving members from " + child;  // child should have numAddedEntities group members.
    print(msg);
    list = getGroupMembers(child);
    assertEquals(msg, (numAddedEntities), list.size());

    msg = "Retrieving members from " + parent;  // parent should have numAddedEntities + 1 group members.
    print(msg);
    list = getAllGroupMembers(parent);
    assertEquals(msg, (numAddedEntities + 1), list.size());

    print("Now updating " + parent + " and " + child);
    child.update();
    parent.update();

    msg = "Retrieving " + parent + " and " + child + " from db.";
    print(msg);
    IEntityGroup retrievedParent = getService().findGroup(parentKey);
    IEntityGroup retrievedChild = getService().findGroup(childKey);
    assertEquals(msg, parent, retrievedParent);
    assertEquals(msg, child, retrievedChild);

    // retrievedChild should have numAddedEntities group members.
    msg = "Retrieving members from " + retrievedChild;
    print(msg);
    list = getAllGroupMembers(retrievedChild);
    assertEquals(msg, numAddedEntities, list.size());

    // retrievedParent should have numAddedEntities + 1 group members.
    msg = "Retrieving members from " + retrievedParent;
    print(msg);
    list = getAllGroupMembers(retrievedParent);
    assertEquals(msg, (numAddedEntities + 1), list.size());

    print("Deleting " + numDeletedEntities + " members from " + retrievedChild);
    for(idx=0; idx<numDeletedEntities; idx++)
        { retrievedChild.removeMember(testEntities[idx]); }

    // retrievedChild should have (numAddedEntities - numDeletedEntities) members.
    msg = "Retrieving members from " + retrievedChild;
    print(msg);
    list = getAllGroupMembers(retrievedChild);
    assertEquals(msg, (numAddedEntities - numDeletedEntities), list.size());

    msg = "Adding back one member to " + retrievedChild;
    print(msg);
    retrievedChild.addMember(testEntities[0]);

    // retrievedChild should have (numAddedEntities - numDeletedEntities + 1) members.
    msg = "Retrieving members from " + retrievedChild;
    print(msg);
    list = getAllGroupMembers(retrievedChild);
    assertEquals(msg, (numAddedEntities - numDeletedEntities + 1), list.size());

    int numChildMembers = list.size();
    print("Now updating " + retrievedChild);
    retrievedChild.update();

    msg = "Re-Retrieving " + retrievedChild + " from db.";
    print(msg);
    IEntityGroup reRetrievedChild = getService().findGroup(childKey);
    assertEquals(msg, retrievedChild, reRetrievedChild);

    // re-RetrievedChild should have (numAddedEntities - numDeletedEntities + 1) members.
    msg = "Retrieving members from " + reRetrievedChild;
    print(msg);
    list = getAllGroupMembers(reRetrievedChild);
    assertEquals(msg, numChildMembers, list.size());

    // Remove parent and child groups from db.
    msg = "Deleting " + retrievedParent + " and " + reRetrievedChild + " from db.";
    print(msg);
    retrievedParent.delete();
    reRetrievedChild.delete();

    IEntityGroup deletedParent = getService().findGroup(parentKey);
    IEntityGroup deletedChild = getService().findGroup(childKey);
    assertNull(msg, deletedParent);
    assertNull(msg, deletedChild);

    print(CR + "***** LEAVING GroupsTester.testGroupMemberUpdate() *****" + CR);

}
/**
 */
public void testGroupMemberValidation() throws Exception
{
    print(CR + "***** ENTERING GroupsTester.testGroupMemberValidation() *****" + CR);
    String msg = null;

    Iterator itr;
    Collection list;
    int idx = 0;
    Exception e = null;

    IEntityGroup parent = getNewGroup(); parent.setName("parent"); parent.setCreatorID("de3");
    IEntityGroup child  = getNewGroup(); child.setName("child");   child.setCreatorID("de3");
    IEntityGroup child2 = getNewGroup(); child2.setName("child");  child2.setCreatorID("de3");

    IEntity entity1 = getNewEntity("child");
    IEntity entity2 = getNewEntity("child");
    IEntity ipersonEntity = getService().getEntity("00000", IPERSON_CLASS);


    msg = "Adding " + child + " to " + parent;
    print(msg);
    parent.addMember(child);

    msg = "Retrieving members from " + parent;  // parent should have 1 group member.
    print(msg);
    list = getGroupMembers(parent);
    assertEquals(msg, 1, list.size());

    // Test adding a group with a duplicate name.
    msg = "Adding " + child2 + " to " + parent + " (should fail).";
    print(msg);
    try { parent.addMember(child2); }
    catch (GroupsException ge) {e = ge;}
    assertNotNull(msg, e);

    msg = "Retrieving members from " + parent;  // parent should STILL have 1 group member.
    print(msg);
    list = getGroupMembers(parent);
    assertEquals(msg, 1, list.size());

    msg = "Adding renamed " + child2 + " to " + parent + " (should succeed).";
    print(msg);
    child2.setName("child2");
    try { parent.addMember(child2); e=null;}
    catch (GroupsException ge) {e=ge;}
    assertNull(msg, e);

    msg = "Retrieving members from " + parent;  // parent should now have 2 group members.
    print(msg);
    list = getGroupMembers(parent);
    assertEquals(msg, 2, list.size());

    // Test adding an ENTITY with the same name as a member GROUP.
    msg = "Adding entity w/same name as child group to " + parent;
    print(msg);
    parent.addMember(entity1);

    msg = "Retrieving members from " + parent;  // parent should now have 3 group members.
    print(msg);
    list = getGroupMembers(parent);
    assertEquals(msg, 3, list.size());

    // Test adding a group member with a duplicate key.
    msg = "Adding another entity w/same name as child group to " + parent + " (should fail).";
    print(msg);
    try { parent.addMember(entity2); e = null;}
    catch (GroupsException ge) {e = ge;}
    assertNotNull(msg, e);

    msg = "Retrieving members from " + parent;  // parent should still have 3 group members.
    print(msg);
    list = getGroupMembers(parent);
    assertEquals(msg, 3, list.size());

    // Test adding a group member with a different type:
    msg = "Adding an entity of different type to " + parent;
    print(msg);
    try { parent.addMember(ipersonEntity); e = null; }
    catch (GroupsException ge) {e = ge;}
    assertNotNull(msg, e);

    msg = "Retrieving members from " + parent;  // parent should still have 3 group members.
    print(msg);
    list = getGroupMembers(parent);
    assertEquals(msg, 3, list.size());

    // Test adding a circular reference.
    try { child.addMember(parent); e = null; }
    catch (GroupsException ge) { e = ge; }
    assertNotNull(msg, e);

    msg = "Retrieving members from " + child;  // child should have 0 members.
    print(msg);
    list = getGroupMembers(child);
    assertEquals(msg, 0, list.size());

    print(CR + "***** LEAVING GroupsTester.testGroupMemberValidation() *****" + CR);

}
}
