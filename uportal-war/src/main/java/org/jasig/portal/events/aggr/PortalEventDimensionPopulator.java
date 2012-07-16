package org.jasig.portal.events.aggr;

import org.jasig.portal.concurrency.locking.IClusterLockService;

/**
 * Handles creation and maintenance of the date and time dimensions used when aggregating portal events
 */
public interface PortalEventDimensionPopulator {
    /**
     * Name of the lock to use with {@link IClusterLockService} to call {@link #doPopulateDimensions()}
     */
    static final String DIMENSION_LOCK_NAME = PortalEventDimensionPopulator.class.getName() + ".DIMENSION_LOCK";

    /**
     * @return true if {@link #doPopulateDimensions()} has been called and has completed
     * successfully since the JVM was started
     */
    boolean isCheckedDimensions();

    /**
     * Populates the {@link DateDimension} and {@link TimeDimension} data required by the event
     * aggregation tools.
     * <br/>
     * Note that this method MUST be called while the current thread & JVM owns the {@link #DIMENSION_LOCK_NAME} cluster
     * wide lock via the {@link IClusterLockService}
     */
    void doPopulateDimensions();
}