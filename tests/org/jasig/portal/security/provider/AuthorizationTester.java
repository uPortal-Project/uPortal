package org.jasig.portal.security.provider;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;
import junit.framework.*;
import org.jasig.portal.security.*;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.groups.*;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.concurrency.*;
import org.jasig.portal.concurrency.caching.*;

/**
 * Tests the authorization framework.
 * @author: Dan Ellentuck
 */
public class AuthorizationTester extends TestCase
{
    private static String OWNER = "UP_FRAMEWORK";
    private static String TEST_TARGET = "Test_Target.";
    private static String TEST_ACTIVITY = "Test_Activity";
    private static String EVERYONE_GROUP_PRINCIPAL_KEY = "3.local.0";
    private static String NOONE_GROUP_PRINCIPAL_KEY = "3.local.999";
    private static String STUDENT_GROUP_PRINCIPAL_KEY = "3.local.1";
    private static String STUDENT_PRINCIPAL_KEY = "2.student";
    private static int NUMBER_TEST_PERMISSIONS = 10;

    private IAuthorizationService authorizationService;
    private IPermissionStore permissionStore;
    private IPermissionPolicy defaultPermissionPolicy;
    private IPermissionPolicy negativePermissionPolicy;
    private IPermissionPolicy positivePermissionPolicy;
    private IPermission[] addedPermissions;
    private List testPermissions = new ArrayList();

    private static Class GROUP_CLASS;
    private static Class IPERSON_CLASS;
    private static String CR = "\n";

    private class NegativePermissionPolicy implements IPermissionPolicy
    {
        private NegativePermissionPolicy() { super(); }
        public boolean doesPrincipalHavePermission(
            IAuthorizationService service,
            IAuthorizationPrincipal principal,
            String owner,
            String activity,
            String target)
        throws AuthorizationException
       {
           return ! (service.equals(service)) &&
                  (principal.equals(principal)) &&
                  (owner.equals(owner)) &&
                  (activity.equals(activity));
       }
        public String toString() { return this.getClass().getName(); }
    }

    private class PositivePermissionPolicy implements IPermissionPolicy
    {
        private PositivePermissionPolicy() { super(); }
        public boolean doesPrincipalHavePermission(
            IAuthorizationService service,
            IAuthorizationPrincipal principal,
            String owner,
            String activity,
            String target)
        throws AuthorizationException
       {
           return (service.equals(service)) &&
                  (principal.equals(principal)) &&
                  (owner.equals(owner)) &&
                  (activity.equals(activity));
       }
        public String toString() { return this.getClass().getName(); }
    }
/**
 * AuthorizationTester constructor comment.
 */
public AuthorizationTester(String name) {
    super(name);
}
/**
 *
 */
private void clearGroupCache() throws CachingException
{
    ((ReferenceEntityCachingService) ReferenceEntityCachingService.singleton())
        .getCache(GROUP_CLASS).clearCache();
}
/**
 * @return org.jasig.portal.security.IPermissionPolicy
 */
private IPermissionPolicy getDefaultPermissionPolicy() throws AuthorizationException
{
    if ( defaultPermissionPolicy == null )
        { initializeDefaultPermissionPolicy(); }
    return defaultPermissionPolicy;
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
 * @return org.jasig.portal.security.IPermissionPolicy
 */
private IPermissionPolicy getNegativePermissionPolicy() throws AuthorizationException
{
    if ( negativePermissionPolicy == null )
        { negativePermissionPolicy = new AuthorizationTester.NegativePermissionPolicy(); }
    return negativePermissionPolicy;
}
/**
 * @return org.jasig.portal.security.IPermissionStore
 */
private IPermissionStore getPermissionStore() throws AuthorizationException
{
    if ( permissionStore == null )
        { initializePermissionStore(); }
    return permissionStore;
}
/**
 * @return org.jasig.portal.security.IPermissionPolicy
 */
private IPermissionPolicy getPositivePermissionPolicy() throws AuthorizationException
{
    if ( positivePermissionPolicy == null )
        { positivePermissionPolicy = new AuthorizationTester.PositivePermissionPolicy(); }
    return positivePermissionPolicy;
}
/**
 * @return org.jasig.portal.security.AuthorizationService
 */
private IAuthorizationService getService() throws AuthorizationException
{
    if ( authorizationService == null )
        { initializeAuthorizationService(); }
    return authorizationService;
}
/**
 * Create an implementation of IAuthorizationService.
 */
private void initializeAuthorizationService() throws AuthorizationException
{
    // Get the security properties file
    java.io.InputStream secprops = AuthorizationService.class.getResourceAsStream("/properties/security.properties");

    // Get the properties from the security properties file
    Properties pr = new Properties();
    String s_factoryName = null;

    try
    {
        pr.load(secprops);
        // Look for our authorization factory and instantiate an instance of it or die trying.
        if ((s_factoryName = pr.getProperty("authorizationProvider")) == null)
        {
            print ("ERROR: AuthorizationProvider not specified or incorrect in security.properties");
        }
        else
        {
            try
            {
                IAuthorizationServiceFactory factory = (IAuthorizationServiceFactory)Class.forName(s_factoryName).newInstance();
                authorizationService = factory.getAuthorization();
            }
            catch (Exception e)
            {
                print ("ERROR: Failed to instantiate " + s_factoryName);
            }
        }

    }
    catch (IOException e)
    {
        print ("ERROR: " + e.getMessage());
    }
}
/**
 * Create the default implementation of IPermissionPolicy.
 */
private void initializeDefaultPermissionPolicy() throws AuthorizationException
{
    String eMsg = null;
    String policyName =
      PropertiesManager.getProperty("org.jasig.portal.security.IPermissionPolicy.defaultImplementation");
    if ( policyName == null )
    {
        eMsg = "AuthorizationTester.initializeDefaultPermissionPolicy(): No entry for org.jasig.portal.security.IPermissionPolicy.defaultImplementation in portal.properties.";
        print (eMsg);
        throw new AuthorizationException(eMsg);
    }

    try
    {
        defaultPermissionPolicy = (IPermissionPolicy)Class.forName(policyName).newInstance();
    }
    catch (Exception e)
    {
        eMsg = "AuthorizationTester.initializeDefaultPermissionPolicy(): Problem creating default permission policy... " + e.getMessage();
        print(eMsg);
        throw new AuthorizationException(eMsg);
    }
}
/**
 * Create an implementation of IPermissionStore.
 */
private void initializePermissionStore() throws AuthorizationException
{
    String eMsg = null;
    String factoryName =
      PropertiesManager.getProperty("org.jasig.portal.security.IPermissionStore.implementation");
    if ( factoryName == null )
    {
        eMsg = "AuthorizationTester.initializePermissionStore(): No entry for org.jasig.portal.security.IPermissionStore.implementation portal.properties.";
        print(eMsg);
        throw new AuthorizationException(eMsg);
    }

    try
    {
        permissionStore = (IPermissionStore)Class.forName(factoryName).newInstance();
    }
    catch (Exception e)
    {
        eMsg = "AuthorizationTester.initializePermissionStore(): Problem creating permission store... " + e.getMessage();
        print(eMsg);
        throw new AuthorizationException(eMsg);
    }
}
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) throws Exception
{
    String[] mainArgs = {"org.jasig.portal.security.provider.AuthorizationTester"};
    print("START TESTING AUTHORIZATION SERVICE");
    printBlankLine();
    junit.swingui.TestRunner.main(mainArgs);
    printBlankLine();
    print("END TESTING AUTHORIZATION SERVICE");

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

    String msg = null;
    IPermission[] retrievedPermissions = null;
    IPermission newPermission, retrievedPermission = null;
    java.util.Date effectiveDate = new java.util.Date();
    java.util.Date expirationDate = new java.util.Date(System.currentTimeMillis() + (60 * 60 * 24 * 1000));
    int idx = 0;

    try
    {
        if ( GROUP_CLASS == null )
            { GROUP_CLASS = Class.forName("org.jasig.portal.groups.IEntityGroup"); }
        if ( IPERSON_CLASS == null )
            { IPERSON_CLASS = Class.forName("org.jasig.portal.security.IPerson"); }

        msg = "Creating test permissions.";
        print(msg);

        retrievedPermissions = getPermissionStore().select
          (OWNER, EVERYONE_GROUP_PRINCIPAL_KEY,
           TEST_ACTIVITY,
           null,
           IPermission.PERMISSION_TYPE_GRANT);
           assertEquals(msg, 0, retrievedPermissions.length);

        for(idx=0; idx<NUMBER_TEST_PERMISSIONS; idx++)
        {
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

        retrievedPermissions = getPermissionStore().select
          (OWNER, EVERYONE_GROUP_PRINCIPAL_KEY,
           TEST_ACTIVITY,
           null,
           IPermission.PERMISSION_TYPE_GRANT);
           assertEquals(msg, NUMBER_TEST_PERMISSIONS, retrievedPermissions.length);

        msg = "Creating test DENY permission for student group.";
        print(msg);

        retrievedPermission = (IPermission)testPermissions.get(0);
        newPermission = getPermissionStore().newInstance(OWNER);
        newPermission.setActivity(TEST_ACTIVITY);
        newPermission.setPrincipal(STUDENT_GROUP_PRINCIPAL_KEY);
        newPermission.setTarget(retrievedPermission.getTarget());
        newPermission.setType(IPermission.PERMISSION_TYPE_DENY);

        retrievedPermissions = getPermissionStore().select
          (OWNER, STUDENT_GROUP_PRINCIPAL_KEY, TEST_ACTIVITY, retrievedPermission.getTarget(), IPermission.PERMISSION_TYPE_DENY);
        assertEquals(msg, 0, retrievedPermissions.length);
        getPermissionStore().add(newPermission);
        retrievedPermissions = getPermissionStore().select
          (OWNER, STUDENT_GROUP_PRINCIPAL_KEY, TEST_ACTIVITY, retrievedPermission.getTarget(), IPermission.PERMISSION_TYPE_DENY);
        assertEquals(msg, 1, retrievedPermissions.length);
        testPermissions.add(newPermission);

        msg = "Creating test DENY permission for student entity.";
        print(msg);

        newPermission = getPermissionStore().newInstance(OWNER);
        retrievedPermission = (IPermission)testPermissions.get(1);
        newPermission.setPrincipal(STUDENT_PRINCIPAL_KEY);
        newPermission.setActivity(TEST_ACTIVITY);
        newPermission.setTarget(retrievedPermission.getTarget());
        newPermission.setType(IPermission.PERMISSION_TYPE_DENY);

        retrievedPermissions = getPermissionStore().select
          (OWNER, STUDENT_PRINCIPAL_KEY, TEST_ACTIVITY, retrievedPermission.getTarget(), IPermission.PERMISSION_TYPE_DENY);
        assertEquals(msg, 0, retrievedPermissions.length);
        getPermissionStore().add(newPermission);
        retrievedPermissions = getPermissionStore().select
          (OWNER, STUDENT_PRINCIPAL_KEY, TEST_ACTIVITY, retrievedPermission.getTarget(), IPermission.PERMISSION_TYPE_DENY);
        assertEquals(msg, 1, retrievedPermissions.length);
        testPermissions.add(newPermission);


    }
    catch (Exception ex) { print("AuthorizationTester.setUp(): " + ex.getMessage());}
 }
/**
 * @return junit.framework.Test
 */
public static junit.framework.Test suite() {
    TestSuite suite = new TestSuite();

  suite.addTest(new AuthorizationTester("testAlternativePermissionPolicies"));
  suite.addTest(new AuthorizationTester("testPermissionStore"));
  suite.addTest(new AuthorizationTester("testDoesPrincipalHavePermission"));

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
        clearGroupCache();

        IPermission[] permissions = (IPermission[])testPermissions.toArray(new IPermission[testPermissions.size()]);
        getPermissionStore().delete(permissions);
        testPermissions.clear();

    }
    catch (Exception ex) { print("AuthorizationTester.tearDown(): " + ex.getMessage());}
}
/**
 */
public void testAlternativePermissionPolicies() throws Exception
{
    print("***** ENTERING AuthorizationTester.testAlternativePermissionPolicies() *****");
    String msg = null;
    boolean testResult = false;
    String activity = IPermission.CHANNEL_SUBSCRIBER_ACTIVITY;
    String existingTarget = "CHAN_ID.1";
    String nonExistingTarget = "CHAN_ID.9999";
    String everyoneKey = "local.0";

    msg = "Creating a group member for everyone (3." + everyoneKey + ").";
    print(msg);
    IGroupMember everyone = GroupService.getGroupMember(everyoneKey, GROUP_CLASS);
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
    testResult = prin.hasPermission(OWNER, activity, existingTarget, getPositivePermissionPolicy());
    assertTrue(msg, testResult);

    msg = "Testing NEGATIVE permission policy for an existing channel";
    print(msg);
    testResult = prin.hasPermission(OWNER, activity, existingTarget, getNegativePermissionPolicy());
    assertTrue(msg, ! testResult);

    msg = "Testing DEFAULT permission policy for a nonexistent channel";
    print(msg);
    testResult = prin.hasPermission(OWNER, activity, nonExistingTarget);
    assertTrue(msg, ! testResult);

    msg = "Testing POSITIVE permission policy for nonexistent channel";
    print(msg);
    testResult = prin.hasPermission(OWNER, activity, nonExistingTarget, getPositivePermissionPolicy());
    assertTrue(msg, testResult);

    msg = "Testing NEGATIVE permission policy for a nonexistent channel";
    print(msg);
    testResult = prin.hasPermission(OWNER, activity, nonExistingTarget, getNegativePermissionPolicy());
    assertTrue(msg, ! testResult);

    print("***** LEAVING AuthorizationTester.testAlternativePermissionPolicies() *****" + CR);

}
/**
 */
public void testDoesPrincipalHavePermission() throws Exception
{
    print("***** ENTERING AuthorizationTester.testDoesPrincipalHavePermission() *****");
    String msg = null;
    IPermission testPermission = null;
    boolean testResult = false;
    int idx = 0;


    msg = "Creating authorizationPrincipal for student.";
    print(msg);
    IAuthorizationPrincipal prin = getService().newPrincipal("student",IPERSON_CLASS);
    assertNotNull(msg, prin);

    testPermission = (IPermission)testPermissions.get(0);
    msg = "Testing  " + testPermission + " (should be TRUE -- inherited from Everyone)";
    print(msg);
    testResult = prin.hasPermission(OWNER, TEST_ACTIVITY, testPermission.getTarget());
    assertTrue(msg, testResult);

    testPermission = (IPermission)testPermissions.get(1);
    msg = "Testing  " + testPermission + " (should be FALSE -- directly denied)";
    print(msg);
    testResult = prin.hasPermission(OWNER, TEST_ACTIVITY, testPermission.getTarget());
    assertTrue(msg, ! testResult);

    msg = "Testing  the rest of the test permissions (should be TRUE).";
    print(msg);
    for (idx=2; idx<NUMBER_TEST_PERMISSIONS; idx++)
    {
        testPermission = (IPermission)testPermissions.get(idx);
        testResult = prin.hasPermission(OWNER, TEST_ACTIVITY, testPermission.getTarget());
        assertTrue(msg, testResult);
    }

    print("***** LEAVING AuthorizationTester.testDoesPrincipalHavePermission() *****" + CR);

}
/**
 */
public void testPermissionStore() throws Exception
{
    print("***** ENTERING AuthorizationTester.testPermissionStore() *****");
    String msg = null;
    boolean testResult = false;
    String activity = IPermission.CHANNEL_SUBSCRIBER_ACTIVITY;
    String existingTarget = "CHAN_ID.1";
    String nonExistingTarget = "CHAN_ID.000";
    String noonePrincipal = "3.local.999";
    IPermission[] permissions, addedPermissions = null;
    IPermission newPermission, retrievedPermission = null;
    java.util.Date effectiveDate = new java.util.Date();
    java.util.Date expirationDate = new java.util.Date(System.currentTimeMillis() + (60 * 60 * 24 * 1000));
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
    permissions = getPermissionStore().
      select(OWNER, EVERYONE_GROUP_PRINCIPAL_KEY, activity, nonExistingTarget, IPermission.PERMISSION_TYPE_GRANT);
    assertEquals(msg, 0, permissions.length);

    msg = "Adding permission to store.";
    print(msg);
    getPermissionStore().add(newPermission);
    permissions = getPermissionStore().
      select(OWNER, EVERYONE_GROUP_PRINCIPAL_KEY, activity, nonExistingTarget, IPermission.PERMISSION_TYPE_GRANT);
    assertEquals(msg, 1, permissions.length);

    // Update the new permission we have just added.
    msg = "Updating permission.";
    print(msg);
    retrievedPermission = permissions[0];
    retrievedPermission.setType(IPermission.PERMISSION_TYPE_DENY);
    retrievedPermission.setEffective(effectiveDate);
    retrievedPermission.setExpires(expirationDate);
    getPermissionStore().update(retrievedPermission);
    permissions = getPermissionStore().
      select(OWNER, EVERYONE_GROUP_PRINCIPAL_KEY, activity, nonExistingTarget, IPermission.PERMISSION_TYPE_DENY);
    assertEquals(msg, 1, permissions.length);
    assertEquals(msg, IPermission.PERMISSION_TYPE_DENY, permissions[0].getType());
    assertEquals(msg, effectiveDate, permissions[0].getEffective());
    assertEquals(msg, expirationDate, permissions[0].getExpires());

    // Delete the retrieved permission.
    msg = "Deleting the updated permission.";
    print(msg);
    getPermissionStore().delete(retrievedPermission);
    permissions = getPermissionStore().
      select(OWNER, EVERYONE_GROUP_PRINCIPAL_KEY, activity, nonExistingTarget, IPermission.PERMISSION_TYPE_DENY);
    assertEquals(msg, 0, permissions.length);

    // Add and delete an array of permissions.
    msg = "Creating and adding an Array of " + numAddedPermissions + " Permissions.";
    print(msg);
    addedPermissions = new IPermission[numAddedPermissions];
    for(idx=0; idx<numAddedPermissions; idx++)
    {
        addedPermissions[idx] = getPermissionStore().newInstance(OWNER);
        addedPermissions[idx].setActivity(activity);
        addedPermissions[idx].setPrincipal(noonePrincipal);
        addedPermissions[idx].setTarget(existingTarget + "_" + idx);
        addedPermissions[idx].setType(IPermission.PERMISSION_TYPE_GRANT);
        addedPermissions[idx].setEffective(effectiveDate);
        addedPermissions[idx].setExpires(expirationDate);
    }
    getPermissionStore().add(addedPermissions);
    permissions = getPermissionStore().select(OWNER, noonePrincipal, activity, null, null);
    assertEquals(msg, numAddedPermissions, permissions.length);

    msg = "Deleting the Array of " + numAddedPermissions + " Permissions.";
    print(msg);
    getPermissionStore().delete(permissions);
    permissions = getPermissionStore().select(OWNER, noonePrincipal, activity, null, null);
    assertEquals(msg, 0, permissions.length);

    print("***** LEAVING AuthorizationTester.testPermissionStore() *****" + CR);

}
}
