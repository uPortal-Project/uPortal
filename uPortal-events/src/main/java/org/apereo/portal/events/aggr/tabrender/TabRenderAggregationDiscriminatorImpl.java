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

import org.apereo.portal.events.aggr.BaseGroupedAggregationDiscriminatorImpl;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.tabs.AggregatedTabMapping;
import org.apereo.portal.utils.ComparableExtractingComparator;

/**
 * Basic impl of {@link org.apereo.portal.events.aggr.tabrender.TabRenderAggregationKey}
 *
 */
public final class TabRenderAggregationDiscriminatorImpl
        extends BaseGroupedAggregationDiscriminatorImpl
        implements TabRenderAggregationDiscriminator {
    private static final long serialVersionUID = 1L;

    private final AggregatedTabMapping tabMapping;
    private int hashCode = 0;

    public TabRenderAggregationDiscriminatorImpl(TabRenderAggregation aggregationData) {
        super(aggregationData.getAggregatedGroup());
        tabMapping = aggregationData.getTabMapping();
    }

    public TabRenderAggregationDiscriminatorImpl(
            AggregatedGroupMapping aggregatedGroupMapping, AggregatedTabMapping tabMapping) {
        super(aggregatedGroupMapping);
        this.tabMapping = tabMapping;
    }

    @Override
    public final AggregatedTabMapping getTabMapping() {
        return this.tabMapping;
    }

    @Override
    public int hashCode() {
        int h = this.hashCode;
        if (h == 0) {
            final int prime = 31;
            h = super.hashCode();
            h = prime * h + ((tabMapping == null) ? 0 : tabMapping.hashCode());
            this.hashCode = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        TabRenderAggregationDiscriminator other = (TabRenderAggregationDiscriminator) obj;
        if (tabMapping == null) {
            if (other.getTabMapping() != null) return false;
        } else if (!tabMapping.equals(other.getTabMapping())) return false;
        return true;
    }

    // Compare discriminators based on the group name and tab information
    public static class Comparator
            extends ComparableExtractingComparator<TabRenderAggregationDiscriminator, String> {

        public static Comparator INSTANCE = new Comparator();

        @Override
        protected String getComparable(TabRenderAggregationDiscriminator o) {
            return o.getAggregatedGroup().getGroupName()
                    + o.getTabMapping().getTabName()
                    + o.getTabMapping().getFragmentName();
        }
    }

    @Override
    public String toString() {
        return "TabRenderAggregationKey [" + super.toString() + ", tabMapping=" + tabMapping + "]";
    }
}
