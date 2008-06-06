/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.events.handlers.db;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMock;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.UserProfile;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.support.ChannelAddedToLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelInstanciatedInLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelMovedInLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelRemovedFromLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelRenderedInLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelTargetedInLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelUpdatedInLayoutPortalEvent;
import org.jasig.portal.events.support.ModifiedChannelDefinitionPortalEvent;
import org.jasig.portal.events.support.PageRenderTimePortalEvent;
import org.jasig.portal.events.support.PortletActionInLayoutPortalEvent;
import org.jasig.portal.events.support.PublishedChannelDefinitionPortalEvent;
import org.jasig.portal.events.support.RemovedChannelDefinitionPortalEvent;
import org.jasig.portal.events.support.UserAddedFolderToLayoutPortalEvent;
import org.jasig.portal.events.support.UserLoggedInPortalEvent;
import org.jasig.portal.events.support.UserLoggedOutPortalEvent;
import org.jasig.portal.events.support.UserMovedFolderInLayoutPortalEvent;
import org.jasig.portal.events.support.UserRemovedFolderFromLayoutPortalEvent;
import org.jasig.portal.events.support.UserSessionCreatedPortalEvent;
import org.jasig.portal.events.support.UserSessionDestroyedPortalEvent;
import org.jasig.portal.events.support.UserUpdatedFolderInLayoutPortalEvent;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;
import org.springframework.test.jpa.AbstractJpaTests;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class JpaPortalEventStoreTest extends AbstractJpaTests {
    private ITestablePortalEventStore jpaPortalEventStore;

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:jpaStatsTestApplicationContext.xml"};
    }
    
    public void setJpaPortalEventStore(final ITestablePortalEventStore dao) {
        this.jpaPortalEventStore = dao;
    }
    
    
    public void testStoreAllEvents() throws Exception {
        PersonImpl person = new PersonImpl();
        person.setID(1);
        person.setAttribute(IPerson.USERNAME, "admin");
        final Set<String> groups = Collections.emptySet();
        this.jpaPortalEventStore.addPersonGroups(person, groups);
        
        
        PortalEvent portalEvent;
        
        portalEvent = new UserLoggedInPortalEvent(this, person);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(1, this.countRowsInTable("STATS_EVENT"));
        assertEquals(1, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(0, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(0, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(0, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new UserLoggedOutPortalEvent(this, person);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(2, this.countRowsInTable("STATS_EVENT"));
        assertEquals(2, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(0, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(0, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(0, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new UserSessionCreatedPortalEvent(this, person);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(3, this.countRowsInTable("STATS_EVENT"));
        assertEquals(3, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(0, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(0, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(0, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new UserSessionDestroyedPortalEvent(this, person);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(4, this.countRowsInTable("STATS_EVENT"));
        assertEquals(4, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(0, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(0, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(0, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        
        final ChannelDefinition channelDefinition = new ChannelDefinition(1);
        
        
        portalEvent = new PublishedChannelDefinitionPortalEvent(this, person, channelDefinition);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(5, this.countRowsInTable("STATS_EVENT"));
        assertEquals(5, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(1, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(0, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(0, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new ModifiedChannelDefinitionPortalEvent(this, person, channelDefinition);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(6, this.countRowsInTable("STATS_EVENT"));
        assertEquals(6, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(2, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(0, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(0, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new RemovedChannelDefinitionPortalEvent(this, person, channelDefinition);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(7, this.countRowsInTable("STATS_EVENT"));
        assertEquals(7, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(3, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(0, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(0, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        
        final UserProfile userProfile = new UserProfile();
        userProfile.setProfileId(1);
        
        final IUserLayoutChannelDescription channelDescription = EasyMock.createMock(IUserLayoutChannelDescription.class);
        EasyMock.expect(channelDescription.getName()).andReturn("chanPub1").anyTimes();
        EasyMock.expect(channelDescription.getChannelPublishId()).andReturn("chanPub1").anyTimes();
        EasyMock.expect(channelDescription.getChannelSubscribeId()).andReturn("chanSub1").anyTimes();
        
        final IUserLayoutNodeDescription parentNodeDescription = EasyMock.createMock(IUserLayoutNodeDescription.class);
        EasyMock.expect(parentNodeDescription.getName()).andReturn("parentNode1").anyTimes();
        EasyMock.expect(parentNodeDescription.getId()).andReturn("parentNode1").anyTimes();
        
        EasyMock.replay(channelDescription, parentNodeDescription);
        
        
        portalEvent = new ChannelAddedToLayoutPortalEvent(this, person, userProfile, channelDescription, parentNodeDescription);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(8, this.countRowsInTable("STATS_EVENT"));
        assertEquals(8, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(4, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(1, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(0, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new ChannelUpdatedInLayoutPortalEvent(this, person, userProfile, channelDescription, parentNodeDescription);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(9, this.countRowsInTable("STATS_EVENT"));
        assertEquals(9, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(5, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(2, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(0, this.countRowsInTable("STATS_RENDER_TIME"));
        
        

        final IUserLayoutNodeDescription oldParentNodeDescription = EasyMock.createMock(IUserLayoutNodeDescription.class);
        EasyMock.expect(oldParentNodeDescription.getId()).andReturn("oldParentNode1").anyTimes();
        EasyMock.expect(oldParentNodeDescription.getName()).andReturn("oldParentNode1").anyTimes();
        
        EasyMock.replay(oldParentNodeDescription);
        
        
        portalEvent = new ChannelMovedInLayoutPortalEvent(this, person, userProfile, channelDescription, oldParentNodeDescription, parentNodeDescription);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(10, this.countRowsInTable("STATS_EVENT"));
        assertEquals(10, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(6, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(3, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(0, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new ChannelRemovedFromLayoutPortalEvent(this, person, userProfile, channelDescription, oldParentNodeDescription);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(11, this.countRowsInTable("STATS_EVENT"));
        assertEquals(11, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(7, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(4, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(0, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new ChannelRenderedInLayoutPortalEvent(this, person, userProfile, channelDescription, parentNodeDescription, 1234, false);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(12, this.countRowsInTable("STATS_EVENT"));
        assertEquals(12, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(8, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(5, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(1, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new ChannelTargetedInLayoutPortalEvent(this, person, userProfile, channelDescription, parentNodeDescription);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(13, this.countRowsInTable("STATS_EVENT"));
        assertEquals(13, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(9, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(6, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(1, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new ChannelInstanciatedInLayoutPortalEvent(this, person, userProfile, channelDescription, parentNodeDescription);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(14, this.countRowsInTable("STATS_EVENT"));
        assertEquals(14, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(10, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(7, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(1, this.countRowsInTable("STATS_RENDER_TIME"));
        
        

        final IUserLayoutFolderDescription layoutFolderDescription = EasyMock.createMock(IUserLayoutFolderDescription.class);
        EasyMock.expect(layoutFolderDescription.getId()).andReturn("layoutFolder1").anyTimes();
        EasyMock.expect(layoutFolderDescription.getName()).andReturn("layoutFolder1").anyTimes();
        
        EasyMock.replay(layoutFolderDescription);
        
        
        portalEvent = new UserAddedFolderToLayoutPortalEvent(this, person, userProfile, layoutFolderDescription);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(15, this.countRowsInTable("STATS_EVENT"));
        assertEquals(15, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(10, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(8, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(1, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new UserMovedFolderInLayoutPortalEvent(this, person, userProfile, layoutFolderDescription);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(16, this.countRowsInTable("STATS_EVENT"));
        assertEquals(16, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(10, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(9, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(1, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new UserRemovedFolderFromLayoutPortalEvent(this, person, userProfile, layoutFolderDescription);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(17, this.countRowsInTable("STATS_EVENT"));
        assertEquals(17, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(10, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(10, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(1, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new UserUpdatedFolderInLayoutPortalEvent(this, person, userProfile, layoutFolderDescription);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(18, this.countRowsInTable("STATS_EVENT"));
        assertEquals(18, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(10, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(11, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(1, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new PageRenderTimePortalEvent(this, person, userProfile, layoutFolderDescription, 12345);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(19, this.countRowsInTable("STATS_EVENT"));
        assertEquals(19, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(10, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(12, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(2, this.countRowsInTable("STATS_RENDER_TIME"));
        
        
        portalEvent = new PortletActionInLayoutPortalEvent(this, person, userProfile, channelDescription, parentNodeDescription, 1236);
        this.jpaPortalEventStore.storePortalEvents(portalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(0, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(20, this.countRowsInTable("STATS_EVENT"));
        assertEquals(20, this.countRowsInTable("STATS_EVENT_TYPE"));
        assertEquals(11, this.countRowsInTable("STATS_CHANNEL"));
        assertEquals(13, this.countRowsInTable("STATS_FOLDER"));
        assertEquals(3, this.countRowsInTable("STATS_RENDER_TIME"));
    }
    
    public void testStoreTwoPeople() throws Exception {
        PersonImpl person = new PersonImpl();
        person.setID(1);
        person.setAttribute(IPerson.USERNAME, "admin");
        this.jpaPortalEventStore.addPersonGroups(person, new HashSet<String>(Arrays.asList("admin", "student")));

        
        UserLoggedInPortalEvent loggedInPortalEvent = new UserLoggedInPortalEvent(this, person);
        this.jpaPortalEventStore.storePortalEvents(loggedInPortalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(2, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(1, this.countRowsInTable("STATS_EVENT"));
        assertEquals(1, this.countRowsInTable("STATS_EVENT_TYPE"));
        
        
        UserLoggedOutPortalEvent loggedOutPortalEvent = new UserLoggedOutPortalEvent(this, person);
        this.jpaPortalEventStore.storePortalEvents(loggedOutPortalEvent);
        this.checkPoint();
        
        assertEquals(1, this.countRowsInTable("STATS_SESSION"));
        assertEquals(2, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(2, this.countRowsInTable("STATS_EVENT"));
        assertEquals(2, this.countRowsInTable("STATS_EVENT_TYPE"));
        


        person = new PersonImpl();
        person.setID(2);
        person.setAttribute(IPerson.USERNAME, "student");
        this.jpaPortalEventStore.addPersonGroups(person, new HashSet<String>(Arrays.asList("developer", "student")));
        
        loggedInPortalEvent = new UserLoggedInPortalEvent(this, person);
        this.jpaPortalEventStore.storePortalEvents(loggedInPortalEvent);
        this.checkPoint();
        
        assertEquals(2, this.countRowsInTable("STATS_SESSION"));
        assertEquals(4, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(3, this.countRowsInTable("STATS_EVENT"));
        assertEquals(2, this.countRowsInTable("STATS_EVENT_TYPE"));
        
        
        loggedOutPortalEvent = new UserLoggedOutPortalEvent(this, person);
        this.jpaPortalEventStore.storePortalEvents(loggedOutPortalEvent);
        this.checkPoint();
        
        assertEquals(2, this.countRowsInTable("STATS_SESSION"));
        assertEquals(4, this.countRowsInTable("STATS_SESSION_GROUPS"));
        assertEquals(4, this.countRowsInTable("STATS_EVENT"));
        assertEquals(2, this.countRowsInTable("STATS_EVENT_TYPE"));
    }

    private void checkPoint() {
        this.sharedEntityManager.flush();
        this.sharedEntityManager.clear();
    }
}
