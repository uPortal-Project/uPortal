/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.utils;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:pooledCounterStoreTestApplicationContext.xml")
public class CounterStoreTest {
    @Autowired
    @Qualifier("one")
    private ICounterStore counterStoreOne;
    @Autowired
    @Qualifier("two")
    private ICounterStore counterStoreTwo;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void onSetUp() throws Exception {
        jdbcTemplate.update("CREATE TABLE UP_SEQUENCE (SEQUENCE_NAME VARCHAR(1000), SEQUENCE_VALUE INTEGER)");
        assertEquals(0, JdbcTestUtils.countRowsInTable(this.jdbcTemplate, "UP_SEQUENCE"));
    }

    @After
    public void onTearDown() throws Exception {
        jdbcTemplate.update("DROP TABLE UP_SEQUENCE");
    }

    @Test
    public void testCounterSingleThread() {
        //Get until DB has to increment
        this.getValue(this.counterStoreOne, "Test1", 1, 7, 1);
        
        this.getValue(this.counterStoreTwo, "Test1", 1, 10, 4);
        
        this.getValue(this.counterStoreOne, "Test1", 1, 10, 2);

        this.getValue(this.counterStoreTwo, "Test1", 1, 10, 5);
        
        this.getValue(this.counterStoreOne, "Test1", 1, 10, 3);

        this.getValue(this.counterStoreTwo, "Test1", 1, 10, 6);
        
        this.getValue(this.counterStoreOne, "Test1", 1, 13, 7);

        this.getValue(this.counterStoreTwo, "Test1", 1, 16, 10);
        
        this.getValue(this.counterStoreOne, "Test1", 1, 16, 8);

        this.getValue(this.counterStoreTwo, "Test1", 1, 16, 11);
        
        this.getValue(this.counterStoreOne, "Test1", 1, 16, 9);
        
        this.getValue(this.counterStoreOne, "Test1", 1, 19, 13);
        
        this.getValue(this.counterStoreOne, "Test2", 2, 7, 1);
        
        this.getValue(this.counterStoreTwo, "Test2", 2, 10, 4);
        
        assertEquals("rowCount", 2, JdbcTestUtils.countRowsInTable(this.jdbcTemplate, "UP_SEQUENCE"));
        assertEquals("Test1 counter value", 19, (int) jdbcTemplate.queryForObject("SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?", Integer.class, "Test1"));
        assertEquals("Test2 counter value", 10, (int) jdbcTemplate.queryForObject("SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?", Integer.class, "Test2"));
    }
    
    protected void getValue(ICounterStore counterStore, String counter, int rows, int tableValue, int counterValue) {
        final int v = counterStore.getNextId(counter);
        assertEquals("counterValue", counterValue, v);
        assertEquals("rows", rows, JdbcTestUtils.countRowsInTable(jdbcTemplate, "UP_SEQUENCE"));
        assertEquals("tableValue", tableValue, (int) jdbcTemplate.queryForObject("SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?", Integer.class, counter));
    }
}
