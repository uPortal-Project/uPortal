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

package org.jasig.portal.events.aggr;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.NaturalId;
import org.jasig.portal.events.aggr.dao.jpa.DateDimensionImpl;
import org.jasig.portal.events.aggr.dao.jpa.TimeDimensionImpl;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMappingImpl;

/**
 * Base implementations for aggregations that are grouped by date, time, interval and group
 * 
 * @author Eric Dalquist
 */
@Access(AccessType.FIELD)
@MappedSuperclass
public abstract class BaseAggregationImpl<K extends BaseAggregationKey> implements BaseAggregation<K> {
    private static final long serialVersionUID = 1L;
    
    @NaturalId
    @ManyToOne(targetEntity=TimeDimensionImpl.class)
    @JoinColumn(name = "TIME_DIMENSION_ID", nullable = false)
    private final TimeDimension timeDimension;

    @NaturalId
    @ManyToOne(targetEntity=DateDimensionImpl.class)
    @JoinColumn(name = "DATE_DIMENSION_ID", nullable = false)
    private final DateDimension dateDimension;
    
    @NaturalId
    @Enumerated(EnumType.STRING)
    @Column(name = "AGGR_INTERVAL", nullable = false)
    private final AggregationInterval interval;
    
    @NaturalId
    @ManyToOne(targetEntity=AggregatedGroupMappingImpl.class)
    @JoinColumn(name = "AGGR_GROUP_ID", nullable = false)
    private final AggregatedGroupMapping aggregatedGroup;
    
    @Column(name = "DURATION", nullable = false)
    private int duration;
    
    @Transient
    private Boolean complete = null;
    
    
    protected BaseAggregationImpl() {
        this.timeDimension = null;
        this.dateDimension = null;
        this.interval = null;
        this.aggregatedGroup = null;
    }
    
    protected BaseAggregationImpl(TimeDimension timeDimension, DateDimension dateDimension, 
            AggregationInterval interval, AggregatedGroupMapping aggregatedGroup) {
        Validate.notNull(timeDimension);
        Validate.notNull(dateDimension);
        Validate.notNull(interval);
        Validate.notNull(aggregatedGroup);
        
        this.timeDimension = timeDimension;
        this.dateDimension = dateDimension;
        this.interval = interval;
        this.aggregatedGroup = aggregatedGroup;
    }
    
    public abstract long getId();

    @Override
    public final TimeDimension getTimeDimension() {
        return this.timeDimension;
    }

    @Override
    public final DateDimension getDateDimension() {
        return this.dateDimension;
    }

    @Override
    public final AggregationInterval getInterval() {
        return this.interval;
    }

    @Override
    public final int getDuration() {
        return this.duration;
    }

    @Override
    public final AggregatedGroupMapping getAggregatedGroup() {
        return this.aggregatedGroup;
    }

    /**
     * Set the duration of the interval
     */
    public final void setDuration(int duration) {
        checkState();
        this.duration = duration;
    }
    
    /**
     * Mark the interval as complete, set the final duration the interval spans
     */
    public final void intervalComplete(int duration) {
        this.duration = duration;
        this.completeInterval();
        this.complete = Boolean.TRUE;
    }
    
    /**
     * Called to check if the interval is "complete". Defined as all data for the interval has been handled and
     * the final aggregation step(s) have been done. Implies that {@link #completeInterval()} has been called at
     * some point in the past
     */
    protected abstract boolean isComplete();
    
    /**
     * Called to tell the the aggregation that the interval it exists for has been completed and any final calculations
     * should be done
     */
    protected abstract void completeInterval();
    
    /**
     * Checks if the aggregation has been closed, throws an {@link IllegalStateException} if the {@link #completeInterval()} has
     * been called at some point in the future
     */
    protected final void checkState() {
        if (this.complete == null) {
            this.complete = this.isComplete();
        }
        if (this.complete == Boolean.TRUE) {
            throw new IllegalStateException("intervalComplete has been called, " + this.getClass().getName() + " can no longer be modified");
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dateDimension == null) ? 0 : dateDimension.hashCode());
        result = prime * result + ((aggregatedGroup == null) ? 0 : aggregatedGroup.hashCode());
        result = prime * result + ((interval == null) ? 0 : interval.hashCode());
        result = prime * result + ((timeDimension == null) ? 0 : timeDimension.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof BaseAggregationImpl))
            return false;
        BaseAggregation<?> other = (BaseAggregation<?>) obj;
        if (dateDimension == null) {
            if (other.getDateDimension() != null)
                return false;
        }
        else if (!dateDimension.equals(other.getDateDimension()))
            return false;
        if (aggregatedGroup == null) {
            if (other.getAggregatedGroup() != null)
                return false;
        }
        else if (!aggregatedGroup.equals(other.getAggregatedGroup()))
            return false;
        if (interval != other.getInterval())
            return false;
        if (timeDimension == null) {
            if (other.getTimeDimension() != null)
                return false;
        }
        else if (!timeDimension.equals(other.getTimeDimension()))
            return false;
        return true;
    }
}
