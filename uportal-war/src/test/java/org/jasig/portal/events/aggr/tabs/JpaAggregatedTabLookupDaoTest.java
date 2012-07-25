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

package org.jasig.portal.events.aggr.tabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.test.BaseAggrEventsJpaDaoTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaAggrEventsTestContext.xml")
public class JpaAggregatedTabLookupDaoTest extends BaseAggrEventsJpaDaoTest {
    @Autowired
    private AggregatedTabLookupDao aggregatedTabLookupDao;
    
    @Autowired
    @Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
    private JdbcOperations portalJdbcOperations;

    @Test
    public void testLoginAggregationLifecycle() throws Exception {
        when(portalJdbcOperations.queryForList(
                "SELECT NAME FROM UP_LAYOUT_STRUCT where USER_ID = ? AND LAYOUT_ID = ? AND STRUCT_ID = ?", 
                String.class, 
                1, 1, 1)).thenReturn(Collections.singletonList("TabName"));
        
        when(portalJdbcOperations.queryForList(
                "SELECT NAME FROM UP_LAYOUT_STRUCT where USER_ID = ? AND LAYOUT_ID = ? AND STRUCT_ID = ?", 
                String.class, 
                1, 1, 2)).thenReturn(Collections.<String>emptyList());

        when(portalJdbcOperations.queryForList(
                "SELECT USER_NAME FROM UP_USER WHERE USER_ID=?", 
                String.class, 
                1)).thenReturn(Collections.singletonList("FragmentName"));
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedTabMapping tabMappings = aggregatedTabLookupDao.getMappedTabForLayoutId("u1l1s1");
                
                assertNotNull(tabMappings);
                assertEquals("FragmentName", tabMappings.getFragmentName());
                assertEquals("TabName", tabMappings.getTabName());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedTabMapping tabMappings = aggregatedTabLookupDao.getMappedTabForLayoutId("u1l1s2");
                
                assertNotNull(tabMappings);
                assertEquals("FragmentName", tabMappings.getFragmentName());
                assertEquals("u1l1s2", tabMappings.getTabName());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedTabMapping tabMappings = aggregatedTabLookupDao.getMappedTabForLayoutId(null);
                
                assertNotNull(tabMappings);
                assertEquals(AggregatedTabMapping.MISSING_TAB_FRAGMENT_NAME, tabMappings.getFragmentName());
                assertEquals(AggregatedTabMapping.MISSING_TAB_NAME, tabMappings.getTabName());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedTabMapping tabMappings = aggregatedTabLookupDao.getMappedTabForLayoutId("s1");
                
                assertNotNull(tabMappings);
                assertEquals(AggregatedTabMapping.PERSONAL_TAB_FRAGMENT_NAME, tabMappings.getFragmentName());
                assertEquals(AggregatedTabMapping.PERSONAL_TAB_NAME, tabMappings.getTabName());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedTabMapping tabMappings = aggregatedTabLookupDao.getMappedTabForLayoutId("u1l1s1");
                
                assertNotNull(tabMappings);
                assertEquals("FragmentName", tabMappings.getFragmentName());
                assertEquals("TabName", tabMappings.getTabName());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedTabMapping tabMappings = aggregatedTabLookupDao.getMappedTabForLayoutId("u1l1s2");
                
                assertNotNull(tabMappings);
                assertEquals("FragmentName", tabMappings.getFragmentName());
                assertEquals("u1l1s2", tabMappings.getTabName());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedTabMapping tabMappings = aggregatedTabLookupDao.getMappedTabForLayoutId(null);
                
                assertNotNull(tabMappings);
                assertEquals(AggregatedTabMapping.MISSING_TAB_FRAGMENT_NAME, tabMappings.getFragmentName());
                assertEquals(AggregatedTabMapping.MISSING_TAB_NAME, tabMappings.getTabName());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedTabMapping tabMappings = aggregatedTabLookupDao.getMappedTabForLayoutId("s1");
                
                assertNotNull(tabMappings);
                assertEquals(AggregatedTabMapping.PERSONAL_TAB_FRAGMENT_NAME, tabMappings.getFragmentName());
                assertEquals(AggregatedTabMapping.PERSONAL_TAB_NAME, tabMappings.getTabName());
            }
        });
    }
}
