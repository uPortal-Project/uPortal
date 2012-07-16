package org.jasig.portal.events.aggr;

import org.jasig.portal.concurrency.locking.IClusterLockService;

/**
 * Handles aggregation of portal events
 */
public interface PortalEventSessionPurger {
    /**
     * Name of the lock to use with {@link IClusterLockService} to call {@link #doPurgeEventSessions()}
     */
    static final String PURGE_EVENT_SESSION_LOCK_NAME = PortalEventSessionPurger.class.getName() + ".PURGE_EVENT_SESSION_LOCK";
    
    /**
     * Purges portal event sessions
     * <br/>
     * Note that this method MUST be called while the current thread & JVM owns the {@link #PURGE_EVENT_SESSION_LOCK_NAME} cluster
     * wide lock via the {@link IClusterLockService}
     * 
     * @return null if purging is not attempted due to some dependency being missing
     */
    EventProcessingResult doPurgeEventSessions();
}
