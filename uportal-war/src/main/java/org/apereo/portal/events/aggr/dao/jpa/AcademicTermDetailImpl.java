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

import com.google.common.collect.ComparisonChain;
import java.io.Serializable;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import org.apache.commons.lang.Validate;
import org.apereo.portal.events.aggr.AcademicTermDetail;
import org.apereo.portal.events.aggr.EventDateTimeUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;

/**
 */
@Entity
@Table(name = "UP_ACADEMIC_TERM_DETAIL")
@SequenceGenerator(
    name = "UP_ACADEMIC_TERM_DETAIL_GEN",
    sequenceName = "UP_ACADEMIC_TERM_DETAIL_SEQ",
    allocationSize = 1
)
@TableGenerator(
    name = "UP_ACADEMIC_TERM_DETAIL_GEN",
    pkColumnValue = "UP_ACADEMIC_TERM_DETAIL_PROP",
    allocationSize = 1
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AcademicTermDetailImpl implements AcademicTermDetail, Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(generator = "UP_ACADEMIC_TERM_DETAIL_GEN")
    @Column(name = "TERM_ID")
    private final long id;

    @NaturalId
    @Column(name = "TERM_START", nullable = false)
    @Type(type = "dateTime")
    private DateTime start;

    @NaturalId
    @Column(name = "TERM_END", nullable = false)
    @Type(type = "dateTime")
    private DateTime end;

    @Column(name = "TERM_NAME", nullable = false)
    private String termName;

    @Transient private DateMidnight startDateMidnight;
    @Transient private DateMidnight endDateMidnight;

    @SuppressWarnings("unused")
    private AcademicTermDetailImpl() {
        this.id = -1;
        this.start = null;
        this.end = null;
        this.termName = null;
    }

    public AcademicTermDetailImpl(DateMidnight start, DateMidnight end, String termName) {
        Validate.notNull(start);
        Validate.notNull(end);
        Validate.notNull(termName);
        if (start.isEqual(end) || end.isBefore(start)) {
            throw new IllegalArgumentException("end cannot be before or equal to start");
        }
        this.id = -1;
        this.start = start.toDateTime();
        this.end = end.toDateTime();
        this.termName = termName;
        this.init();
    }

    @PostLoad
    @PostUpdate
    @PostPersist
    void init() {
        this.startDateMidnight = this.start.toDateMidnight();
        this.endDateMidnight = this.end.toDateMidnight();
    }

    @Override
    public String getTermName() {
        return this.termName;
    }

    @Override
    public void setTermName(String termName) {
        Validate.notNull(termName);
        this.termName = termName;
    }

    @Override
    public DateMidnight getStart() {
        return this.startDateMidnight;
    }

    @Override
    public void setStart(DateMidnight start) {
        this.startDateMidnight = start;
        this.start = start.toDateTime();
    }

    @Override
    public DateMidnight getEnd() {
        return this.endDateMidnight;
    }

    @Override
    public void setEnd(DateMidnight end) {
        this.endDateMidnight = end;
        this.end = end.toDateTime();
    }

    @Override
    public int compareTo(ReadableInstant instant) {
        return EventDateTimeUtils.compareTo(this.start, this.end, instant);
    }

    @Override
    public int compareTo(AcademicTermDetail o) {
        return ComparisonChain.start()
                .compare(this.getStart(), o.getStart())
                .compare(this.getEnd(), o.getEnd())
                .result();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof AcademicTermDetail)) return false;
        AcademicTermDetail other = (AcademicTermDetail) obj;
        if (getEnd() == null) {
            if (other.getEnd() != null) return false;
        } else if (!getEnd().equals(other.getEnd())) return false;
        if (getStart() == null) {
            if (other.getStart() != null) return false;
        } else if (!getStart().equals(other.getStart())) return false;
        return true;
    }

    @Override
    public String toString() {
        return "AcademicTermDetailImpl [termName="
                + this.termName
                + ", startDateMidnight="
                + this.startDateMidnight
                + ", endDateMidnight="
                + this.endDateMidnight
                + "]";
    }
}
