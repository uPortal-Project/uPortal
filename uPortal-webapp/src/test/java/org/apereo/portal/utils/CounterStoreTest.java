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
package org.apereo.portal.utils;

import static org.junit.Assert.assertEquals;

import org.apereo.portal.ICounterStore;
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

/** */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:pooledCounterStoreTestApplicationContext.xml")
public class CounterStoreTest {
    @Autowired
    @Qualifier("one")
    private ICounterStore counterStoreOne;

    @Autowired
    @Qualifier("two")
    private ICounterStore counterStoreTwo;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Before
    public void onSetUp() throws Exception {
        jdbcTemplate.update(
                "CREATE TABLE UP_SEQUENCE (SEQUENCE_NAME VARCHAR(1000), SEQUENCE_VALUE INTEGER)");
        assertEquals(0, JdbcTestUtils.countRowsInTable(this.jdbcTemplate, "UP_SEQUENCE"));
    }

    @After
    public void onTearDown() throws Exception {
        jdbcTemplate.update("DROP TABLE UP_SEQUENCE");
    }

    @Test
    public void testCounterSingleThread() {
        // Values reflect the POOLED_LO optimizer (Hibernate 4.3): the persisted
        // UP_SEQUENCE value is the next low value to hand out, so the table
        // advances one increment (3) ahead of the issued IDs rather than two.
        // Get until DB has to increment
        this.getValue(this.counterStoreOne, "Test1", 1, 4, 1);

        this.getValue(this.counterStoreTwo, "Test1", 1, 7, 4);

        this.getValue(this.counterStoreOne, "Test1", 1, 7, 2);

        this.getValue(this.counterStoreTwo, "Test1", 1, 7, 5);

        this.getValue(this.counterStoreOne, "Test1", 1, 7, 3);

        this.getValue(this.counterStoreTwo, "Test1", 1, 7, 6);

        this.getValue(this.counterStoreOne, "Test1", 1, 10, 7);

        this.getValue(this.counterStoreTwo, "Test1", 1, 13, 10);

        this.getValue(this.counterStoreOne, "Test1", 1, 13, 8);

        this.getValue(this.counterStoreTwo, "Test1", 1, 13, 11);

        this.getValue(this.counterStoreOne, "Test1", 1, 13, 9);

        this.getValue(this.counterStoreOne, "Test1", 1, 16, 13);

        this.getValue(this.counterStoreOne, "Test2", 2, 4, 1);

        this.getValue(this.counterStoreTwo, "Test2", 2, 7, 4);

        assertEquals(
                "rowCount", 2, JdbcTestUtils.countRowsInTable(this.jdbcTemplate, "UP_SEQUENCE"));
        assertEquals(
                "Test1 counter value",
                16,
                (int)
                        jdbcTemplate.queryForObject(
                                "SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?",
                                Integer.class,
                                "Test1"));
        assertEquals(
                "Test2 counter value",
                7,
                (int)
                        jdbcTemplate.queryForObject(
                                "SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?",
                                Integer.class,
                                "Test2"));
    }

    protected void getValue(
            ICounterStore counterStore,
            String counter,
            int rows,
            int tableValue,
            int counterValue) {
        final int v = counterStore.getNextId(counter);
        assertEquals("counterValue", counterValue, v);
        assertEquals("rows", rows, JdbcTestUtils.countRowsInTable(jdbcTemplate, "UP_SEQUENCE"));
        assertEquals(
                "tableValue",
                tableValue,
                (int)
                        jdbcTemplate.queryForObject(
                                "SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?",
                                Integer.class,
                                counter));
    }
}
