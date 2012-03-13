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

package org.jasig.portal.events.aggr.login;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.dao.jpa.DateDimensionImpl;
import org.jasig.portal.events.aggr.dao.jpa.TimeDimensionImpl;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMappingImpl;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_LOGIN_EVENT_AGGREGATE")
@Inheritance(strategy=InheritanceType.JOINED)
@SequenceGenerator(
        name="UP_LOGIN_EVENT_AGGREGATE_GEN",
        sequenceName="UP_LOGIN_EVENT_AGGREGATE_SEQ",
        allocationSize=100
    )
@TableGenerator(
        name="UP_LOGIN_EVENT_AGGREGATE_GEN",
        pkColumnValue="UP_LOGIN_EVENT_AGGREGATE_PROP",
        allocationSize=100
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LoginAggregationImpl implements LoginAggregation, Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(generator = "UP_LOGIN_EVENT_AGGREGATE_GEN")
    @Column(name="ID")
    @SuppressWarnings("unused")
    private final long id;
    
    @NaturalId
    @ManyToOne(targetEntity=TimeDimensionImpl.class)
    @JoinColumn(name = "TIME_DIMENSION_ID", nullable = false)
    private final TimeDimension timeDimension;

    @NaturalId
    @ManyToOne(targetEntity=DateDimensionImpl.class)
    @JoinColumn(name = "DATE_DIMENSION_ID", nullable = false)
    private final DateDimension dateDimension;
    
    @NaturalId
    @Enumerated(EnumType.STRING)
    @Column(name = "AGGR_INTERVAL", nullable = false)
    private final AggregationInterval interval;
    
    @NaturalId
    @ManyToOne(targetEntity=AggregatedGroupMappingImpl.class)
    @JoinColumn(name = "AGGREGATED_GROUP_ID", nullable = false)
    private final AggregatedGroupMapping aggregatedGroup;
    
    @Column(name = "DURATION", nullable = false)
    private int duration;
    
    @Column(name = "LOGIN_COUNT", nullable = false)
    private int loginCount;
    
    @Column(name = "UNIQUE_LOGIN_COUNT", nullable = false)
    private int uniqueLoginCount;
    
    @ElementCollection(fetch=FetchType.EAGER)
    @JoinTable(
            name = "UP_LOGIN_EVENT_AGGREGATE__UIDS",
            joinColumns = @JoinColumn(name = "LOGIN_AGGR_ID")
        )
    private Set<String> uniqueUserNames = new LinkedHashSet<String>();
    
    @SuppressWarnings("unused")
    private LoginAggregationImpl() {
        this.id = -1;
        this.timeDimension = null;
        this.dateDimension = null;
        this.interval = null;
        this.aggregatedGroup = null;
    }
    
    LoginAggregationImpl(TimeDimension timeDimension, DateDimension dateDimension, 
            AggregationInterval interval, AggregatedGroupMapping aggregatedGroup) {
        Validate.notNull(timeDimension);
        Validate.notNull(dateDimension);
        Validate.notNull(interval);
        Validate.notNull(aggregatedGroup);
        
        this.id = -1;
        this.timeDimension = timeDimension;
        this.dateDimension = dateDimension;
        this.interval = interval;
        this.aggregatedGroup = aggregatedGroup;
    }

    @Override
    public TimeDimension getTimeDimension() {
        return this.timeDimension;
    }

    @Override
    public DateDimension getDateDimension() {
        return this.dateDimension;
    }

    @Override
    public AggregationInterval getInterval() {
        return this.interval;
    }

    @Override
    public int getDuration() {
        return this.duration;
    }

    @Override
    public AggregatedGroupMapping getAggregatedGroup() {
        return this.aggregatedGroup;
    }

    @Override
    public int getLoginCount() {
        return this.loginCount;
    }

    @Override
    public int getUniqueLoginCount() {
        return this.uniqueLoginCount;
    }
    
    void setDuration(int duration) {
        checkState();
        
        this.duration = duration;
    }
    
    void countUser(String userName) {
        checkState();
        
        if (this.uniqueUserNames.add(userName)) {
            this.uniqueLoginCount++;
        }
        this.loginCount++;
    }
    
    private void checkState() {
        if (this.loginCount > 0 && this.uniqueUserNames.isEmpty()) {
            throw new IllegalStateException("intervalComplete has been called, countUser can no longer be called");
        }
    }
    
    void intervalComplete(int duration) {
        this.duration = duration;
        this.uniqueUserNames.clear();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dateDimension == null) ? 0 : dateDimension.hashCode());
        result = prime * result + ((aggregatedGroup == null) ? 0 : aggregatedGroup.hashCode());
        result = prime * result + ((interval == null) ? 0 : interval.hashCode());
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
        LoginAggregationImpl other = (LoginAggregationImpl) obj;
        if (dateDimension == null) {
            if (other.dateDimension != null)
                return false;
        }
        else if (!dateDimension.equals(other.dateDimension))
            return false;
        if (aggregatedGroup == null) {
            if (other.aggregatedGroup != null)
                return false;
        }
        else if (!aggregatedGroup.equals(other.aggregatedGroup))
            return false;
        if (interval != other.interval)
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
        return "LoginAggregationImpl [timeDimension=" + timeDimension + ", dateDimension=" + dateDimension
                + ", interval=" + interval + ", groupName=" + aggregatedGroup + ", duration=" + duration + ", loginCount="
                + loginCount + ", uniqueLoginCount=" + uniqueLoginCount + "]";
    }
}
