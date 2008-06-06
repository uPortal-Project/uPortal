/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.events.handlers.db;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.support.UserLoggedInPortalEvent;
import org.jasig.portal.events.support.UserLoggedOutPortalEvent;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class JpaPortalEventStorePerformanceProfiler extends AbstractDependencyInjectionSpringContextTests {
    private ITestablePortalEventStore jpaPortalEventStore;
    private CacheManager cacheManager;

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:jpaStatsPerformanceContext.xml"};
    }
    
    public void setJpaPortalEventStore(final ITestablePortalEventStore dao) {
        this.jpaPortalEventStore = dao;
    }
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void testBatchStore() throws Exception {
        for (int runCount = 0; runCount < 10; runCount++) {
            final PersonImpl adminPerson = new PersonImpl();
            adminPerson.setAttribute(IPerson.USERNAME, "admin");
            this.jpaPortalEventStore.addPersonGroups(adminPerson, new HashSet<String>(Arrays.asList("admin")));
            
            final PersonImpl studentPerson = new PersonImpl();
            studentPerson.setAttribute(IPerson.USERNAME, "student");
            this.jpaPortalEventStore.addPersonGroups(studentPerson, new HashSet<String>(Arrays.asList("student")));
            
            final PersonImpl facStaffPerson = new PersonImpl();
            facStaffPerson.setAttribute(IPerson.USERNAME, "facStaff");
            this.jpaPortalEventStore.addPersonGroups(facStaffPerson, new HashSet<String>(Arrays.asList("faculty", "staff", "admin")));
            
            UserLoggedInPortalEvent loggedInPortalEvent;
            UserLoggedOutPortalEvent loggedOutPortalEvent;
            final Queue<PortalEvent> eventQueue = new LinkedList<PortalEvent>();
            
            while (eventQueue.size() < 500) {
                loggedInPortalEvent = new UserLoggedInPortalEvent(this, adminPerson);
                eventQueue.offer(loggedInPortalEvent);
                loggedOutPortalEvent = new UserLoggedOutPortalEvent(this, adminPerson);
                eventQueue.offer(loggedOutPortalEvent);
                
                loggedInPortalEvent = new UserLoggedInPortalEvent(this, studentPerson);
                eventQueue.offer(loggedInPortalEvent);
                
                loggedInPortalEvent = new UserLoggedInPortalEvent(this, adminPerson);
                eventQueue.offer(loggedInPortalEvent);
                loggedOutPortalEvent = new UserLoggedOutPortalEvent(this, adminPerson);
                eventQueue.offer(loggedOutPortalEvent);
        
                loggedOutPortalEvent = new UserLoggedOutPortalEvent(this, studentPerson);
                eventQueue.offer(loggedOutPortalEvent);
                
                loggedInPortalEvent = new UserLoggedInPortalEvent(this, facStaffPerson);
                eventQueue.offer(loggedInPortalEvent);
                loggedOutPortalEvent = new UserLoggedOutPortalEvent(this, facStaffPerson);
                eventQueue.offer(loggedOutPortalEvent);
                
                loggedInPortalEvent = new UserLoggedInPortalEvent(this, facStaffPerson);
                eventQueue.offer(loggedInPortalEvent);
                loggedOutPortalEvent = new UserLoggedOutPortalEvent(this, facStaffPerson);
                eventQueue.offer(loggedOutPortalEvent);
                
                loggedInPortalEvent = new UserLoggedInPortalEvent(this, studentPerson);
                eventQueue.offer(loggedInPortalEvent);
                loggedOutPortalEvent = new UserLoggedOutPortalEvent(this, studentPerson);
                eventQueue.offer(loggedOutPortalEvent);
            }
        
        
            PortalEvent[] events = new PortalEvent[50];

            final long start = System.currentTimeMillis();
            while (eventQueue.size() > 0) {
                for (int i = 0; i < events.length; i++) {
                    final PortalEvent event = eventQueue.poll();
                    if (event != null) {
                        events[i] = event;
                    }
                    else {
                        PortalEvent[] lastEvents = new PortalEvent[i];
                        System.arraycopy(events, 0, lastEvents, 0, i);
                        events = lastEvents;
                        break;
                    }
                }
                this.jpaPortalEventStore.storePortalEvents(events);
            }
            System.out.println(System.currentTimeMillis() - start);
        }
    }
}
