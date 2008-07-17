/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups.filesystem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.groups.EntityImpl;
import org.jasig.portal.groups.GroupServiceConfiguration;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.rdbm.TransientDatasource;

/**
 * This class was rewritten to eliminate external dependencies, chiefly
 * on the composite group service.  Although this was mostly achieved,
 * 2 dependencies remain.  It needs a file system to read via java.io, 
 * since this is what is being tested, and it requires a composite
 * group service configuration document.  I will eventually remove these
 * dependencies but it seemed better to get the test in now.  I was 
 * thinking we could eventually use something like Apache Commons VFS 
 * to set up a virtual file system (a future enhancement for the 
 * FileSystem group service).  In the meantime, this class must create a 
 * GROUPS_ROOT directory and write to it.  The class first tries to 
 * create the directory in the user.home.  If unsuccessful, it tries to 
 * create it in the current directory, and if this is unsuccessful, 
 * it dies.     
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class FileSystemGroupsTest extends TestCase {
    private static Class GROUP_CLASS;
    private static Class IPERSON_CLASS;
    private static String CR = "\n";

    private String[] testEntityKeys;
    private String[] testFileNames;
    private List testGroupKeys;
    private int numTestFiles;
    private int numTestEntities;

    private String GROUPS_ROOT;
    private File groupsRoot;
    private String IPERSON_GROUPS_ROOT;
    private List allFiles = null, directoryFiles = null, keyFiles = null;
    private String NON_EXISTENT_ID = "xyzxyzxyz";
    private IEntityGroupStore groupStore;
    private String GROUP_SEPARATOR;
    
    private DataSource testDataSource;
/**
 * FileSystemGroupsTest.
 */
public FileSystemGroupsTest(String name) {
    super(name);
}
/**
 */
protected void addIdsToFile(File f)
{
    long now = System.currentTimeMillis() / 10;
    long div = now % 5;
    div += 5;

    try
    {
        String line = null, start = null;
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));

        bw.write("# test file written at " + new java.util.Date());
        bw.newLine();
        bw.write("#");
        bw.newLine();

        for (int i=0; i<numTestEntities; i++)
        {
            start = ( (i > 0) && (i % div == 0) ) ? "   " : "";
            line = start + testEntityKeys[i] + " is entity " + (i + 1);
            bw.write(line);
            bw.newLine();
        }

        bw.write("# end of test file ");
        bw.newLine();
        bw.write("#");
        bw.newLine();

        bw.close();

    } // end try
    catch (Exception ex) { print("FileSystemGroupsTest.addIdsToFile(): " + ex.getMessage());}
 }
/**
 * @return org.jasig.portal.groups.IEntityGroup
 */
private IEntityGroup findGroup(File file) throws GroupsException
{
    String key = getKeyFromFile(file);
    return findGroup(key);
}
/**
 * Note that this is the local, not composite, key.
 * @return org.jasig.portal.groups.IEntityGroup
 */
private IEntityGroup findGroup(String key) throws GroupsException
{
    return getGroupStore().find(key);
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
private File getGroupsRoot() {
    if ( groupsRoot == null )
        { groupsRoot = createGroupsRoot(); }
    return groupsRoot;
}
/**
 * Try to create the groups root directory in the user.home and if
 * not possible try in the current directory.
 * @return java.io.File
 */
private File createGroupsRoot() 
{
    File gr = null;
    String userHome = System.getProperty("user.home");
    if ( userHome != null )
    {
        File uh = new File(userHome);
        if ( uh.exists() && uh.canWrite() )
            { gr = new File(uh.getPath() + File.separator + "GROUPS-ROOT"); }
    }
    else
        { gr = new File("GROUPS-ROOT"); }

    return ( gr.mkdir() ) ? gr : null;
}
/**
 * @return FileSystemGroupStore
 */
private FileSystemGroupStore getGroupStore() throws GroupsException
{
    if ( groupStore == null )
    { 
        GroupServiceConfiguration config = new GroupServiceConfiguration();
        Map atts = config.getAttributes();
        atts.put("nodeSeparator",IGroupConstants.NODE_SEPARATOR);
        groupStore = new FileSystemGroupStore(config);
    }
    return (FileSystemGroupStore)groupStore;
}
/**
 * @return org.jasig.portal.groups.IEntityGroup
 */
private String getKeyFromFile(File file) throws GroupsException
{
    String key = file.getPath();
    if ( key.startsWith(GROUPS_ROOT) )
    {
        key = key.substring(GROUPS_ROOT.length());
        if ( GROUP_SEPARATOR.equals(String.valueOf(FileSystemGroupStore.PERIOD)) )
            { key = key.replace(FileSystemGroupStore.PERIOD, FileSystemGroupStore.SUBSTITUTE_PERIOD); } 
    }
    return key;
}
/**
 * @return org.jasig.portal.groups.IEntity
 */
private IEntity getNewIPersonEntity(String key) throws GroupsException
{
    return getNewEntity(IPERSON_CLASS, key);
}
/**
 * @return org.jasig.portal.groups.IEntity
 */
private IEntity getNewEntity(Class type, String key) throws GroupsException
{
    return  new EntityImpl(key, type);
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
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) throws Exception
{
    String[] mainArgs = {"org.jasig.portal.groups.filesystem.FileSystemGroupsTest"};
    print("START TESTING FILESYSTEM GROUP STORE" + CR);
    TestRunner.main(mainArgs);
    print(CR + "END TESTING FILESYSTEM GROUP STORE");
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
 */
protected void setUp()
{
//    print("Entering FilesystemGroupsTester.setUp()");
    try {
        if ( GROUP_CLASS == null )
            { GROUP_CLASS = Class.forName("org.jasig.portal.groups.IEntityGroup"); }
        if ( IPERSON_CLASS == null )
            { IPERSON_CLASS = Class.forName("org.jasig.portal.security.IPerson"); }

    numTestEntities = 10;
    numTestFiles = 2;
    allFiles = new ArrayList();
    directoryFiles = new ArrayList();
    keyFiles = new ArrayList();

    char sep = getGroupStore().getGoodSeparator();
    File groupsRootDir, iPersonGroupsRootDir;
    String fileName = null;
    File f=null, ff=null, fff=null;

    int i=0, j=0, k=0;
    int totalNumTestFiles = numTestFiles + numTestFiles * numTestFiles +
      numTestFiles * numTestFiles * numTestFiles;

    // Entities and their keys:
    testEntityKeys = new String[numTestEntities];
    java.util.Random random = new java.util.Random();
    for (i=0; i<numTestEntities; i++)
        { testEntityKeys[i] = (getRandomString(random, 3) + i); }

    // File names:
    testFileNames = new String[totalNumTestFiles];
    random = new java.util.Random();
    for (i=0; i<totalNumTestFiles; i++)
        { testFileNames[i] = (getRandomString(random, 3) + i); }

    // GroupKeys:
    testGroupKeys = new ArrayList();

    // Create directory structure:
    File gr = getGroupsRoot();
    if ( gr == null )
    {
        print("COULD NOT CREATE GROUPS ROOT DIRECTORY!!!");
        print("You must have WRITE permission on either user.home or the current directory.");
        throw new RuntimeException("Could not create groups root directory.");
    }
    String tempGroupsRoot = gr.getAbsolutePath();
    getGroupStore().setGroupsRootPath(tempGroupsRoot);
    GROUPS_ROOT = getGroupStore().getGroupsRootPath();

    GROUP_SEPARATOR = IGroupConstants.NODE_SEPARATOR;

    // initialize composite service:
    // GroupService.findGroup("local" + GROUP_SEPARATOR + "0");

    IPERSON_GROUPS_ROOT = GROUPS_ROOT + IPERSON_CLASS.getName();
    iPersonGroupsRootDir = new File(IPERSON_GROUPS_ROOT);
    if ( ! iPersonGroupsRootDir.exists() )
    {
        iPersonGroupsRootDir.mkdir();
        allFiles.add(iPersonGroupsRootDir);
    }
    int fileNameIdx = 0;
    for (i=0; i<numTestFiles; i++)
    {
        fileName = iPersonGroupsRootDir.getPath() + sep + testFileNames[fileNameIdx++];
        f = new File(fileName);
        f.mkdir();
        allFiles.add(f);
        directoryFiles.add(f);
        for (j=numTestFiles; j<(numTestFiles*2); j++)
        {
            fileName = f.getPath() + sep + testFileNames[fileNameIdx++];
            ff = new File(fileName);
            ff.mkdir();
            allFiles.add(ff);
            directoryFiles.add(ff);
            for (k=(numTestFiles*2); k<(numTestFiles*3); k++)
            {
                fileName = ff.getPath() + sep + testFileNames[fileNameIdx++];
                fff = new File(fileName);
                fff.createNewFile();
                addIdsToFile(fff);
                allFiles.add(fff);
                keyFiles.add(fff);
            }
        }
    }
    
    this.testDataSource = new TransientDatasource();
    Connection con = testDataSource.getConnection();
    
    con.prepareStatement("CREATE TABLE UP_ENTITY_TYPE " +
                              "(ENTITY_TYPE_ID INTEGER, " +
                              "ENTITY_TYPE_NAME VARCHAR, " +
                              "DESCRIPTIVE_NAME VARCHAR)").execute();

    con.prepareStatement("INSERT INTO UP_ENTITY_TYPE " +
                              "VALUES (1, 'java.lang.Object', 'Generic')").execute();

    con.prepareStatement("INSERT INTO UP_ENTITY_TYPE " +
                             "VALUES (2, 'org.jasig.portal.security.IPerson', 'IPerson')").execute();

    con.prepareStatement("INSERT INTO UP_ENTITY_TYPE " +
                             "VALUES (3, 'org.jasig.portal.groups.IEntityGroup', 'Group')").execute();
    
    con.prepareStatement("INSERT INTO UP_ENTITY_TYPE " +
                             "VALUES (4, 'org.jasig.portal.ChannelDefinition', 'Channel')").execute();

    con.prepareStatement("INSERT INTO UP_ENTITY_TYPE " +
                             "VALUES (5, 'org.jasig.portal.groups.IEntity', 'Grouped Entity')").execute();

    con.close(); 
    
    // initialize EntityTypes
    EntityTypes.singleton(testDataSource);
    

    } // end try
    catch (Exception ex) { print("FileSystemGroupsTest.setUp(): " + ex.getMessage());}

  //  print("Leaving FileSystemGroupsTest.setUp()" + CR);

 }
/**
 */
protected void tearDown()
{
//    print("Entering FileSystemGroupsTest.tearDown()");
    try
    {
        testEntityKeys = null;
        testFileNames = null;
        testGroupKeys = null;

        File[] oldFiles = (File[])allFiles.toArray(new File[allFiles.size()]);

        for ( int i = oldFiles.length; i>0; i-- )
            { oldFiles[i - 1].delete(); }
        
        getGroupsRoot().delete();

        allFiles = null;
        directoryFiles = null;
        keyFiles = null;
        groupStore = null;
        groupsRoot = null;
        
        
        Connection con = this.testDataSource.getConnection();
        
        con.prepareStatement("DROP TABLE UP_ENTITY_TYPE").execute();
        con.prepareStatement("SHUTDOWN").execute();

        con.close();
        
        this.testDataSource = null;


    }
    catch (Exception ex) { print("FileSystemGroupsTest.tearDown()" + ex.getMessage());}

//    print("Leaving FilesystemGroupsTester.tearDown()");

}
/**
 * Tests IEntityGroupStore.find(), which returns an instance of IEntityGroup
 * given a key.
 */
public void testFind() throws Exception
{
    print("***** ENTERING FilesyStemGroupsTester.testFind() *****" + CR);

    String msg = null;
    Class type = IPERSON_CLASS;
    String existingKey = null, nonExistingKey = null;
    IEntityGroup existingGroup = null;
    File f = null;

    msg = "Finding existing groups by key...";
    print(msg);
    for ( Iterator itr = directoryFiles.iterator(); itr.hasNext(); )
    {
        f = (File) itr.next();
        existingKey = getKeyFromFile(f);
        msg = "Finding group key " + existingKey;
        existingGroup = getGroupStore().find(existingKey);
        assertNotNull(msg, existingGroup);
    }

    nonExistingKey = existingKey + "x";
    msg = "Finding non-existing key: " + nonExistingKey;
    print(msg);
    existingGroup = getGroupStore().find(nonExistingKey);
    assertNull(msg, existingGroup);

    print("Test completed successfully." + CR);

    print("***** LEAVING FileSystemGroupsTest.testFind() *****" + CR);

}
/**
 * Tests IEntityGroupStore.findContainingGroups() for both an IEntity and
 * an IEntityGroup.
 */
public void testFindContainingGroups() throws Exception
{
    print("***** ENTERING FileSystemGroupsTest.testFindContainingGroups() *****" + CR);

    String msg = null;
    Class type = IPERSON_CLASS;
    String ipersonKey = null, groupKey = null;
    IEntityGroup group = null, containingGroup = null;
    IEntity ent = null;
    File f = null;
    Iterator itr = null;
    List containingGroups = new ArrayList();

    msg = "Finding containing groups for entity keys...";
    print(msg);
    for ( int i=0; i<testEntityKeys.length; i++ )
    {
        ent = getNewIPersonEntity(testEntityKeys[i]);
        msg = "Finding containing groups for " + ent;
        print(msg);
        containingGroups.clear();
        for (itr = getGroupStore().findContainingGroups(ent); itr.hasNext();)
        {
            group = (IEntityGroup) itr.next();
            containingGroups.add(group);
            assertTrue(msg, group instanceof IEntityGroup);
        }
        assertEquals(msg, keyFiles.size(), containingGroups.size());
    }

    ent = getNewIPersonEntity(NON_EXISTENT_ID);
    msg = "Finding containing groups for non-existent key: " + NON_EXISTENT_ID;
    print(msg);
    containingGroups.clear();
    for (itr = getGroupStore().findContainingGroups(ent); itr.hasNext();)
            { containingGroups.add(itr.next()); }
        assertEquals(msg, 0, containingGroups.size());

    msg = "Finding containing groups for groups...";
    print(msg);
    // Each file that contains keys should have 1 and only 1 containing group.
    for ( itr = keyFiles.iterator(); itr.hasNext(); )
    {
        f = (File) itr.next();
        group = findGroup(f);
        assertTrue(msg, group instanceof IEntityGroup);
        containingGroups.clear();

        for (Iterator cg = getGroupStore().findContainingGroups(group); cg.hasNext();)
        {
            containingGroup = (IEntityGroup) cg.next();
            assertTrue(msg, containingGroup instanceof IEntityGroup);
            containingGroups.add(containingGroup);
        }
        assertEquals(msg, 1, containingGroups.size());
    }
    
    msg = "Finding containing groups for a non-existent type...";
    print(msg);

    ent = getNewEntity(new Object().getClass(), testEntityKeys[0]);
    itr = getGroupStore().findContainingGroups(ent);
    boolean hasContainingGroup = itr.hasNext();
    assertTrue(msg, ! hasContainingGroup);
    
    print("Test completed successfully." + CR);

    print("***** LEAVING FileSystemGroupsTest.testFindContainingGroups() *****" + CR);

}
/**
 * Tests IEntityGroupStore.findMemberGroups(), findContainingGroups
 */
public void testFindEmbeddedMemberGroups() throws Exception
{
    print("***** ENTERING FileSystemGroupsTest.testFindEmbeddedMemberGroups() *****" + CR);

    String msg = null;
    IEntityGroup group = null, memberGroup = null;
    File f = null, f2 = null;
    String memberKeys[] = null;

    f = (File)keyFiles.get(keyFiles.size() - 1);  // member
    f2 = (File)keyFiles.get(keyFiles.size() - 2); // group
    String memberKey = getKeyFromFile(f);
    String groupKey = getKeyFromFile(f2);
    
    msg = "Now adding member group key " + memberKey + " to " + groupKey;
    print(msg);
    BufferedWriter bw = new BufferedWriter(new FileWriter(f2.getPath(), true));
    bw.write("group:" + memberKey);
    bw.newLine();
    bw.close();

    msg = "Finding member group keys for key file " + groupKey;
    print(msg);
    group = findGroup(f2);
    assertTrue(msg, group instanceof IEntityGroup);
    memberKeys = getGroupStore().findMemberGroupKeys(group);
    assertEquals(msg, 1, memberKeys.length);
    memberGroup = findGroup(memberKeys[0]);
    assertNotNull(msg, memberGroup);
    assertTrue(msg, getGroupStore().contains(group, memberGroup));

    print("Test completed successfully." + CR);

    print("***** LEAVING FileSystemGroupsTest.testFindEmbeddedMemberGroups() *****" + CR);

}
/**
 * Tests IEntityGroupStore.findEntitiesForGroup().
 */
public void testFindEntitiesForGroup() throws Exception
{
    print("***** ENTERING FileSystemGroupsTest.testFindEntitiesForGroup() *****" + CR);

    String msg = null;
    IEntityGroup group = null;
    String entityKey = null;
    File f = null, f2 = null;
    Iterator itr = null;
    List memberEntities = new ArrayList();

    msg = "Finding entities for files...";
    print(msg);

    for ( itr = keyFiles.iterator(); itr.hasNext(); )
    {
        f = (File) itr.next();
        msg = "finding group: " + f;
        group = findGroup(f);
        assertTrue(msg, group instanceof IEntityGroup);
        memberEntities.clear();

        for (Iterator members = getGroupStore().getEntityIdsFromFile(f).iterator(); members.hasNext();)
        {
            entityKey = (String) members.next();
            assertTrue(msg, entityKey != null);
            assertTrue(msg, entityKey.length() > 0);
            memberEntities.add(entityKey);
        }
        assertEquals(msg, numTestEntities, memberEntities.size());
    }


    f = (File) keyFiles.get(0);
    f2 = f.getParentFile();
    msg = "Finding entities for " + f2 + " (should have none).";
    group = findGroup(f2);
    assertTrue(msg, group instanceof IEntityGroup);
    boolean hasEntities = getGroupStore().findEntitiesForGroup(group).hasNext();
    assertTrue(msg, ! hasEntities);

    print("Test completed successfully." + CR);

    print("***** LEAVING FileSystemGroupsTest.testFindEntitiesForGroup() *****" + CR);

}
/**
 * Tests IEntityGroupStore.findMemberGroupKeys().
 */
public void testFindMemberGroupKeys() throws Exception
{
    print("***** ENTERING FileSystemGroupsTest.testFindMemberGroupKeys() *****" + CR);

    String msg = null;
    IEntityGroup group = null, memberGroup = null;
    File f = null, f2 = null;
    Iterator itr = null;
    String memberKeys[] = null;

    msg = "Finding member group keys for directory files...";
    print(msg);

    for ( itr = directoryFiles.iterator(); itr.hasNext(); )
    {
        f = (File) itr.next();
        msg = "Finding member group keys for group: " + f;
        group = findGroup(f);
        assertTrue(msg, group instanceof IEntityGroup);
        memberKeys = getGroupStore().findMemberGroupKeys(group);
        assertEquals(msg, numTestFiles, memberKeys.length);
        for ( int i=0; i<memberKeys.length; i++ )
        {
            memberGroup = findGroup(memberKeys[i]);
            assertNotNull(msg, memberGroup);
            assertTrue(msg, getGroupStore().contains(group, memberGroup));
        }
    }

    msg = "Finding member group keys for key files...";
    print(msg);

    for ( itr = keyFiles.iterator(); itr.hasNext(); )
    {
        f = (File) itr.next();
        msg = "Finding member group keys for group: " + f;
        group = findGroup(f);
        assertTrue(msg, group instanceof IEntityGroup);
        memberKeys = getGroupStore().findMemberGroupKeys(group);
        assertEquals(msg, 0, memberKeys.length);
    }


    print("Test completed successfully." + CR);

    print("***** LEAVING FileSystemGroupsTest.testFindMemberGroupKeys() *****" + CR);

}
/**
 * Tests IEntityGroupStore.findMemberGroups().
 */
public void testFindMemberGroups() throws Exception
{
    print("***** ENTERING FileSystemGroupsTest.testFindMemberGroups() *****" + CR);

    String msg = null, groupKey = null;
    IEntityGroup group = null, memberGroup = null;
    File f = null, f2 = null;
    Iterator itr = null;
    Iterator memberGroups = null;

    msg = "Finding member groups for directory files...";
    print(msg);

    for ( itr = directoryFiles.iterator(); itr.hasNext(); )
    {
        f = (File) itr.next();
        msg = "Finding member groups for group: " + f;
        group = findGroup(f);
        assertTrue(msg, group instanceof IEntityGroup);
        memberGroups = getGroupStore().findMemberGroups(group);
        while ( memberGroups.hasNext() )
        {
            memberGroup = (IEntityGroup)memberGroups.next();
            assertNotNull(msg, memberGroup);
            groupKey = memberGroup.getKey();
            memberGroup = findGroup(groupKey);
            assertTrue(msg, getGroupStore().contains(group, memberGroup));
        }
    }

    msg = "Finding member groups for key files...";
    print(msg);

    for ( itr = keyFiles.iterator(); itr.hasNext(); )
    {
        f = (File) itr.next();
        msg = "Finding member groups for group: " + f;
        group = findGroup(f);
        assertTrue(msg, group instanceof IEntityGroup);
        memberGroups = getGroupStore().findMemberGroups(group);
        assertTrue( msg, ! memberGroups.hasNext() );
    }

    print("Test completed successfully." + CR);

    print("***** LEAVING FileSystemGroupsTest.testFindMemberGroups() *****" + CR);

}
/**
 * Tests IEntityGroupStore.searchForGroups(), which returns EntityIdentifier[] given
 * a search string.
 */
public void testSearchForGroups() throws Exception
{
    print("***** ENTERING FileSystemGroupsTest.testSearchForGroups() *****" + CR);

    String msg = null;
    String is = null, startsWith = null, endsWith = null, contains = null, badQuery = null;
    Class type = IPERSON_CLASS;
    IEntityGroup existingGroup = null;
    IGroupMember member = null;
    EntityIdentifier[] ids = null;

    msg = "Searching for existing groups...";
    print(msg);
    for ( int i=0; i<testFileNames.length; i++ )
    {
        is = testFileNames[i];
        startsWith = is.substring(0, (is.length() - 1) );
        endsWith = is.substring(1);
        contains = is.substring(1, (is.length() - 1));
        badQuery = is + " a b c";

        msg = "Searching for IS " + is;
        ids = getGroupStore().searchForGroups(is, IGroupConstants.IS, type);
        assertEquals(msg, ids.length, 1);
        member = findGroup(ids[0].getKey());
        assertTrue(msg, member.isGroup());

        msg = "Searching for STARTS WITH " + startsWith;
        ids = getGroupStore().searchForGroups(startsWith, IGroupConstants.STARTS_WITH, type);
        assertTrue(msg, ids.length > 0);

        msg = "Searching for ENDS WITH " + endsWith;
        ids = getGroupStore().searchForGroups(endsWith, IGroupConstants.ENDS_WITH, type);
        assertTrue(msg, ids.length > 0);

        msg = "Searching for CONTAINS " + contains;
        ids = getGroupStore().searchForGroups(contains, IGroupConstants.CONTAINS, type);
        assertTrue(msg, ids.length > 0);

        msg = "Searching for IS " + badQuery;
        ids = getGroupStore().searchForGroups(badQuery, IGroupConstants.IS, type);
        assertEquals(msg, ids.length, 0);

    }

    print("Test completed successfully." + CR);

    print("***** LEAVING FileSystemGroupsTest.testSearchForGroups() *****" + CR);

}
}
