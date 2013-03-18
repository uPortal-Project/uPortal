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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jgroups.Address;
import org.jgroups.Event;
import org.jgroups.PhysicalAddress;
import org.jgroups.View;
import org.jgroups.annotations.Property;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.logging.LogFactory;
import org.jgroups.protocols.Discovery;

/**
 * Discovery protocol that delegates to a {@link PingDao} implementation. The {@link PingDao} impl
 * MUST call the static {@link #setPingDao(PingDao)} method when it is ready to handle calls. Until that
 * time the DAO_PING protocol will behave as if there are no other visible members.
 * 
 * @author Eric Dalquist
 */
public class DAO_PING extends Discovery {
    static {
        //Register the protocol with jGroups
        ClassConfigurator.addProtocol((short) 600, DAO_PING.class);
    }
    
    private static volatile PingDao pingDao;
    
    public static void setPingDao(PingDao pingDao) {
        if (DAO_PING.pingDao != null) {
            LogFactory.getLog(DAO_PING.class).warn("A PingDao was already set. " + DAO_PING.pingDao + " will be replaced with " + pingDao);
        }
        DAO_PING.pingDao = pingDao;
    }
    
    @Property(description="Interval (in milliseconds) at which the own Address is written. 0 disables it.")
    protected long interval=60000;
    
    private Future<?> writer_future;

    @Override
    public boolean sendDiscoveryRequestsInParallel() {
        return true;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    public void start() throws Exception {
        super.start();
        if(interval > 0) {
            writer_future=timer.scheduleWithFixedDelay(new WriterTask(), interval, interval, TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        final Future<?> wf = writer_future;
        if(wf != null) {
            wf.cancel(false);
            writer_future=null;
        }
        
        super.stop();
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        pingDao = null;
    }

    @Override
    public Collection<PhysicalAddress> fetchClusterMembers(String clusterName) {
        //If no DAO has been set just return an empty list
        if (pingDao == null) {
            log.info("No PingDao set, returning empty set for current cluster members");
            return Collections.emptyList();
        }
        
        //Get the current cluster members
        final Map<Address, PhysicalAddress> existing_mbrs = pingDao.getAddresses(clusterName);

        //Add our address to the store
        getAndSavePhysicalAddress(clusterName);
        
        //Return current members
        return existing_mbrs.values();
    }

    public Object down(Event evt) {
        final Object retval = super.down(evt);
        
        if (evt.getType() == Event.VIEW_CHANGE) {
            //Handle view changes to make sure the dao store is consistent
            handleView((View) evt.getArg());
        }
        
        return retval;
    }

    protected void getAndSavePhysicalAddress(String clusterName) {
        if (pingDao == null) {
            log.info("No PingDao set, skiping save of physical address for cluster " + clusterName);
            return;
        }
        
        PhysicalAddress physicalAddr = (PhysicalAddress) down(new Event(Event.GET_PHYSICAL_ADDRESS, local_addr));
        pingDao.addAddress(clusterName, local_addr, physicalAddr);
    }

    protected void handleView(View view) {
        if (pingDao == null) {
            //If no dao is set yet ignore the view change.
            log.info("No PingDao set, ignoring view change.");
            return;
        }
        
        final Collection<Address> mbrs = view.getMembers();
        final boolean is_coordinator = !mbrs.isEmpty() && mbrs.iterator().next().equals(local_addr);
        if (is_coordinator) {
            //Delete all member addresses other than those in the current view
            pingDao.purgeOtherAddresses(group_addr, mbrs);
        }
    }

    protected final class WriterTask implements Runnable {
        public void run() {
            getAndSavePhysicalAddress(group_addr);
        }
    }
}
