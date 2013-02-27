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
package org.jasig.portal.jgroups.auth;

import static junit.framework.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.test.BasePortalJpaDaoTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaPortalTestApplicationContext.xml")
public class JdbcAuthDaoTest extends BasePortalJpaDaoTest {

    @Autowired
    private JdbcAuthDao authDao;
    
    @Autowired
    private JdbcOperations jdbcOperations;
    
    @Test
    public void testSimpleGetCreate() throws UnknownHostException {
        final String service = "foo";
        
        //Create & Return
        final String token = this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final String t = authDao.getAuthToken(service);
                assertEquals(1000, t.length());
                return t;
            }
        });
        
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final String t = authDao.getAuthToken(service);
                assertEquals(token, t);
            }
        });
   }
    

    @Test
    public void testConcurrentCreate() throws UnknownHostException {
        final String service = "foo";
        
        //Create & Return
        final String token = this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final String t = authDao.getAuthToken(service);
                assertEquals(1000, t.length());
                return t;
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                authDao.createToken(service);
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final String t = authDao.getAuthToken(service);
                assertEquals(token, t);
            }
        });
   }
}