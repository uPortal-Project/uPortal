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
import java.util.Collection;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.login.LoginAggregationImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;
import org.joda.time.LocalTime;

/**
 */
@Entity
@Table(name = "UP_TIME_DIMENSION")
@SequenceGenerator(
    name = "UP_TIME_DIMENSION_GEN",
    sequenceName = "UP_TIME_DIMENSION_SEQ",
    allocationSize = 1
)
@TableGenerator(
    name = "UP_TIME_DIMENSION_GEN",
    pkColumnValue = "UP_TIME_DIMENSION_PROP",
    allocationSize = 1
)
@Immutable
@NaturalIdCache(region = "org.apereo.portal.events.aggr.dao.jpa.TimeDimensionImpl-NaturalId")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public final class TimeDimensionImpl implements TimeDimension, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_TIME_DIMENSION_GEN")
    @Column(name = "TIME_ID")
    private final long id;

    @NaturalId
    @Column(name = "TD_TIME", nullable = false)
    @Type(type = "localTime")
    private final LocalTime time;

    @Index(name = "IDX_UP_TD_HOUR")
    @Column(name = "TD_HOUR", nullable = false)
    private final int hour;

    @Index(name = "IDX_UP_TD_FIVE_MIN_INCR")
    @Column(name = "TD_FIVE_MINUTE_INCREMENT", nullable = false)
    private final int fiveMinuteIncrement;

    @Index(name = "IDX_UP_TD_MINUTE")
    @Column(name = "TD_MINUTE", nullable = false)
    private final int minute;

    /** NEVER used directly, simply needed for join queries */
    @SuppressWarnings("unused")
    @OneToMany(mappedBy = "timeDimension", fetch = FetchType.LAZY)
    private Collection<LoginAggregationImpl> loginAggregations;

    @Transient private int hashCode = 0;

    /** no-arg needed by hibernate */
    @SuppressWarnings("unused")
    private TimeDimensionImpl() {
        this.id = -1;
        this.time = null;
        this.hour = -1;
        this.fiveMinuteIncrement = -1;
        this.minute = -1;
    }

    TimeDimensionImpl(LocalTime time) {
        this.id = -1;
        this.time = time.minuteOfHour().roundFloorCopy(); //truncate at minute level
        this.hour = this.time.getHourOfDay();
        this.minute = this.time.getMinuteOfHour();
        this.fiveMinuteIncrement = this.minute / 5;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public LocalTime getTime() {
        return this.time;
    }

    @Override
    public int getHour() {
        return this.hour;
    }

    @Override
    public int getFiveMinuteIncrement() {
        return this.fiveMinuteIncrement;
    }

    @Override
    public int getMinute() {
        return this.minute;
    }

    @Override
    public int hashCode() {
        int h = this.hashCode;
        if (h == 0) {
            final int prime = 31;
            h = 1;
            h = prime * h + ((time == null) ? 0 : time.hashCode());
            this.hashCode = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof TimeDimension)) return false;
        TimeDimension other = (TimeDimension) obj;
        if (time == null) {
            if (other.getTime() != null) return false;
        } else if (!time.equals(other.getTime())) return false;
        return true;
    }

    @Override
    public String toString() {
        return time.toString();
    }
}
