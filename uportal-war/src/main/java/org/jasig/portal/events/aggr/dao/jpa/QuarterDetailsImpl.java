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

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.jasig.portal.events.aggr.QuarterDetails;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.MonthDay;
import org.joda.time.ReadableInstant;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_QUARTER_DETAILS")
@Inheritance(strategy=InheritanceType.JOINED)
@SequenceGenerator(
        name="UP_QUARTER_DETAILS_GEN",
        sequenceName="UP_QUARTER_DETAILS_SEQ",
        allocationSize=1
    )
@TableGenerator(
        name="UP_QUARTER_DETAILS_GEN",
        pkColumnValue="UP_QUARTER_DETAILS_PROP",
        allocationSize=1
    )
@Immutable
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class QuarterDetailsImpl implements QuarterDetails, Serializable {
    private static final long serialVersionUID = 1L;
   
    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(generator = "UP_QUARTER_DETAILS_GEN")
    @Column(name="QUARTER_ID")
    private final long id;
    
    @NaturalId
    @Column(name="QUARTER_START", nullable=false)
    @Type(type="monthDay")
    private final MonthDay start;
    
    @NaturalId
    @Column(name="QUARTER_END", nullable=false)
    @Type(type="monthDay")
    private final MonthDay end;

    @Column(name="QUARTER_NUMBER", nullable=false)
    private final int quarterId;
    
    @SuppressWarnings("unused")
    private QuarterDetailsImpl() {
        this.id = -1;
        this.start = null;
        this.end = null;
        this.quarterId = -1;
    }
    
    QuarterDetailsImpl(MonthDay start, MonthDay end, int quarterId) {
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
    public MonthDay getEnd() {
        return this.end;
    }
    
    @Override
    public boolean contains(ReadableInstant instant) {
        //start is before end, don't need to muck with years
        if (this.start.isBefore(this.end)) {
            final DateMidnight startDateTime = this.start.toDateTime(instant).toDateMidnight();
            final DateMidnight endDateTime = this.end.toDateTime(instant).toDateMidnight();
            return endDateTime.isAfter(instant) && (startDateTime.isBefore(instant) || startDateTime.isEqual(instant));
        }
        
        //end is before start, first try shifting year one back
        final DateTime dateTime = new DateTime(instant);
        DateMidnight startDateTime = this.start.toDateTime(dateTime.minusYears(1)).toDateMidnight();
        DateMidnight endDateTime = this.end.toDateTime(instant).toDateMidnight();
        if (endDateTime.isAfter(instant) && (startDateTime.isBefore(instant) || startDateTime.isEqual(instant))) {
            return true;
        }
        
        //didn't match, try shifting year one forward
        startDateTime = this.start.toDateTime(instant).toDateMidnight();
        endDateTime = this.end.toDateTime(dateTime.plusYears(1)).toDateMidnight();
        return endDateTime.isAfter(instant) && (startDateTime.isBefore(instant) || startDateTime.isEqual(instant));
    }

    @Override
    public int compareTo(QuarterDetails o) {
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QuarterDetailsImpl other = (QuarterDetailsImpl) obj;
        if (this.end == null) {
            if (other.end != null)
                return false;
        }
        else if (!this.end.equals(other.end))
            return false;
        if (this.quarterId != other.quarterId)
            return false;
        if (this.start == null) {
            if (other.start != null)
                return false;
        }
        else if (!this.start.equals(other.start))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "QuarterDetailsImpl [quarterId=" + this.quarterId + ", start=" + this.start + ", end=" + this.end + "]";
    }
}
