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
package org.jasig.portal.events.aggr.portletlayout;

import org.jasig.portal.events.aggr.BaseGroupedAggregationDiscriminatorImpl;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.jasig.portal.utils.ComparableExtractingComparator;

/**
 * Basic impl of {@link PortletLayoutAggregationDiscriminator}
 *
 * @author Chris Waymire <cwaymire@unicon.net>
 */
public class PortletLayoutAggregationDiscriminatorImpl extends BaseGroupedAggregationDiscriminatorImpl
        implements PortletLayoutAggregationDiscriminator {
    private static final long serialVersionUID = 1L;

    private final AggregatedPortletMapping portletMapping;
    private int hashCode = 0;

    public PortletLayoutAggregationDiscriminatorImpl(PortletLayoutAggregation aggregationData) {
        super(aggregationData.getAggregatedGroup());
        portletMapping = aggregationData.getPortletMapping();
    }

    public PortletLayoutAggregationDiscriminatorImpl(AggregatedGroupMapping aggregatedGroupMapping, AggregatedPortletMapping portletMapping) {
        super(aggregatedGroupMapping);
        this.portletMapping = portletMapping;
    }

    @Override
    public final AggregatedPortletMapping getPortletMapping() {
        return this.portletMapping;
    }

    @Override
    public int hashCode() {
        int h = this.hashCode;
        if (h == 0) {
            final int prime = 31;
            h = super.hashCode();
            h = prime * h + ((portletMapping == null) ? 0 : portletMapping.hashCode());
            this.hashCode = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PortletLayoutAggregationDiscriminator other = (PortletLayoutAggregationDiscriminator) obj;
        if (portletMapping == null) {
            if (other.getPortletMapping() != null)
                return false;
        }
        else if (!portletMapping.equals(other.getPortletMapping()))
            return false;
        return true;
    }

    // Compare discriminators based on the group name and tab information
    public static class Comparator extends
            ComparableExtractingComparator<PortletLayoutAggregationDiscriminator, String> {

        public static Comparator INSTANCE = new Comparator();

        @Override
        protected String getComparable(PortletLayoutAggregationDiscriminator o) {
            return o.getAggregatedGroup().getGroupName() + o.getPortletMapping().getFname();
        }

    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + super.toString() + ", portletMapping=" + portletMapping + "]";
    }
}
