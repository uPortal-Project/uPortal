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
package org.apereo.portal.events.aggr;

import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * Basic impl of {@link BaseAggregationKey}
 *
 */
public abstract class BaseAggregationKeyImpl implements BaseAggregationKey {
    private static final long serialVersionUID = 1L;

    private final TimeDimension timeDimension;
    private final DateDimension dateDimension;
    private final AggregationInterval aggregationInterval;
    private final AggregatedGroupMapping aggregatedGroupMapping;

    private int hashCode = 0;

    public BaseAggregationKeyImpl(BaseAggregation<?, ?> baseAggregation) {
        this(
                baseAggregation.getDateDimension(),
                baseAggregation.getTimeDimension(),
                baseAggregation.getInterval(),
                baseAggregation.getAggregatedGroup());
    }

    public BaseAggregationKeyImpl(
            AggregationInterval aggregationInterval,
            AggregatedGroupMapping aggregatedGroupMapping) {
        this(null, null, aggregationInterval, aggregatedGroupMapping);
    }

    public BaseAggregationKeyImpl(
            DateDimension dateDimension,
            TimeDimension timeDimension,
            AggregationInterval aggregationInterval,
            AggregatedGroupMapping aggregatedGroupMapping) {
        this.timeDimension = timeDimension;
        this.dateDimension = dateDimension;
        this.aggregationInterval = aggregationInterval;
        this.aggregatedGroupMapping = aggregatedGroupMapping;
    }

    @Override
    public final TimeDimension getTimeDimension() {
        return this.timeDimension;
    }

    @Override
    public final DateDimension getDateDimension() {
        return this.dateDimension;
    }

    @Override
    public final AggregationInterval getInterval() {
        return this.aggregationInterval;
    }

    @Override
    public final AggregatedGroupMapping getAggregatedGroup() {
        return this.aggregatedGroupMapping;
    }

    @Override
    public int hashCode() {
        int h = this.hashCode;
        if (h == 0) {
            final int prime = 31;
            h = 1;
            h =
                    prime * h
                            + ((aggregatedGroupMapping == null)
                                    ? 0
                                    : aggregatedGroupMapping.hashCode());
            h = prime * h + ((aggregationInterval == null) ? 0 : aggregationInterval.hashCode());
            h = prime * h + ((dateDimension == null) ? 0 : dateDimension.hashCode());
            h = prime * h + ((timeDimension == null) ? 0 : timeDimension.hashCode());
            this.hashCode = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof BaseAggregationKey)) return false;
        if (this.hashCode() != obj.hashCode()) return false;
        BaseAggregationKey other = (BaseAggregationKey) obj;
        if (aggregatedGroupMapping == null) {
            if (other.getAggregatedGroup() != null) return false;
        } else if (!aggregatedGroupMapping.equals(other.getAggregatedGroup())) return false;
        if (aggregationInterval != other.getInterval()) return false;
        if (dateDimension == null) {
            if (other.getDateDimension() != null) return false;
        } else if (!dateDimension.equals(other.getDateDimension())) return false;
        if (timeDimension == null) {
            if (other.getTimeDimension() != null) return false;
        } else if (!timeDimension.equals(other.getTimeDimension())) return false;
        return true;
    }
}
