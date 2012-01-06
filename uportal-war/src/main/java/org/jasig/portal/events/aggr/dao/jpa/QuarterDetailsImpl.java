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
import org.jasig.portal.events.aggr.QuarterDetails;
import org.jasig.portal.events.aggr.TimeDimension;

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
   
    @Id
    @GeneratedValue(generator = "UP_QUARTER_DETAILS_GEN")
    @Column(name="TIME_ID")
    private final long id;
    
    @NaturalId
    @Column(name="QD_START", nullable=false)
    private final Date start;
    
    @NaturalId
    @Column(name="QD_END", nullable=false)
    private final Date end;

    @Column(name="QD_ID", nullable=false)
    private final int quarterId;
    
    private QuarterDetailsImpl() {
    }
    
    QuarterDetailsImpl(Date start, Date end, int quarterId) {
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
    public Date getStart() {
        return (Date)this.start.clone();
    }

    @Override
    public Date getEnd() {
        return (Date)this.end.clone();
    }
    
}
