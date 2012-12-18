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
package org.jasig.portal.events.aggr.concuser;

import org.jasig.portal.events.aggr.BaseGroupedAggregationDiscriminatorImpl;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.utils.ComparableExtractingComparator;

public final class ConcurrentUserAggregationDiscriminatorImpl extends BaseGroupedAggregationDiscriminatorImpl
        implements ConcurrentUserAggregationDiscriminator {
    private static final long serialVersionUID = 1L;

    public ConcurrentUserAggregationDiscriminatorImpl(ConcurrentUserAggregation concurrentUserAggregation) {
        super(concurrentUserAggregation);
    }

    public ConcurrentUserAggregationDiscriminatorImpl(AggregatedGroupMapping aggregatedGroupMapping) {
        super(aggregatedGroupMapping);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ConcurrentUserAggregationDiscriminator))
            return false;
        return super.equals(obj);
    }

    // Compare discriminators based on the group name
    public static class Comparator extends
            ComparableExtractingComparator<ConcurrentUserAggregationDiscriminator, String> {

        public static Comparator INSTANCE = new Comparator();

        @Override
        protected String getComparable(ConcurrentUserAggregationDiscriminator o) {
            return o.getAggregatedGroup().getGroupName();
        }

    }

    @Override
    public String toString() {
        return "ConcurrentUserAggregationDiscriminator [" + super.toString() + "]";
    }
}
