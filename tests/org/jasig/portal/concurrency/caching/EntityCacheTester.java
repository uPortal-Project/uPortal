package org.jasig.portal.concurrency.caching;

import java.util.Date;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.IEntityCache;
import org.jasig.portal.services.EntityCachingService;

/**
 * Tests the entity caching framework.
 * @author: Dan Ellentuck
 * 
 * TESTS:
 *  (1) testEntityServiceAddsAndDeletes() -- Add, retrieve and remove entities 
 *      via the service facade.
 *  (2) testEntityCacheAddsAndDeletes() -- Add, retrieve and remove entries
 *      from an IEntityCache.
 *  (3) testEntityCacheSweep() -- Sweep must remove correct number of entries 
 *      from cache, based on time of last use.
 *  (4) testInvalidatingCacheAddsAndDeletes() -- Add, retrieve and remove 
 *      entries from an invalidating IEntityCache.
 *  (5) testInvalidatingCacheInvalidation() -- Invalidating IEntityCaches must 
 *      invalidate each others' entries when their own entries are either 
 *      updated or deleted.
 *  (6) testStoreAddsAndDeletes() -- Add and delete invalidations via the 
 *      invalidation store.
 *  (7) testStoreBeforeAndAfter() -- Retrieve invalidations from the store 
 *      that were added after some point in time.
 *  (8) testStoreUpdates() -- Retrieve invalidations from the store that
 *      were updated after some point in time.
 *  (9) testFudgeFactor() -- An earlier invalidation from one invalidating cache
 *      should invalidate its corresponding entity in another cache IF the
 *      interval is no greater than the fudge factor.  But a cache must not
 *      clobber its own entries.
 *
 */
public class EntityCacheTester extends TestCase {
    private static Class GROUP_CLASS;
    private static Class IPERSON_CLASS;
    private static Class MINIMAL_ENTITY_CLASS;
    private IBasicEntity[] testEntities;
    private String[] testEntityKeys;
    private int numTestEntities = 0;

    // cache defaults:
    private int cacheSize = 1000;
    private int cacheIdleTimeSecs = 5*60;
    private int cacheSweepIntervalSecs = 30;
    private int clockTolerance = 5000;


    private class MinimalEntity implements IBasicEntity
    {
        private String key;
        private MinimalEntity(String entityKey) {
            super();
            key = entityKey;
        }
        public EntityIdentifier getEntityIdentifier() {
            return new EntityIdentifier(getKey(), getType());
        }
        public Class getType() {
            return this.getClass();
        }
        public String getKey() {
            return key;
        }
        public boolean equals(Object o) {
            if ( o == null )
                return false;
            if ( ! (o instanceof IBasicEntity) )
                return false;
            IBasicEntity ent = (IBasicEntity) o;
            return ent.getEntityIdentifier().getType() == getType() &&
                   ent.getEntityIdentifier().getKey().equals(getKey());
        }
        public String toString() {
            return "MinimalEntity(" + key + ")";
        }
    }
/**
 * EntityLockTester constructor comment.
 */
public EntityCacheTester(String name) {
    super(name);
}
/**
 */
protected void addTestEntityType()
{
    try
    {
        org.jasig.portal.EntityTypes.singleton().
          addEntityTypeIfNecessary(MINIMAL_ENTITY_CLASS, "Test Entity Type");
    }
    catch (Exception ex) { print("EntityCacheTester.addTestEntityType(): " + ex.getMessage());}
 }
/**
 */
protected void deleteTestEntityType()
{
    try
    {
        org.jasig.portal.EntityTypes.singleton().deleteEntityType(MINIMAL_ENTITY_CLASS);
    }
    catch (Exception ex) { print("EntityCacheTester.deleteTestEntityType(): " + ex.getMessage());}
 }
/**
 * @return org.jasig.portal.concurrency.caching.IEntityCache
 */
private IEntityCache getEntityCache() throws CachingException
{
    return getEntityCache(cacheSize, cacheIdleTimeSecs*1000, cacheSweepIntervalSecs*1000);
}
/**
 * @return org.jasig.portal.concurrency.caching.IEntityCache
 */
private IEntityCache getEntityCache(int size, int idleTime, int sweepInterval)
throws CachingException
{
    return new ReferenceEntityCache(MINIMAL_ENTITY_CLASS, size, idleTime, sweepInterval);
}
/**
 * @return org.jasig.portal.concurrency.IEntityCache
 */
private IEntityCache getInvalidatingEntityCache() throws CachingException
{
    return getInvalidatingEntityCache(cacheSize, cacheIdleTimeSecs*1000, cacheSweepIntervalSecs*1000, clockTolerance);
}
/**
 * @return org.jasig.portal.concurrency.IEntityCache
 */
private IEntityCache getInvalidatingEntityCache(int size, int idleTime, int sweepInterval, int tolerance)
throws CachingException
{
    return new ReferenceInvalidatingEntityCache(MINIMAL_ENTITY_CLASS, size, idleTime, sweepInterval, tolerance);
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
 * @return RDBMCachedEntityInvalidationStore
 */
private RDBMCachedEntityInvalidationStore getStore() throws CachingException
{
    return RDBMCachedEntityInvalidationStore.singleton();
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
 */
private static void print (IBasicEntity[] entities)
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
    getStore().deleteAll();

    numTestEntities = 1000;

    // Entity keys:
    testEntityKeys = new String[numTestEntities];
    java.util.Random random = new java.util.Random();
    for (int i=0; i<numTestEntities; i++)
        { testEntityKeys[i] = (getRandomString(random, 3) + i); }

    // Entities
    testEntities = new IBasicEntity[numTestEntities];
    for (int i=0; i<numTestEntities; i++)
        { testEntities[i] = new MinimalEntity(testEntityKeys[i]); }

    }
    catch (Exception ex) { print("EntityCacheTester.setUp(): " + ex.getMessage());}
 }
/**
 * @return junit.framework.Test
 */
public static junit.framework.Test suite() {
    TestSuite suite = new TestSuite();

  suite.addTest(new EntityCacheTester("testIEntityCacheAddsAndDeletes"));
  suite.addTest(new EntityCacheTester("testStoreAddsAndDeletes"));
  suite.addTest(new EntityCacheTester("testStoreBeforeAndAfter"));
  suite.addTest(new EntityCacheTester("testStoreUpdates"));
  suite.addTest(new EntityCacheTester("testInvalidatingCacheAddsAndDeletes"));
  suite.addTest(new EntityCacheTester("testInvalidatingCacheInvalidation"));
  suite.addTest(new EntityCacheTester("testIEntityCacheSweep"));
  suite.addTest(new EntityCacheTester("testEntityCachingServiceAddsAndDeletes"));
  suite.addTest(new EntityCacheTester("testFudgeFactor"));


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
        deleteTestEntityType();
        getStore().deleteAll();
    }
    catch (Exception ex) { print("EntityCacheTester.tearDown(): " + ex.getMessage());}
}
/**
 * Adds, retrieves and removes entities via the service facade,
 * org.jasig.portal.services.EntityCachingServices.
 *  - Adds must be found.
 *  - Removes must not be found.
 */
public void testEntityCachingServiceAddsAndDeletes() throws Exception
{
    print("***** ENTERING EntityCacheTester.testEntityCachingAddsAndDeletes() *****");
    String msg = null;
    int idx = 0;
    IBasicEntity ent = null;
    int adds = 100;
    Class type = MINIMAL_ENTITY_CLASS;

    EntityCachingService service = EntityCachingService.instance();

    msg = "Adding " + adds + " entities to the cache.";
    print(msg);
    for(idx=0; idx<adds; idx++)
        { service.add(testEntities[idx]); }

    msg = "Retrieving entities from the cache.";
    print(msg);
    for(idx=0; idx<adds; idx++)
    {
        ent = service.get(type, testEntityKeys[idx] );
        assertEquals(msg, ent, testEntities[idx]);
    }

    msg = "Removing entities from the cache.";
    print(msg);
    for(idx=0; idx<numTestEntities; idx++)
    {
        service.remove( type, testEntityKeys[idx] );
        ent = service.get( type, testEntityKeys[idx] );
        assertNull(msg, ent);
    }

    print("***** LEAVING EntityCacheTester.testEntityCachingServiceAddsAndDeletes() *****");

}
/**
 * Gets an instance of IEntityCache and adds, retrieves and removes 
 * entities directly from the cache.  
 * 
 *  - After adds, cache size must equal number of adds.
 *  - Adds must be correctly retrieved.
 *  - Removes must not be found.
 *  - After removes, cache size must be 0.
 */
public void testIEntityCacheAddsAndDeletes() throws Exception
{
    print("***** ENTERING EntityCacheTester.testIEntityCacheAddsAndDeletes() *****");
    String msg = null;
    int idx = 0;
    IBasicEntity ent = null;

    IEntityCache c = getEntityCache();
    ReferenceEntityCache rec = (ReferenceEntityCache) c;
    msg = "Adding " + numTestEntities + " entities to the cache.";
    print(msg);
    for(idx=0; idx<numTestEntities; idx++)
        { c.add(testEntities[idx]); }

    assertEquals(msg, rec.size(), numTestEntities);

    msg = "Retrieving entities from the cache.";
    print(msg);
    for(idx=0; idx<numTestEntities; idx++)
    {
        ent = c.get( testEntityKeys[idx] );
        assertEquals(msg, ent, testEntities[idx]);
    }

    msg = "Removing entities from the cache.";
    print(msg);
    for(idx=0; idx<numTestEntities; idx++)
    {
        c.remove( testEntityKeys[idx] );
        ent = c.get( testEntityKeys[idx] );
        assertNull(msg, ent);
    }

    // We should have removed all entries.
    assertEquals(msg, rec.size(), 0);

    print("***** LEAVING EntityCacheTester.testIEntityCacheAddsAndDeletes() *****");

}
/**
 * Creates an IEntityCache and adds [firstAdded] number of entities.
 *  - Size of cache must equal number of adds.
 * Sleeps for 1/2 of (sweep interval + max age), then touches [touched] number 
 * of entities. Now adds [secondAdded] number of entities and sleeps for
 * 1/2 of sweep interval. Sweep should remove all firstAdded entities except those 
 * that were touched.
 *  - Size of cache must = secondAdded + touched - firstAdded. 
 */
public void testIEntityCacheSweep() throws Exception
{
    print("***** ENTERING EntityCacheTester.testIEntityCacheSweep() *****");
    String msg = null;
    int idx = 0;
    IBasicEntity ent = null;

    int firstAdded = 100;
    int secondAdded = 25;
    int touched = firstAdded / 2;
    int maxSize = (firstAdded + 1);
    int maxIdleSecs = 50;
    int sweepIntervalSecs = maxIdleSecs / 2;

    IEntityCache c = getEntityCache(maxSize, maxIdleSecs*1000, sweepIntervalSecs*1000);
    msg = "Adding " + firstAdded + " entities to the cache.";
    print(msg);
    for(idx=0; idx<firstAdded; idx++)
        { c.add(testEntities[idx]); }

    assertEquals(msg, c.size(), firstAdded);

    int sleepSecs = ((maxIdleSecs + sweepIntervalSecs) / 2);
    print("Now sleeping for " + sleepSecs  + " secs.");
    Thread.sleep(sleepSecs*1000);

    print("Now touching " + touched + " entries.");
    for(idx=0; idx<touched; idx++)
        { c.get(testEntityKeys[idx]); }

    msg = "Adding " + secondAdded + " entities to the cache.";
    print(msg);
    for(idx=firstAdded; idx<firstAdded+secondAdded; idx++)
        { c.add(testEntities[idx]); }

    print("Now sleeping for " + (maxIdleSecs/2) + " secs.");
    Thread.sleep(maxIdleSecs*500);

    msg = "Sweep should have purged " + (firstAdded - touched) + " entries.";
    print(msg);
    assertEquals(msg, (touched + secondAdded), c.size());

    print("***** LEAVING EntityCacheTester.testIEntityCacheSweep() *****");

}
/**
 * Gets an INVALIDATING instance of IEntityCache and adds, retrieves and removes 
 * entities directly from the cache.  
 * 
 *  - After adds, cache size must equal number of adds.
 *  - Adds must be correctly retrieved.
 *  - Removes must not be found.
 *  - After removes, cache size must be 0.
 */
public void testInvalidatingCacheAddsAndDeletes() throws Exception
{
    print("***** ENTERING EntityCacheTester.testInvalidatingCacheAddsAndDeletes() *****");
    String msg = null;
    int idx = 0;
    IBasicEntity ent = null;
    int numEntitiesToBeTested = 100;

    IEntityCache c = getInvalidatingEntityCache();
    ReferenceInvalidatingEntityCache rec = (ReferenceInvalidatingEntityCache) c;
    msg = "Adding " + numEntitiesToBeTested + " entities to the cache.";
    print(msg);
    for(idx=0; idx<numEntitiesToBeTested; idx++)
        { c.add(testEntities[idx]); }

    assertEquals(msg, rec.size(), numEntitiesToBeTested);

    msg = "Retrieving entities from the cache.";
    print(msg);
    for(idx=0; idx<numEntitiesToBeTested; idx++)
    {
        ent = c.get( testEntityKeys[idx] );
        assertEquals(msg, ent, testEntities[idx]);
    }

    msg = "Removing entities from the cache.";
    print(msg);
    for(idx=0; idx<numEntitiesToBeTested; idx++)
    {
        c.remove( testEntityKeys[idx] );
        ent = c.get( testEntityKeys[idx] );
        assertNull(msg, ent);
    }

    // We should have removed all entries.
    assertEquals(msg, rec.size(), 0);

    print("***** LEAVING EntityCacheTester.testInvalidatingCacheAddsAndDeletes() *****");

}
/**
 * Get 2 INVALIDATING IEntityCaches and add the same entities to each.
 *  - Size of each cache must = number entities added.
 * Update some entities from the first and remove other entities from the second,
 * then sleep for the sweep interval.
 *  - First cache must have number entities added - number of deletes from second.
 *  - Second cache must have number entities added - number of deletes - number updates.
 */
public void testInvalidatingCacheInvalidation() throws Exception
{
    print("***** ENTERING EntityCacheTester.testInvalidatingInvalidation() *****");
    String msg = null;
    int idx = 0;
    IBasicEntity ent = null;
    int numEntitiesToBeAdded = 10;
    int numEntitiesToBeUpdated = 5;
    int numEntitiesToBeDeleted = 5;

    IEntityCache cacheA = getInvalidatingEntityCache();
    ReferenceInvalidatingEntityCache recA = (ReferenceInvalidatingEntityCache) cacheA;
    IEntityCache cacheB = getInvalidatingEntityCache();
    ReferenceInvalidatingEntityCache recB = (ReferenceInvalidatingEntityCache) cacheB;

    msg = "Adding " + numEntitiesToBeAdded + " entities to both caches.";
    print(msg);
    for(idx=0; idx<numEntitiesToBeAdded; idx++)
    {
        cacheA.add(testEntities[idx]);
        cacheB.add(testEntities[idx]);
    }

    assertEquals(msg, recA.size(), numEntitiesToBeAdded);
    assertEquals(msg, recB.size(), numEntitiesToBeAdded);

    Thread.sleep(100);

    msg = "Updating " + numEntitiesToBeUpdated + " in first cache.";
    print(msg);
    for(idx=0; idx<numEntitiesToBeUpdated; idx++)
        { cacheA.update( testEntities[idx] ); }

    msg = "Removing " + numEntitiesToBeDeleted + " from second cache.";
    print(msg);
    for(idx=numEntitiesToBeUpdated; idx<numEntitiesToBeAdded; idx++)
        { cacheB.remove( testEntityKeys[idx] ); }

    print("Will now sleep for " + (cacheSweepIntervalSecs + 5) + " seconds.");
    Thread.sleep( (cacheSweepIntervalSecs + 5) * 1000);

    // Check the caches.
    msg = "Checking first cache for invalidations";
    print(msg);
    assertEquals(msg, (numEntitiesToBeAdded - numEntitiesToBeDeleted), recA.size());
    for(idx=numEntitiesToBeUpdated; idx<numEntitiesToBeAdded; idx++)
        { assertNull(msg, cacheA.get( testEntityKeys[idx] )); }

    msg = "Check second cache for invalidations";
    assertEquals(msg, (numEntitiesToBeAdded - numEntitiesToBeDeleted - numEntitiesToBeUpdated), recB.size());


    print("***** LEAVING EntityCacheTester.testInvalidatingCacheInvalidation() *****");

}
/**
 * Adds and deletes invalidations directly via RDBMCachedEntityInvalidationStore.
 *  - Must be able to retrieve number of invalidations added from the store.
 *  - After deletions, must retrieve 0 invalidations from the store.
 */
public void testStoreAddsAndDeletes() throws Exception
{
    print("***** ENTERING EntityCacheTester.testStoreAddsAndDeletes() *****");
    String msg = null;
    int idx = 0;
    CachedEntityInvalidation[] invalidations = null;
    int numAdds = 5;

    msg = "Adding " + numAdds + " invalidations to the store.";
    print(msg);
    for(idx=0; idx<numAdds; idx++)
        { getStore().add(testEntities[idx], 0); }

    msg = "Retrieving invalidations from the store.";
    print(msg);
    invalidations = getStore().find(MINIMAL_ENTITY_CLASS, null);

    assertEquals(msg, invalidations.length, numAdds);

    msg = "Deleting invalidations from the store.";
    print(msg);
    getStore().deleteBefore(new Date());

    msg = "Retrieving invalidations from the store.";
    print(msg);
    invalidations = getStore().find(MINIMAL_ENTITY_CLASS, null);

    assertEquals(msg, invalidations.length, 0);

    print("***** LEAVING EntityCacheTester.testStoreAddsAndDeletes() *****");

}
/**
 * Adds invalidations directly to the store in 2 batches.  
 * - Must be able to correctly findAfter(). 
 */
public void testStoreBeforeAndAfter() throws Exception
{
    print("***** ENTERING EntityCacheTester.testStoreBeforeAndAfter() *****");
    String msg = null;
    int idx = 0;
    CachedEntityInvalidation[] invalidations = null;
    int numBeforeAdds = 3;
    int numAfterAdds = 2;
    int cacheID = 0;

    msg = "Adding " + numBeforeAdds + " invalidations to the store.";
    print(msg);
    for(idx=0; idx<numBeforeAdds; idx++)
        { getStore().add(testEntities[idx], cacheID); }

    msg = "Retrieving invalidations from the store.";
    print(msg);
    invalidations = getStore().find(MINIMAL_ENTITY_CLASS, null);

    assertEquals(msg, invalidations.length, numBeforeAdds);

    Date now = new Date();
    Thread.sleep(10);

    msg = "Adding " + numAfterAdds + " invalidations to the store.";
    print(msg);
    for(idx=numBeforeAdds; idx<(numAfterAdds + numBeforeAdds); idx++)
        { getStore().add(testEntities[idx], cacheID); }

    msg = "Retrieving invalidations from the store.";
    print(msg);
    invalidations = getStore().find(MINIMAL_ENTITY_CLASS, null);

    assertEquals(msg, invalidations.length, numBeforeAdds + numAfterAdds);

    msg = "Retrieving invalidations inserted AFTER first batch from the store.";
    print(msg);
    invalidations = getStore().findAfter(now, MINIMAL_ENTITY_CLASS, null, null);

    assertEquals(msg, invalidations.length, numAfterAdds);


    msg = "Deleting first batch of invalidations from the store.";
    print(msg);
    getStore().deleteBefore(now);

    msg = "Retrieving invalidations from the store.";
    print(msg);
    invalidations = getStore().find(MINIMAL_ENTITY_CLASS, null);

    assertEquals(msg, invalidations.length, numAfterAdds);

    print("***** LEAVING EntityCacheTester.testStoreBeforeAndAfter() *****");

}
/**
 * Add some invalidations to the store and then update some of them.  
 * - Test findAfter() to retrieve only the updated ones.
 */
public void testStoreUpdates() throws Exception
{
    print("***** ENTERING EntityCacheTester.testStoreUpdates() *****");
    String msg = null;
    int idx = 0;
    CachedEntityInvalidation[] invalidations = null;
    int numAdds = 5;
    int numUpdates = 2;

    msg = "Adding " + numAdds + " invalidations to the store.";
    print(msg);
    for(idx=0; idx<numAdds; idx++)
        { getStore().add(testEntities[idx], 0); }

    msg = "Retrieving invalidations from the store.";
    print(msg);
    invalidations = getStore().find(MINIMAL_ENTITY_CLASS, null);

    assertEquals(msg, invalidations.length, numAdds);

    Date now = new Date();
    Thread.sleep(10);

    msg = "Updating " + numUpdates + " invalidations in the store.";
    print(msg);
    for(idx=0; idx<numUpdates; idx++)
        { getStore().add(testEntities[idx], 0); }

    msg = "Retrieving only updated invalidations from the store.";
    print(msg);
    for(idx=0; idx<numUpdates; idx++)
    {
        String key = testEntities[idx].getEntityIdentifier().getKey();
        invalidations = getStore().findAfter(now, MINIMAL_ENTITY_CLASS, key, null);
        assertEquals(msg, 1, invalidations.length);
    }

    print("***** LEAVING EntityCacheTester.testStoreUpdates() *****");

}
/*
 * 
 */
public void testFudgeFactor() throws Exception 
{
    print("***** ENTERING EntityCacheTester.testFudgeFactor() *****");

    String msg = null;
    int idx = 0;
    int numEntitiesToBeAdded = 10;
    int numEntitiesToBeUpdated = 5;
    int numEntitiesToBeDeleted = 2;

    IEntityCache cacheA = getInvalidatingEntityCache();
    ReferenceInvalidatingEntityCache recA = (ReferenceInvalidatingEntityCache) cacheA;
    IEntityCache cacheB = getInvalidatingEntityCache();
    ReferenceInvalidatingEntityCache recB = (ReferenceInvalidatingEntityCache) cacheB;

    msg = "Adding " + numEntitiesToBeAdded + " entities to FIRST cache.";
    print(msg);
    for(idx=0; idx<numEntitiesToBeAdded; idx++)
        { cacheA.add(testEntities[idx]); }
    assertEquals(msg, recA.size(), numEntitiesToBeAdded);

    Thread.sleep(100);

    msg = "Updating " + numEntitiesToBeUpdated + " in first cache.";
    print(msg);
    for(idx=0; idx<numEntitiesToBeUpdated; idx++)
        { cacheA.update( testEntities[idx] ); }

    msg = "Adding " + numEntitiesToBeAdded + " entities to SECOND cache.";
    print(msg);
    for(idx=0; idx<numEntitiesToBeAdded; idx++)
        { cacheB.add(testEntities[idx]); }
    assertEquals(msg, recB.size(), numEntitiesToBeAdded);
    
    msg = "Removing " + numEntitiesToBeDeleted + " from second cache.";
     print(msg);
     for(idx=numEntitiesToBeUpdated; idx<numEntitiesToBeUpdated + numEntitiesToBeDeleted; idx++)
         { cacheB.remove( testEntityKeys[idx] ); }
   
    print("Will now sleep for " + (cacheSweepIntervalSecs + 5) + " seconds.");
    Thread.sleep( (cacheSweepIntervalSecs + 5) * 1000);

    // Check the caches.
    msg = "Checking second cache for invalidations";
    print(msg);
    assertEquals(msg, (numEntitiesToBeAdded - numEntitiesToBeUpdated - numEntitiesToBeDeleted), recB.size());
    
    msg = "Checking first cache for invalidations";
    print(msg);
    for(idx=numEntitiesToBeUpdated; idx<numEntitiesToBeUpdated + numEntitiesToBeDeleted; idx++)
        { assertNull(msg, cacheA.get( testEntityKeys[idx] )); }

    print("***** LEAVING EntityCacheTester.testFudgeFactor() *****");
}
}
