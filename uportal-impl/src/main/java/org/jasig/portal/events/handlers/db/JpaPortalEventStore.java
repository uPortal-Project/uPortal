/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.events.handlers.db;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.ejb.HibernateEntityManager;
import org.jasig.portal.events.BatchingEventHandler;
import org.jasig.portal.events.EventType;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.handlers.AbstractLimitedSupportEventHandler;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.springframework.stereotype.Repository;

/**
 * Stores portal events using JPA/Hibenate no internall batch segementation is done to the passed list
 * of {@link PortalEvent}s. If a {@link PortalEvent} is not mapped as a persistent entity a message is logged
 * at the WARN level and the event is ignored.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaPortalEventStore extends AbstractLimitedSupportEventHandler implements BatchingEventHandler, IPortalEventStore {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    protected static final String STATS_SESSION_PERSON_ATTR = JpaPortalEventStore.class.getName() + ".StatsSession";
    
    private EntityManager entityManager;
    private boolean logSessionGroups = true;
    
    /**
     * @return the entityManager
     */
    public EntityManager getEntityManager() {
        return this.entityManager;
    }
    /**
     * @param entityManager the entityManager to set
     */
    @PersistenceContext(unitName = "uPortalStatsPersistence")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    /**
     * @return the logSessionGroups
     */
    public boolean isLogSessionGroups() {
        return logSessionGroups;
    }
    /**
     * If group keys should be included in the stats session. This can be disabled to reduce the ammount
     * of data stored in the stats tables.
     * 
     * @param logSessionGroups the logSessionGroups to set
     */
    public void setLogSessionGroups(boolean logSessionGroups) {
        this.logSessionGroups = logSessionGroups;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.events.EventHandler#handleEvent(org.jasig.portal.events.PortalEvent)
     */
    public void handleEvent(PortalEvent event) {
        this.storePortalEvents(event);
        
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.events.BatchingEventHandler#handleEvents(org.jasig.portal.events.PortalEvent[])
     */
    public void handleEvents(PortalEvent... events) {
        this.storePortalEvents(events);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.events.handlers.db.IPortalEventStore#storePortalEvents(org.jasig.portal.events.PortalEvent[])
     */
    public void storePortalEvents(PortalEvent... portalEvents) {
        for (final PortalEvent portalEvent : portalEvents) {
            //Load the StatsSession into the event if it isn't already there
            if (portalEvent.getStatsSession() == null) {
                final IPerson person = portalEvent.getPerson();
                final StatsSession statsSession = this.getStatsSession(person);
                portalEvent.setStatsSession(statsSession);
            }

            //Ensure the EventType has been persisted, assumes un-persisted events have an id of 0
            final EventType eventType = portalEvent.getEventType().intern();
            if (eventType.getId() == 0 || !this.entityManager.contains(eventType)) {
                //If an existing EventType is found load it, if not persist the one we have
                //Due to EventType's behavior we don't need to replace the eventType with the foundEventType, they synchronize on load
                final EventType foundEventType = this.findExistingEventType(eventType);
                if (foundEventType == null) {
                    this.entityManager.persist(eventType);
                }
            }
            
            try {
                this.entityManager.persist(portalEvent);
            }
            catch (IllegalArgumentException iae) {
                this.logger.warn(portalEvent.getClass().getName() + " is not mapped as a persistent entity and will not be stored. event=[" + portalEvent + "], message=" + iae.getMessage());
            }
        }
    }

    /**
     * Gets a StatsSession object for the specified person, creating, populating and persisting it if needed
     */
    protected StatsSession getStatsSession(final IPerson person) {
        //Gets the StatsSession from the IPerson object
        StatsSession statsSession = (StatsSession)person.getAttribute(STATS_SESSION_PERSON_ATTR);
        
        //If no StatsSession is found, create one for the user
        if (statsSession == null || statsSession.getSessionId() == 0) {
            statsSession = new StatsSession();
            final String userName = (String)person.getAttribute(IPerson.USERNAME);
            statsSession.setUserName(userName);
            
            if (this.logSessionGroups) {
                try {
                    //Load the user's groups for this session
                    this.updateStatsSessionGroups(statsSession, person);
                }
                catch (org.jasig.portal.groups.GroupsException ge) {
                    this.logger.warn("Exception while loading groups for person='" + person + "' and session='" + statsSession + "'", ge);
                }
            }
            
            this.entityManager.persist(statsSession);
            
            person.setAttribute(STATS_SESSION_PERSON_ATTR, statsSession);
        }

        return statsSession;
    }
    
    /**
     * Sets the {@link StatsSession#setGroups(Set)} using the keys from the {@link IGroupMember}s returned by
     * {@link IGroupMember#getAllContainingGroups()} 
     */
    protected void updateStatsSessionGroups(final StatsSession session, final IPerson person) throws GroupsException {
        final IGroupMember member = GroupService.getGroupMember(person.getEntityIdentifier());
        
        final Set<String> groupKeys = new HashSet<String>();
        for (final Iterator<IGroupMember> groupItr = member.getAllContainingGroups(); groupItr.hasNext(); ) {
            final IGroupMember group = groupItr.next();
            groupKeys.add(group.getKey());
        }
        
        session.setGroups(groupKeys);
    }

    /**
     * Contains the logic to query for an already persisted EventType with the same type value.
     */
    protected EventType findExistingEventType(EventType eventType) {
        if (eventType.getId() != 0) {
            final EventType foundEventType = this.entityManager.find(EventType.class, eventType.getId());
            if (foundEventType != null) {
                return foundEventType;
            }
            
            eventType.setId(0);
        }
        
        //No id, do a lookup based on the event type
        final HibernateEntityManager hibernateEntityManager = ((HibernateEntityManager)this.entityManager);
        final Session session = hibernateEntityManager.getSession();
        
        //Setup the Criteria query
        final Criteria eventTypeCriteria = session.createCriteria(EventType.class);
        eventTypeCriteria.add(Restrictions.naturalId().set("type", eventType.getType()));
        eventTypeCriteria.setCacheable(true);
        eventTypeCriteria.setMaxResults(1);
        
        final EventType foundEventType = (EventType)eventTypeCriteria.uniqueResult();
        return foundEventType;
    }
}
