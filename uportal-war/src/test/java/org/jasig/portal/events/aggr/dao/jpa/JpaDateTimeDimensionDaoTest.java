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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
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
public class JpaDateTimeDimensionDaoTest extends BaseJpaDaoTest {
	
    @Autowired
	private DateDimensionDao dateDimensionDao;
    @Autowired
    private TimeDimensionDao timeDimensionDao;
    
    @PersistenceContext(unitName = "uPortalAggrEventsPersistence")
    private EntityManager entityManager;
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

	@Test
	public void testGetMinMaxDateDimension() {
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension newestDateDimension = dateDimensionDao.getNewestDateDimension();
                assertNull(newestDateDimension);
            }
        });
        
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final List<DateDimension> dateDimensions = dateDimensionDao.getDateDimensions();
                
                assertEquals(Collections.EMPTY_LIST, dateDimensions);
            }
        });
        
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                dateDimensionDao.createDateDimension(2012, 0, 1);
                dateDimensionDao.createDateDimension(2012, 0, 2);
                dateDimensionDao.createDateDimension(2012, 0, 3);
                dateDimensionDao.createDateDimension(2012, 0, 4);
                dateDimensionDao.createDateDimension(2012, 0, 5);
                dateDimensionDao.createDateDimension(2012, 0, 6);
                dateDimensionDao.createDateDimension(2012, 0, 7);
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByYearMonthDay(2012, 0, 1);
                assertNotNull(dateDimension);
            }
        });
        
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final List<DateDimension> dateDimensions = dateDimensionDao.getDateDimensions();
                
                assertEquals(7, dateDimensions.size());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension newestDateDimension = dateDimensionDao.getNewestDateDimension();
                
                assertEquals(2012, newestDateDimension.getYear());
                assertEquals(0, newestDateDimension.getMonth());
                assertEquals(7, newestDateDimension.getDay());
            }
        });
	}
}
