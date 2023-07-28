package org.apereo.portal.groups;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.naming.Name;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apereo.portal.EntityTypes;
import org.apereo.portal.jdbc.RDBMServices;
import org.apereo.portal.spring.locator.ApplicationContextLocator;
import org.apereo.portal.spring.locator.EntityTypesLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class RDBMSEntityGroupStoreTest {
    private static final String SEARCH_CACHE_NAME =
            "org.apereo.portal.groups.RDBMEntityGroupStore.search";
    private static final String PARENT_GROUP_BY_ENTITY_CACHE_NAME =
            "org.apereo.portal.groups.RDBMEntityGroupStore.parentGroupEntity";
    private static final String PARENT_GROUP_BY_ENTITY_GROUP_CACHE_NAME =
            "org.apereo.portal.groups.RDBMEntityGroupStore.parentGroupEntityGroup";

    @Mock ApplicationContext context;
    @Mock Connection conn;
    @Mock PreparedStatement ps;
    @Mock EntityTypes entityTypes;
    @Mock ResultSet rs;
    @Mock IEntity entity;
    @Mock IEntityGroup entityGroup;
    @Mock Name name;

    Cache groupSearchCache;
    Cache parentGroupEntityCache;
    Cache parentGroupEntityGroupCache;
    CacheManager cacheManager;
    RDBMEntityGroupStore store;

    private static final String entityKey = "entity-key";
    private static final String entityLocalKey = "entity-local-key";
    private static final String serviceName = "service-name";

    @Before
    public void setUp() throws Exception {
        setUpCaching();
        setUpRdbms();
        when(entity.getKey()).thenReturn(entityKey);
        when(entityGroup.getLocalKey()).thenReturn(entityLocalKey);
        when(entityGroup.getServiceName()).thenReturn(name);
        when(name.toString()).thenReturn(serviceName);
        when(entityTypes.getEntityTypeFromID(anyInt())).thenReturn(null);
    }

    @After
    public void tearDown() throws Exception {
        cacheManager.shutdown();
        cacheManager = null;
    }

    // These tests reflect the complexity of the RDBMEntityGroupStore class
    // The RDBMEntityGroupStore class should really be two separate classes:
    //   a repository for reading from the database
    //   a service for processing the results.
    //
    // In addition, RDBMEntityGroupStore should be migrated to Spring annotations
    // and bean management, which would eliminate the need for CacheManager
    // and ApplicationContext

    @Test
    public void testFindParentGroupsByIEntityForCacheMiss() throws Exception {
        String cacheKey = entityKey + ":0";
        try (MockStaticUtil util = new MockStaticUtil()) {
            store = new RDBMEntityGroupStore();
            store.findParentGroups(entity);
            verify(parentGroupEntityCache, times(1)).get(cacheKey);
            verify(parentGroupEntityCache, times(1)).put(new Element(cacheKey, null));
            verify(ps).executeQuery();
        }
    }

    @Test
    public void testFindParentGroupsByIEntityForCacheHit() throws Exception {
        String cacheKey = entityKey + ":0";
        List<IEntityGroup> expectedGroups = Collections.singletonList(entityGroup);
        try (MockStaticUtil util = new MockStaticUtil()) {
            parentGroupEntityCache.put(new Element(cacheKey, expectedGroups));
            verify(parentGroupEntityCache, times(1)).put(any());
            store = new RDBMEntityGroupStore();
            Iterator<IEntityGroup> iterator = store.findParentGroups(entity);
            verify(parentGroupEntityCache, times(1)).get(cacheKey);
            // check this again to make sure it wasn't called again
            verify(parentGroupEntityCache, times(1)).put(any());
            verify(ps, never()).executeQuery();
            verifyResults(iterator, expectedGroups);
        }
    }

    @Test
    public void testFindParentGroupsByIEntityCalledMultipleTimes() throws Exception {
        // This test confirms that calling findParentGroups(IEntity) with the same key  multiple
        // times will check the
        // cache each time but will only write to the cache one time.
        String cacheKey = entityKey + ":0";
        try (MockStaticUtil util = new MockStaticUtil()) {
            // for the purposes of this test, we don't care what is actually returned
            // from the database; we're just confirming that the cache is being
            // accessed the correct number of times
            store = new RDBMEntityGroupStore();
            store.findParentGroups(entity);
            store.findParentGroups(entity);
            store.findParentGroups(entity);
            store.findParentGroups(entity);
            verify(parentGroupEntityCache, times(4)).get(cacheKey);
            verify(parentGroupEntityCache, times(1)).put(new Element(cacheKey, null));
        }
    }

    @Test
    public void
            testFindParentGroupsByIEntityForCacheHitReturnsIteratorThatDoesNotSupportRemoveMethod()
                    throws Exception {
        String cacheKey = entityKey + ":0";
        try (MockStaticUtil util = new MockStaticUtil()) {
            store = new RDBMEntityGroupStore();
            parentGroupEntityCache.put(
                    new Element(cacheKey, Collections.singletonList(entityGroup)));
            Iterator<IEntityGroup> iterator = store.findParentGroups(entity);
            assertNotNull(iterator);
            assertNotNull(iterator.next());
            assertIteratorDoesNotSupportRemove(iterator);
        }
    }

    @Test
    public void
            testFindParentGroupsByIEntityForCacheMissReturnsIteratorThatDoesNotSupportRemoveMethod()
                    throws Exception {
        try (MockStaticUtil util = new MockStaticUtil()) {
            store = new RDBMEntityGroupStore();
            Iterator<IEntityGroup> iterator = store.findParentGroups(entity);
            assertNotNull(iterator);
            assertNotNull(iterator.next());
            assertIteratorDoesNotSupportRemove(iterator);
        }
    }

    @Test
    public void testFindParentGroupsByIEntityGroupForCacheMiss() throws Exception {
        String cacheKey = entityLocalKey + ":0:" + serviceName;
        try (MockStaticUtil util = new MockStaticUtil()) {
            store = new RDBMEntityGroupStore();
            store.findParentGroups(entityGroup);
            verify(parentGroupEntityGroupCache, times(1)).get(cacheKey);
            verify(parentGroupEntityGroupCache, times(1)).put(new Element(cacheKey, null));
            verify(ps).executeQuery();
        }
    }

    @Test
    public void testFindParentGroupsByIEntityGroupForCacheHit() throws Exception {
        String cacheKey = entityLocalKey + ":0:" + serviceName;
        List<IEntityGroup> expectedGroups = Collections.singletonList(entityGroup);
        try (MockStaticUtil util = new MockStaticUtil()) {
            parentGroupEntityGroupCache.put(new Element(cacheKey, expectedGroups));
            verify(parentGroupEntityGroupCache, times(1)).put(new Element(cacheKey, null));
            store = new RDBMEntityGroupStore();
            Iterator<IEntityGroup> iterator = store.findParentGroups(entityGroup);
            verify(parentGroupEntityGroupCache, times(1)).get(cacheKey);
            // check this again to make sure it wasn't called again
            verify(parentGroupEntityGroupCache, times(1)).put(new Element(cacheKey, null));
            verify(ps, never()).executeQuery();
            verifyResults(iterator, expectedGroups);
        }
    }

    @Test
    public void testFindParentGroupsByIEntityGroupCalledMultipleTimes() throws Exception {
        // This test confirms that calling findParentGroups(IEntityGroup) with the same key
        // multiple times will check the cache each time but will only write
        // to the cache one time.
        String cacheKey = entityLocalKey + ":0:" + serviceName;
        try (MockStaticUtil util = new MockStaticUtil()) {
            // for the purposes of this test, we don't care what is actually returned
            // from the database; we're just confirming that the cache is being
            // accessed the correct number of times
            store = new RDBMEntityGroupStore();
            store.findParentGroups(entityGroup);
            store.findParentGroups(entityGroup);
            store.findParentGroups(entityGroup);
            store.findParentGroups(entityGroup);
            verify(parentGroupEntityGroupCache, times(4)).get(cacheKey);
            verify(parentGroupEntityGroupCache, times(1)).put(new Element(cacheKey, null));
        }
    }

    private void verifyResults(Iterator<IEntityGroup> iterator, List<IEntityGroup> expectedGroups) {
        assertNotNull(iterator);
        for (IEntityGroup expectedGroup : expectedGroups) {
            assertTrue(iterator.hasNext());
            IEntityGroup group = iterator.next();
            assertNotNull(group);
            assertEquals(expectedGroup, group);
        }
        assertFalse(iterator.hasNext());
    }

    private void assertIteratorDoesNotSupportRemove(Iterator<IEntityGroup> iterator) {
        try {
            iterator.remove();
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    private void setUpCaching() {
        groupSearchCache = spy(createNewCache(SEARCH_CACHE_NAME));
        parentGroupEntityCache = spy(createNewCache(PARENT_GROUP_BY_ENTITY_CACHE_NAME));
        parentGroupEntityGroupCache = spy(createNewCache(PARENT_GROUP_BY_ENTITY_GROUP_CACHE_NAME));
        cacheManager = new CacheManager();
        cacheManager.addCache(groupSearchCache);
        cacheManager.addCache(parentGroupEntityCache);
        cacheManager.addCache(parentGroupEntityGroupCache);
        when(context.getBean("cacheManager", CacheManager.class)).thenReturn(cacheManager);
    }

    private Cache createNewCache(String cacheName) {
        return new Cache(cacheName, 100, false, false, 300, 300);
    }

    private void setUpRdbms() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        // set to null in order to avoid more mocks in
        // RDBMEntityGroupStore.instanceFromResultSet since we don't care about the results here
        when(rs.getString(1)).thenReturn("key1");
        when(rs.getString(2)).thenReturn("ignore-me");
        // value is ignored  by entityTypes.getEntityTypeFromID() (above)
        when(rs.getInt(3)).thenReturn(1);
        when(rs.getString(4)).thenReturn("ignore-me");
        when(rs.getString(5)).thenReturn("ignore-me");
    }

    class MockStaticUtil implements AutoCloseable {
        MockedStatic<ApplicationContextLocator> applicationContextLocator =
                mockStatic(ApplicationContextLocator.class);
        MockedStatic<RDBMServices> rdbmServices = mockStatic(RDBMServices.class);
        MockedStatic<EntityTypesLocator> entityTypesLocator = mockStatic(EntityTypesLocator.class);

        public MockStaticUtil() {
            applicationContextLocator
                    .when(ApplicationContextLocator::getApplicationContext)
                    .thenReturn(context);
            entityTypesLocator.when(EntityTypesLocator::getEntityTypes).thenReturn(entityTypes);
            rdbmServices.when(RDBMServices::getConnection).thenReturn(conn);
        }

        @Override
        public void close() {
            applicationContextLocator.close();
            rdbmServices.close();
            entityTypesLocator.close();
        }
    }
}
