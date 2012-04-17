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

package org.jasig.portal.groups.filesystem;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
@Ignore
public class FileSystemGroupsTest {
    protected static final Log LOG = LogFactory.getLog(FileSystemGroupsTest.class);

    private static Class GROUP_CLASS;
    private static Class IPERSON_CLASS;
    private static String CR = "\n";

    private String[] testEntityKeys;
    private String[] testFileNames;
    private List testGroupKeys;
    private int numTestFiles;
    private int numTestEntities;

    
    @Rule
    public final TemporaryFolder groupsRoot = new TemporaryFolder();

    private String GROUPS_ROOT;
    private String IPERSON_GROUPS_ROOT;
    private List allFiles = null, directoryFiles = null, keyFiles = null;
    private final String NON_EXISTENT_ID = "xyzxyzxyz";
    private IEntityGroupStore groupStore;
    private String GROUP_SEPARATOR;

    private DataSource testDataSource;

    /**
     */
    protected void addIdsToFile(File f) {
        final long now = System.currentTimeMillis() / 10;
        long div = now % 5;
        div += 5;

        try {
            String line = null, start = null;
            final BufferedWriter bw = new BufferedWriter(new FileWriter(f));

            bw.write("# test file written at " + new java.util.Date());
            bw.newLine();
            bw.write("#");
            bw.newLine();

            for (int i = 0; i < this.numTestEntities; i++) {
                start = i > 0 && i % div == 0 ? "   " : "";
                line = start + this.testEntityKeys[i] + " is entity " + (i + 1);
                bw.write(line);
                bw.newLine();
            }

            bw.write("# end of test file ");
            bw.newLine();
            bw.write("#");
            bw.newLine();

            bw.close();

        } // end try
        catch (final Exception ex) {
            print("FileSystemGroupsTest.addIdsToFile(): " + ex.getMessage());
        }
    }

    /**
     * @return org.jasig.portal.groups.IEntityGroup
     */
    private IEntityGroup findGroup(File file) throws GroupsException {
        final String key = this.getKeyFromFile(file);
        return this.findGroup(key);
    }

    /**
     * Note that this is the local, not composite, key.
     * @return org.jasig.portal.groups.IEntityGroup
     */
    private IEntityGroup findGroup(String key) throws GroupsException {
        return this.getGroupStore().find(key);
    }

    private File getGroupsRoot() {
        return this.groupsRoot.getRoot();
    }


    /**
     * @return FileSystemGroupStore
     */
    private FileSystemGroupStore getGroupStore() throws GroupsException {
        if (this.groupStore == null) {
            final GroupServiceConfiguration config = new GroupServiceConfiguration();
            final Map atts = config.getAttributes();
            atts.put("nodeSeparator", IGroupConstants.NODE_SEPARATOR);
            this.groupStore = new FileSystemGroupStore(config);
        }
        return (FileSystemGroupStore) this.groupStore;
    }

    /**
     * @return org.jasig.portal.groups.IEntityGroup
     */
    private String getKeyFromFile(File file) throws GroupsException {
        String key = file.getPath();
        if (key.startsWith(this.GROUPS_ROOT)) {
            key = key.substring(this.GROUPS_ROOT.length());
            if (this.GROUP_SEPARATOR.equals(String.valueOf(FileSystemGroupStore.PERIOD))) {
                key = key.replace(FileSystemGroupStore.PERIOD, FileSystemGroupStore.SUBSTITUTE_PERIOD);
            }
        }
        return key;
    }

    /**
     * @return org.jasig.portal.groups.IEntity
     */
    private IEntity getNewIPersonEntity(String key) throws GroupsException {
        return this.getNewEntity(IPERSON_CLASS, key);
    }

    /**
     * @return org.jasig.portal.groups.IEntity
     */
    private IEntity getNewEntity(Class type, String key) throws GroupsException {
        return new EntityImpl(key, type);
    }

    /**
    *  @return java.lang.String
     * @param length int
     */
    private String getRandomString(java.util.Random r, int length) {

        final char[] chars = new char[length];

        for (int i = 0; i < length; i++) {
            final int diff = r.nextInt(25);
            final int charValue = 'A' + diff;
            chars[i] = (char) charValue;
        }
        return new String(chars);
    }

    /**
     * Starts the application.
     * @param args an array of command-line arguments
     */
    public static void main(java.lang.String[] args) throws Exception {
        final String[] mainArgs = { "org.jasig.portal.groups.filesystem.FileSystemGroupsTest" };
        print("START TESTING FILESYSTEM GROUP STORE" + CR);
        TestRunner.main(mainArgs);
        print(CR + "END TESTING FILESYSTEM GROUP STORE");
    }

    /**
     * @param msg java.lang.String
     */
    private static void print(String msg) {
        LOG.debug(msg);
    }

    /**
     */
    @Before
    public void setUp() throws Exception {
        if (GROUP_CLASS == null) {
            GROUP_CLASS = Class.forName("org.jasig.portal.groups.IEntityGroup");
        }
        if (IPERSON_CLASS == null) {
            IPERSON_CLASS = Class.forName("org.jasig.portal.security.IPerson");
        }

        this.numTestEntities = 10;
        this.numTestFiles = 2;
        this.allFiles = new ArrayList();
        this.directoryFiles = new ArrayList();
        this.keyFiles = new ArrayList();

        final char sep = this.getGroupStore().getGoodSeparator();
        File iPersonGroupsRootDir;
        String fileName = null;
        File f = null, ff = null, fff = null;

        int i = 0, j = 0, k = 0;
        final int totalNumTestFiles = this.numTestFiles + this.numTestFiles * this.numTestFiles + this.numTestFiles
                * this.numTestFiles * this.numTestFiles;

        // Entities and their keys:
        this.testEntityKeys = new String[this.numTestEntities];
        java.util.Random random = new java.util.Random();
        for (i = 0; i < this.numTestEntities; i++) {
            this.testEntityKeys[i] = this.getRandomString(random, 3) + i;
        }

        // File names:
        this.testFileNames = new String[totalNumTestFiles];
        random = new java.util.Random();
        for (i = 0; i < totalNumTestFiles; i++) {
            this.testFileNames[i] = this.getRandomString(random, 3) + i;
        }

        // GroupKeys:
        this.testGroupKeys = new ArrayList();

        // Create directory structure:
        final File gr = this.getGroupsRoot();
        if (gr == null) {
            throw new RuntimeException("COULD NOT CREATE GROUPS ROOT DIRECTORY!!!\n" + 
                    "You must have WRITE permission on either user.home or the current directory.\n" + 
                    "Could not create groups root directory.");
        }
        final String tempGroupsRoot = gr.getAbsolutePath();
        this.getGroupStore().setGroupsRootPath(tempGroupsRoot);
        this.GROUPS_ROOT = this.getGroupStore().getGroupsRootPath();

        this.GROUP_SEPARATOR = IGroupConstants.NODE_SEPARATOR;

        // initialize composite service:
        // GroupService.findGroup("local" + GROUP_SEPARATOR + "0");

        this.IPERSON_GROUPS_ROOT = this.GROUPS_ROOT + IPERSON_CLASS.getName();
        iPersonGroupsRootDir = new File(this.IPERSON_GROUPS_ROOT);
        if (!iPersonGroupsRootDir.exists()) {
            iPersonGroupsRootDir.mkdir();
            this.allFiles.add(iPersonGroupsRootDir);
        }
        int fileNameIdx = 0;
        for (i = 0; i < this.numTestFiles; i++) {
            fileName = iPersonGroupsRootDir.getPath() + sep + this.testFileNames[fileNameIdx++];
            f = new File(fileName);
            f.mkdir();
            this.allFiles.add(f);
            this.directoryFiles.add(f);
            for (j = this.numTestFiles; j < this.numTestFiles * 2; j++) {
                fileName = f.getPath() + sep + this.testFileNames[fileNameIdx++];
                ff = new File(fileName);
                ff.mkdir();
                this.allFiles.add(ff);
                this.directoryFiles.add(ff);
                for (k = this.numTestFiles * 2; k < this.numTestFiles * 3; k++) {
                    fileName = ff.getPath() + sep + this.testFileNames[fileNameIdx++];
                    fff = new File(fileName);
                    fff.createNewFile();
                    this.addIdsToFile(fff);
                    this.allFiles.add(fff);
                    this.keyFiles.add(fff);
                }
            }
        }

        this.testDataSource = new TransientDatasource();
        final Connection con = this.testDataSource.getConnection();

        con.prepareStatement("CREATE TABLE UP_ENTITY_TYPE " + "(ENTITY_TYPE_ID INTEGER, "
                + "ENTITY_TYPE_NAME VARCHAR(1000), " + "DESCRIPTIVE_NAME VARCHAR(1000))").execute();

        con.prepareStatement("INSERT INTO UP_ENTITY_TYPE " + "VALUES (1, 'java.lang.Object', 'Generic')").execute();

        con.prepareStatement("INSERT INTO UP_ENTITY_TYPE "
                + "VALUES (2, 'org.jasig.portal.security.IPerson', 'IPerson')").execute();

        con.prepareStatement("INSERT INTO UP_ENTITY_TYPE "
                + "VALUES (3, 'org.jasig.portal.groups.IEntityGroup', 'Group')").execute();

        con.prepareStatement("INSERT INTO UP_ENTITY_TYPE "
                + "VALUES (4, 'org.jasig.portal.ChannelDefinition', 'Channel')").execute();

        con.prepareStatement("INSERT INTO UP_ENTITY_TYPE "
                + "VALUES (5, 'org.jasig.portal.groups.IEntity', 'Grouped Entity')").execute();

        con.close();

        // initialize EntityTypes
//        EntityTypes.singleton(this.testDataSource);

        //  print("Leaving FileSystemGroupsTest.setUp()" + CR);

    }

    /**
     */
    @After
    public void tearDown() throws Exception {
        this.testEntityKeys = null;
        this.testFileNames = null;
        this.testGroupKeys = null;

        final File[] oldFiles = (File[]) this.allFiles.toArray(new File[this.allFiles.size()]);

        for (int i = oldFiles.length; i > 0; i--) {
            oldFiles[i - 1].delete();
        }

        this.getGroupsRoot().delete();

        this.allFiles = null;
        this.directoryFiles = null;
        this.keyFiles = null;
        this.groupStore = null;

        final Connection con = this.testDataSource.getConnection();

        con.prepareStatement("DROP TABLE UP_ENTITY_TYPE").execute();
        con.prepareStatement("SHUTDOWN").execute();

        con.close();

        this.testDataSource = null;
    }

    /**
     * Tests IEntityGroupStore.find(), which returns an instance of IEntityGroup
     * given a key.
     */
    @Test
    public void testFind() throws Exception {
        print("***** ENTERING FilesyStemGroupsTester.testFind() *****" + CR);

        String msg = null;
        String existingKey = null, nonExistingKey = null;
        IEntityGroup existingGroup = null;
        File f = null;

        msg = "Finding existing groups by key...";
        print(msg);
        for (final Iterator itr = this.directoryFiles.iterator(); itr.hasNext();) {
            f = (File) itr.next();
            existingKey = this.getKeyFromFile(f);
            msg = "Finding group key " + existingKey;
            existingGroup = this.getGroupStore().find(existingKey);
            assertNotNull(msg, existingGroup);
        }

        nonExistingKey = existingKey + "x";
        msg = "Finding non-existing key: " + nonExistingKey;
        print(msg);
        existingGroup = this.getGroupStore().find(nonExistingKey);
        assertNull(msg, existingGroup);

        print("Test completed successfully." + CR);

        print("***** LEAVING FileSystemGroupsTest.testFind() *****" + CR);

    }

    /**
     * Tests IEntityGroupStore.findContainingGroups() for both an IEntity and
     * an IEntityGroup.
     */
    @Test
    public void testFindContainingGroups() throws Exception {
        print("***** ENTERING FileSystemGroupsTest.testFindContainingGroups() *****" + CR);

        String msg = null;
        final String groupKey = null;
        IEntityGroup group = null, containingGroup = null;
        IEntity ent = null;
        File f = null;
        Iterator itr = null;
        final List containingGroups = new ArrayList();

        msg = "Finding containing groups for entity keys...";
        print(msg);
        for (final String testEntityKey : this.testEntityKeys) {
            ent = this.getNewIPersonEntity(testEntityKey);
            msg = "Finding containing groups for " + ent;
            print(msg);
            containingGroups.clear();
            for (itr = this.getGroupStore().findContainingGroups(ent); itr.hasNext();) {
                group = (IEntityGroup) itr.next();
                containingGroups.add(group);
                assertTrue(msg, group instanceof IEntityGroup);
            }
            assertEquals(msg, this.keyFiles.size(), containingGroups.size());
        }

        ent = this.getNewIPersonEntity(this.NON_EXISTENT_ID);
        msg = "Finding containing groups for non-existent key: " + this.NON_EXISTENT_ID;
        print(msg);
        containingGroups.clear();
        for (itr = this.getGroupStore().findContainingGroups(ent); itr.hasNext();) {
            containingGroups.add(itr.next());
        }
        assertEquals(msg, 0, containingGroups.size());

        msg = "Finding containing groups for groups...";
        print(msg);
        // Each file that contains keys should have 1 and only 1 containing group.
        for (itr = this.keyFiles.iterator(); itr.hasNext();) {
            f = (File) itr.next();
            group = this.findGroup(f);
            assertTrue(msg, group instanceof IEntityGroup);
            containingGroups.clear();

            for (final Iterator cg = this.getGroupStore().findContainingGroups(group); cg.hasNext();) {
                containingGroup = (IEntityGroup) cg.next();
                assertTrue(msg, containingGroup instanceof IEntityGroup);
                containingGroups.add(containingGroup);
            }
            assertEquals(msg, 1, containingGroups.size());
        }

        msg = "Finding containing groups for a non-existent type...";
        print(msg);

        ent = this.getNewEntity(new Object().getClass(), this.testEntityKeys[0]);
        itr = this.getGroupStore().findContainingGroups(ent);
        final boolean hasContainingGroup = itr.hasNext();
        assertTrue(msg, !hasContainingGroup);

        print("Test completed successfully." + CR);

        print("***** LEAVING FileSystemGroupsTest.testFindContainingGroups() *****" + CR);

    }

    /**
     * Tests IEntityGroupStore.findMemberGroups(), findContainingGroups
     */
    @Test
    public void testFindEmbeddedMemberGroups() throws Exception {
        print("***** ENTERING FileSystemGroupsTest.testFindEmbeddedMemberGroups() *****" + CR);

        String msg = null;
        IEntityGroup group = null, memberGroup = null;
        File f = null, f2 = null;
        String memberKeys[] = null;

        f = (File) this.keyFiles.get(this.keyFiles.size() - 1); // member
        f2 = (File) this.keyFiles.get(this.keyFiles.size() - 2); // group
        final String memberKey = this.getKeyFromFile(f);
        final String groupKey = this.getKeyFromFile(f2);

        msg = "Now adding member group key " + memberKey + " to " + groupKey;
        print(msg);
        final BufferedWriter bw = new BufferedWriter(new FileWriter(f2.getPath(), true));
        bw.write("group:" + memberKey);
        bw.newLine();
        bw.close();

        msg = "Finding member group keys for key file " + groupKey;
        print(msg);
        group = this.findGroup(f2);
        assertTrue(msg, group instanceof IEntityGroup);
        memberKeys = this.getGroupStore().findMemberGroupKeys(group);
        assertEquals(msg, 1, memberKeys.length);
        memberGroup = this.findGroup(memberKeys[0]);
        assertNotNull(msg, memberGroup);
        assertTrue(msg, this.getGroupStore().contains(group, memberGroup));

        print("Test completed successfully." + CR);

        print("***** LEAVING FileSystemGroupsTest.testFindEmbeddedMemberGroups() *****" + CR);

    }

    /**
     * Tests IEntityGroupStore.findEntitiesForGroup().
     */
    @Test
    public void testFindEntitiesForGroup() throws Exception {
        print("***** ENTERING FileSystemGroupsTest.testFindEntitiesForGroup() *****" + CR);

        String msg = null;
        IEntityGroup group = null;
        String entityKey = null;
        File f = null, f2 = null;
        Iterator itr = null;
        final List memberEntities = new ArrayList();

        msg = "Finding entities for files...";
        print(msg);

        for (itr = this.keyFiles.iterator(); itr.hasNext();) {
            f = (File) itr.next();
            msg = "finding group: " + f;
            group = this.findGroup(f);
            assertTrue(msg, group instanceof IEntityGroup);
            memberEntities.clear();

            for (final Iterator members = this.getGroupStore().getEntityIdsFromFile(f).iterator(); members.hasNext();) {
                entityKey = (String) members.next();
                assertTrue(msg, entityKey != null);
                assertTrue(msg, entityKey.length() > 0);
                memberEntities.add(entityKey);
            }
            assertEquals(msg, this.numTestEntities, memberEntities.size());
        }

        f = (File) this.keyFiles.get(0);
        f2 = f.getParentFile();
        msg = "Finding entities for " + f2 + " (should have none).";
        group = this.findGroup(f2);
        assertTrue(msg, group instanceof IEntityGroup);
        final boolean hasEntities = this.getGroupStore().findEntitiesForGroup(group).hasNext();
        assertTrue(msg, !hasEntities);

        print("Test completed successfully." + CR);

        print("***** LEAVING FileSystemGroupsTest.testFindEntitiesForGroup() *****" + CR);

    }

    /**
     * Tests IEntityGroupStore.findMemberGroupKeys().
     */
    @Test
    public void testFindMemberGroupKeys() throws Exception {
        print("***** ENTERING FileSystemGroupsTest.testFindMemberGroupKeys() *****" + CR);

        String msg = null;
        IEntityGroup group = null, memberGroup = null;
        File f = null;
        final File f2 = null;
        Iterator itr = null;
        String memberKeys[] = null;

        msg = "Finding member group keys for directory files...";
        print(msg);

        for (itr = this.directoryFiles.iterator(); itr.hasNext();) {
            f = (File) itr.next();
            msg = "Finding member group keys for group: " + f;
            group = this.findGroup(f);
            assertTrue(msg, group instanceof IEntityGroup);
            memberKeys = this.getGroupStore().findMemberGroupKeys(group);
            assertEquals(msg, this.numTestFiles, memberKeys.length);
            for (final String memberKey : memberKeys) {
                memberGroup = this.findGroup(memberKey);
                assertNotNull(msg, memberGroup);
                assertTrue(msg, this.getGroupStore().contains(group, memberGroup));
            }
        }

        msg = "Finding member group keys for key files...";
        print(msg);

        for (itr = this.keyFiles.iterator(); itr.hasNext();) {
            f = (File) itr.next();
            msg = "Finding member group keys for group: " + f;
            group = this.findGroup(f);
            assertTrue(msg, group instanceof IEntityGroup);
            memberKeys = this.getGroupStore().findMemberGroupKeys(group);
            assertEquals(msg, 0, memberKeys.length);
        }

        print("Test completed successfully." + CR);

        print("***** LEAVING FileSystemGroupsTest.testFindMemberGroupKeys() *****" + CR);

    }

    /**
     * Tests IEntityGroupStore.findMemberGroups().
     */
    @Test
    public void testFindMemberGroups() throws Exception {
        print("***** ENTERING FileSystemGroupsTest.testFindMemberGroups() *****" + CR);

        String msg = null, groupKey = null;
        IEntityGroup group = null, memberGroup = null;
        File f = null;
        final File f2 = null;
        Iterator itr = null;
        Iterator memberGroups = null;

        msg = "Finding member groups for directory files...";
        print(msg);

        for (itr = this.directoryFiles.iterator(); itr.hasNext();) {
            f = (File) itr.next();
            msg = "Finding member groups for group: " + f;
            group = this.findGroup(f);
            assertTrue(msg, group instanceof IEntityGroup);
            memberGroups = this.getGroupStore().findMemberGroups(group);
            while (memberGroups.hasNext()) {
                memberGroup = (IEntityGroup) memberGroups.next();
                assertNotNull(msg, memberGroup);
                groupKey = memberGroup.getKey();
                memberGroup = this.findGroup(groupKey);
                assertTrue(msg, this.getGroupStore().contains(group, memberGroup));
            }
        }

        msg = "Finding member groups for key files...";
        print(msg);

        for (itr = this.keyFiles.iterator(); itr.hasNext();) {
            f = (File) itr.next();
            msg = "Finding member groups for group: " + f;
            group = this.findGroup(f);
            assertTrue(msg, group instanceof IEntityGroup);
            memberGroups = this.getGroupStore().findMemberGroups(group);
            assertTrue(msg, !memberGroups.hasNext());
        }

        print("Test completed successfully." + CR);

        print("***** LEAVING FileSystemGroupsTest.testFindMemberGroups() *****" + CR);

    }

    /**
     * Tests IEntityGroupStore.searchForGroups(), which returns EntityIdentifier[] given
     * a search string.
     */
    @Test
    public void testSearchForGroups() throws Exception {
        print("***** ENTERING FileSystemGroupsTest.testSearchForGroups() *****" + CR);

        String msg = null;
        String is = null, startsWith = null, endsWith = null, contains = null, badQuery = null;
        final Class type = IPERSON_CLASS;
        final IEntityGroup existingGroup = null;
        IGroupMember member = null;
        EntityIdentifier[] ids = null;

        msg = "Searching for existing groups...";
        print(msg);
        for (final String testFileName : this.testFileNames) {
            is = testFileName;
            startsWith = is.substring(0, (is.length() - 1));
            endsWith = is.substring(1);
            contains = is.substring(1, (is.length() - 1));
            badQuery = is + " a b c";

            msg = "Searching for IS " + is;
            ids = this.getGroupStore().searchForGroups(is, IGroupConstants.IS, type);
            assertEquals(msg, ids.length, 1);
            member = this.findGroup(ids[0].getKey());
            assertTrue(msg, member.isGroup());

            msg = "Searching for STARTS WITH " + startsWith;
            ids = this.getGroupStore().searchForGroups(startsWith, IGroupConstants.STARTS_WITH, type);
            assertTrue(msg, ids.length > 0);

            msg = "Searching for ENDS WITH " + endsWith;
            ids = this.getGroupStore().searchForGroups(endsWith, IGroupConstants.ENDS_WITH, type);
            assertTrue(msg, ids.length > 0);

            msg = "Searching for CONTAINS " + contains;
            ids = this.getGroupStore().searchForGroups(contains, IGroupConstants.CONTAINS, type);
            assertTrue(msg, ids.length > 0);

            msg = "Searching for IS " + badQuery;
            ids = this.getGroupStore().searchForGroups(badQuery, IGroupConstants.IS, type);
            assertEquals(msg, ids.length, 0);

        }

        print("Test completed successfully." + CR);

        print("***** LEAVING FileSystemGroupsTest.testSearchForGroups() *****" + CR);

    }
}
