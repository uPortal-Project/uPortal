package org.jasig.portal.events.aggr;

import org.jasig.portal.concurrency.locking.IClusterLockService;

/**
 * Handles closing of event aggregations
 */
public interface PortalEventAggregationCloser {
    /**
     * Name of the lock to use with {@link IClusterLockService} to call {@link #doCloseAggregations()}
     */
    static final String AGGREGATION_CLOSER_LOCK_NAME = PortalEventAggregationCloser.class.getName() + ".AGGREGATION_CLOSER_LOCK";
    
    /**
     * Close aggregations that were missed when crossing an interval boundary.
     * <br/>
     * Note that this method MUST be called while the current thread & JVM owns the {@link #AGGREGATION_CLOSER_LOCK_NAME} cluster
     * wide lock via the {@link IClusterLockService}
     */
    EventProcessingResult doCloseAggregations();
}
