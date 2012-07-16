package org.jasig.portal.events.aggr;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.jasig.portal.concurrency.locking.IClusterLockService;

/**
 * Handles aggregation of portal raw events as well as cleanup of the generated aggregations
 */
public interface PortalRawEventsAggregator {
    /**
     * Name of the lock to use with {@link IClusterLockService} to call {@link #doAggregateRawEvents()}
     */
    static final String AGGREGATION_LOCK_NAME = PortalRawEventsAggregator.class.getName() + ".AGGREGATION_LOCK";
    
    /**
     * Aggregates raw portal event data
     * <br/>
     * Note that this method MUST be called while the current thread & JVM owns the {@link #AGGREGATION_LOCK_NAME} cluster
     * wide lock via the {@link IClusterLockService}
     * 
     * @return null if aggregation is not attempted due to some dependency being missing
     * @see IPortalEventAggregator#aggregateEvent(org.jasig.portal.events.PortalEvent, org.jasig.portal.events.aggr.session.EventSession, EventAggregationContext, Map)
     */
    EventProcessingResult doAggregateRawEvents();
    
    /**
     * Close aggregations that were missed when crossing an interval boundary.
     * <br/>
     * Note that this method MUST be called while the current thread & JVM owns the {@link #AGGREGATION_LOCK_NAME} cluster
     * wide lock via the {@link IClusterLockService}
     * 
     * @see IPortalEventAggregator#cleanUnclosedAggregations(org.joda.time.DateTime, org.joda.time.DateTime, AggregationInterval)
     */
    EventProcessingResult doCloseAggregations();
    
    /**
     * Evict cached data for the specified entities and keys.
     * 
     * @param evictedEntities Map of entity type to collection of primary keys to be evicted
     */
    void evictAggregates(Map<Class<?>, Collection<Serializable>> evictedEntities);
}
