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
package org.apereo.portal.events.aggr.action;

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
import javax.persistence.Transient;
import org.apache.commons.lang.Validate;
import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.BaseAggregationImpl;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

@Entity
@Table(name = "UP_SEARCH_REQ_AGGR")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(
    name = "UP_SEARCH_REQ_AGGR_GEN",
    sequenceName = "UP_SEARCH_REQ_AGGR_SEQ",
    allocationSize = 1000
)
@TableGenerator(
    name = "UP_SEARCH_REQ_AGGR_GEN",
    pkColumnValue = "UP_SEARCH_REQ_AGGR_PROP",
    allocationSize = 1000
)
@org.hibernate.annotations.Table(
    appliesTo = "UP_SEARCH_REQ_AGGR",
    indexes = {
        @Index(
            name = "IDX_UP_SEARCH_REQ_AGGR_DTI",
            columnNames = {"DATE_DIMENSION_ID", "TIME_DIMENSION_ID", "AGGR_INTERVAL"}
        ),
        @Index(
            name = "IDX_UP_SEARCH_REQ_INTRVL",
            columnNames = {"AGGR_INTERVAL"}
        ),
        @Index(
            name = "IDX_UP_SEARCH_REQ_GRP",
            columnNames = {"AGGR_GROUP_ID"}
        ),
        @Index(
            name = "IDX_UP_SEARCH_REQ_TRM",
            columnNames = {"SEARCH_TERM"}
        )
    }
)
@NaturalIdCache(
    region = "org.apereo.portal.events.aggr.action.SearchRequestAggregationImpl-NaturalId"
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SearchRequestAggregationImpl
        extends BaseAggregationImpl<
                SearchRequestAggregationKey, SearchRequestAggregationDiscriminator>
        implements SearchRequestAggregation, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_SEARCH_REQ_AGGR_GEN")
    @Column(name = "ID")
    private final long id;

    @NaturalId
    @Column(name = "SEARCH_TERM", nullable = false)
    private String searchTerm;

    @Column(name = "SEARCH_COUNT", nullable = false)
    private int count;

    @Column(name = "STATS_COMPLETE", nullable = false)
    private boolean complete = false;

    @Transient private SearchRequestAggregationKey aggregationKey;
    @Transient private SearchRequestAggregationDiscriminator aggregationDiscriminator;

    @SuppressWarnings("unused")
    private SearchRequestAggregationImpl() {
        super();
        this.id = -1;
        this.searchTerm = null;
    }

    SearchRequestAggregationImpl(
            TimeDimension timeDimension,
            DateDimension dateDimension,
            AggregationInterval interval,
            AggregatedGroupMapping aggregatedGroup,
            String searchTerm) {
        super(timeDimension, dateDimension, interval, aggregatedGroup);

        Validate.notNull(searchTerm);

        this.id = -1;
        this.searchTerm = SearchRequestAggregationUtil.normalizeSearchTerm(searchTerm);
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public int getCount() {
        return this.count;
    }

    @Override
    public String getSearchTerm() {
        return this.searchTerm;
    }

    @Override
    public SearchRequestAggregationKey getAggregationKey() {
        SearchRequestAggregationKey key = this.aggregationKey;
        if (key == null) {
            key = new SearchRequestAggregationKeyImpl(this);
            this.aggregationKey = key;
        }
        return key;
    }

    @Override
    public SearchRequestAggregationDiscriminator getAggregationDiscriminator() {
        SearchRequestAggregationDiscriminator discriminator = this.aggregationDiscriminator;
        if (discriminator == null) {
            discriminator = new SearchRequestAggregationDiscriminatorImpl(this);
            this.aggregationDiscriminator = discriminator;
        }
        return discriminator;
    }

    @Override
    protected boolean isComplete() {
        return this.complete && this.count > 0;
    }

    @Override
    protected void completeInterval() {
        this.complete = true;
    }

    void increment() {
        this.count++;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((searchTerm == null) ? 0 : searchTerm.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (!(obj instanceof SearchRequestAggregation)) return false;
        SearchRequestAggregation other = (SearchRequestAggregation) obj;
        if (searchTerm == null) {
            if (other.getSearchTerm() != null) return false;
        } else if (!searchTerm.equals(other.getSearchTerm())) return false;
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "[searchTerm="
                + searchTerm
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
