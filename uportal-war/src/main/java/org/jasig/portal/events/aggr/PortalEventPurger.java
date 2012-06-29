package org.jasig.portal.events.aggr;

import org.jasig.portal.concurrency.locking.IClusterLockService;

/**
 * Handles purging of raw portal events
 */
public interface PortalEventPurger {
    /**
     * Name of the lock to use with {@link IClusterLockService} to call {@link #doPurgeRawEvents()}
     */
    static final String PURGE_RAW_EVENTS_LOCK_NAME = PortalEventPurger.class.getName() + ".PURGE_RAW_EVENTS_LOCK";
    
    /**
     * Purges raw portal event data
     * <br/>
     * Note that this method MUST be called while the current thread & JVM owns the {@link #PURGE_RAW_EVENTS_LOCK_NAME} cluster
     * wide lock via the {@link IClusterLockService}
     * 
     * @return null if purging is not attempted due to some dependency being missing
     */
    EventProcessingResult doPurgeRawEvents();
}
