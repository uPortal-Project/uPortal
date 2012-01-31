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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import javax.naming.CompositeName;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.events.aggr.AcademicTermDetail;
import org.jasig.portal.events.aggr.AggregatedGroupConfig;
import org.jasig.portal.events.aggr.AggregatedIntervalConfig;
import org.jasig.portal.events.aggr.EventDateTimeUtils;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.QuarterDetail;
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.login.LoginPortalEventAggregator;
import org.jasig.portal.groups.ICompositeGroupService;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.test.BaseJpaDaoTest;
import org.joda.time.DateMidnight;
import org.joda.time.MonthDay;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaAggrEventsTestContext.xml")
public class JpaEventAggregationManagementDaoTest extends BaseJpaDaoTest {
	
    @Autowired
	private IEventAggregationManagementDao eventAggregationManagementDao;
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
	public void testAggregatedGroupConfig() throws Exception {
        final IEntityGroup everyoneGroup = mock(IEntityGroup.class);
        when(everyoneGroup.getServiceName()).thenReturn(new CompositeName("local"));
        when(everyoneGroup.getName()).thenReturn("Everyone");
        when(compositeGroupService.findGroup("local.0")).thenReturn(everyoneGroup);
        
	    this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupConfig defaultAggregatedGroupConfig = eventAggregationManagementDao.getDefaultAggregatedGroupConfig();
                assertNotNull(defaultAggregatedGroupConfig);
                assertEquals(0, defaultAggregatedGroupConfig.getExcluded().size());
                assertEquals(0, defaultAggregatedGroupConfig.getIncluded().size());
                
                AggregatedGroupConfig loginAggregatedGroupConfig = eventAggregationManagementDao.getAggregatedGroupConfig(LoginPortalEventAggregator.class);
                assertNull(loginAggregatedGroupConfig);
                loginAggregatedGroupConfig = eventAggregationManagementDao.createAggregatedGroupConfig(LoginPortalEventAggregator.class);
                assertNotNull(loginAggregatedGroupConfig);
                assertEquals(0, loginAggregatedGroupConfig.getExcluded().size());
                assertEquals(0, loginAggregatedGroupConfig.getIncluded().size());
                
                final AggregatedGroupMapping group = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                defaultAggregatedGroupConfig.getIncluded().add(group);
                loginAggregatedGroupConfig.getExcluded().add(group);

                eventAggregationManagementDao.updateAggregatedGroupConfig(defaultAggregatedGroupConfig);
                eventAggregationManagementDao.updateAggregatedGroupConfig(loginAggregatedGroupConfig);
            }
        });
	    
	    this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupConfig defaultAggregatedGroupConfig = eventAggregationManagementDao.getDefaultAggregatedGroupConfig();
                assertNotNull(defaultAggregatedGroupConfig);
                assertEquals(0, defaultAggregatedGroupConfig.getExcluded().size());
                assertEquals(1, defaultAggregatedGroupConfig.getIncluded().size());
                
                AggregatedGroupConfig loginAggregatedGroupConfig = eventAggregationManagementDao.getAggregatedGroupConfig(LoginPortalEventAggregator.class);
                assertNotNull(loginAggregatedGroupConfig);
                assertEquals(1, loginAggregatedGroupConfig.getExcluded().size());
                assertEquals(0, loginAggregatedGroupConfig.getIncluded().size());
                
                eventAggregationManagementDao.deleteAggregatedGroupConfig(defaultAggregatedGroupConfig);
                eventAggregationManagementDao.deleteAggregatedGroupConfig(loginAggregatedGroupConfig);
            }
        });
	    
	    this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupConfig defaultAggregatedGroupConfig = eventAggregationManagementDao.getDefaultAggregatedGroupConfig();
                assertNotNull(defaultAggregatedGroupConfig);
                assertEquals(0, defaultAggregatedGroupConfig.getExcluded().size());
                assertEquals(0, defaultAggregatedGroupConfig.getIncluded().size());
                
                AggregatedGroupConfig loginAggregatedGroupConfig = eventAggregationManagementDao.getAggregatedGroupConfig(LoginPortalEventAggregator.class);
                assertNull(loginAggregatedGroupConfig);
            }
        });
	}

    @Test
    public void testAggregatedIntervalConfig() throws Exception {
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedIntervalConfig defaultAggregatedIntervalConfig = eventAggregationManagementDao.getDefaultAggregatedIntervalConfig();
                assertNotNull(defaultAggregatedIntervalConfig);
                assertEquals(0, defaultAggregatedIntervalConfig.getExcluded().size());
                assertEquals(0, defaultAggregatedIntervalConfig.getIncluded().size());
                
                AggregatedIntervalConfig loginAggregatedIntervalConfig = eventAggregationManagementDao.getAggregatedIntervalConfig(LoginPortalEventAggregator.class);
                assertNull(loginAggregatedIntervalConfig);
                loginAggregatedIntervalConfig = eventAggregationManagementDao.createAggregatedIntervalConfig(LoginPortalEventAggregator.class);
                assertNotNull(loginAggregatedIntervalConfig);
                assertEquals(0, loginAggregatedIntervalConfig.getExcluded().size());
                assertEquals(0, loginAggregatedIntervalConfig.getIncluded().size());
                
                defaultAggregatedIntervalConfig.getIncluded().add(AggregationInterval.MINUTE);
                loginAggregatedIntervalConfig.getExcluded().add(AggregationInterval.MINUTE);

                eventAggregationManagementDao.updateAggregatedIntervalConfig(defaultAggregatedIntervalConfig);
                eventAggregationManagementDao.updateAggregatedIntervalConfig(loginAggregatedIntervalConfig);
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedIntervalConfig defaultAggregatedIntervalConfig = eventAggregationManagementDao.getDefaultAggregatedIntervalConfig();
                assertNotNull(defaultAggregatedIntervalConfig);
                assertEquals(0, defaultAggregatedIntervalConfig.getExcluded().size());
                assertEquals(1, defaultAggregatedIntervalConfig.getIncluded().size());
                
                AggregatedIntervalConfig loginAggregatedIntervalConfig = eventAggregationManagementDao.getAggregatedIntervalConfig(LoginPortalEventAggregator.class);
                assertNotNull(loginAggregatedIntervalConfig);
                assertEquals(1, loginAggregatedIntervalConfig.getExcluded().size());
                assertEquals(0, loginAggregatedIntervalConfig.getIncluded().size());
                
                eventAggregationManagementDao.deleteAggregatedIntervalConfig(defaultAggregatedIntervalConfig);
                eventAggregationManagementDao.deleteAggregatedIntervalConfig(loginAggregatedIntervalConfig);
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedIntervalConfig defaultAggregatedIntervalConfig = eventAggregationManagementDao.getDefaultAggregatedIntervalConfig();
                assertNotNull(defaultAggregatedIntervalConfig);
                assertEquals(0, defaultAggregatedIntervalConfig.getExcluded().size());
                assertEquals(0, defaultAggregatedIntervalConfig.getIncluded().size());
                
                AggregatedIntervalConfig loginAggregatedIntervalConfig = eventAggregationManagementDao.getAggregatedIntervalConfig(LoginPortalEventAggregator.class);
                assertNull(loginAggregatedIntervalConfig);
            }
        });
    }
    
    @Test
    public void testQuarterDetails() throws Exception {
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final List<QuarterDetail> quartersDetails = eventAggregationManagementDao.getQuartersDetails();
                EventDateTimeUtils.validateQuarters(quartersDetails);
                
                eventAggregationManagementDao.setQuarterDetails(Arrays.<QuarterDetail>asList(
                        new QuarterDetailImpl(new MonthDay(5, 1), new MonthDay(8, 1), 0),
                        new QuarterDetailImpl(new MonthDay(8, 1), new MonthDay(11, 1), 1),
                        new QuarterDetailImpl(new MonthDay(11, 1), new MonthDay(2, 1), 2),
                        new QuarterDetailImpl(new MonthDay(2, 1), new MonthDay(5, 1), 3)
                    ));
            }
        });
    }
    
    @Test
    public void testAcademicTermDetails() throws Exception {
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final List<AcademicTermDetail> academicTermDetail = eventAggregationManagementDao.getAcademicTermDetails();
                assertEquals(0, academicTermDetail.size());
                
                eventAggregationManagementDao.addAcademicTermDetails(new DateMidnight(2012, 1, 1), new DateMidnight(2012, 6, 1), "Spring 2012");
                eventAggregationManagementDao.addAcademicTermDetails(new DateMidnight(2012, 6, 1), new DateMidnight(2012, 9, 1), "Summer 2012");
                eventAggregationManagementDao.addAcademicTermDetails(new DateMidnight(2012, 9, 1), new DateMidnight(2013, 1, 1), "Fall 2012");
            }
        });
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final List<AcademicTermDetail> academicTermDetail = eventAggregationManagementDao.getAcademicTermDetails();
                assertEquals(3, academicTermDetail.size());
                
                try {
                    eventAggregationManagementDao.addAcademicTermDetails(new DateMidnight(2012, 1, 1), new DateMidnight(2013, 6, 1), "Spring 2013");
                    fail();
                }
                catch (IllegalArgumentException e) {
                    //expected
                }
                try {
                    eventAggregationManagementDao.addAcademicTermDetails(new DateMidnight(2011, 1, 1), new DateMidnight(2012, 6, 1), "Fall 2011");
                    fail();
                }
                catch (IllegalArgumentException e) {
                    //expected
                }
                
                academicTermDetail.get(0).setTermName("New Term");
                eventAggregationManagementDao.updateAcademicTermDetails(academicTermDetail.get(0));
                eventAggregationManagementDao.deleteAcademicTermDetails(academicTermDetail.get(2));
            }
        });
        

        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final List<AcademicTermDetail> academicTermDetail = eventAggregationManagementDao.getAcademicTermDetails();
                assertEquals(2, academicTermDetail.size());
            }
        });
    }
}
