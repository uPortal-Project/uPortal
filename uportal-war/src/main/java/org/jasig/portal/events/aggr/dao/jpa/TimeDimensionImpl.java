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
import org.jasig.portal.events.aggr.TimeDimension;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_TIME_DIMENSION")
@Inheritance(strategy=InheritanceType.JOINED)
@SequenceGenerator(
        name="UP_TIME_DIMENSION_GEN",
        sequenceName="UP_TIME_DIMENSION_SEQ",
        allocationSize=1
    )
@TableGenerator(
        name="UP_TIME_DIMENSION_GEN",
        pkColumnValue="UP_TIME_DIMENSION_PROP",
        allocationSize=1
    )
@Immutable
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class TimeDimensionImpl implements TimeDimension, Serializable {
    private static final long serialVersionUID = 1L;
   
    @Id
    @GeneratedValue(generator = "UP_TIME_DIMENSION_GEN")
    @Column(name="TIME_ID")
    private final long id;
    
    @NaturalId
    @Column(name="TD_HOUR", nullable=false)
    private final int hour;
    
    @Index(name = "IDX_UP_TD_FIVE_MIN_INCR")
    @Column(name="TD_FIVE_MINUTE_INCREMENT", nullable=false)
    private final int fiveMinuteIncrement;
    
    @NaturalId
    @Column(name="TD_MINUTE", nullable=false)
    private final int minute;
    
    @Transient
    private int hashCode = 0;

    /**
     * no-arg needed by hibernate
     */
    @SuppressWarnings("unused")
    private TimeDimensionImpl() {
        this.id = -1;
        this.hour = -1;
        this.fiveMinuteIncrement = -1;
        this.minute = -1;
    }
    TimeDimensionImpl(int hour, int minute) {
        final Calendar cal = Calendar.getInstance();
        cal.setLenient(false); //Make the Calendar do the bounds checking, will throw IllegalArgumentException on call to getTime()
        cal.clear();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        
        this.id = -1;
        this.hour = cal.get(Calendar.HOUR_OF_DAY);
        this.minute = cal.get(Calendar.MINUTE);
        this.fiveMinuteIncrement = this.minute / 5;
    }

    @Override
    public long getId() {
        return this.id;
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
        int h = hashCode;
        if (h == 0) {
            final int prime = 31;
            h = 1;
            h = prime * h + hour;
            h = prime * h + minute;
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
        TimeDimensionImpl other = (TimeDimensionImpl) obj;
        if (hour != other.hour)
            return false;
        if (minute != other.minute)
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "TimeDimension [id=" + id + ", hour=" + hour + ", fiveMinuteIncrement=" + fiveMinuteIncrement + ", minute=" + minute + "]";
    }
}
