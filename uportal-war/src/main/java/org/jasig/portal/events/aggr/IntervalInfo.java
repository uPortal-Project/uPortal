package org.jasig.portal.events.aggr;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.util.Assert;

/**
 * The start and end dates for a time interval. The start date is inclusive, the end date is exclusive. Also includes
 * the synthetic keys for the start date and start time of the interval
 */
public class IntervalInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Date start;
    private final Date end;
    private final DateDimension dateDimension;
    private final TimeDimension timeDimension;
    private int timeSpan = -1;

    public IntervalInfo(Date start, Date end, DateDimension dateDimension, TimeDimension timeDimension) {
        Assert.notNull(start, "start can not be null");
        Assert.notNull(end, "end can not be null");
        if (!start.before(end)) {
            throw new IllegalArgumentException("Start date must be before end date. start=" + start + ", end=" + end);
        }
        
        this.start = start;
        this.end = end;
        this.dateDimension = dateDimension;
        this.timeDimension = timeDimension;
    }

    /**
     * @return The first date in the interval, inclusive
     */
    public Date getStart() {
        return this.start;
    }
    /**
     * @return The last date in the interval, exclusive
     */
    public Date getEnd() {
        return this.end;
    }
    /**
     * @return The date dimension that corresponds with the {@link #getStart()} value
     */
    public DateDimension getDateDimension() {
        return dateDimension;
    }
    /**
     * @return The time dimension that corresponds with the {@link #getStart()} value
     */
    public TimeDimension getTimeDimension() {
        return timeDimension;
    }

    /**
     * @return Minutes between start and end
     */
    public int getTimeSpan() {
        if (this.timeSpan < 0) {
            this.timeSpan = (int)((this.end.getTime() - this.start.getTime()) / (60l * 1000l));
        }
        return this.timeSpan;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IntervalInfo)) {
            return false;
        }
        IntervalInfo rhs = (IntervalInfo) object;
        return new EqualsBuilder()
            .append(this.dateDimension, rhs.dateDimension)
            .append(this.timeDimension, rhs.timeDimension)
            .append(this.start, rhs.start)
            .append(this.end, rhs.end)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-2116278315, 401037003)
            .append(this.dateDimension)
            .append(this.timeDimension)
            .append(this.start)
            .append(this.end)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("start", this.start)
            .append("dateDimension", this.dateDimension)
            .append("timeDimension", this.timeDimension)
            .append("end", this.end)
            .append("timeSpan", this.getTimeSpan())
            .toString();
    }
}