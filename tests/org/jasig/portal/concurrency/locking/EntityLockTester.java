package org.jasig.portal.concurrency.locking;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jasig.portal.concurrency.IEntityLock;
import org.jasig.portal.concurrency.IEntityLockService;
import org.jasig.portal.concurrency.LockingException;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.services.EntityLockService;

/**
 * Tests the entity lock framework.
 * @author: Dan Ellentuck
 */
public class EntityLockTester extends TestCase {
    private static Class GROUP_CLASS;
    private static Class IPERSON_CLASS;
    private IEntityLock[] testLocks;
    private IEntityLockStore lockStore;
    private int numUnexpiredIEntityGroupLocksInStore = 0;
    private int numExpiredLocks = 0;
    private int numUnexpiredIPersonLocksInStore = 0;
    private int numIPersonLocksInStoreForTestId = 0;
    private String[] testKeys = {"101", "102", "9999999", "12345"};
    private String[] testIds = {"de3", "df7", "av317"};
/**
 * EntityLockTester constructor comment.
 */
public EntityLockTester(String name) {
    super(name);
}
/**
 * @return org.jasig.portal.concurrency.locking.IEntityLockStore
 */
private IEntityLockStore getLockStore() {
    return lockStore;
}
/**
 * @return org.jasig.portal.groups.IEntityLockService
 */
private IEntityLockService getService()  throws LockingException{
    return ReferenceEntityLockService.singleton();
}
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) throws Exception
{
    String[] mainArgs = {"org.jasig.portal.concurrency.locking.EntityLockTester"};
    print("START TESTING LOCK STORE");
    printBlankLine();
    junit.swingui.TestRunner.main(mainArgs);
    printBlankLine();
    print("END TESTING LOCK STORE");

}
/**
 */
private static void print (IEntityLock[] locks)
{
    for ( int i=0; i<locks.length; i++ )
    {
        print("(" + (i+1) + ") " + locks[i]);
    }
    print("  Total: " + locks.length);
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


    try
    {
        boolean multiServer =
            PropertiesManager.getPropertyAsBoolean("org.jasig.portal.concurrency.multiServer");

        lockStore = ( multiServer )
            ? RDBMEntityLockStore.singleton()
            : MemoryEntityLockStore.singleton();
    }
    catch ( Exception e )
    {
        System.out.println("EntityLockTester:setUp(): Failed to instantiate entity lock store. " + e);
    }


        lockStore.deleteAll();

        java.util.Date earlier  =
            new java.util.Date(System.currentTimeMillis() - 1000);    // - 1 second
        java.util.Date later =
            new java.util.Date(System.currentTimeMillis() + 300000);  // + 5 minutes
        testLocks = new IEntityLock[5];
        testLocks[0] = new EntityLockImpl(GROUP_CLASS, testKeys[0], 0, later, testIds[0]);
        testLocks[1] = new EntityLockImpl(GROUP_CLASS, testKeys[1], 0, later, testIds[0]);
        testLocks[2] = new EntityLockImpl(IPERSON_CLASS, testKeys[0], 0, earlier, testIds[0]);
        testLocks[3] = new EntityLockImpl(IPERSON_CLASS, testKeys[1], 1, later, testIds[0]);
        testLocks[4] = new EntityLockImpl(IPERSON_CLASS, testKeys[2], 1, later, testIds[1]);

        print("Adding test locks.");
        for (int i=0; i<testLocks.length; i++) {
            getLockStore().add(testLocks[i]);
            // print("Added " + testLocks[i]);
        }

        numUnexpiredIEntityGroupLocksInStore = 2;
        numExpiredLocks = 1;
        numUnexpiredIPersonLocksInStore = 2;
        numIPersonLocksInStoreForTestId = 1;
    }
    catch (Exception ex) { print("EntityLockTester.setUp(): " + ex.getMessage());}
 }
/**
 * @return junit.framework.Test
 */
public static junit.framework.Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTest(new EntityLockTester("testExistsInStore"));
    suite.addTest(new EntityLockTester("testSelectFromStore"));
    suite.addTest(new EntityLockTester("testExpirationInStore"));
    suite.addTest(new EntityLockTester("testStoreUpdate"));
    suite.addTest(new EntityLockTester("testServiceNewLock"));
    suite.addTest(new EntityLockTester("testServiceLockRenewal"));
    suite.addTest(new EntityLockTester("testServiceConvert"));
    suite.addTest(new EntityLockTester("testService"));

//	Add more tests here.
//  NB: Order of tests is not guaranteed.

    return suite;
}
/**
 */
protected void tearDown()
{
    try {
        // delete any remaining test locks.
        print("Deleting test locks.");
        for (int i=0; i<testLocks.length; i++) { getLockStore().delete(testLocks[i]); }
    }
    catch (Exception ex) { print("EntityLockTester.tearDown(): " + ex.getMessage());}
}
/**
 */
public void testExistsInStore() throws Exception
{
        String msg = null;
        IEntityLock lock = testLocks[4];
        msg = "Checking if " + lock + " exists in database.";
        print(msg);
        boolean exists = getService().existsInStore(lock);
        assertTrue(msg, exists);

        // Delete the lock:
        print("Deleting lock from database.");
        getLockStore().delete(lock);

        // does lock exist?
        msg = "Checking if deleted lock exists in database.";
        print(msg);
        exists = getService().existsInStore(lock);
        assertTrue(msg, ! exists);

       // Add the lock back:
        print("Adding back the lock just deleted from database.");
        getLockStore().add(lock);

}
/**
 */
public void testExpirationInStore() throws Exception
{
    int numLocks = 0;
    int ctr = 0;
    String msg = null;
    IEntityLock[] selectedLocks = null;
    java.util.Date now = new java.util.Date(System.currentTimeMillis());

    // select unexpired locks by entity type
    msg = "Selecting unexpired locks by type.";
    print(msg);
    selectedLocks = getLockStore().findUnexpired(now, IPERSON_CLASS, null, null, null);
    numLocks = selectedLocks.length;
    assertEquals(numLocks, numUnexpiredIPersonLocksInStore);
    for (ctr=0; ctr<numLocks; ctr++)
        { assertTrue(msg, selectedLocks[ctr].getExpirationTime().after(now)); }

    // Get rid of the EXPIRED locks.
    print("Deleting expired locks.");
     getLockStore().deleteExpired(new java.util.Date(System.currentTimeMillis()));

    // Select the remaining UNEXPIRED locks.
    msg = "Selected all remaining locks.";
    print(msg);
    selectedLocks = getLockStore().find(null, null, null, null, null);

    numLocks = selectedLocks.length;
    msg = "Selected " + numLocks + " unexpired locks";
    print(msg);
    assertEquals(msg, testLocks.length - numExpiredLocks, numLocks);
    for (ctr=0; ctr<numLocks; ctr++)
        { assertTrue(msg, selectedLocks[ctr].getExpirationTime().after(now)); }

}
/**
 */
public void testSelectFromStore() throws Exception
{
    int numLocks = 0;
    int ctr = 0;
    IEntityLock[] selectedLocks = null;
    String msg = null;
    java.util.Date now = new java.util.Date(System.currentTimeMillis());

        // select locks by entity type
        msg = "Selecting locks for Group type.";
        print(msg);
        selectedLocks = getLockStore().find(GROUP_CLASS, null, null, null, null);
        numLocks = selectedLocks.length;
        assertEquals(numLocks, numUnexpiredIEntityGroupLocksInStore);
        for (ctr=0; ctr<numLocks; ctr++)
            { assertEquals(msg, selectedLocks[ctr].getEntityType(), GROUP_CLASS); }

        // select locks by entity type and entity key
        msg = "Selecting locks by type and key.";
        print(msg);
        selectedLocks = getLockStore().find(GROUP_CLASS, testKeys[1], null, null, null);
        numLocks = selectedLocks.length;
        assertEquals(msg, numLocks, 1);
        assertEquals(msg, selectedLocks[0].getEntityType(), GROUP_CLASS);
        assertEquals(msg, selectedLocks[0].getEntityKey(), testKeys[1]);

       // select locks by entity type, entity key and lock type
        msg = "Selecting locks by type, key, and lock type";
        print(msg);
        selectedLocks = getLockStore().find(IPERSON_CLASS, testKeys[1], new Integer(1), null, null);
        numLocks = selectedLocks.length;
        assertEquals(msg, numLocks, 1);
        assertEquals(msg, selectedLocks[0].getEntityType(), IPERSON_CLASS);
        assertEquals(msg, selectedLocks[0].getEntityKey(), testKeys[1]);
        assertEquals(msg, selectedLocks[0].getLockType(), 1);

        selectedLocks = getLockStore().find(IPERSON_CLASS, testKeys[1], new Integer(0), null, null);
        numLocks = selectedLocks.length;
        assertEquals(msg, numLocks, 0);

        // select locks by entity type and owner
        msg = "Selecting locks by entity type and owner";
        String id = testIds[1];
        print(msg);
        selectedLocks = getLockStore().find(IPERSON_CLASS, null, null, null, id );
        numLocks = selectedLocks.length;
        assertEquals(numLocks, numIPersonLocksInStoreForTestId);
        for (ctr=0; ctr<numLocks; ctr++)
        {
            assertEquals(msg, selectedLocks[ctr].getEntityType(), IPERSON_CLASS);
            assertEquals(msg, selectedLocks[ctr].getLockOwner(), id);
        }
}
/**
 */
public void testService() throws Exception
{

    String msg = null;
    IEntityLock readLock1, readLock2, writeLock = null;

    String key = System.currentTimeMillis() + "";

    print("Creating first read lock.");
    readLock1 = EntityLockService.instance().newReadLock(IPERSON_CLASS, key, testIds[0]);

    print("Creating second read lock (for same entity).");
    readLock2 = EntityLockService.instance().newReadLock(IPERSON_CLASS, key, testIds[1]);

    msg = "Attempting to create a write lock for the entity: should fail.";
    print(msg);
    try
        { writeLock = EntityLockService.instance().newWriteLock(IPERSON_CLASS, key, testIds[2]); }
    catch (LockingException le)
        { System.out.println("Caught Exception: " + le.getMessage()); }

    assertNull(msg, writeLock);

    msg = "Releasing read locks: lock should be invalid.";
    print(msg);
    readLock1.release();
    assertTrue( msg, ! readLock1.isValid() );
    readLock2.release();
    assertTrue( msg, ! readLock2.isValid() );

    msg = "Attempting to create a write lock for the entity: should succeed.";
    print(msg);
    try
        { writeLock = EntityLockService.instance().newWriteLock(IPERSON_CLASS, key, testIds[2]); }
    catch (LockingException le)
        { System.out.println("Caught Exception: " + le.getMessage()); }

    assertTrue( msg, writeLock.isValid() );
    msg = "Releasing write lock: should be invalid.";
    print(msg);
    writeLock.release();
    assertTrue( msg, ! writeLock.isValid() );
}
/**
 */
public void testServiceConvert() throws Exception
{

    String msg = null;
    boolean valid = false;
    IEntityLockService service = getService();
    int readSecs = 30;
    int writeSecs = 45;

    // Create a READ lock on Group testKeys[3], owned by testIds[0]:
    print("Creating new READ lock");
    IEntityLock lock = service.newLock(GROUP_CLASS, testKeys[3], IEntityLockService.READ_LOCK, testIds[0], readSecs);
    msg = "Testing if new lock is valid";
    valid = service.isValid(lock);
    print(msg);
    assertTrue(msg, valid);

    // Convert the READ lock to a WRITE lock:
    print("Converting READ lock to WRITE");
    service.convert(lock, IEntityLockService.WRITE_LOCK, writeSecs);
    msg = "Testing if converted lock is still valid";
    valid = service.isValid(lock);
    print(msg);
    assertTrue(msg, valid);

    // Convert the WRITE lock back to a READ lock:
    print("Converting WRITE lock to READ");
    service.convert(lock, IEntityLockService.READ_LOCK, readSecs);
    msg = "Testing if converted lock is still valid";
    valid = service.isValid(lock);
    print(msg);
    assertTrue(msg, valid);

    // Now try to create a WRITE lock on the same entity for a different owner.
    IEntityLock duplicateLock = null;
    msg = "Attempting to create a duplicate lock; should be null";
    print(msg);

    try { duplicateLock = service.newLock(GROUP_CLASS, testKeys[3], IEntityLockService.WRITE_LOCK, testIds[1]); }
    catch (LockingException le) {print("Caught exception: " + le.getMessage()); }

    assertNull(msg, duplicateLock);

}
/**
 */
public void testServiceLockRenewal() throws Exception
{
    String msg = null;
    boolean valid = false;
    IEntityLockService service = getService();

    msg = "Attempting to renew an old lock";
    print(msg);
    IEntityLock badLock = testLocks[2];
    msg = "Checking if lock was renewed.";
    print(msg);
    try { service.renew(badLock); } catch (Exception ex) {print("Caught Exception: " + ex.getMessage()); }
    assertTrue(msg, ! service.isValid(badLock));

    msg = "Attempting to renew a valid lock";
    print(msg);
    IEntityLock goodLock = testLocks[0];
    msg = "Checking if lock was renewed.";
    print(msg);
    try { service.renew(goodLock); } catch (Exception ex) {print("Caught Exception: " + ex.getMessage()); }
    assertTrue(msg, service.isValid(goodLock));

}
/**
 */
public void testServiceNewLock() throws Exception
{

    String msg = null;
    boolean valid = false;

    print("Creating new lock");
    IEntityLockService service = getService();
    IEntityLock newLock = service.newLock(GROUP_CLASS, testKeys[3], IEntityLockService.WRITE_LOCK, testIds[0]);
    msg = "Testing if new lock is valid";
    valid = getService().existsInStore(newLock);
    print(msg);
    assertTrue(msg, valid);

    print("Releasing new lock");
    getService().release(newLock);
    msg = "Testing if new lock is still valid";
    valid = getService().existsInStore(newLock);
    print(msg);
    assertTrue(msg, ! valid);

}
/**
 */
public void testStoreUpdate() throws Exception
{
    long fiveMinutes = 1000 * 60 * 5;
    long tenMinutes =  1000 * 60 * 10;
    long now = System.currentTimeMillis();
    String msg = null;

    print("Update expiration and lock type of testLocks[1].");

    java.util.Date newExpiration = new java.util.Date(now + fiveMinutes);
    int newType = IEntityLockService.WRITE_LOCK;

    // Copy testLocks[1] to lock1.
    IEntityLock lock1 = new EntityLockImpl
        (testLocks[1].getEntityType(), testLocks[1].getEntityKey(),
         testLocks[1].getLockType(), testLocks[1].getExpirationTime(), testLocks[1].getLockOwner());

    // Update testLocks[1].
    getLockStore().update(testLocks[1], newExpiration, new Integer(newType));
    ((EntityLockImpl)testLocks[1]).setExpirationTime(newExpiration);
    ((EntityLockImpl)testLocks[1]).setLockType(newType);

    msg = "Check if the old version (lock1) still exists in store.";
    print(msg);
    assertTrue(msg, ! getService().existsInStore(lock1) );

    msg = "Check if new version exists in store.";
    print(msg);
    IEntityLock lock2 = new EntityLockImpl
        (lock1.getEntityType(), lock1.getEntityKey(), newType, newExpiration, lock1.getLockOwner());
    assertTrue( msg, getService().existsInStore(lock2) );

    print("Update only expiration on (updated) lock.");
    newExpiration = new java.util.Date(now + tenMinutes);
    getLockStore().update(lock2, newExpiration, null);
    ((EntityLockImpl)lock2).setExpirationTime(newExpiration);

    msg = "Check if un-updated lock still exists in store.";
    print(msg);
    assertTrue( msg, ! getService().existsInStore(testLocks[1]) );

    msg = "Check if the doubly-updated lock exists in store.";
    print(msg);
    IEntityLock lock3 = new EntityLockImpl
        (lock2.getEntityType(), lock2.getEntityKey(), lock2.getLockType(), newExpiration, lock2.getLockOwner());
    assertTrue( msg, getService().existsInStore(lock3) );

    testLocks[0] = lock3;
}
}
