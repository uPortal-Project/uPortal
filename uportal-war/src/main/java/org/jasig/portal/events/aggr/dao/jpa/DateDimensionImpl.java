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

package org.jasig.portal.events.aggr.dao.jpa;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.jasig.portal.events.aggr.DateDimension;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_DATE_DIMENSION")
@Inheritance(strategy=InheritanceType.JOINED)
@SequenceGenerator(
        name="UP_DATE_DIMENSION_GEN",
        sequenceName="UP_DATE_DIMENSION_SEQ",
        allocationSize=1
    )
@TableGenerator(
        name="UP_DATE_DIMENSION_GEN",
        pkColumnValue="UP_DATE_DIMENSION_PROP",
        allocationSize=1
    )
@Immutable
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class DateDimensionImpl implements DateDimension, Serializable {
    private static final long serialVersionUID = 1L;
   
    @Id
    @GeneratedValue(generator = "UP_DATE_DIMENSION_GEN")
    @Column(name="DATE_ID")
    private final long id;
    
    @Index(name = "IDX_UP_DD_FULL_DATE")
    @Column(name="DD_FULL_DATE", nullable=false)
    private final Date fullDate;
    
    @NaturalId
    @Column(name="DD_YEAR", nullable=false)
    private final int year;

    @NaturalId
    @Column(name="DD_MONTH", nullable=false)
    private final int month;

    @NaturalId
    @Column(name="DD_DAY", nullable=false)
    private final int day;
    
    @Index(name = "IDX_UP_DD_WEEK")
    @Column(name="DD_WEEK", nullable=false)
    private final int week;
    
    @Index(name = "IDX_UP_DD_QUARTER")
    @Column(name="DD_QUARTER", nullable=false)
    private final int quarter;
    
    @Index(name = "IDX_UP_DD_TERM")
    @Column(name="DD_TERM", length=200)
    private final String term;
    
    @Transient
    private int hashCode = 0;

    /**
     * no-arg needed by hibernate
     */
    @SuppressWarnings("unused")
    private DateDimensionImpl() {
        this.id = -1;
        this.fullDate = null;
        this.year = -1;
        this.month = -1;
        this.day = -1;
        this.week = -1;
        this.quarter = -1;
        this.term = null;
    }
    DateDimensionImpl(int year, int month, int day, int quarter, String term) {
        if (quarter < 0 || quarter > 3) {
            throw new IllegalArgumentException("Month must be between 0 and 3, it is: " + quarter);
        }
        
        final Calendar cal = Calendar.getInstance();
        cal.setLenient(false); //Make the Calendar do the bounds checking, will throw IllegalArgumentException on call to getTime()
        cal.clear();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        
        this.id = -1;
        this.fullDate = cal.getTime();
        this.year = cal.get(Calendar.YEAR);
        this.month = cal.get(Calendar.MONTH);
        this.day = cal.get(Calendar.DAY_OF_MONTH);
        this.week = cal.get(Calendar.WEEK_OF_YEAR);
        this.quarter = quarter;
        this.term = term;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Date getFullDate() {
        return this.fullDate;
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
    public int hashCode() {
        int h = hashCode;
        if (h == 0) {
            final int prime = 31;
            int result = 1;
            result = prime * result + day;
            result = prime * result + month;
            result = prime * result + year;
            return result;
        }
        return h;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DateDimensionImpl other = (DateDimensionImpl) obj;
        if (day != other.day)
            return false;
        if (month != other.month)
            return false;
        if (year != other.year)
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "DateDimension [id=" + id + ", fullDate=" + fullDate + ", year=" + year + ", month=" + month
                + ", day=" + day + ", week=" + week + ", quarter=" + quarter + ", term=" + term + "]";
    }
    
}
