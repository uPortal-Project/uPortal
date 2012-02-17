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

package org.jasig.portal.persondir.dao.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.persondir.ILocalAccountDao;
import org.jasig.portal.persondir.ILocalAccountPerson;
import org.jasig.portal.persondir.LocalAccountQuery;
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
@ContextConfiguration(locations = "classpath:jpaPortalTestApplicationContext.xml")
public class JpaLocalAccountDaoImplTest extends BaseJpaDaoTest {
    @Autowired
    private ILocalAccountDao localAccountDao;
    
    @PersistenceContext(unitName = "uPortalPersistence")
    private EntityManager entityManager;
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Test
    public void testAccountSearch() throws Exception {
        //Create users
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final ILocalAccountPerson user1 = localAccountDao.createPerson("user1");
                user1.setAttribute("attr1", "value1", "ValUe2", "blue");
                user1.setAttribute("attr2", "foobar");
                localAccountDao.updateAccount(user1);
                
                final ILocalAccountPerson user2 = localAccountDao.createPerson("user2");
                user2.setAttribute("attr1", "blue");
                user2.setAttribute("attr2", "barrun");
                localAccountDao.updateAccount(user2);
                
                return null;
            }
        });
        
        //Direct Access
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                
                final ILocalAccountPerson user1 = localAccountDao.getPerson("user1");
                assertNotNull(user1);
                assertEquals("user1", user1.getName());
                
                final Map<String, List<Object>> attributes = user1.getAttributes();
                assertNotNull(attributes);
                assertEquals(2, attributes.size());
                
                return null;
            }
        });
        
        //Query 0
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final LocalAccountQuery query = new LocalAccountQuery();
                
                query.setAttribute("attr1", Arrays.asList("black"));
                
                final List<ILocalAccountPerson> people = localAccountDao.getPeople(query);
                assertNotNull(people);
                assertEquals(0, people.size());
                
                return null;
            }
        });
        
        //Query 1
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final LocalAccountQuery query = new LocalAccountQuery();
                
                query.setAttribute("attr1", Arrays.asList("value"));
                query.setAttribute("attr2", Arrays.asList("bar"));
                
                final List<ILocalAccountPerson> people = localAccountDao.getPeople(query);
                assertNotNull(people);
                assertEquals(2, people.size());
                
                return null;
            }
        });
        
        //Query 2
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final LocalAccountQuery query = new LocalAccountQuery();
                
                query.setAttribute("attr1", Arrays.asList("black"));
                query.setAttribute("attr2", Arrays.asList("foo", "run"));
                
                final List<ILocalAccountPerson> people = localAccountDao.getPeople(query);
                assertNotNull(people);
                assertEquals(2, people.size());
                
                return null;
            }
        });
    }
}
