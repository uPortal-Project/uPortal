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
import org.apereo.portal.utils.ComparableExtractingComparator;

/**
 * Basic impl of {@link BaseGroupedAggregationDiscriminator}
 *
 */
public class BaseGroupedAggregationDiscriminatorImpl
        implements BaseGroupedAggregationDiscriminator {
    private static final long serialVersionUID = 1L;

    private final AggregatedGroupMapping aggregatedGroupMapping;

    private int hashCode = 0;

    public BaseGroupedAggregationDiscriminatorImpl(BaseAggregation aggregationData) {
        this(aggregationData.getAggregatedGroup());
    }

    public BaseGroupedAggregationDiscriminatorImpl(AggregatedGroupMapping aggregatedGroupMapping) {
        this.aggregatedGroupMapping = aggregatedGroupMapping;
    }

    @Override
    public final AggregatedGroupMapping getAggregatedGroup() {
        return this.aggregatedGroupMapping;
    }

    @Override
    public int hashCode() {
        int h = this.hashCode;
        if (h == 0) {
            h = aggregatedGroupMapping == null ? 1 : aggregatedGroupMapping.hashCode();
            this.hashCode = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof BaseGroupedAggregationDiscriminator)) return false;
        if (this.hashCode() != obj.hashCode()) return false;
        BaseAggregationKey other = (BaseAggregationKey) obj;
        if (aggregatedGroupMapping == null) {
            if (other.getAggregatedGroup() != null) return false;
        } else if (!aggregatedGroupMapping.equals(other.getAggregatedGroup())) return false;
        return true;
    }

    // Compare discriminators based on the group name
    public static class Comparator
            extends ComparableExtractingComparator<BaseGroupedAggregationDiscriminator, String> {

        public static Comparator INSTANCE = new Comparator();

        @Override
        protected String getComparable(BaseGroupedAggregationDiscriminator o) {
            return o.getAggregatedGroup().getGroupName();
        }
    }
}
