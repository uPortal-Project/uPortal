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

package org.jasig.portal.layout.dlm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.test.BaseJpaDaoTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author awills
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaPortalTestApplicationContext.xml")
public class JpaFragmentDefinitionDaoTest extends BaseJpaDaoTest {
    @Autowired
    private IFragmentDefinitionDao dao;

    @PersistenceContext(unitName = "uPortalPersistence")
    private EntityManager entityManager;
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    @Test
    public void testNoopOperations() throws Exception {
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {

                final FragmentDefinition nullFd = dao.getFragmentDefinition("THIS_IS_NOT_IN_FACT_A_FRAGMENT");
                assertNull(nullFd);
                
                final FragmentDefinition fakeFd = MockFragmentDefinition.newFragmentDefinition("THIS_IS_NOT_IN_FACT_A_FRAGMENT");
                dao.removeFragmentDefinition(fakeFd);
                
                return null;
            }
        });
                
    }

    @Test
    public void testAllMethods() throws Exception {
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                FragmentDefinition foo1 = MockFragmentDefinition.newFragmentDefinition("foo");
                dao.updateFragmentDefinition(foo1);
                FragmentDefinition bar1 = MockFragmentDefinition.newFragmentDefinition("bar");
                dao.updateFragmentDefinition(bar1);
                assertFalse(foo1.getName().equals(bar1.getName()));

                FragmentDefinition foo2 = dao.getFragmentDefinition("foo");
                assertNotNull(foo2);
                assertTrue(foo1.getName().equals(foo2.getName()));
                FragmentDefinition bar2 = dao.getFragmentDefinition("bar");
                assertNotNull(bar2);
                assertTrue(bar1.getName().equals(bar2.getName()));
                assertFalse(foo2.getName().equals(bar2.getName()));
                
                return null;
            }
        });

    }
    
    public static class Util {
        public static <T> Set<T> unmodifiableSet(T... o) {
            return Collections.unmodifiableSet(new HashSet<T>(Arrays.asList(o)));
        }
    }
    
    private static final class MockFragmentDefinition extends FragmentDefinition {
        public static FragmentDefinition newFragmentDefinition(String name) {
            return new FragmentDefinition(name);
        }

        /**
         * @param name
         */
        public MockFragmentDefinition(String name) {
            super(name);
        }
    }

}
