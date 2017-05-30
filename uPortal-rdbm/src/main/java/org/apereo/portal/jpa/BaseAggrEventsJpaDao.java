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
package org.apereo.portal.jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Base for DAOs that interact with the "AggrEventsDb" JPA Persistent Unit
 *
 */
public class BaseAggrEventsJpaDao extends BaseJpaDao {
    public static final String PERSISTENCE_UNIT_NAME = "AggrEventsDb";

    private EntityManager entityManager;
    private TransactionOperations transactionOperations;

    @PersistenceContext(unitName = PERSISTENCE_UNIT_NAME)
    public final void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    @Qualifier(PERSISTENCE_UNIT_NAME)
    public final void setTransactionOperations(TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }

    @Override
    protected final EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Override
    protected final TransactionOperations getTransactionOperations() {
        return this.transactionOperations;
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Transactional(PERSISTENCE_UNIT_NAME)
    public @interface AggrEventsTransactional {}

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Transactional(value = PERSISTENCE_UNIT_NAME, propagation = Propagation.REQUIRES_NEW)
    public @interface AggrEventsTransactionalRequiresNew {}
}
