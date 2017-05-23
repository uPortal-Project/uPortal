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
package org.apereo.portal.events.aggr.concuser;

import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.BaseAggregationKeyImpl;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;

public final class ConcurrentUserAggregationKeyImpl extends BaseAggregationKeyImpl
        implements ConcurrentUserAggregationKey {
    private static final long serialVersionUID = 1L;

    public ConcurrentUserAggregationKeyImpl(ConcurrentUserAggregation concurrentUserAggregation) {
        super(concurrentUserAggregation);
    }

    public ConcurrentUserAggregationKeyImpl(
            AggregationInterval aggregationInterval,
            AggregatedGroupMapping aggregatedGroupMapping) {
        super(aggregationInterval, aggregatedGroupMapping);
    }

    public ConcurrentUserAggregationKeyImpl(
            DateDimension dateDimension,
            TimeDimension timeDimension,
            AggregationInterval aggregationInterval,
            AggregatedGroupMapping aggregatedGroupMapping) {
        super(dateDimension, timeDimension, aggregationInterval, aggregatedGroupMapping);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ConcurrentUserAggregationKey)) return false;
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "ConcurrentUserAggregationKey [dateDimension="
                + getDateDimension()
                + ", timeDimension="
                + getTimeDimension()
                + ", interval="
                + getInterval()
                + ", aggregatedGroup="
                + getAggregatedGroup()
                + "]";
    }
}
