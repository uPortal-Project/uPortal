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
package org.apereo.portal.events.aggr.portletexec;

import org.apereo.portal.events.aggr.BaseGroupedAggregationDiscriminatorImpl;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.portletexec.PortletExecutionAggregationKey.ExecutionType;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.apereo.portal.utils.ComparableExtractingComparator;

/**
 * Basic impl of {@link
 * org.apereo.portal.events.aggr.portletexec.PortletExecutionAggregationDiscriminator}
 *
 */
public final class PortletExecutionAggregationDiscriminatorImpl
        extends BaseGroupedAggregationDiscriminatorImpl
        implements PortletExecutionAggregationDiscriminator {
    private static final long serialVersionUID = 1L;

    private final AggregatedPortletMapping portletMapping;
    private final ExecutionType executionType;
    private int hashCode = 0;

    public PortletExecutionAggregationDiscriminatorImpl(
            PortletExecutionAggregation baseAggregation) {
        super(baseAggregation);
        this.portletMapping = baseAggregation.getPortletMapping();
        this.executionType = baseAggregation.getExecutionType();
    }

    public PortletExecutionAggregationDiscriminatorImpl(
            AggregatedGroupMapping aggregatedGroupMapping,
            AggregatedPortletMapping portletMapping,
            PortletExecutionAggregationKey.ExecutionType executionType) {
        super(aggregatedGroupMapping);
        this.portletMapping = portletMapping;
        this.executionType = executionType;
    }

    @Override
    public final AggregatedPortletMapping getPortletMapping() {
        return this.portletMapping;
    }

    @Override
    public final ExecutionType getExecutionType() {
        return this.executionType;
    }

    @Override
    public int hashCode() {
        int h = this.hashCode;
        if (h == 0) {
            final int prime = 31;
            h = super.hashCode();
            h = prime * h + ((executionType == null) ? 0 : executionType.hashCode());
            h = prime * h + ((portletMapping == null) ? 0 : portletMapping.hashCode());
            this.hashCode = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (!(obj instanceof PortletExecutionAggregationDiscriminator)) return false;
        PortletExecutionAggregationDiscriminator other =
                (PortletExecutionAggregationDiscriminator) obj;
        if (executionType != other.getExecutionType()) return false;
        if (portletMapping == null) {
            if (other.getPortletMapping() != null) return false;
        } else if (!portletMapping.equals(other.getPortletMapping())) return false;
        return true;
    }

    // Compare discriminators based on the group name and tab information
    public static class Comparator
            extends ComparableExtractingComparator<
                    PortletExecutionAggregationDiscriminator, String> {

        public static Comparator INSTANCE = new Comparator();

        @Override
        protected String getComparable(PortletExecutionAggregationDiscriminator o) {
            return o.getAggregatedGroup().getGroupName()
                    + o.getPortletMapping().getFname()
                    + o.getExecutionType().name();
        }
    }

    @Override
    public String toString() {
        return "PortletExecutionAggregationDiscriminator ["
                + super.toString()
                + ", executionType="
                + executionType
                + ", portletMapping="
                + portletMapping
                + "]";
    }
}
