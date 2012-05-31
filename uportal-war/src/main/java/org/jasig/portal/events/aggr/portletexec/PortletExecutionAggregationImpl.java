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

package org.jasig.portal.events.aggr.portletexec;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseTimedAggregationStatsImpl;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * @author Eric Dalquist
 */
@Entity
@Table(name = "UP_PORTLET_EXEC_AGGR")
@Inheritance(strategy=InheritanceType.JOINED)
@SequenceGenerator(
        name="UP_PORTLET_EXEC_AGGR_GEN",
        sequenceName="UP_PORTLET_EXEC_AGGR_SEQ",
        allocationSize=10000
    )
@TableGenerator(
        name="UP_PORTLET_EXEC_AGGR_GEN",
        pkColumnValue="UP_PORTLET_EXEC_AGGR_PROP",
        allocationSize=10000
    )
@org.hibernate.annotations.Table(
        appliesTo = "UP_PORTLET_EXEC_AGGR",
        indexes = {
                @Index(name = "IDX_UP_PLT_EXEC_AGGR_DTI", columnNames = { "DATE_DIMENSION_ID", "TIME_DIMENSION_ID", "AGGR_INTERVAL" }),
                @Index(name = "IDX_UP_PLT_EXEC_AGGR_DTIC", columnNames = { "DATE_DIMENSION_ID", "TIME_DIMENSION_ID", "AGGR_INTERVAL", "STATS_COMPLETE" })
            }
        )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PortletExecutionAggregationImpl extends BaseTimedAggregationStatsImpl implements PortletExecutionAggregation, Serializable {
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(generator = "UP_PORTLET_EXEC_AGGR_GEN")
    @Column(name="ID")
    private final long id;
    
    @NaturalId
    @Column(name = "FNAME", nullable = false, length=400)
    @Type(type="fname")
    private final String fname;
    
    @NaturalId
    @Column(name = "EXECUTION_TYPE", nullable = false, length=50)
    @Enumerated(EnumType.STRING)
    private final ExecutionType executionType;
    
    @SuppressWarnings("unused")
    private PortletExecutionAggregationImpl() {
        super();
        this.id = -1;
        this.fname = null;
        this.executionType = null;
    }
    
    PortletExecutionAggregationImpl(TimeDimension timeDimension, DateDimension dateDimension, 
            AggregationInterval interval, AggregatedGroupMapping aggregatedGroup, String tabName, ExecutionType executionType) {
        super(timeDimension, dateDimension, interval, aggregatedGroup);

        Validate.notNull(tabName);
        Validate.notNull(executionType);
        
        this.id = -1;
        this.fname = tabName;
        this.executionType = executionType;
    }

    @Override
    public String getFname() {
        return this.fname;
    }
    
    @Override
    public int getExecutionCount() {
        return (int)this.getN();
    }
    
    @Override
    public ExecutionType getExecutionType() {
        return this.executionType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((executionType == null) ? 0 : executionType.hashCode());
        result = prime * result + ((fname == null) ? 0 : fname.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PortletExecutionAggregationImpl other = (PortletExecutionAggregationImpl) obj;
        if (executionType != other.executionType)
            return false;
        if (fname == null) {
            if (other.fname != null)
                return false;
        }
        else if (!fname.equals(other.fname))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletExecutionAggregationImpl [fname=" + fname + ", executionType=" + executionType
                + ", getTimeDimension=" + getTimeDimension() + ", getDateDimension=" + getDateDimension()
                + ", getInterval=" + getInterval() + ", getAggregatedGroup=" + getAggregatedGroup() + "]";
    }
}
