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

package org.jasig.portal.events.aggr.dao.jpa;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.events.aggr.EventAggregationConfiguration;
import org.jasig.portal.events.aggr.QuarterDetails;
import org.jasig.portal.events.aggr.dao.EventAggregationConfigurationDao;
import org.jasig.portal.jpa.BaseJpaDao;
import org.joda.time.MonthDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.ImmutableSet;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaEventAggregationConfigurationDao extends BaseJpaDao implements EventAggregationConfigurationDao {
    private TransactionOperations transactionOperations;
    private EntityManager entityManager;
    
    @PersistenceContext(unitName = "uPortalAggrEventsPersistence")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Autowired
    public void setTransactionOperations(@Qualifier("aggrEvents") TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }

    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Override
    public EventAggregationConfiguration getAggregationConfiguration() {
        EventAggregationConfigurationImpl eventAggregationConfiguration = this.entityManager.find(EventAggregationConfigurationImpl.class, EventAggregationConfigurationImpl.INSTANCE_ID);
        if (eventAggregationConfiguration == null) {
            eventAggregationConfiguration = this.transactionOperations.execute(new TransactionCallback<EventAggregationConfigurationImpl>() {
                @Override
                public EventAggregationConfigurationImpl doInTransaction(TransactionStatus status) {
                    final Set<QuarterDetails> standardQuarters = ImmutableSet.<QuarterDetails>of(
                            new QuarterDetailsImpl(new MonthDay(1, 1), new MonthDay(4, 1), 0),
                            new QuarterDetailsImpl(new MonthDay(7, 1), new MonthDay(10, 1), 2),
                            new QuarterDetailsImpl(new MonthDay(4, 1), new MonthDay(7, 1), 1),
                            new QuarterDetailsImpl(new MonthDay(10, 1), new MonthDay(1, 1), 3));
                    
                    for (final QuarterDetails details : standardQuarters) {
                        entityManager.persist(details);
                    }
                    
                    final EventAggregationConfigurationImpl config = new EventAggregationConfigurationImpl(standardQuarters);
                    entityManager.persist(config);
                    return config;
                }
            });
        }
        
        return eventAggregationConfiguration;
    }

    @Transactional("aggrEvents")
    @Override
    public void updateEventAggregationConfiguration(EventAggregationConfiguration eventAggregationConfiguration) {
        this.entityManager.persist(eventAggregationConfiguration);
    }
}
