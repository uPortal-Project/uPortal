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
package org.apereo.portal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class provides access to the entity types used by <code>IBasicEntities</code> and the
 * classes in <code>org.apereo.portal.groups</code> and <code>org.apereo.portal.concurrency</code>.
 *
 * <p>Each type is associated with an <code>Integer</code> used to represent the type in the portal
 * data store. This class translates between the <code>Integer</code> and <code>Class</code> values.
 *
 * @see org.apereo.portal.IBasicEntity
 */
@Repository("entityTypes")
public class EntityTypes {

    private static final RowMapper<Class<?>> CLASS_ROW_MAPPER =
            new RowMapper<Class<?>>() {

                private final Logger logger = LoggerFactory.getLogger(getClass());

                @Override
                public Class<?> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    final String className = rs.getString("ENTITY_TYPE_NAME");
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        logger.error(
                                "Failed to find the specified EntityType class '{}';  "
                                        + "this is either a catastrophic problem (is the portal working "
                                        + "at all?) or an artifact of orphaned data.",
                                className);
                        // These will be removed from the allEntityTypes collection
                        return null;
                    }
                }
            };

    private JdbcOperations jdbcOperations;
    private ICounterStore counterStore;

    @Autowired
    public void setJdbcOperations(@Qualifier("PortalDb") JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Autowired
    public void setCounterStore(ICounterStore counterStore) {
        this.counterStore = counterStore;
    }

    @Cacheable("org.apereo.portal.EntityTypes.CLASS_BY_ID")
    public Class<? extends IBasicEntity> getEntityTypeFromID(Integer id) {
        final List<Class<?>> result =
                this.jdbcOperations.query(
                        "SELECT ENTITY_TYPE_NAME FROM UP_ENTITY_TYPE WHERE ENTITY_TYPE_ID = ?",
                        CLASS_ROW_MAPPER,
                        id);
        @SuppressWarnings(
                "unchecked") // There is an unused(?) row for java.lang.Object that looks as though
        // it will fail here
        Class<? extends IBasicEntity> rslt =
                (Class<? extends IBasicEntity>) DataAccessUtils.singleResult(result);
        return rslt;
    }

    @Cacheable(cacheNames = "org.apereo.portal.EntityTypes.ID_BY_CLASS", key = "#type.Name")
    public Integer getEntityIDFromType(Class<? extends IBasicEntity> type) {
        return DataAccessUtils.singleResult(
                this.jdbcOperations.queryForList(
                        "SELECT ENTITY_TYPE_ID FROM UP_ENTITY_TYPE WHERE ENTITY_TYPE_NAME = ?",
                        Integer.class,
                        type.getName()));
    }

    @Cacheable("org.apereo.portal.EntityTypes.ALL")
    public Iterator<Class<?>> getAllEntityTypes() {
        final List<Class<?>> entityTypes =
                this.jdbcOperations.query(
                        "SELECT ENTITY_TYPE_NAME FROM UP_ENTITY_TYPE", CLASS_ROW_MAPPER);
        // Filter null values
        final Set<Class<?>> rslt = new HashSet<>();
        for (Class<?> clazz : entityTypes) {
            if (clazz != null) {
                rslt.add(clazz);
            }
        }
        return rslt.iterator();
    }

    @CacheEvict(value = "org.apereo.portal.EntityTypes.ALL", allEntries = true)
    @Transactional
    public void addEntityTypeIfNecessary(Class<? extends IBasicEntity> newType, String description)
            throws java.lang.Exception {
        final Integer existingId = this.getEntityIDFromType(newType);
        if (existingId != null) {
            // Entity type already exists, ignore call
            return;
        }

        final int nextId = counterStore.getNextId("UP_ENTITY_TYPE");
        this.jdbcOperations.update(
                "INSERT INTO UP_ENTITY_TYPE (ENTITY_TYPE_ID, ENTITY_TYPE_NAME, DESCRIPTIVE_NAME) VALUES (?, ?, ?)",
                nextId,
                newType.getName(),
                description);
    }

    @CacheEvict(
            cacheNames = {
                "org.apereo.portal.EntityTypes.CLASS_BY_ID",
                "org.apereo.portal.EntityTypes.ID_BY_CLASS",
                "org.apereo.portal.EntityTypes.ALL"
            },
            allEntries = true)
    @Transactional
    public void deleteEntityType(Class<?> type) throws SQLException {
        this.jdbcOperations.update(
                "DELETE FROM UP_ENTITY_TYPE WHERE ENTITY_TYPE_NAME = ?", type.getName());
    }
}
