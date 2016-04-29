/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.spring.locator.EntityTypesLocator;
import org.jasig.portal.utils.ICounterStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.TriggersRemove;
import com.googlecode.ehcache.annotations.key.ListCacheKeyGenerator;

/**
 * This class provides access to the entity types used by <code>IBasicEntities</code>
 * and the classes in <code>org.jasig.portal.groups</code> and
 * <code>org.jasig.portal.concurrency</code>.
 * <p>
 * Each type is associated with an <code>Integer</code> used to represent the
 * type in the portal data store.  This class translates between the
 * <code>Integer</code> and <code>Class</code> values.
 *
 * @author Dan Ellentuck
 * @see org.jasig.portal.IBasicEntity
 */
@Repository("entityTypes")
public class EntityTypes {

    private static final RowMapper<Class<?>> CLASS_ROW_MAPPER = new RowMapper<Class<?>>() {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public Class<?> mapRow(ResultSet rs, int rowNum) throws SQLException {
            final String className = rs.getString("ENTITY_TYPE_NAME");
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                logger.error("Failed to find the specified EntityType class '{}';  "
                        + "this is either a catastrophic problem (is the portal working "
                        + "at all?) or an artifact of orphaned data.", className);
                // These will be removed from the allEntityTypes collection
                return null;
            }
        }
    };

    public static final Class<IEntityGroup> GROUP_ENTITY_TYPE = org.jasig.portal.groups.IEntityGroup.class;
    public static final Class<IEntity> LEAF_ENTITY_TYPE = org.jasig.portal.groups.IEntity.class;


    @Deprecated
    public static Class<? extends IBasicEntity> getEntityType(Integer typeID) {
        return singleton().getEntityTypeFromID(typeID);
    }

    @Deprecated
    public static Integer getEntityTypeID(Class<? extends IBasicEntity> type) {
        return singleton().getEntityIDFromType(type);
    }

    @Deprecated
    public static EntityTypes singleton() {
        return EntityTypesLocator.getEntityTypes();
    }

    private JdbcOperations jdbcOperations;
    private ICounterStore counterStore;

    @Autowired
    public void setJdbcOperations(@Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME) JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Autowired
    public void setCounterStore(ICounterStore counterStore) {
        this.counterStore = counterStore;
    }

    @Cacheable(cacheName = "org.jasig.portal.EntityTypes.CLASS_BY_ID", keyGeneratorName = ListCacheKeyGenerator.DEFAULT_BEAN_NAME)
    public Class<? extends IBasicEntity> getEntityTypeFromID(Integer id) {
        final List<Class<?>> result = this.jdbcOperations.query("SELECT ENTITY_TYPE_NAME FROM UP_ENTITY_TYPE WHERE ENTITY_TYPE_ID = ?", CLASS_ROW_MAPPER, id);
        @SuppressWarnings("unchecked") // There is an unused(?) row for java.lang.Object that looks as though it will fail here
        Class<? extends IBasicEntity> rslt = (Class<? extends IBasicEntity>) DataAccessUtils.singleResult(result);
        return rslt;
    }

    @Cacheable(cacheName = "org.jasig.portal.EntityTypes.ID_BY_CLASS", keyGeneratorName = ListCacheKeyGenerator.DEFAULT_BEAN_NAME)
    public Integer getEntityIDFromType(Class<? extends IBasicEntity> type) {
        return DataAccessUtils.singleResult(
                this.jdbcOperations.queryForList("SELECT ENTITY_TYPE_ID FROM UP_ENTITY_TYPE WHERE ENTITY_TYPE_NAME = ?", Integer.class, type.getName()));
    }

    @Cacheable(cacheName = "org.jasig.portal.EntityTypes.ALL", keyGeneratorName = ListCacheKeyGenerator.DEFAULT_BEAN_NAME)
    public Iterator<Class<?>> getAllEntityTypes() {
        final List<Class<?>> entityTypes =  this.jdbcOperations.query("SELECT ENTITY_TYPE_NAME FROM UP_ENTITY_TYPE", CLASS_ROW_MAPPER);
        // Filter null values
        final Set<Class<?>> rslt = new HashSet<>();
        for (Class<?> clazz : entityTypes) {
            if (clazz != null) {
                rslt.add(clazz);
            }
        }
        return rslt.iterator();
    }

    @TriggersRemove(cacheName = { "org.jasig.portal.EntityTypes.ALL" }, removeAll=true)
    @Transactional
    public void addEntityTypeIfNecessary(Class<? extends IBasicEntity> newType, String description) throws java.lang.Exception {
        final Integer existingId = this.getEntityIDFromType(newType);
        if (existingId != null) {
            //Entity type already exists, ignore call
            return;
        }

        final int nextId = counterStore.getNextId("UP_ENTITY_TYPE");
        this.jdbcOperations.update("INSERT INTO UP_ENTITY_TYPE (ENTITY_TYPE_ID, ENTITY_TYPE_NAME, DESCRIPTIVE_NAME) VALUES (?, ?, ?)", 
                nextId, newType.getName(), description);
    }

    @TriggersRemove(cacheName = { "org.jasig.portal.EntityTypes.CLASS_BY_ID", "org.jasig.portal.EntityTypes.ID_BY_CLASS", "org.jasig.portal.EntityTypes.ALL"}, removeAll=true)
    @Transactional
    public void deleteEntityType(Class<?> type) throws SQLException {
        this.jdbcOperations.update("DELETE FROM UP_ENTITY_TYPE WHERE ENTITY_TYPE_NAME = ?", type.getName());
    }
}
