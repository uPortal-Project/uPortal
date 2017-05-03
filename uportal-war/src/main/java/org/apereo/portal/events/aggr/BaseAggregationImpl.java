/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.events.aggr;

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
import org.apereo.portal.events.aggr.dao.jpa.DateDimensionImpl;
import org.apereo.portal.events.aggr.dao.jpa.TimeDimensionImpl;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMappingImpl;
import org.hibernate.annotations.NaturalId;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementations for aggregations that are grouped by date, time, interval and group
 *
 */
@Access(AccessType.FIELD)
@MappedSuperclass
public abstract class BaseAggregationImpl<
                K extends BaseAggregationKey, D extends BaseGroupedAggregationDiscriminator>
        implements BaseAggregation<K, D> {
    private static final long serialVersionUID = 1L;

    @Transient private Logger logger = null;

    @NaturalId
    @ManyToOne(targetEntity = TimeDimensionImpl.class)
    @JoinColumn(name = "TIME_DIMENSION_ID", nullable = false)
    private final TimeDimension timeDimension;

    @NaturalId
    @ManyToOne(targetEntity = DateDimensionImpl.class)
    @JoinColumn(name = "DATE_DIMENSION_ID", nullable = false)
    private final DateDimension dateDimension;

    @NaturalId
    @Enumerated(EnumType.STRING)
    @Column(name = "AGGR_INTERVAL", nullable = false)
    private final AggregationInterval interval;

    @NaturalId
    @ManyToOne(targetEntity = AggregatedGroupMappingImpl.class)
    @JoinColumn(name = "AGGR_GROUP_ID", nullable = false)
    private final AggregatedGroupMapping aggregatedGroup;

    @Column(name = "DURATION", nullable = false)
    private int duration;

    @Transient private Boolean complete = null;
    @Transient private DateTime dateTime = null;

    protected BaseAggregationImpl() {
        this.timeDimension = null;
        this.dateDimension = null;
        this.interval = null;
        this.aggregatedGroup = null;
    }

    protected BaseAggregationImpl(
            TimeDimension timeDimension,
            DateDimension dateDimension,
            AggregationInterval interval,
            AggregatedGroupMapping aggregatedGroup) {
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
    public DateTime getDateTime() {
        DateTime dt = this.dateTime;
        if (dt == null) {
            dt = this.timeDimension.getTime().toDateTime(this.dateDimension.getDate());
            this.dateTime = dt;
        }
        return dt;
    }

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

    /** Set the duration of the interval */
    public final void setDuration(int duration) {
        if (isComplete()) {
            this.getLogger()
                    .warn(
                            "{} is already closed, the new duration of {} will be ignored on: {}",
                            this.getClass().getSimpleName(),
                            duration,
                            this);
            return;
        }
        this.duration = duration;
    }

    /** Mark the interval as complete, set the final duration the interval spans */
    public final void intervalComplete(int duration) {
        this.duration = duration;
        this.completeInterval();
        this.complete = Boolean.TRUE;
    }

    /**
     * Called to check if the interval is "complete". Defined as all data for the interval has been
     * handled and the final aggregation step(s) have been done. Implies that {@link
     * #completeInterval()} has been called at some point in the past
     */
    protected abstract boolean isComplete();

    /**
     * Called to tell the the aggregation that the interval it exists for has been completed and any
     * final calculations should be done
     */
    protected abstract void completeInterval();

    /**
     * @return The {@link Logger} to use for this event, lazy init as Loggers are rarely used for
     *     events
     */
    protected final Logger getLogger() {
        Logger l = this.logger;
        if (l == null) {
            l = LoggerFactory.getLogger(this.getClass());
            this.logger = l;
        }
        return l;
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
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof BaseAggregationImpl)) return false;
        BaseAggregation<?, ?> other = (BaseAggregation<?, ?>) obj;
        if (dateDimension == null) {
            if (other.getDateDimension() != null) return false;
        } else if (!dateDimension.equals(other.getDateDimension())) return false;
        if (aggregatedGroup == null) {
            if (other.getAggregatedGroup() != null) return false;
        } else if (!aggregatedGroup.equals(other.getAggregatedGroup())) return false;
        if (interval != other.getInterval()) return false;
        if (timeDimension == null) {
            if (other.getTimeDimension() != null) return false;
        } else if (!timeDimension.equals(other.getTimeDimension())) return false;
        return true;
    }
}
