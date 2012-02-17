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

package org.jasig.portal.events.aggr.groups;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.naming.CompositeName;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.groups.ICompositeGroupService;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.test.BaseJpaDaoTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaAggrEventsTestContext.xml")
public class JpaAggregatedGroupLookupDaoTest extends BaseJpaDaoTest {
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
    public void testLoginAggregationLifecycle() throws Exception {
        final IEntityGroup everyoneGroup = mock(IEntityGroup.class);
        when(everyoneGroup.getServiceName()).thenReturn(new CompositeName("local"));
        when(everyoneGroup.getName()).thenReturn("Everyone");
        when(compositeGroupService.findGroup("local.0")).thenReturn(everyoneGroup);
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupMapping = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                assertNotNull(groupMapping);
                assertEquals("local", groupMapping.getGroupService());
                assertEquals("Everyone", groupMapping.getGroupName());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupMapping = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                assertNotNull(groupMapping);
                assertEquals("local", groupMapping.getGroupService());
                assertEquals("Everyone", groupMapping.getGroupName());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupMapping = aggregatedGroupLookupDao.getGroupMapping("local.2");
                
                assertNull(groupMapping);
            }
        });
    }
}
