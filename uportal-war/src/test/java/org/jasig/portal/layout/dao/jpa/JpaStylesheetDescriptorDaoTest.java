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

package org.jasig.portal.layout.dao.jpa;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.concurrent.Callable;

import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.portlet.dao.jpa.BaseJpaDaoTest;
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
@ContextConfiguration(locations = "classpath:jpaStylesheetDescriptorDaoTestContext.xml")
public class JpaStylesheetDescriptorDaoTest extends BaseJpaDaoTest {
    private IStylesheetDescriptorDao stylesheetDescriptorDao;

    @Autowired
    public void setStylesheetDescriptorDao(IStylesheetDescriptorDao stylesheetDescriptorDao) {
        this.stylesheetDescriptorDao = stylesheetDescriptorDao;
    }
    
    @Test
    public void testStylesheetDescriptorDao() throws Exception {
        final String ssName = this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final IStylesheetDescriptor stylesheetDescriptor = stylesheetDescriptorDao.createStylesheetDescriptor("columns", "classpath:/layout/struct/columns.xsl");
                
                assertNotSame(-1, stylesheetDescriptor.getId());
                
                return stylesheetDescriptor.getName();
            }
        });
        
        this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final IStylesheetDescriptor stylesheetDescriptor = stylesheetDescriptorDao.getStylesheetDescriptorByName(ssName);
                assertNotNull(stylesheetDescriptor);
                
                return null;
            }
        });
    }
}
