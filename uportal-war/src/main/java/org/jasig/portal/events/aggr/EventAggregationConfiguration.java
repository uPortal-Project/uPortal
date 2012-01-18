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

package org.jasig.portal.events.aggr;

import java.util.Set;
import java.util.SortedSet;

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * Configuration of the event aggregation process.
 * <p/>
 * All returned collections are intended to be modified by reference when making
 * changes to this object.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface EventAggregationConfiguration {
    /**
     * Globally included groups, if not empty only groups listed in this set will be included
     * in aggregation. Can be overridden on a per-aggregator basis via {@link #getIncludedGroupsForAggregator(Class)}
     */
    Set<AggregatedGroupMapping> getIncludedGroups();
    
    /**
     * Globally excluded groups, groups listed in this set will be excluded from aggregation.
     * Can be overridden on a per-aggregator basis via {@link #getExcludedGroupsForAggregator(Class)}
     */
    Set<AggregatedGroupMapping> getExcludedGroups();
    
    /**
     * Included groups for a specific aggregator. If not empty only groups listed in this set will
     * included in aggregation
     */
    Set<AggregatedGroupMapping> getIncludedGroupsForAggregator(Class<? extends IPortalEventAggregator<? extends PortalEvent>> aggregatorType);
    
    /**
     * Remove aggregator specific group includes
     */
    void clearIncludedGroupsForAggregator(Class<? extends IPortalEventAggregator<? extends PortalEvent>> aggregatorType);
    
    /**
     * Excluded groups for a specific aggregator. Groups list in this set will be excluded from
     * aggregation.
     */
    Set<AggregatedGroupMapping> getExcludedGroupsForAggregator(Class<? extends IPortalEventAggregator<? extends PortalEvent>> aggregatorType);

    /**
     * Remove aggregator specific group excludes
     */
    void clearExcludedGroupsForAggregator(Class<? extends IPortalEventAggregator<? extends PortalEvent>> aggregatorType);
    
    
    /**
     * Globally excluded intervals, intervals listed in this set will be excluded from aggregation.
     * Can be overridden on a per-aggregator basis via {@link #getExcludedIntervalsForAggregators(Class)}
     */
    Set<Interval> getExcludedIntervals();
    
    /**
     * Excluded intervals for a specific aggregator. Intervals listed in this set will be excluded from aggregation.
     */
    Set<Interval> getExcludedIntervalsForAggregators(Class<? extends IPortalEventAggregator<? extends PortalEvent>> aggregatorType);
    
    /**
     * Remove aggregator specific interval excludes
     */
    void clearExcludedIntervalsForAggregators(Class<? extends IPortalEventAggregator<? extends PortalEvent>> aggregatorType);
    
    
    /**
     * The configured quarter details
     */
    SortedSet<QuarterDetails> getQuartersDetails();
    
    /**
     * The configured academic terms
     */
    SortedSet<AcademicTermDetails> getAcademicTermDetails();
}
