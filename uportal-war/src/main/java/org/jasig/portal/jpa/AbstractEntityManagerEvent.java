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
package org.jasig.portal.jpa;

import javax.persistence.EntityManager;

import org.springframework.context.ApplicationEvent;

/**
 * Base {@link EntityManager} related event.
 * 
 * @author Eric Dalquist
 */
public abstract class AbstractEntityManagerEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    
    private final String entityManagerId;
    private final String persistenceUnitName;
    private final transient EntityManager entityManager;
    
    AbstractEntityManagerEvent(Object source, long entityManagerId, String persistenceUnitName, EntityManager entityManager) {
        super(source);
        this.entityManagerId = persistenceUnitName + "." + entityManagerId;
        this.persistenceUnitName = persistenceUnitName;
        this.entityManager = entityManager;
    }
    
    /**
     * Unique id (within this JVM) for the entity manager
     */
    public String getEntityManagerId() {
        return entityManagerId;
    }
    
    /**
     * Name of the JPA Persistent Unit
     */
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    /**
     * The EntityManager, transient so may be null if event has been serialized
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
