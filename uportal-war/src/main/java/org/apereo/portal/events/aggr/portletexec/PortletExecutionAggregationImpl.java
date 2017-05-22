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
package org.apereo.portal.events.aggr.portletexec;

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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import org.apache.commons.lang.Validate;
import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.BaseTimedAggregationStatsImpl;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.portletexec.PortletExecutionAggregationKey.ExecutionType;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletMappingImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

@Entity
@Table(name = "UP_PORTLET_EXEC_AGGR")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(
    name = "UP_PORTLET_EXEC_AGGR_GEN",
    sequenceName = "UP_PORTLET_EXEC_AGGR_SEQ",
    allocationSize = 10000
)
@TableGenerator(
    name = "UP_PORTLET_EXEC_AGGR_GEN",
    pkColumnValue = "UP_PORTLET_EXEC_AGGR_PROP",
    allocationSize = 10000
)
@org.hibernate.annotations.Table(
    appliesTo = "UP_PORTLET_EXEC_AGGR",
    indexes = {
        @Index(
            name = "IDX_UP_PLT_EXEC_AGGR_DTI",
            columnNames = {"DATE_DIMENSION_ID", "TIME_DIMENSION_ID", "AGGR_INTERVAL"}
        ),
        @Index(
            name = "IDX_UP_PLT_EXEC_AGGR_DTIC",
            columnNames = {
                "DATE_DIMENSION_ID",
                "TIME_DIMENSION_ID",
                "AGGR_INTERVAL",
                "STATS_COMPLETE"
            }
        ),
        @Index(
            name = "IDX_UP_PLT_EXEC_INTRVL",
            columnNames = {"AGGR_INTERVAL"}
        ),
        @Index(
            name = "IDX_UP_PLT_EXEC_GRP",
            columnNames = {"AGGR_GROUP_ID"}
        )
    }
)
@NaturalIdCache(
    region = "org.apereo.portal.events.aggr.portletexec.PortletExecutionAggregationImpl-NaturalId"
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public final class PortletExecutionAggregationImpl
        extends BaseTimedAggregationStatsImpl<
                PortletExecutionAggregationKey, PortletExecutionAggregationDiscriminator>
        implements PortletExecutionAggregation, Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(generator = "UP_PORTLET_EXEC_AGGR_GEN")
    @Column(name = "ID")
    private final long id;

    @NaturalId
    @ManyToOne(targetEntity = AggregatedPortletMappingImpl.class)
    @JoinColumn(name = "AGGR_PORTLET_ID", nullable = false)
    private final AggregatedPortletMapping aggregatedPortlet;

    @NaturalId
    @Column(name = "EXECUTION_TYPE", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private final ExecutionType executionType;

    @Transient private PortletExecutionAggregationKey aggregationKey;
    @Transient private PortletExecutionAggregationDiscriminator aggregationDiscriminator;

    @SuppressWarnings("unused")
    private PortletExecutionAggregationImpl() {
        super();
        this.id = -1;
        this.aggregatedPortlet = null;
        this.executionType = null;
    }

    PortletExecutionAggregationImpl(
            TimeDimension timeDimension,
            DateDimension dateDimension,
            AggregationInterval interval,
            AggregatedGroupMapping aggregatedGroup,
            AggregatedPortletMapping aggregatedPortlet,
            ExecutionType executionType) {
        super(timeDimension, dateDimension, interval, aggregatedGroup);

        Validate.notNull(aggregatedPortlet);
        Validate.notNull(executionType);

        this.id = -1;
        this.aggregatedPortlet = aggregatedPortlet;
        this.executionType = executionType;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public AggregatedPortletMapping getPortletMapping() {
        return this.aggregatedPortlet;
    }

    @Override
    public int getExecutionCount() {
        return (int) this.getN();
    }

    @Override
    public ExecutionType getExecutionType() {
        return this.executionType;
    }

    @Override
    public PortletExecutionAggregationKey getAggregationKey() {
        PortletExecutionAggregationKey key = this.aggregationKey;
        if (key == null) {
            key = new PortletExecutionAggregationKeyImpl(this);
            this.aggregationKey = key;
        }
        return key;
    }

    @Override
    public PortletExecutionAggregationDiscriminator getAggregationDiscriminator() {
        PortletExecutionAggregationDiscriminator discriminator = this.aggregationDiscriminator;
        if (discriminator == null) {
            discriminator = new PortletExecutionAggregationDiscriminatorImpl(this);
            this.aggregationDiscriminator = discriminator;
        }
        return discriminator;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((aggregatedPortlet == null) ? 0 : aggregatedPortlet.hashCode());
        result = prime * result + ((executionType == null) ? 0 : executionType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (!(obj instanceof PortletExecutionAggregation)) return false;
        PortletExecutionAggregation other = (PortletExecutionAggregation) obj;
        if (aggregatedPortlet == null) {
            if (other.getPortletMapping() != null) return false;
        } else if (!aggregatedPortlet.equals(other.getPortletMapping())) return false;
        if (executionType != other.getExecutionType()) return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletExecutionAggregationImpl [aggregatedPortlet="
                + aggregatedPortlet
                + ", executionType="
                + executionType
                + ", timeDimension="
                + getTimeDimension()
                + ", dateDimension="
                + getDateDimension()
                + ", interval="
                + getInterval()
                + ", aggregatedGroup="
                + getAggregatedGroup()
                + "]";
    }
}
