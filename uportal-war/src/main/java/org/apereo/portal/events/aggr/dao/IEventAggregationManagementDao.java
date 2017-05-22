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
package org.apereo.portal.events.aggr.dao;

import java.util.List;
import java.util.Set;
import org.apereo.portal.events.aggr.AcademicTermDetail;
import org.apereo.portal.events.aggr.AggregatedGroupConfig;
import org.apereo.portal.events.aggr.AggregatedIntervalConfig;
import org.apereo.portal.events.aggr.IEventAggregatorStatus;
import org.apereo.portal.events.aggr.IPortalEventAggregator;
import org.apereo.portal.events.aggr.QuarterDetail;

/**
 * Operations central to the management of portal event aggregation
 *
 */
public interface IEventAggregationManagementDao {
    /** Get the aggregation status for the specified processing type */
    IEventAggregatorStatus getEventAggregatorStatus(
            IEventAggregatorStatus.ProcessingType processingType, boolean create);

    /** Update changes to the aggregation status object */
    void updateEventAggregatorStatus(IEventAggregatorStatus eventAggregatorStatus);

    /** All aggregated group configurations */
    Set<AggregatedGroupConfig> getAggregatedGroupConfigs();

    /**
     * Default group includes/excludes, applied to all aggregators that do not have specific
     * configurations.
     */
    AggregatedGroupConfig getDefaultAggregatedGroupConfig();

    /** Get the aggregated group configuration for the specified aggregator */
    AggregatedGroupConfig getAggregatedGroupConfig(
            Class<? extends IPortalEventAggregator> aggregatorType);

    /** Create config for specified aggregator */
    AggregatedGroupConfig createAggregatedGroupConfig(
            Class<? extends IPortalEventAggregator> aggregatorType);

    /** Store changes made to the specified config */
    void updateAggregatedGroupConfig(AggregatedGroupConfig aggregatedGroupConfig);

    /** Delete the specified configuration */
    void deleteAggregatedGroupConfig(AggregatedGroupConfig aggregatedGroupConfig);

    /** @return All aggregated interval configurations */
    Set<AggregatedIntervalConfig> getAggregatedIntervalConfigs();

    /**
     * Default interval includes/excludes, applied to all aggregators that do not have specific
     * configurations.
     */
    AggregatedIntervalConfig getDefaultAggregatedIntervalConfig();

    /** Get the aggregated interval configuration for the specified aggregator */
    AggregatedIntervalConfig getAggregatedIntervalConfig(
            Class<? extends IPortalEventAggregator> aggregatorType);

    /** Create config for specified aggregator */
    AggregatedIntervalConfig createAggregatedIntervalConfig(
            Class<? extends IPortalEventAggregator> aggregatorType);

    /** Store changes made to the specified config */
    void updateAggregatedIntervalConfig(AggregatedIntervalConfig aggregatedIntervalConfig);

    /** Delete the specified configuration */
    void deleteAggregatedIntervalConfig(AggregatedIntervalConfig aggregatedIntervalConfig);

    /** An immutable list of the configured quarter details sorted by its natural ordering */
    List<QuarterDetail> getQuartersDetails();

    /**
     * Update the the quarter list to match the specified list
     *
     * @param quarterDetail Must contain four sequential quarters with no gaps between dates
     */
    void setQuarterDetails(List<QuarterDetail> quarterDetail);

    /**
     * An immutable list of the currently configured academic terms sorted by its natural ordering
     */
    List<AcademicTermDetail> getAcademicTermDetails();

    /**
     * Update the academic term list to match the specified list
     *
     * @param academicTermDetails Must contain a list on non-overlapping terms
     */
    void setAcademicTermDetails(List<AcademicTermDetail> academicTermDetails);
}
