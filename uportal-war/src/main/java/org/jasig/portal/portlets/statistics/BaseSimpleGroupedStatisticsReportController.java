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
package org.jasig.portal.portlets.statistics;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jasig.portal.events.aggr.BaseAggregation;
import org.jasig.portal.events.aggr.BaseAggregationDateTimeComparator;
import org.jasig.portal.events.aggr.BaseAggregationKey;
import org.jasig.portal.events.aggr.BaseGroupedAggregationDiscriminator;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * Extension of {@link BaseStatisticsReportController} for reports that only need to differentiate columns
 * by group
 * 
 * @author Eric Dalquist
 */
public abstract class BaseSimpleGroupedStatisticsReportController<
            T extends BaseAggregation<K, D>, 
            K extends BaseAggregationKey,
            D extends BaseGroupedAggregationDiscriminator, 
            F extends BaseReportForm>
        extends BaseStatisticsReportController<T, K, D, F> {

    /**
     * Default implementation to create a map of the report column discriminators based on the submitted form to
     * collate the aggregation data into each column of a report when the only grouping parameter is
     * AggregatedGroupMapping.
     *
     * The map entries are a time-ordered sorted set of aggregation data points.
     *
     * @param form Form submitted by the user
     * @return Map of report column discriminators to sorted set of time-based aggregation data
     */
    protected Map<D, SortedSet<T>>
            getDefaultGroupedColumnDiscriminatorMap (F form){
        List<Long> groups = form.getGroups();
        //Collections used to track the queried groups and the results
        final Map<D, SortedSet<T>> groupedAggregations =
                new TreeMap<D, SortedSet<T>>((Comparator<? super D>) getDiscriminatorComparator());

        //Get concrete group mapping objects that are being queried for
        for (final Long queryGroupId : groups) {
            final D groupMapping = createGroupedDiscriminatorInstance(this.aggregatedGroupDao.getGroupMapping(queryGroupId));

            //Create the set the aggregations for this report column will be stored in, sorted chronologically
            final SortedSet<T> aggregations = new TreeSet<T>(BaseAggregationDateTimeComparator.INSTANCE);

            //Map the group to the set
            groupedAggregations.put(groupMapping, aggregations);
        }

        return groupedAggregations;
    }

    /**
     * Creates an instance of a BaseGroupedAggregationDiscriminator or any descendants that only
     * require an AggregatedGroupMapping (e.g. won't work for TabRender or other reports that have
     * item other than group (user group) being queried on.
     * @param groupMapping group mapping
     * @return Fully populated child instance of BaseGroupedAggregationDiscriminator
     */
    protected abstract D createGroupedDiscriminatorInstance(AggregatedGroupMapping groupMapping);

}
