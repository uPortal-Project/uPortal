package org.jasig.portal.groups.filesystem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.ComponentGroupServiceDescriptor;
import org.jasig.portal.groups.GroupServiceConfiguration;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.RDBMEntityStore;
import org.jasig.portal.services.GroupService;

/**
 * This class tests the filesystem group store in the context of the composite
 * group service.  In order to run these tests, you must (i) define a composite 
 * groups service; (ii) configure a component file system service named 
 * <code>GROUP_SERVICE_NAME</code> (see below); and (iii) create an accessible 
 * groupsRoot directory declared in (ii).
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class FileSystemGroupsTester extends TestCase {
    private static Class GROUP_CLASS;
    private static Class IPERSON_CLASS;
    private static String CR = "\n";

    private String[] testEntityKeys;
    private String[] testFileNames;
    private List testGroupKeys;
    private int numTestFiles;
    private int numTestEntities;

    private static String GROUPS_ROOT;
    private static String IPERSON_GROUPS_ROOT;
    private List allFiles = null, directoryFiles = null, keyFiles = null;
    private String NON_EXISTENT_ID = "xyzxyzxyz";
    private String GROUP_SERVICE_NAME = "filesystem";
    private IEntityGroupStore groupStore;
    private String GROUP_SEPARATOR;
/**
 * FileSystemGroupsTester.
 */
public FileSystemGroupsTester(String name) {
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
    catch (Exception ex) { print("FileSystemGroupsTester.addIdsToFile(): " + ex.getMessage());}
 }
/**
 * @return org.jasig.portal.groups.IEntityGroup
 */
private IEntityGroup findGroup(File file) throws GroupsException
{
    String key = getKeyFromFile(file);
    return findGroup(GROUP_SERVICE_NAME + GROUP_SEPARATOR + key);
}
/**
 * @return org.jasig.portal.groups.IEntityGroup
 */
private IEntityGroup findGroup(String key) throws GroupsException
{
    return GroupService.findGroup(key);
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
 * @return java.lang.String
 * @param serviceName java.lang.String
 */
private String getGroupsRoot(String serviceName) 
{
    ComponentGroupServiceDescriptor desc = null;
    String groupsRoot = null;
    try 
    {
        List descriptors = GroupServiceConfiguration.getConfiguration().getServiceDescriptors();
        for ( Iterator itr=descriptors.iterator(); itr.hasNext(); )
        {
            desc = (ComponentGroupServiceDescriptor) itr.next();
            if ( desc.getName().equals(serviceName) )
            {
                groupsRoot = (String)desc.get("groupsRoot");
                break;
            }
        }
    }
    catch (Exception ex) {}
    return groupsRoot;
}
/**
 * @return FileSystemGroupStore
 */
private FileSystemGroupStore getGroupStore() throws GroupsException
{
    if ( groupStore == null )
        { groupStore = new FileSystemGroupStore(); }
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
        if ( GROUP_SEPARATOR.equals(".") )
            { key = key.replace(FileSystemGroupStore.PERIOD, FileSystemGroupStore.SUBSTITUTE_PERIOD); } 
    }
    return key;
}
/**
 * @return org.jasig.portal.groups.IEntity
 */
private IEntity getNewEntity(String key) throws GroupsException
{
    return 	GroupService.getEntity(key, IPERSON_CLASS);
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
    String[] mainArgs = {"org.jasig.portal.groups.filesystem.FileSystemGroupsTester"};
    print("START TESTING FILESYSTEM GROUP STORE");
    printBlankLine();
    junit.swingui.TestRunner.main(mainArgs);
    printBlankLine();
    print("END TESTING FILESYSTEM GROUP STORE");

}
/**
 * @param msg java.lang.String
 */
private static void print(String msg)
{
    java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
    System.out.println(ts + " : " + msg);
}
private static void printBlankLine()
{
    System.out.println("");
}
/**
 */
protected void setUp()
{
    print("Entering FilesystemGroupsTester.setUp()");
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
    String tempGroupsRoot = getGroupsRoot("filesystem");
    getGroupStore().setGroupsRootPath(tempGroupsRoot);
    GROUPS_ROOT = getGroupStore().getGroupsRootPath();

    // initialize composite service:
    GROUP_SEPARATOR = GroupServiceConfiguration.getConfiguration().getNodeSeparator();
    GroupService.findGroup("local" + GROUP_SEPARATOR + "0");

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

    } // end try
    catch (Exception ex) { print("FileSystemGroupsTester.setUp(): " + ex.getMessage());}

    print("Leaving FileSystemGroupsTester.setUp()" + CR);

 }
/**
 * @return junit.framework.Test
 */
public static junit.framework.Test suite() {
    TestSuite suite = new TestSuite();

  suite.addTest(new FileSystemGroupsTester("testFind"));
  suite.addTest(new FileSystemGroupsTester("testFindContainingGroups"));
  suite.addTest(new FileSystemGroupsTester("testFindEntitiesForGroup"));
  suite.addTest(new FileSystemGroupsTester("testFindMemberGroupKeys"));
  suite.addTest(new FileSystemGroupsTester("testFindMemberGroups"));
  suite.addTest(new FileSystemGroupsTester("testSearchForGroups"));
  suite.addTest(new FileSystemGroupsTester("testFindEmbeddedMemberGroups"));

//	Add more tests here.
//  NB: Order of tests is not guaranteed.

    return suite;
}
/**
 */
protected void tearDown()
{
    print("Entering FileSystemGroupsTester.tearDown()");
    try
    {
        testEntityKeys = null;
        testFileNames = null;
        testGroupKeys = null;

        File[] oldFiles = (File[])allFiles.toArray(new File[allFiles.size()]);

        for ( int i = oldFiles.length; i>0; i-- )
            { oldFiles[i - 1].delete(); }

        allFiles = null;
        directoryFiles = null;
        keyFiles = null;
        groupStore = null;

    }
    catch (Exception ex) { print("FileSystemGroupsTester.tearDown()" + ex.getMessage());}

    print("Leaving FilesystemGroupsTester.tearDown()");

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

    print("***** LEAVING FileSystemGroupsTester.testFind() *****" + CR);

}
/**
 * Tests IEntityGroupStore.findContainingGroups() for both an IEntity and
 * an IEntityGroup.
 */
public void testFindContainingGroups() throws Exception
{
    print("***** ENTERING FileSystemGroupsTester.testFindContainingGroups() *****" + CR);

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
        ent = getNewEntity(testEntityKeys[i]);
        msg = "Finding containing groups for " + ent;
        print(msg);
        containingGroups.clear();
        for (itr = ent.getContainingGroups(); itr.hasNext();)
        {
            group = (IEntityGroup) itr.next();
            containingGroups.add(group);
            assertTrue(msg, group instanceof IEntityGroup);
        }
        assertEquals(msg, keyFiles.size(), containingGroups.size());
    }

    ent = getNewEntity(NON_EXISTENT_ID);
    msg = "Finding containing groups for non-existent key: " + NON_EXISTENT_ID;
    print(msg);
    containingGroups.clear();
    for (itr = ent.getContainingGroups(); itr.hasNext();)
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

        for (Iterator cg = group.getContainingGroups(); cg.hasNext();)
        {
            containingGroup = (IEntityGroup) cg.next();
            assertTrue(msg, containingGroup instanceof IEntityGroup);
            containingGroups.add(containingGroup);
        }
        assertEquals(msg, 1, containingGroups.size());
    }
    
    msg = "Finding containing groups for a non-existent type...";
    print(msg);

    ent = GroupService.getEntity(testEntityKeys[0], new Object().getClass());
    itr = ent.getContainingGroups();
    boolean hasContainingGroup = itr.hasNext();
    assertTrue(msg, ! hasContainingGroup);
    
    print("Test completed successfully." + CR);

    print("***** LEAVING FileSystemGroupsTester.testFindContainingGroups() *****" + CR);

}
/**
 * Tests IEntityGroupStore.findMemberGroups(), findContainingGroups
 */
public void testFindEmbeddedMemberGroups() throws Exception
{
    print("***** ENTERING FileSystemGroupsTester.testFindEmbeddedMemberGroups() *****" + CR);

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
    memberGroup = findGroup(GROUP_SERVICE_NAME + GROUP_SEPARATOR + memberKeys[0]);
    assertNotNull(msg, memberGroup);
    assertTrue(msg, memberGroup.isMemberOf(group));

    print("Test completed successfully." + CR);

    print("***** LEAVING FileSystemGroupsTester.testFindEmbeddedMemberGroups() *****" + CR);

}
/**
 * Tests IEntityGroupStore.findEntitiesForGroup().
 */
public void testFindEntitiesForGroup() throws Exception
{
    print("***** ENTERING FileSystemGroupsTester.testFindEntitiesForGroup() *****" + CR);

    String msg = null;
    IEntityGroup group = null;
    IEntity ent = null;
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

        for (Iterator members = group.getEntities(); members.hasNext();)
        {
            ent = (IEntity) members.next();
            assertTrue(msg, ent instanceof IEntity);
            memberEntities.add(ent);
        }
        assertEquals(msg, numTestEntities, memberEntities.size());
    }


    f = (File) keyFiles.get(0);
    f2 = f.getParentFile();
    msg = "Finding entities for " + f2 + " (should have none).";
    group = findGroup(f2);
    assertTrue(msg, group instanceof IEntityGroup);
    boolean hasEntities = group.getEntities().hasNext();
    assertTrue(msg, ! hasEntities);

    msg = "Finding entities for a directory (should be none).";

    print("Test completed successfully." + CR);

    print("***** LEAVING FileSystemGroupsTester.testFindEntitiesForGroup() *****" + CR);

}
/**
 * Tests IEntityGroupStore.findMemberGroupKeys().
 */
public void testFindMemberGroupKeys() throws Exception
{
    print("***** ENTERING FileSystemGroupsTester.testFindMemberGroupKeys() *****" + CR);

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
            memberGroup = findGroup(GROUP_SERVICE_NAME + GROUP_SEPARATOR + memberKeys[i]);
            assertNotNull(msg, memberGroup);
            assertTrue(msg, memberGroup.isMemberOf(group));
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

    print("***** LEAVING FileSystemGroupsTester.testFindMemberGroupKeys() *****" + CR);

}
/**
 * Tests IEntityGroupStore.findMemberGroups().
 */
public void testFindMemberGroups() throws Exception
{
    print("***** ENTERING FileSystemGroupsTester.testFindMemberGroups() *****" + CR);

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
            groupKey = GROUP_SERVICE_NAME + GROUP_SEPARATOR + memberGroup.getKey();
            memberGroup = findGroup(groupKey);
            assertTrue(msg, memberGroup.isMemberOf(group));
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

    print("***** LEAVING FileSystemGroupsTester.testFindMemberGroups() *****" + CR);

}
/**
 * Tests IEntityGroupStore.searchForGroups(), which returns EntityIdentifier[] given
 * a search string.
 */
public void testSearchForGroups() throws Exception
{
    print("***** ENTERING FileSystemGroupsTester.testSearchForGroups() *****" + CR);

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
        member = GroupService.findGroup(GROUP_SERVICE_NAME + GROUP_SEPARATOR + ids[0].getKey());
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

    print("***** LEAVING FileSwystemGroupsTester.testSearchForGroups() *****" + CR);

}
}
