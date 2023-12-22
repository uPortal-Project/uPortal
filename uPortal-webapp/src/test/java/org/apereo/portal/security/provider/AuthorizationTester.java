/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.security.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.apereo.portal.AuthorizationException;
import org.apereo.portal.groups.GroupServiceConfiguration;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.permission.IPermissionActivity;
import org.apereo.portal.permission.IPermissionOwner;
import org.apereo.portal.permission.target.IPermissionTarget;
import org.apereo.portal.properties.PropertiesManager;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPermissionPolicy;
import org.apereo.portal.security.IPermissionStore;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.spring.locator.AuthorizationServiceLocator;
import org.junit.Ignore;

/** Tests the authorization framework. */
@Ignore // Breaks on move to Gradle
public class AuthorizationTester extends TestCase {
    private String OWNER = "UP_FRAMEWORK";
    private String TEST_TARGET = "Test_Target.";
    private String TEST_ACTIVITY = "Test_Activity";
    private String EVERYONE_GROUP_KEY;
    private String EVERYONE_GROUP_PRINCIPAL_KEY;
    private String NOONE_GROUP_PRINCIPAL_KEY;
    private String STUDENT_GROUP_PRINCIPAL_KEY;
    private String STUDENT_PRINCIPAL_KEY = "2.student";
    private String GROUP_SEPARATOR;
    private int NUMBER_TEST_PERMISSIONS = 10;

    private IAuthorizationService authorizationService;
    private IPermissionStore permissionStore;
    private IPermissionPolicy defaultPermissionPolicy;
    private IPermissionPolicy negativePermissionPolicy;
    private IPermissionPolicy positivePermissionPolicy;
    private List testPermissions = new ArrayList();

    private static Class GROUP_CLASS;
    private static Class IPERSON_CLASS;
    private static String CR = "\n";

    private Random random = new Random();

    private class NegativePermissionPolicy implements IPermissionPolicy {
        private NegativePermissionPolicy() {
            super();
        }

        @Override
        public boolean doesPrincipalHavePermission(
                IAuthorizationService service,
                IAuthorizationPrincipal principal,
                IPermissionOwner owner,
                IPermissionActivity activity,
                IPermissionTarget target)
                throws AuthorizationException {
            // Seems the only value this method provides is NPE detection
            return service == null && principal != null && owner != null && activity != null;
        }

        @Override
        public String toString() {
            return this.getClass().getName();
        }
    }

    private class PositivePermissionPolicy implements IPermissionPolicy {
        private PositivePermissionPolicy() {
            super();
        }

        @Override
        public boolean doesPrincipalHavePermission(
                IAuthorizationService service,
                IAuthorizationPrincipal principal,
                IPermissionOwner owner,
                IPermissionActivity activity,
                IPermissionTarget target)
                throws AuthorizationException {
            // Seems the only value this method provides is NPE detection
            return service != null && principal != null && owner != null && activity != null;
        }

        @Override
        public String toString() {
            return this.getClass().getName();
        }
    }

    private class PrincipalTester implements Runnable {
        protected Class type;
        protected String key;
        int numTests = 0;
        protected String testerID = null;
        protected String printID = null;
        protected IPermission testPermission;

        protected PrincipalTester(
                String pKey, Class pType, int tests, String id, IPermission permission) {
            super();
            key = pKey;
            type = pType;
            numTests = tests;
            testerID = id;
            testPermission = permission;
        }

        @Override
        public void run() {
            printID = "Tester " + testerID;
            print(printID + " starting.");

            runAndSleep(numTests, true);
            print(
                    printID
                            + " finished first part of tests.  Will now sleep for 5000 ms to let main thread catch up.");
            try {
                Thread.sleep(5000);
            } catch (Exception ex) {
            }
            print(printID + " starting second part of tests.");
            runAndSleep(numTests, false);
            print(printID + " is done.");
        }

        private void runAndSleep(int cycles, boolean expectedResult) {
            for (int i = 0; i < cycles; i++) {
                // print(printID + " running test # " + (i+1));
                try {
                    String msg =
                            "Testing  for "
                                    + testPermission
                                    + " (should be "
                                    + expectedResult
                                    + ")";
                    boolean testResult = (runTest() == expectedResult);
                    assertTrue(msg, testResult);
                } catch (Exception ex) {
                }
                int sleepMillis = random.nextInt(10);
                // print(printID + " will now sleep for " + sleepMillis + " ms.");
                try {
                    Thread.sleep(sleepMillis);
                } catch (Exception ex) {
                }
            }
        }

        private boolean runTest() throws AuthorizationException {
            IAuthorizationPrincipal principal = getService().newPrincipal(key, type);
            // print("Testing  principal for " + testPermission);
            return principal.hasPermission(OWNER, TEST_ACTIVITY, testPermission.getTarget());
        }
    }

    /** AuthorizationTester constructor comment. */
    public AuthorizationTester(String name) {
        super(name);
    }

    /** @return org.apereo.portal.security.IPermissionPolicy */
    private IPermissionPolicy getNegativePermissionPolicy() throws AuthorizationException {
        if (negativePermissionPolicy == null) {
            negativePermissionPolicy = new AuthorizationTester.NegativePermissionPolicy();
        }
        return negativePermissionPolicy;
    }
    /** @return org.apereo.portal.security.IPermissionStore */
    private IPermissionStore getPermissionStore() throws AuthorizationException {
        if (permissionStore == null) {
            initializePermissionStore();
        }
        return permissionStore;
    }
    /** @return org.apereo.portal.security.IPermissionPolicy */
    private IPermissionPolicy getPositivePermissionPolicy() throws AuthorizationException {
        if (positivePermissionPolicy == null) {
            positivePermissionPolicy = new AuthorizationTester.PositivePermissionPolicy();
        }
        return positivePermissionPolicy;
    }
    /** @return org.apereo.portal.security.AuthorizationServiceFacade */
    private IAuthorizationService getService() throws AuthorizationException {
        if (authorizationService == null) {
            initializeAuthorizationService();
        }
        return authorizationService;
    }
    /** Create an implementation of IAuthorizationService. */
    private void initializeAuthorizationService() throws AuthorizationException {
        authorizationService = AuthorizationServiceLocator.getAuthorizationService();
    }

    /** Create an implementation of IPermissionStore. */
    private void initializePermissionStore() throws AuthorizationException {
        String eMsg = null;
        String factoryName =
                PropertiesManager.getProperty(
                        "org.apereo.portal.security.IPermissionStore.implementation");
        if (factoryName == null) {
            eMsg =
                    "AuthorizationTester.initializePermissionStore(): No entry for org.apereo.portal.security.IPermissionStore.implementation portal.properties.";
            print(eMsg);
            throw new AuthorizationException(eMsg);
        }

        try {
            permissionStore = (IPermissionStore) Class.forName(factoryName).newInstance();
        } catch (Exception e) {
            eMsg =
                    "AuthorizationTester.initializePermissionStore(): Problem creating permission store... "
                            + e.getMessage();
            print(eMsg);
            throw new AuthorizationException(eMsg);
        }
    }
    /**
     * Starts the application.
     *
     * @param args an array of command-line arguments
     */
    public static void main(java.lang.String[] args) throws Exception {
        String[] mainArgs = {"org.apereo.portal.security.provider.AuthorizationTester"};
        print("START TESTING AUTHORIZATION SERVICE");
        printBlankLine();
        TestRunner.main(mainArgs);
        printBlankLine();
        print("END TESTING AUTHORIZATION SERVICE");
    }
    /** @param msg java.lang.String */
    private static void print(String msg) {
        java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
        System.out.println(ts + " : " + msg);
    }

    private static void printBlankLine() {
        System.out.println("");
    }

    @Override
    protected void setUp() {
        String msg = null;
        IPermission[] retrievedPermissions = null;
        IPermission newPermission, retrievedPermission = null;
        java.util.Date effectiveDate = new java.util.Date();
        java.util.Date expirationDate =
                new java.util.Date(System.currentTimeMillis() + (60 * 60 * 24 * 1000));
        int idx = 0;

        try {
            if (GROUP_CLASS == null) {
                GROUP_CLASS = Class.forName("org.apereo.portal.groups.IEntityGroup");
            }
            if (IPERSON_CLASS == null) {
                IPERSON_CLASS = Class.forName("org.apereo.portal.security.IPerson");
            }

            GROUP_SEPARATOR = GroupServiceConfiguration.getConfiguration().getNodeSeparator();
            EVERYONE_GROUP_KEY = "local" + GROUP_SEPARATOR + "0";
            EVERYONE_GROUP_PRINCIPAL_KEY = "3." + EVERYONE_GROUP_KEY;
            NOONE_GROUP_PRINCIPAL_KEY = "3.local" + GROUP_SEPARATOR + "999";
            STUDENT_GROUP_PRINCIPAL_KEY = "3.local" + GROUP_SEPARATOR + "1";

            msg = "Creating test permissions.";
            print(msg);

            retrievedPermissions =
                    getPermissionStore()
                            .select(
                                    OWNER,
                                    EVERYONE_GROUP_PRINCIPAL_KEY,
                                    TEST_ACTIVITY,
                                    null,
                                    IPermission.PERMISSION_TYPE_GRANT);
            assertEquals(msg, 0, retrievedPermissions.length);

            for (idx = 0; idx < NUMBER_TEST_PERMISSIONS; idx++) {
                newPermission = getPermissionStore().newInstance(OWNER);
                newPermission.setPrincipal(EVERYONE_GROUP_PRINCIPAL_KEY);
                newPermission.setActivity(TEST_ACTIVITY);
                newPermission.setTarget(TEST_TARGET + idx);
                newPermission.setType(IPermission.PERMISSION_TYPE_GRANT);
                newPermission.setEffective(effectiveDate);
                newPermission.setExpires(expirationDate);

                getPermissionStore().add(newPermission);
                testPermissions.add(newPermission);
            }

            retrievedPermissions =
                    getPermissionStore()
                            .select(
                                    OWNER,
                                    EVERYONE_GROUP_PRINCIPAL_KEY,
                                    TEST_ACTIVITY,
                                    null,
                                    IPermission.PERMISSION_TYPE_GRANT);
            assertEquals(msg, NUMBER_TEST_PERMISSIONS, retrievedPermissions.length);

            msg = "Creating test DENY permission for student group.";
            print(msg);

            retrievedPermission = (IPermission) testPermissions.get(0);
            newPermission = getPermissionStore().newInstance(OWNER);
            newPermission.setActivity(TEST_ACTIVITY);
            newPermission.setPrincipal(STUDENT_GROUP_PRINCIPAL_KEY);
            newPermission.setTarget(retrievedPermission.getTarget());
            newPermission.setType(IPermission.PERMISSION_TYPE_DENY);

            retrievedPermissions =
                    getPermissionStore()
                            .select(
                                    OWNER,
                                    STUDENT_GROUP_PRINCIPAL_KEY,
                                    TEST_ACTIVITY,
                                    retrievedPermission.getTarget(),
                                    IPermission.PERMISSION_TYPE_DENY);
            assertEquals(msg, 0, retrievedPermissions.length);
            getPermissionStore().add(newPermission);
            retrievedPermissions =
                    getPermissionStore()
                            .select(
                                    OWNER,
                                    STUDENT_GROUP_PRINCIPAL_KEY,
                                    TEST_ACTIVITY,
                                    retrievedPermission.getTarget(),
                                    IPermission.PERMISSION_TYPE_DENY);
            assertEquals(msg, 1, retrievedPermissions.length);
            testPermissions.add(newPermission);

            msg = "Creating test DENY permission for student entity.";
            print(msg);

            newPermission = getPermissionStore().newInstance(OWNER);
            retrievedPermission = (IPermission) testPermissions.get(1);
            newPermission.setPrincipal(STUDENT_PRINCIPAL_KEY);
            newPermission.setActivity(TEST_ACTIVITY);
            newPermission.setTarget(retrievedPermission.getTarget());
            newPermission.setType(IPermission.PERMISSION_TYPE_DENY);

            retrievedPermissions =
                    getPermissionStore()
                            .select(
                                    OWNER,
                                    STUDENT_PRINCIPAL_KEY,
                                    TEST_ACTIVITY,
                                    retrievedPermission.getTarget(),
                                    IPermission.PERMISSION_TYPE_DENY);
            assertEquals(msg, 0, retrievedPermissions.length);
            getPermissionStore().add(newPermission);
            retrievedPermissions =
                    getPermissionStore()
                            .select(
                                    OWNER,
                                    STUDENT_PRINCIPAL_KEY,
                                    TEST_ACTIVITY,
                                    retrievedPermission.getTarget(),
                                    IPermission.PERMISSION_TYPE_DENY);
            assertEquals(msg, 1, retrievedPermissions.length);
            testPermissions.add(newPermission);

        } catch (Exception ex) {
            print("AuthorizationTester.setUp(): " + ex.getMessage());
        }
    }

    @Override
    protected void tearDown() {
        try {
            IPermission[] permissions =
                    (IPermission[])
                            testPermissions.toArray(new IPermission[testPermissions.size()]);
            getPermissionStore().delete(permissions);
            testPermissions.clear();

        } catch (Exception ex) {
            print("AuthorizationTester.tearDown(): " + ex.getMessage());
        }
    }

    public void testAlternativePermissionPolicies() throws Exception {
        print("***** ENTERING AuthorizationTester.testAlternativePermissionPolicies() *****");
        String msg = null;
        boolean testResult = false;
        String activity = IPermission.PORTLET_SUBSCRIBER_ACTIVITY;
        String existingTarget = "CHAN_ID.1";
        String nonExistingTarget = "CHAN_ID.9999";

        msg = "Creating a group member for everyone (" + EVERYONE_GROUP_PRINCIPAL_KEY + ").";
        print(msg);
        IGroupMember everyone = GroupService.getGroupMember(EVERYONE_GROUP_KEY, GROUP_CLASS);
        assertNotNull(msg, everyone);

        msg = "Getting principal for " + everyone;
        print(msg);
        IAuthorizationPrincipal prin = getService().newPrincipal(everyone);
        assertNotNull(msg, prin);

        msg = "Testing DEFAULT permission policy for an existing channel";
        print(msg);
        testResult = prin.hasPermission(OWNER, activity, existingTarget);
        assertTrue(msg, testResult);

        msg = "Testing POSITIVE permission policy for an existing channel";
        print(msg);
        testResult =
                prin.hasPermission(OWNER, activity, existingTarget, getPositivePermissionPolicy());
        assertTrue(msg, testResult);

        msg = "Testing NEGATIVE permission policy for an existing channel";
        print(msg);
        testResult =
                prin.hasPermission(OWNER, activity, existingTarget, getNegativePermissionPolicy());
        assertTrue(msg, !testResult);

        msg = "Testing DEFAULT permission policy for a nonexistent channel";
        print(msg);
        testResult = prin.hasPermission(OWNER, activity, nonExistingTarget);
        assertTrue(msg, !testResult);

        msg = "Testing POSITIVE permission policy for nonexistent channel";
        print(msg);
        testResult =
                prin.hasPermission(
                        OWNER, activity, nonExistingTarget, getPositivePermissionPolicy());
        assertTrue(msg, testResult);

        msg = "Testing NEGATIVE permission policy for a nonexistent channel";
        print(msg);
        testResult =
                prin.hasPermission(
                        OWNER, activity, nonExistingTarget, getNegativePermissionPolicy());
        assertTrue(msg, !testResult);

        print("***** LEAVING AuthorizationTester.testAlternativePermissionPolicies() *****" + CR);
    }

    public void testDoesPrincipalHavePermission() throws Exception {
        print("***** ENTERING AuthorizationTester.testDoesPrincipalHavePermission() *****");
        String msg = null;
        IPermission testPermission = null;
        boolean testResult = false;
        int idx = 0;

        msg = "Creating authorizationPrincipal for student.";
        print(msg);
        IAuthorizationPrincipal prin = getService().newPrincipal("student", IPERSON_CLASS);
        assertNotNull(msg, prin);

        testPermission = (IPermission) testPermissions.get(0);
        msg = "Testing  " + testPermission + " (should be TRUE -- inherited from Everyone)";
        print(msg);
        testResult = prin.hasPermission(OWNER, TEST_ACTIVITY, testPermission.getTarget());
        assertTrue(msg, testResult);

        testPermission = (IPermission) testPermissions.get(1);
        msg = "Testing  " + testPermission + " (should be FALSE -- directly denied)";
        print(msg);
        testResult = prin.hasPermission(OWNER, TEST_ACTIVITY, testPermission.getTarget());
        assertTrue(msg, !testResult);

        msg = "Testing  the rest of the test permissions (should be TRUE).";
        print(msg);
        for (idx = 2; idx < NUMBER_TEST_PERMISSIONS; idx++) {
            testPermission = (IPermission) testPermissions.get(idx);
            testResult = prin.hasPermission(OWNER, TEST_ACTIVITY, testPermission.getTarget());
            assertTrue(msg, testResult);
        }

        print("***** LEAVING AuthorizationTester.testDoesPrincipalHavePermission() *****" + CR);
    }

    public void testPermissionStore() throws Exception {
        print("***** ENTERING AuthorizationTester.testPermissionStore() *****");
        String msg = null;
        String activity = IPermission.PORTLET_SUBSCRIBER_ACTIVITY;
        String existingTarget = "CHAN_ID.1";
        String nonExistingTarget = "CHAN_ID.000";
        //    String noonePrincipal = "3.local.999";
        IPermission[] permissions, addedPermissions = null;
        IPermission newPermission, retrievedPermission = null;
        java.util.Date effectiveDate = new java.util.Date();
        java.util.Date expirationDate =
                new java.util.Date(System.currentTimeMillis() + (60 * 60 * 24 * 1000));
        int numAddedPermissions = 10;
        int idx = 0;

        // Add a new permission.

        msg = "Creating a new permission for everyone (" + EVERYONE_GROUP_PRINCIPAL_KEY + ").";
        print(msg);
        newPermission = getPermissionStore().newInstance(OWNER);
        assertNotNull(msg, newPermission);

        newPermission.setPrincipal(EVERYONE_GROUP_PRINCIPAL_KEY);
        newPermission.setActivity(activity);
        newPermission.setTarget(nonExistingTarget);
        newPermission.setType(IPermission.PERMISSION_TYPE_GRANT);

        msg = "Testing if new permission exists in store.";
        print(msg);
        permissions =
                getPermissionStore()
                        .select(
                                OWNER,
                                EVERYONE_GROUP_PRINCIPAL_KEY,
                                activity,
                                nonExistingTarget,
                                IPermission.PERMISSION_TYPE_GRANT);
        assertEquals(msg, 0, permissions.length);

        msg = "Adding permission to store.";
        print(msg);
        getPermissionStore().add(newPermission);
        permissions =
                getPermissionStore()
                        .select(
                                OWNER,
                                EVERYONE_GROUP_PRINCIPAL_KEY,
                                activity,
                                nonExistingTarget,
                                IPermission.PERMISSION_TYPE_GRANT);
        assertEquals(msg, 1, permissions.length);

        // Update the new permission we have just added.
        msg = "Updating permission.";
        print(msg);
        retrievedPermission = permissions[0];
        retrievedPermission.setType(IPermission.PERMISSION_TYPE_DENY);
        retrievedPermission.setEffective(effectiveDate);
        retrievedPermission.setExpires(expirationDate);
        getPermissionStore().update(retrievedPermission);
        permissions =
                getPermissionStore()
                        .select(
                                OWNER,
                                EVERYONE_GROUP_PRINCIPAL_KEY,
                                activity,
                                nonExistingTarget,
                                IPermission.PERMISSION_TYPE_DENY);
        assertEquals(msg, 1, permissions.length);
        assertEquals(msg, IPermission.PERMISSION_TYPE_DENY, permissions[0].getType());
        assertEquals(msg, effectiveDate, permissions[0].getEffective());
        assertEquals(msg, expirationDate, permissions[0].getExpires());

        // Delete the retrieved permission.
        msg = "Deleting the updated permission.";
        print(msg);
        getPermissionStore().delete(retrievedPermission);
        permissions =
                getPermissionStore()
                        .select(
                                OWNER,
                                EVERYONE_GROUP_PRINCIPAL_KEY,
                                activity,
                                nonExistingTarget,
                                IPermission.PERMISSION_TYPE_DENY);
        assertEquals(msg, 0, permissions.length);

        // Add and delete an array of permissions.
        msg = "Creating and adding an Array of " + numAddedPermissions + " Permissions.";
        print(msg);
        addedPermissions = new IPermission[numAddedPermissions];
        for (idx = 0; idx < numAddedPermissions; idx++) {
            addedPermissions[idx] = getPermissionStore().newInstance(OWNER);
            addedPermissions[idx].setActivity(activity);
            addedPermissions[idx].setPrincipal(NOONE_GROUP_PRINCIPAL_KEY);
            addedPermissions[idx].setTarget(existingTarget + "_" + idx);
            addedPermissions[idx].setType(IPermission.PERMISSION_TYPE_GRANT);
            addedPermissions[idx].setEffective(effectiveDate);
            addedPermissions[idx].setExpires(expirationDate);
        }
        getPermissionStore().add(addedPermissions);
        permissions =
                getPermissionStore().select(OWNER, NOONE_GROUP_PRINCIPAL_KEY, activity, null, null);
        assertEquals(msg, numAddedPermissions, permissions.length);

        msg = "Deleting the Array of " + numAddedPermissions + " Permissions.";
        print(msg);
        getPermissionStore().delete(permissions);
        permissions =
                getPermissionStore().select(OWNER, NOONE_GROUP_PRINCIPAL_KEY, activity, null, null);
        assertEquals(msg, 0, permissions.length);

        print("***** LEAVING AuthorizationTester.testPermissionStore() *****" + CR);
    }
    /**
     * Tests concurrent access to permissions via "singleton" principal objects. Only run this test
     * when the property org.apereo.portal.security.IAuthorizationService.cachePermissions=true,
     * since performance of the db calls will distort the time needed to complete the various parts
     * of the test.
     */
    public void testPermissionPrincipal() throws Exception {
        print("***** ENTERING AuthorizationTester.testPermissionPrincipal() *****");
        Class type = IPERSON_CLASS;
        String key = "student";
        int numPrincipals = 10;
        int numTestingThreads = 10;
        int idx = 0;
        long pauseBeforeUpdateMillis = 3000;
        long pauseAfterUpdateMillis = 10000;
        IAuthorizationPrincipal[] principals = new IAuthorizationPrincipal[numPrincipals];
        for (idx = 0; idx < numPrincipals; idx++) {
            principals[idx] = getService().newPrincipal(key, type);
        }

        String msg = "Test that principal " + principals[0] + " is being cached.";
        print(msg);
        for (idx = 1; idx < numPrincipals; idx++) {
            assertTrue(msg, principals[idx] == principals[0]);
        }

        IAuthorizationPrincipal p1 = principals[0];

        IPermission testPermission = (IPermission) testPermissions.get(0);
        msg =
                "Testing  first principal for "
                        + testPermission
                        + " (should be TRUE -- inherited from Everyone)";
        print(msg);
        boolean testResult = p1.hasPermission(OWNER, TEST_ACTIVITY, testPermission.getTarget());
        assertTrue(msg, testResult);

        print("Starting testing Threads.");
        Thread[] testers = new Thread[numTestingThreads];
        for (idx = 0; idx < numTestingThreads; idx++) {
            String id = "" + idx;
            PrincipalTester pt = new PrincipalTester(key, type, 10, id, testPermission);
            testers[idx] = new Thread(pt);
            testers[idx].start();
        }

        print("Will now sleep for " + pauseBeforeUpdateMillis + " ms to let testing threads run.");
        try {
            Thread.sleep(pauseBeforeUpdateMillis);
        } catch (Exception ex) {
        }

        /*
         * Remove a permission and test a principal.  After a pause, the testing threads
         * will wake up and perform the 2nd part of their tests to confirm this update.
         */

        msg = "Deleting " + testPermission;
        print(msg);
        IPermission[] perms = new IPermission[1];
        perms[0] = testPermission;
        getService().removePermissions(perms);

        msg =
                "Testing  first principal for "
                        + testPermission
                        + " (should be FALSE -- has been removed.)";
        print(msg);
        testResult = p1.hasPermission(OWNER, TEST_ACTIVITY, testPermission.getTarget());
        assertTrue(msg, !testResult);

        print(
                "Will now sleep for "
                        + pauseAfterUpdateMillis
                        + " ms to let testing threads complete.");
        try {
            Thread.sleep(pauseAfterUpdateMillis);
        } catch (Exception ex) {
        }

        print("***** LEAVING AuthorizationTester.testPermissionPrincipal() *****" + CR);
    }
}
