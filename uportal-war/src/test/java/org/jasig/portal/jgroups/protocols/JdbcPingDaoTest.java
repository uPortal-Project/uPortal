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
package org.jasig.portal.jgroups.protocols;

import static junit.framework.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.test.BasePortalJpaDaoTest;
import org.jgroups.Address;
import org.jgroups.PhysicalAddress;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaPortalTestApplicationContext.xml")
public class JdbcPingDaoTest extends BasePortalJpaDaoTest {

    @Autowired
    private PingDao pingDao;
    
    @Test
    public void testPingLifecycle() throws UnknownHostException {
        final String cluster = "cluster";
        final UUID uuid = UUID.randomUUID();
        final IpAddress ipAddress = new IpAddress("127.0.0.1", 1337);
        final IpAddress ipAddress2 = new IpAddress("127.0.0.2", 7331);
        
        //Doesn't exist
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Map<Address, PhysicalAddress> addresses = pingDao.getAddresses(cluster);
                assertEquals(0, addresses.size());
            }
        });

        //Delete nothing
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Collection<Address> addresses = Arrays.<Address>asList(ipAddress, ipAddress2);
                pingDao.purgeOtherAddresses(cluster, addresses);
            }
        });

        //Create
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                pingDao.addAddress(cluster, uuid, ipAddress);
            }
        });

        //verify
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Map<Address, PhysicalAddress> addresses = pingDao.getAddresses(cluster);
                
                assertEquals(1, addresses.size());
                
                final Entry<Address, PhysicalAddress> entry = addresses.entrySet().iterator().next();
                assertEquals(uuid, entry.getKey());
                assertEquals(ipAddress, entry.getValue());
            }
        });

        //Update
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                pingDao.addAddress(cluster, uuid, ipAddress2);
            }
        });

        //verify
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Map<Address, PhysicalAddress> addresses = pingDao.getAddresses(cluster);
                
                assertEquals(1, addresses.size());
                
                final Entry<Address, PhysicalAddress> entry = addresses.entrySet().iterator().next();
                assertEquals(uuid, entry.getKey());
                assertEquals(ipAddress2, entry.getValue());
            }
        });

        //Delete
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Collection<Address> addresses = Arrays.<Address>asList(ipAddress);
                pingDao.purgeOtherAddresses(cluster, addresses);
            }
        });
        
        //Doesn't exist
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Map<Address, PhysicalAddress> addresses = pingDao.getAddresses(cluster);
                assertEquals(0, addresses.size());
            }
        });
    }
}