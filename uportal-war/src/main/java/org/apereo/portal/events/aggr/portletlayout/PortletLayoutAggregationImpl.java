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
package org.apereo.portal.events.aggr.portletlayout;

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
import org.apereo.portal.events.aggr.BaseAggregationImpl;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletMappingImpl;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

@Entity
@Table(name = "UP_PORTLET_LAYOUT_AGGR")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(
    name = "UP_PORTLET_LAYOUT_AGGR_GEN",
    sequenceName = "UP_PORTLET_LAYOUT_AGGR_SEQ",
    allocationSize = 1000
)
@TableGenerator(
    name = "UP_PORTLET_LAYOUT_AGGR_GEN",
    pkColumnValue = "UP_PORTLET_LAYOUT_AGGR_PROP",
    allocationSize = 1000
)
@org.hibernate.annotations.Table(
    appliesTo = "UP_PORTLET_LAYOUT_AGGR",
    indexes = {
        @Index(
            name = "IDX_UP_PORTLET_LAYOUT_AGGR_DTI",
            columnNames = {"DATE_DIMENSION_ID", "TIME_DIMENSION_ID", "AGGR_INTERVAL"}
        ),
        @Index(
            name = "IDX_UP_PORTLET_LAYOUT_INTRVL",
            columnNames = {"AGGR_INTERVAL"}
        ),
        @Index(
            name = "IDX_UP_PORTLET_LAYOUT_GRP",
            columnNames = {"AGGR_GROUP_ID"}
        )
    }
)
@NaturalIdCache(
    region = "org.apereo.portal.events.aggr.portletlayout.PortletLayoutAggregationImpl-NaturalId"
)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public final class PortletLayoutAggregationImpl
        extends BaseAggregationImpl<
                PortletLayoutAggregationKey, PortletLayoutAggregationDiscriminator>
        implements PortletLayoutAggregation, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_PORTLET_LAYOUT_AGGR_GEN")
    @Column(name = "ID")
    private final long id;

    @Column(name = "ADD_COUNT", nullable = false)
    private int addCount;

    @Column(name = "DEL_COUNT", nullable = false)
    private int delCount;

    @Column(name = "MOVE_COUNT", nullable = false)
    private int moveCount;

    @NaturalId
    @ManyToOne(targetEntity = AggregatedPortletMappingImpl.class)
    @JoinColumn(name = "AGGR_PORTLET_ID", nullable = false)
    private final AggregatedPortletMapping aggregatedPortlet;

    @Column(name = "STATS_COMPLETE", nullable = false)
    private boolean complete = false;

    @Transient private PortletLayoutAggregationKey aggregationKey;
    @Transient private PortletLayoutAggregationDiscriminator aggregationDiscriminator;

    @SuppressWarnings("unused")
    private PortletLayoutAggregationImpl() {
        super();
        this.id = -1;
        this.aggregatedPortlet = null;
    }

    PortletLayoutAggregationImpl(
            TimeDimension timeDimension,
            DateDimension dateDimension,
            AggregationInterval interval,
            AggregatedGroupMapping aggregatedGroup,
            AggregatedPortletMapping aggregatedPortlet) {
        super(timeDimension, dateDimension, interval, aggregatedGroup);

        Validate.notNull(aggregatedPortlet);

        this.id = -1;
        this.aggregatedPortlet = aggregatedPortlet;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public int getAddCount() {
        return this.addCount;
    }

    @Override
    public int getDeleteCount() {
        return this.delCount;
    }

    @Override
    public int getMoveCount() {
        return this.moveCount;
    }

    @Override
    public AggregatedPortletMapping getPortletMapping() {
        return this.aggregatedPortlet;
    }

    @Override
    public PortletLayoutAggregationKey getAggregationKey() {
        PortletLayoutAggregationKey key = this.aggregationKey;
        if (key == null) {
            key = new PortletLayoutAggregationKeyImpl(this);
            this.aggregationKey = key;
        }
        return key;
    }

    @Override
    public PortletLayoutAggregationDiscriminator getAggregationDiscriminator() {
        PortletLayoutAggregationDiscriminator discriminator = this.aggregationDiscriminator;
        if (discriminator == null) {
            discriminator = new PortletLayoutAggregationDiscriminatorImpl(this);
            this.aggregationDiscriminator = discriminator;
        }
        return discriminator;
    }

    @Override
    protected boolean isComplete() {
        return this.complete
                && ((this.addCount > 0) || (this.delCount > 0) || (this.moveCount > 0));
    }

    @Override
    protected void completeInterval() {
        this.complete = true;
    }

    void countPortletAdd() {
        this.addCount++;
    }

    void countPortletDelete() {
        this.delCount++;
    }

    void countPortletMove() {
        this.moveCount++;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((aggregatedPortlet == null) ? 0 : aggregatedPortlet.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (!(obj instanceof PortletLayoutAggregation)) return false;
        PortletLayoutAggregation other = (PortletLayoutAggregation) obj;
        if (aggregatedPortlet == null) {
            if (other.getPortletMapping() != null) return false;
        } else if (!aggregatedPortlet.equals(other.getPortletMapping())) return false;
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "[aggregatedPortlet="
                + aggregatedPortlet
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
