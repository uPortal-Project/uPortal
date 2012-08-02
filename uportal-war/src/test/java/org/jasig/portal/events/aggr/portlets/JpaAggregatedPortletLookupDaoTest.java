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

package org.jasig.portal.events.aggr.portlets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.test.BaseAggrEventsJpaDaoTest;
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
public class JpaAggregatedPortletLookupDaoTest extends BaseAggrEventsJpaDaoTest {
    @Autowired
    private AggregatedPortletLookupDao aggregatedPortletLookupDao;
    @Autowired
    private IPortletDefinitionDao portletDefinitionDao;
    
    @Test
    public void testLoginAggregationLifecycle() throws Exception {
        final IPortletDefinition portletDefinition = mock(IPortletDefinition.class);
        when(portletDefinition.getName()).thenReturn("PortletName");
        when(portletDefinitionDao.getPortletDefinitionByFname("fname")).thenReturn(portletDefinition);
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedPortletMapping portletMapping = aggregatedPortletLookupDao.getMappedPortletForFname("fname");
                
                assertNotNull(portletMapping);
                assertEquals("fname", portletMapping.getFName());
                assertEquals("PortletName", portletMapping.getName());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedPortletMapping portletMapping = aggregatedPortletLookupDao.getMappedPortletForFname("fname");
                
                assertNotNull(portletMapping);
                assertEquals("fname", portletMapping.getFName());
                assertEquals("PortletName", portletMapping.getName());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedPortletMapping portletMapping = aggregatedPortletLookupDao.getMappedPortletForFname("fname_old");
                
                assertNotNull(portletMapping);
                assertEquals("fname_old", portletMapping.getFName());
                assertEquals("fname_old", portletMapping.getName());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedPortletMapping portletMapping = aggregatedPortletLookupDao.getMappedPortletForFname("fname_old");
                
                assertNotNull(portletMapping);
                assertEquals("fname_old", portletMapping.getFName());
                assertEquals("fname_old", portletMapping.getName());
            }
        });
    }
}
