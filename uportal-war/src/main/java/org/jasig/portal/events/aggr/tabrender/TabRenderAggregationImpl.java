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

package org.jasig.portal.events.aggr.tabrender;

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
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseTimedAggregationStatsImpl;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * @author Eric Dalquist
 */
@Entity
@Table(name = "UP_TAB_RENDER_AGGR")
@Inheritance(strategy=InheritanceType.JOINED)
@SequenceGenerator(
        name="UP_TAB_RENDER_AGGR_GEN",
        sequenceName="UP_TAB_RENDER_AGGR_SEQ",
        allocationSize=5000
    )
@TableGenerator(
        name="UP_TAB_RENDER_AGGR_GEN",
        pkColumnValue="UP_TAB_RENDER_AGGR_PROP",
        allocationSize=5000
    )
@org.hibernate.annotations.Table(
        appliesTo = "UP_TAB_RENDER_AGGR",
        indexes = {
                @Index(name = "IDX_UP_TAB_REND_AGGR_DTI", columnNames = { "DATE_DIMENSION_ID", "TIME_DIMENSION_ID", "AGGR_INTERVAL" }),
                @Index(name = "IDX_UP_TAB_REND_AGGR_DTIC", columnNames = { "DATE_DIMENSION_ID", "TIME_DIMENSION_ID", "AGGR_INTERVAL", "STATS_COMPLETE" })
            }
        )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TabRenderAggregationImpl extends BaseTimedAggregationStatsImpl implements TabRenderAggregation, Serializable {
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(generator = "UP_TAB_RENDER_AGGR_GEN")
    @Column(name="ID")
    private final long id;
    
    @NaturalId
    @Column(name = "TAB_NAME", nullable = false, length=400)
    private final String tabName;
    
    @SuppressWarnings("unused")
    private TabRenderAggregationImpl() {
        super();
        this.id = -1;
        this.tabName = null;
    }
    
    TabRenderAggregationImpl(TimeDimension timeDimension, DateDimension dateDimension, 
            AggregationInterval interval, AggregatedGroupMapping aggregatedGroup, String tabName) {
        super(timeDimension, dateDimension, interval, aggregatedGroup);

        Validate.notNull(tabName);
        
        this.id = -1;
        this.tabName = tabName;
    }

    @Override
    public String getTabName() {
        return this.tabName;
    }
    
    @Override
    public int getRenderCount() {
        return (int)this.getN();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((tabName == null) ? 0 : tabName.hashCode());
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
        TabRenderAggregationImpl other = (TabRenderAggregationImpl) obj;
        if (tabName == null) {
            if (other.tabName != null)
                return false;
        }
        else if (!tabName.equals(other.tabName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TabRenderAggregationImpl [tabName=" + tabName + ", getTimeDimension=" + getTimeDimension()
                + ", getDateDimension=" + getDateDimension() + ", getInterval=" + getInterval()
                + ", getAggregatedGroup=" + getAggregatedGroup() + "]";
    }
}
