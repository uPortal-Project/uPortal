package org.apereo.portal.groups;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import javax.naming.Name;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apereo.portal.EntityTypes;
import org.apereo.portal.jdbc.RDBMServices;
import org.apereo.portal.spring.locator.ApplicationContextLocator;
import org.apereo.portal.spring.locator.EntityTypesLocator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class RDBMSEntityGroupStoreTest {
    private static final String CACHE_NAME = "org.apereo.portal.groups.RDBMEntityGroupStore.search";
    private static final String PARENT_GROUP_BY_ENTITY_CACHE_NAME =
            "org.apereo.portal.groups.RDBMEntityGroupStore.parentGroupEntity";
    private static final String PARENT_GROUP_BY_ENTTITY_GROUP_CACHE_NAME =
            "org.apereo.portal.groups.RDBMEntityGroupStore.parentGroupEntityGroup";

    @Test
    public void testFindParentGroupsByIEntity() throws Exception {
        // This test confirms that calling findParentGroups(IEntity) with the same key
        // multiple times will check the cache each time but will only write
        // to the cache one time.
        //
        // This test reflects the complexity of the RDBMEntityGroupStore class
        // The RDBMEntityGroupStore class should really be two separate classes:
        //   a repository for reading from the database
        //   a service for processing the results.
        //
        // In addition, RDBMEntityGroupStore should be migrated to Spring annotations
        // and bean management, which would eliminate the need for CacheManager
        // and ApplicationContext
        String cacheKey = "cache-key";
        try (MockedStatic<ApplicationContextLocator> applicationContextLocator =
                        mockStatic(ApplicationContextLocator.class);
                MockedStatic<RDBMServices> rdbmServices = mockStatic(RDBMServices.class);
                MockedStatic<EntityTypesLocator> entityTypesLocator =
                        mockStatic(EntityTypesLocator.class)) {
            // define all mocks
            ApplicationContext context = mock(ApplicationContext.class);
            Cache groupSearchCache = mock(Cache.class);
            Cache parentGroupEntityCache = mock(Cache.class);
            Cache parentGroupEntityGroupCache = mock(Cache.class);
            CacheManager cacheManager = mock(CacheManager.class);
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            EntityTypes entityTypes = mock(EntityTypes.class);
            ResultSet rs = mock(ResultSet.class);
            IEntity entity = mock(IEntity.class);

            // define behavior for mocked classes
            // during initialization
            applicationContextLocator
                    .when(() -> ApplicationContextLocator.getApplicationContext())
                    .thenReturn(context);
            when(context.getBean("cacheManager", CacheManager.class)).thenReturn(cacheManager);
            when(cacheManager.getCache(CACHE_NAME)).thenReturn(groupSearchCache);
            when(cacheManager.getCache(PARENT_GROUP_BY_ENTITY_CACHE_NAME))
                    .thenReturn(parentGroupEntityCache);
            when(cacheManager.getCache(PARENT_GROUP_BY_ENTTITY_GROUP_CACHE_NAME))
                    .thenReturn(parentGroupEntityGroupCache);
            // during findParentGroups
            when(entity.getKey()).thenReturn(cacheKey);
            entityTypesLocator
                    .when(() -> EntityTypesLocator.getEntityTypes())
                    .thenReturn(entityTypes);
            when(entityTypes.getEntityTypeFromID(anyInt())).thenReturn(null);
            // during findParentGroupsForEntity
            rdbmServices.when(() -> RDBMServices.getConnection()).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            // set to null in order to avoid more mocks in
            // RDBMEntityGroupStore.instanceFromResultSet since we don't care about the results here
            when(rs.getString(1)).thenReturn(null);
            when(rs.getString(2)).thenReturn("ignore-me");
            // value is ignored  by entityTypes.getEntityTypeFromID() (above)
            when(rs.getInt(3)).thenReturn(1);
            when(rs.getString(4)).thenReturn("ignore-me");
            when(rs.getString(5)).thenReturn("ignore-me");

            Collection collection = new ArrayList();
            Element el = new Element(cacheKey, collection.iterator());
            when(parentGroupEntityCache.get(cacheKey)).thenReturn(null, el, el, el);

            // for the purposes of this test, we don't care what is actually returned
            // from the database; we're just confirming that the cache is being
            // accessed the correct number of times
            RDBMEntityGroupStore store = new RDBMEntityGroupStore();
            store.findParentGroups(entity);
            store.findParentGroups(entity);
            store.findParentGroups(entity);
            store.findParentGroups(entity);
            verify(parentGroupEntityCache, times(4)).get(cacheKey);
            verify(parentGroupEntityCache, times(1)).put(el);
        }
    }

    @Test
    public void testFindParentGroupsByIEntityGroup() throws Exception {
        // This test confirms that calling findParentGroups(IEntityGroup) with the same key
        // multiple times will check the cache each time but will only write
        // to the cache one time.
        //
        // This test reflects the complexity of the RDBMEntityGroupStore class
        // The RDBMEntityGroupStore class should really be two separate classes:
        //   a repository for reading from the database
        //   a service for processing the results.
        //
        // In addition, RDBMEntityGroupStore should be migrated to Spring annotations
        // and bean management, which would eliminate the need for CacheManager
        // and ApplicationContext
        String cacheKey = "cache-key";
        String serviceName = "service-name";
        try (MockedStatic<ApplicationContextLocator> applicationContextLocator =
                        mockStatic(ApplicationContextLocator.class);
                MockedStatic<RDBMServices> rdbmServices = mockStatic(RDBMServices.class);
                MockedStatic<EntityTypesLocator> entityTypesLocator =
                        mockStatic(EntityTypesLocator.class)) {
            // define all mocks
            ApplicationContext context = mock(ApplicationContext.class);
            Cache groupSearchCache = mock(Cache.class);
            Cache parentGroupEntityCache = mock(Cache.class);
            Cache parentGroupEntityGroupCache = mock(Cache.class);
            CacheManager cacheManager = mock(CacheManager.class);
            Name name = mock(Name.class);
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            EntityTypes entityTypes = mock(EntityTypes.class);
            ResultSet rs = mock(ResultSet.class);
            IEntityGroup entityGroup = mock(IEntityGroup.class);

            // define behavior for mocked classes
            // during initialization
            applicationContextLocator
                    .when(() -> ApplicationContextLocator.getApplicationContext())
                    .thenReturn(context);
            when(context.getBean("cacheManager", CacheManager.class)).thenReturn(cacheManager);
            when(cacheManager.getCache(CACHE_NAME)).thenReturn(groupSearchCache);
            when(cacheManager.getCache(PARENT_GROUP_BY_ENTITY_CACHE_NAME))
                    .thenReturn(parentGroupEntityCache);
            when(cacheManager.getCache(PARENT_GROUP_BY_ENTTITY_GROUP_CACHE_NAME))
                    .thenReturn(parentGroupEntityGroupCache);
            // during findParentGroups
            when(entityGroup.getLocalKey()).thenReturn(cacheKey);
            when(name.toString()).thenReturn(serviceName);
            when(entityGroup.getServiceName()).thenReturn(name);
            entityTypesLocator
                    .when(() -> EntityTypesLocator.getEntityTypes())
                    .thenReturn(entityTypes);
            when(entityTypes.getEntityTypeFromID(anyInt())).thenReturn(null);
            // during findParentGroupsForEntity
            rdbmServices.when(() -> RDBMServices.getConnection()).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            // set to null in order to avoid more mocks in
            // RDBMEntityGroupStore.instanceFromResultSet since we don't care about the results here
            when(rs.getString(1)).thenReturn(null);
            when(rs.getString(2)).thenReturn("ignore-me");
            // value is ignored  by entityTypes.getEntityTypeFromID() (above)
            when(rs.getInt(3)).thenReturn(1);
            when(rs.getString(4)).thenReturn("ignore-me");
            when(rs.getString(5)).thenReturn("ignore-me");

            Collection collection = new ArrayList();
            Element el = new Element(cacheKey, collection.iterator());
            when(parentGroupEntityGroupCache.get(cacheKey)).thenReturn(null, el, el, el);

            // for the purposes of this test, we don't care what is actually returned
            // from the database; we're just confirming that the cache is being
            // accessed the correct number of times
            RDBMEntityGroupStore store = new RDBMEntityGroupStore();
            store.findParentGroups(entityGroup);
            store.findParentGroups(entityGroup);
            store.findParentGroups(entityGroup);
            store.findParentGroups(entityGroup);
            verify(parentGroupEntityGroupCache, times(4)).get(cacheKey);
            verify(parentGroupEntityGroupCache, times(1)).put(el);
        }
    }
}
