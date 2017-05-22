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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;
import org.apache.commons.lang.Validate;
import org.apereo.portal.events.aggr.AggregatedIntervalConfig;
import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.IPortalEventAggregator;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NaturalId;

/**
 */
@Entity
@Table(name = "UP_EVENT_AGGR_CONF_INTRVL")
@SequenceGenerator(
    name = "UP_EVENT_AGGR_CONF_INTRVL_GEN",
    sequenceName = "UP_EVENT_AGGR_CONF_INTRVL_SEQ",
    allocationSize = 1
)
@TableGenerator(
    name = "UP_EVENT_AGGR_CONF_INTRVL_GEN",
    pkColumnValue = "UP_EVENT_AGGR_CONF_INTRVL",
    allocationSize = 1
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AggregatedIntervalConfigImpl
        extends BaseAggregatedDimensionConfigImpl<AggregationInterval>
        implements AggregatedIntervalConfig {
    @Id
    @GeneratedValue(generator = "UP_EVENT_AGGR_CONF_INTRVL_GEN")
    @Column(name = "ID")
    @SuppressWarnings("unused")
    private final long id;

    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion = -1;

    @NaturalId
    @Column(name = "AGGREGATOR_TYPE", nullable = false, updatable = false)
    private final Class<? extends IPortalEventAggregator> aggregatorType;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(
        name = "UP_EVENT_AGGR_CONF_INTRVL_INC",
        joinColumns = @JoinColumn(name = "UP_EVENT_AGGR_CONF_INTRVL_ID")
    )
    @Fetch(FetchMode.JOIN)
    @Enumerated(EnumType.STRING)
    @Column(name = "AGGR_INTERVAL")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private final Set<AggregationInterval> includedIntervals;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(
        name = "UP_EVENT_AGGR_CONF_INTRVL_EXC",
        joinColumns = @JoinColumn(name = "UP_EVENT_AGGR_CONF_INTRVL_ID")
    )
    @Fetch(FetchMode.JOIN)
    @Enumerated(EnumType.STRING)
    @Column(name = "AGGR_INTERVAL")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private final Set<AggregationInterval> excludedIntervals;

    @SuppressWarnings("unused")
    private AggregatedIntervalConfigImpl() {
        this.id = -1;
        this.aggregatorType = null;
        this.includedIntervals = null;
        this.excludedIntervals = null;
    }

    AggregatedIntervalConfigImpl(Class<? extends IPortalEventAggregator> aggregatorType) {
        Validate.notNull(aggregatorType);
        this.id = -1;
        this.aggregatorType = aggregatorType;
        this.includedIntervals = new LinkedHashSet<AggregationInterval>();
        this.excludedIntervals = new LinkedHashSet<AggregationInterval>();
    }

    @Override
    public long getVersion() {
        return this.entityVersion;
    }

    @Override
    public Class<? extends IPortalEventAggregator> getAggregatorType() {
        return aggregatorType;
    }

    @Override
    public Set<AggregationInterval> getIncluded() {
        return includedIntervals;
    }

    @Override
    public Set<AggregationInterval> getExcluded() {
        return excludedIntervals;
    }

    @Override
    public String toString() {
        return "AggregatedIntervalConfigImpl [aggregatorType=" + aggregatorType + "]";
    }
}
