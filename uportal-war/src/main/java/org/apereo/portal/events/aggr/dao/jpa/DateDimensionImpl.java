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
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.login.LoginAggregationImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;
import org.joda.time.DateMidnight;
import org.joda.time.LocalDate;

/**
 */
@Entity
@Table(name = "UP_DATE_DIMENSION")
@SequenceGenerator(
    name = "UP_DATE_DIMENSION_GEN",
    sequenceName = "UP_DATE_DIMENSION_SEQ",
    allocationSize = 1
)
@TableGenerator(
    name = "UP_DATE_DIMENSION_GEN",
    pkColumnValue = "UP_DATE_DIMENSION_PROP",
    allocationSize = 1
)
@NaturalIdCache(region = "org.apereo.portal.events.aggr.dao.jpa.DateDimensionImpl-NaturalId")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DateDimensionImpl implements DateDimension, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_DATE_DIMENSION_GEN")
    @Column(name = "DATE_ID")
    private final long id;

    @NaturalId
    @Column(name = "DD_DATE", nullable = false, updatable = false)
    @Type(type = "localDate")
    private final LocalDate date;

    @Index(name = "IDX_UP_DD_YEAR")
    @Column(name = "DD_YEAR", nullable = false, updatable = false)
    private final int year;

    @Index(name = "IDX_UP_DD_MONTH")
    @Column(name = "DD_MONTH", nullable = false, updatable = false)
    private final int month;

    @Index(name = "IDX_UP_DD_DAY")
    @Column(name = "DD_DAY", nullable = false, updatable = false)
    private final int day;

    @Index(name = "IDX_UP_DD_WEEK")
    @Column(name = "DD_WEEK", nullable = false, updatable = false)
    private final int week;

    @Index(name = "IDX_UP_DD_QUARTER")
    @Column(name = "DD_QUARTER", nullable = false)
    private int quarter;

    @Index(name = "IDX_UP_DD_TERM")
    @Column(name = "DD_TERM", length = 200)
    private String term;

    /** NEVER used directly, simply needed for join queries */
    @SuppressWarnings("unused")
    @OneToMany(mappedBy = "dateDimension", fetch = FetchType.LAZY)
    private Collection<LoginAggregationImpl> loginAggregations;

    @Transient private int hashCode = 0;
    @Transient private DateMidnight dateMidnight;

    /** no-arg needed by hibernate */
    @SuppressWarnings("unused")
    private DateDimensionImpl() {
        this.id = -1;
        this.date = null;
        this.dateMidnight = null;
        this.year = -1;
        this.month = -1;
        this.day = -1;
        this.week = -1;
        this.quarter = -1;
        this.term = null;
    }

    DateDimensionImpl(DateMidnight date, int quarter, String term) {
        if (quarter < 0 || quarter > 3) {
            throw new IllegalArgumentException(
                    "Quarter must be between 0 and 3, it is: " + quarter);
        }

        this.dateMidnight = date;

        this.id = -1;
        this.date = this.dateMidnight.toLocalDate();
        this.year = dateMidnight.getYear();
        this.month = dateMidnight.getMonthOfYear();
        this.day = dateMidnight.getDayOfMonth();
        this.week = (dateMidnight.getWeekyear() * 100) + dateMidnight.getWeekOfWeekyear();
        this.quarter = quarter;
        this.term = term;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public DateMidnight getDate() {
        DateMidnight dm = this.dateMidnight;
        if (dm == null) {
            dm = this.date.toDateMidnight();
            this.dateMidnight = dm;
        }

        return dm;
    }

    @Override
    public int getYear() {
        return this.year;
    }

    @Override
    public int getQuarter() {
        return this.quarter;
    }

    @Override
    public int getMonth() {
        return this.month;
    }

    @Override
    public int getWeek() {
        return this.week;
    }

    @Override
    public int getDay() {
        return this.day;
    }

    @Override
    public String getTerm() {
        return this.term;
    }

    @Override
    public void setTerm(String term) {
        if (this.term != null) {
            throw new IllegalStateException("term is already set");
        }
        this.term = term;
    }

    @Override
    public int hashCode() {
        int h = this.hashCode;
        if (h == 0) {
            final int prime = 31;
            h = 1;
            h = prime * h + ((getDate() == null) ? 0 : getDate().hashCode());
            this.hashCode = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof DateDimension)) return false;
        DateDimension other = (DateDimension) obj;
        if (getDate() == null) {
            if (other.getDate() != null) return false;
        } else if (!getDate().equals(other.getDate())) return false;
        return true;
    }

    @Override
    public String toString() {
        return getDate().toString("yyyy-MM-dd ZZ");
    }
}
