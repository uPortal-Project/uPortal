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
package org.apereo.portal.events.aggr.tabrender;

import java.io.Serializable;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import org.apereo.portal.events.aggr.tabs.AggregatedTabMapping;
import org.apereo.portal.events.aggr.tabs.AggregatedTabMappingImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

@Entity
@Table(name = "UP_TAB_RENDER_AGGR")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(
    name = "UP_TAB_RENDER_AGGR_GEN",
    sequenceName = "UP_TAB_RENDER_AGGR_SEQ",
    allocationSize = 5000
)
@TableGenerator(
    name = "UP_TAB_RENDER_AGGR_GEN",
    pkColumnValue = "UP_TAB_RENDER_AGGR_PROP",
    allocationSize = 5000
)
@org.hibernate.annotations.Table(
    appliesTo = "UP_TAB_RENDER_AGGR",
    indexes = {
        @Index(
            name = "IDX_UP_TAB_REND_AGGR_DTI",
            columnNames = {"DATE_DIMENSION_ID", "TIME_DIMENSION_ID", "AGGR_INTERVAL"}
        ),
        @Index(
            name = "IDX_UP_TAB_REND_AGGR_DTIC",
            columnNames = {
                "DATE_DIMENSION_ID",
                "TIME_DIMENSION_ID",
                "AGGR_INTERVAL",
                "STATS_COMPLETE"
            }
        ),
        @Index(
            name = "IDX_UP_TAB_REND_INTRVL",
            columnNames = {"AGGR_INTERVAL"}
        ),
        @Index(
            name = "IDX_UP_TAB_REND_GRP",
            columnNames = {"AGGR_GROUP_ID"}
        )
    }
)
@NaturalIdCache(
    region = "org.apereo.portal.events.aggr.tabrender.TabRenderAggregationImpl-NaturalId"
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public final class TabRenderAggregationImpl
        extends BaseTimedAggregationStatsImpl<
                TabRenderAggregationKey, TabRenderAggregationDiscriminator>
        implements TabRenderAggregation, Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(generator = "UP_TAB_RENDER_AGGR_GEN")
    @Column(name = "ID")
    private final long id;

    @NaturalId
    @ManyToOne(targetEntity = AggregatedTabMappingImpl.class)
    @JoinColumn(name = "AGGR_TAB_ID", nullable = false)
    private final AggregatedTabMapping aggregatedTab;

    @Transient private TabRenderAggregationKey aggregationKey;
    @Transient private TabRenderAggregationDiscriminator aggregationDiscriminator;

    @SuppressWarnings("unused")
    private TabRenderAggregationImpl() {
        super();
        this.id = -1;
        this.aggregatedTab = null;
    }

    TabRenderAggregationImpl(
            TimeDimension timeDimension,
            DateDimension dateDimension,
            AggregationInterval interval,
            AggregatedGroupMapping aggregatedGroup,
            AggregatedTabMapping aggregatedTab) {
        super(timeDimension, dateDimension, interval, aggregatedGroup);

        Validate.notNull(aggregatedTab);

        this.id = -1;
        this.aggregatedTab = aggregatedTab;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public AggregatedTabMapping getTabMapping() {
        return this.aggregatedTab;
    }

    @Override
    public int getRenderCount() {
        return (int) this.getN();
    }

    @Override
    public TabRenderAggregationKey getAggregationKey() {
        TabRenderAggregationKey key = this.aggregationKey;
        if (key == null) {
            key = new TabRenderAggregationKeyImpl(this);
            this.aggregationKey = key;
        }
        return key;
    }

    @Override
    public TabRenderAggregationDiscriminator getAggregationDiscriminator() {
        TabRenderAggregationDiscriminator discriminator = this.aggregationDiscriminator;
        if (discriminator == null) {
            discriminator = new TabRenderAggregationDiscriminatorImpl(this);
            this.aggregationDiscriminator = discriminator;
        }
        return discriminator;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((aggregatedTab == null) ? 0 : aggregatedTab.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (!(obj instanceof TabRenderAggregation)) return false;
        TabRenderAggregation other = (TabRenderAggregation) obj;
        if (aggregatedTab == null) {
            if (other.getTabMapping() != null) return false;
        } else if (!aggregatedTab.equals(other.getTabMapping())) return false;
        return true;
    }

    @Override
    public String toString() {
        return "TabRenderAggregationImpl [aggregatedTab="
                + aggregatedTab
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
