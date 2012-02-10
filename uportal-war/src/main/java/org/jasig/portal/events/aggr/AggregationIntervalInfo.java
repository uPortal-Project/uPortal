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

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.ReadableInstant;
import org.springframework.util.Assert;

/**
 * The start and end dates for a time interval. The start date is inclusive, the end date is exclusive. Also includes
 * the synthetic keys for the start date and start time of the interval
 */
public class AggregationIntervalInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final AggregationInterval aggregationInterval;
    private final DateTime start;
    private final DateTime end;
    private final DateDimension dateDimension;
    private final TimeDimension timeDimension;
    private int duration = -1;

    AggregationIntervalInfo(AggregationInterval aggregationInterval, DateTime start, DateTime end, DateDimension dateDimension, TimeDimension timeDimension) {
        Assert.notNull(aggregationInterval, "aggregationInterval can not be null");
        Assert.notNull(start, "start can not be null");
        Assert.notNull(end, "end can not be null");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start date must be before end date. start=" + start + ", end=" + end);
        }
        
        this.aggregationInterval = aggregationInterval;
        this.start = start;
        this.end = end;
        this.dateDimension = dateDimension;
        this.timeDimension = timeDimension;
    }
    

    /**
     * @return The type of interval the info is about
     */
    public AggregationInterval getAggregationInterval() {
        return aggregationInterval;
    }

    /**
     * @return The first date in the interval, inclusive
     */
    public DateTime getStart() {
        return this.start;
    }
    /**
     * @return The last date in the interval, exclusive
     */
    public DateTime getEnd() {
        return this.end;
    }
    /**
     * @return The date dimension that corresponds with the {@link #getStart()} value, null if no date dimension exists yet for the start {@link DateTime}
     */
    public DateDimension getDateDimension() {
        return dateDimension;
    }
    /**
     * @return The time dimension that corresponds with the {@link #getStart()} value, null if no time dimension exists yet for the start {@link DateTime}
     */
    public TimeDimension getTimeDimension() {
        return timeDimension;
    }

    /**
     * @return Minutes between {@link #getStart()} and {@link #getEnd()}
     */
    public int getTotalDuration() {
        int d = this.duration;
        if (d < 0) {
            d = Minutes.minutesBetween(this.start, this.end).getMinutes();
            d = Math.abs(d);
            this.duration = d;
        }
        return d;
    }
    
    /**
     * @return Minutes between {@link #getStart()} and the end parameter, if the parameter is after {@link #getEnd()} then {@link #getEnd()} is used
     */
    public int getDurationTo(ReadableInstant end) {
        if (end.isAfter(this.end)) {
            return this.getTotalDuration();
        }
        
        return Math.abs(Minutes.minutesBetween(this.start, end).getMinutes());
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aggregationInterval == null) ? 0 : aggregationInterval.hashCode());
        result = prime * result + ((dateDimension == null) ? 0 : dateDimension.hashCode());
        result = prime * result + duration;
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        result = prime * result + ((timeDimension == null) ? 0 : timeDimension.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AggregationIntervalInfo other = (AggregationIntervalInfo) obj;
        if (aggregationInterval != other.aggregationInterval)
            return false;
        if (dateDimension == null) {
            if (other.dateDimension != null)
                return false;
        }
        else if (!dateDimension.equals(other.dateDimension))
            return false;
        if (duration != other.duration)
            return false;
        if (end == null) {
            if (other.end != null)
                return false;
        }
        else if (!end.equals(other.end))
            return false;
        if (start == null) {
            if (other.start != null)
                return false;
        }
        else if (!start.equals(other.start))
            return false;
        if (timeDimension == null) {
            if (other.timeDimension != null)
                return false;
        }
        else if (!timeDimension.equals(other.timeDimension))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "AggregationIntervalInfo [aggregationInterval=" + aggregationInterval + ", start=" + start + ", end="
                + end + ", dateDimension=" + dateDimension + ", timeDimension=" + timeDimension + ", duration="
                + duration + "]";
    }
}