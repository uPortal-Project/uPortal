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

import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.BaseAggregationKeyImpl;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;

public class SearchRequestAggregationKeyImpl extends BaseAggregationKeyImpl
        implements SearchRequestAggregationKey {
    private static final long serialVersionUID = 1L;

    private final String searchTerm;
    private int hashCode = 0;

    public SearchRequestAggregationKeyImpl(SearchRequestAggregation baseAggregation) {
        super(baseAggregation);
        this.searchTerm = baseAggregation.getSearchTerm();
    }

    public SearchRequestAggregationKeyImpl(
            AggregationInterval aggregationInterval,
            AggregatedGroupMapping aggregatedGroupMapping,
            String searchTerm) {
        super(aggregationInterval, aggregatedGroupMapping);
        this.searchTerm = SearchRequestAggregationUtil.normalizeSearchTerm(searchTerm);
    }

    public SearchRequestAggregationKeyImpl(
            DateDimension dateDimension,
            TimeDimension timeDimension,
            AggregationInterval aggregationInterval,
            AggregatedGroupMapping aggregatedGroupMapping,
            String searchTerm) {
        super(dateDimension, timeDimension, aggregationInterval, aggregatedGroupMapping);
        this.searchTerm = SearchRequestAggregationUtil.normalizeSearchTerm(searchTerm);
    }

    @Override
    public final String getSearchTerm() {
        return this.searchTerm;
    }

    @Override
    public int hashCode() {
        int h = this.hashCode;
        if (h == 0) {
            final int prime = 31;
            h = super.hashCode();
            h = prime * h + ((searchTerm == null) ? 0 : searchTerm.hashCode());
            this.hashCode = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!super.equals(obj)) return false;

        if (getClass() != obj.getClass()) return false;

        SearchRequestAggregationKey other = (SearchRequestAggregationKey) obj;
        if (searchTerm == null) {
            return false;
        }

        if (!searchTerm.equals(other.getSearchTerm())) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + " [dateDimension="
                + getDateDimension()
                + ", timeDimension="
                + getTimeDimension()
                + ", interval="
                + getInterval()
                + ", aggregatedGroup="
                + getAggregatedGroup()
                + ", searchTerm="
                + searchTerm
                + "]";
    }
}
