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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.springframework.test.jpa.AbstractJpaTests;

/**
 * @author awills
 */
public class JpaFragmentDefinitionDaoTest extends AbstractJpaTests /*implements BeanFactoryAware*/ {
    private FragmentDefinitionDao dao;

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:jpaTestApplicationContext.xml"};
    }

    public void setFragmentDefinitionDao(final FragmentDefinitionDao dao) {
        this.dao = dao;
    }
            
    public void testNoopOperations() throws Exception {

        final FragmentDefinition nullFd = this.dao.getFragmentDefinition("THIS_IS_NOT_IN_FACT_A_FRAGMENT");
        assertNull(nullFd);
        
        final FragmentDefinition fakeFd = MockFragmentDefinition.newFragmentDefinition("THIS_IS_NOT_IN_FACT_A_FRAGMENT");
        this.dao.removeFragmentDefinition(fakeFd);
                
    }

    public void testAllMethods() throws Exception {
        
        FragmentDefinition foo1 = MockFragmentDefinition.newFragmentDefinition("foo");
        this.dao.updateFragmentDefinition(foo1);
        FragmentDefinition bar1 = MockFragmentDefinition.newFragmentDefinition("bar");
        this.dao.updateFragmentDefinition(bar1);
        assertFalse(foo1.getName().equals(bar1.getName()));
        checkPoint();
        
        FragmentDefinition foo2 = this.dao.getFragmentDefinition("foo");
        assertNotNull(foo2);
        assertTrue(foo1.getName().equals(foo2.getName()));
        FragmentDefinition bar2 = this.dao.getFragmentDefinition("bar");
        assertNotNull(bar2);
        assertTrue(bar1.getName().equals(bar2.getName()));
        assertFalse(foo2.getName().equals(bar2.getName()));
        checkPoint();

    }

    private void checkPoint() {
        final EntityManager entityManager = this.dao.getEntityManager();
        entityManager.flush();
        entityManager.clear();
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
    }

}
