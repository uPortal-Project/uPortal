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

import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.BaseAggregationKeyImpl;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletMapping;

/**
 * Basic impl of {@link PortletExecutionAggregationKey}
 *
 */
public final class PortletExecutionAggregationKeyImpl extends BaseAggregationKeyImpl
        implements PortletExecutionAggregationKey {
    private static final long serialVersionUID = 1L;

    private final AggregatedPortletMapping portletMapping;
    private final ExecutionType executionType;
    private int hashCode = 0;

    public PortletExecutionAggregationKeyImpl(PortletExecutionAggregation baseAggregation) {
        super(baseAggregation);
        this.portletMapping = baseAggregation.getPortletMapping();
        this.executionType = baseAggregation.getExecutionType();
    }

    public PortletExecutionAggregationKeyImpl(
            AggregationInterval aggregationInterval,
            AggregatedGroupMapping aggregatedGroupMapping,
            AggregatedPortletMapping portletMapping,
            ExecutionType executionType) {
        super(aggregationInterval, aggregatedGroupMapping);
        this.portletMapping = portletMapping;
        this.executionType = executionType;
    }

    public PortletExecutionAggregationKeyImpl(
            DateDimension dateDimension,
            TimeDimension timeDimension,
            AggregationInterval aggregationInterval,
            AggregatedGroupMapping aggregatedGroupMapping,
            AggregatedPortletMapping portletMapping,
            ExecutionType executionType) {
        super(dateDimension, timeDimension, aggregationInterval, aggregatedGroupMapping);
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
        if (!(obj instanceof PortletExecutionAggregationKey)) return false;
        PortletExecutionAggregationKey other = (PortletExecutionAggregationKey) obj;
        if (executionType != other.getExecutionType()) return false;
        if (portletMapping == null) {
            if (other.getPortletMapping() != null) return false;
        } else if (!portletMapping.equals(other.getPortletMapping())) return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletExecutionAggregationKey [dateDimension="
                + getDateDimension()
                + ", timeDimension="
                + getTimeDimension()
                + ", interval="
                + getInterval()
                + ", aggregatedGroup="
                + getAggregatedGroup()
                + ", executionType="
                + executionType
                + ", portletMapping="
                + portletMapping
                + "]";
    }
}
