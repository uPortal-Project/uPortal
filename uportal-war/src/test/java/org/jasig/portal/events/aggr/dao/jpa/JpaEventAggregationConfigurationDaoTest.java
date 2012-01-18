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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.naming.CompositeName;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.events.aggr.EventAggregationConfiguration;
import org.jasig.portal.events.aggr.Interval;
import org.jasig.portal.events.aggr.dao.EventAggregationConfigurationDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.login.LoginPortalEventAggregator;
import org.jasig.portal.groups.ICompositeGroupService;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.test.BaseJpaDaoTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaAggrEventsTestContext.xml")
public class JpaEventAggregationConfigurationDaoTest extends BaseJpaDaoTest {
	
    @Autowired
	private EventAggregationConfigurationDao eventAggregationConfigurationDao;
    @Autowired
    private AggregatedGroupLookupDao aggregatedGroupLookupDao;
    @Autowired
    private ICompositeGroupService compositeGroupService;
    
    
    @PersistenceContext(unitName = "uPortalAggrEventsPersistence")
    private EntityManager entityManager;
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

	@Test
	public void testEventAggregationConfigurationDao() throws Exception {
        final IEntityGroup everyoneGroup = mock(IEntityGroup.class);
        when(everyoneGroup.getServiceName()).thenReturn(new CompositeName("local"));
        when(everyoneGroup.getName()).thenReturn("Everyone");
        when(compositeGroupService.findGroup("local.0")).thenReturn(everyoneGroup);
        
	    this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final EventAggregationConfiguration config = eventAggregationConfigurationDao.getAggregationConfiguration();
                assertNotNull(config);
                
                assertEquals(4, config.getQuartersDetails().size());
                
                config.getExcludedIntervals().add(Interval.MINUTE);
                
                final AggregatedGroupMapping group = aggregatedGroupLookupDao.getGroupMapping("local.0");
                config.getIncludedGroupsForAggregator(LoginPortalEventAggregator.class).add(group);
                
                eventAggregationConfigurationDao.updateEventAggregationConfiguration(config);
            }
        });
	    
	    this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final EventAggregationConfiguration config = eventAggregationConfigurationDao.getAggregationConfiguration();
                assertNotNull(config);
                
                assertEquals(4, config.getQuartersDetails().size());
                
                assertEquals(1, config.getExcludedIntervals().size());
                
                assertEquals(1, config.getIncludedGroupsForAggregator(LoginPortalEventAggregator.class).size());
            }
        });
	}
}
