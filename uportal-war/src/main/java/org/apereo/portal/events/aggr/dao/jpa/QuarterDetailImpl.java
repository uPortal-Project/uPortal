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
package org.apereo.portal.events.aggr.dao.jpa;

import java.io.Serializable;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import org.apache.commons.lang.Validate;
import org.apereo.portal.events.aggr.EventDateTimeUtils;
import org.apereo.portal.events.aggr.QuarterDetail;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.MonthDay;
import org.joda.time.ReadableInstant;

/**
 */
@Entity
@Table(name = "UP_QUARTER_DETAIL")
@SequenceGenerator(
    name = "UP_QUARTER_DETAIL_GEN",
    sequenceName = "UP_QUARTER_DETAIL_SEQ",
    allocationSize = 1
)
@TableGenerator(
    name = "UP_QUARTER_DETAIL_GEN",
    pkColumnValue = "UP_QUARTER_DETAIL_PROP",
    allocationSize = 1
)
@Immutable
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class QuarterDetailImpl implements QuarterDetail, Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(generator = "UP_QUARTER_DETAIL_GEN")
    @Column(name = "QUARTER_ID")
    private final long id;

    @NaturalId
    @Column(name = "QUARTER_START", nullable = false)
    @Type(type = "monthDay")
    private final MonthDay start;

    @NaturalId
    @Column(name = "QUARTER_END", nullable = false)
    @Type(type = "monthDay")
    private final MonthDay end;

    @NaturalId
    @Column(name = "QUARTER_NUMBER", nullable = false)
    private final int quarterId;

    @SuppressWarnings("unused")
    private QuarterDetailImpl() {
        this.id = -1;
        this.start = null;
        this.end = null;
        this.quarterId = -1;
    }

    public QuarterDetailImpl(MonthDay start, MonthDay end, int quarterId) {
        Validate.notNull(start);
        Validate.notNull(end);
        if (start.isEqual(end)) {
            throw new IllegalArgumentException("start cannot equal end");
        }
        this.id = -1;
        this.start = start;
        this.end = end;
        this.quarterId = quarterId;
    }

    @Override
    public int getQuarterId() {
        return this.quarterId;
    }

    @Override
    public MonthDay getStart() {
        return this.start;
    }

    @Override
    public DateMidnight getStartDateMidnight(ReadableInstant instant) {
        final MonthDay instantMonthDay = new MonthDay(instant);

        //If the quarter wraps a year boundary AND
        //   the instant MonthDay is before the start AND
        //   the end is after the instant MonthDay
        // then shift the start year back by one to deal with the year boundary
        if (this.end.isBefore(this.start)
                && instantMonthDay.isBefore(this.start)
                && this.end.isAfter(instantMonthDay)) {
            return this.start.toDateTime(new DateTime(instant).minusYears(1)).toDateMidnight();
        }

        return this.start.toDateTime(instant).toDateMidnight();
    }

    @Override
    public DateMidnight getEndDateMidnight(ReadableInstant instant) {
        final MonthDay instantMonthDay = new MonthDay(instant);

        //If the quarter wraps a year boundary AND
        //   the end is NOT after the instant MonthDay AND
        //   the instant MonthDay is NOT before the start
        // then shift the end year forward by one to deal with the year boundary
        if (this.end.isBefore(this.start)
                && !this.end.isAfter(instantMonthDay)
                && !instantMonthDay.isBefore(this.start)) {
            return this.end.toDateTime(new DateTime(instant).plusYears(1)).toDateMidnight();
        }

        return this.end.toDateTime(instant).toDateMidnight();
    }

    @Override
    public MonthDay getEnd() {
        return this.end;
    }

    @Override
    public int compareTo(ReadableInstant instant) {
        final DateMidnight startDateTime = this.getStartDateMidnight(instant);
        final DateMidnight endDateTime = this.getEndDateMidnight(instant);

        return EventDateTimeUtils.compareTo(startDateTime, endDateTime, instant);
    }

    @Override
    public int compareTo(QuarterDetail o) {
        return this.getQuarterId() - o.getQuarterId();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.end == null) ? 0 : this.end.hashCode());
        result = prime * result + this.quarterId;
        result = prime * result + ((this.start == null) ? 0 : this.start.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        QuarterDetailImpl other = (QuarterDetailImpl) obj;
        if (this.end == null) {
            if (other.end != null) return false;
        } else if (!this.end.equals(other.end)) return false;
        if (this.quarterId != other.quarterId) return false;
        if (this.start == null) {
            if (other.start != null) return false;
        } else if (!this.start.equals(other.start)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "QuarterDetailImpl [quarterId="
                + this.quarterId
                + ", start="
                + this.start
                + ", end="
                + this.end
                + "]";
    }
}
