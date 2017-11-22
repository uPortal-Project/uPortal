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

import java.util.Collection;
import org.joda.time.DateTime;

/**
 * Extension of the {@link BaseAggregationDao} that provides create/update operations
 *
 * @param <T> Aggregation type
 * @param <K> The key type for the aggregation
 */
public interface BaseAggregationPrivateDao<
                T extends BaseAggregationImpl<K, ?>, K extends BaseAggregationKey>
        extends BaseAggregationDao<T, K> {

    /**
     * Aggregations that have not been closed for an interval and occur in the date range
     *
     * @param start the start {@link DateTime} of the range, inclusive
     * @param end the end {@link DateTime} of the range, exclusive
     * @param interval the interval to get aggregations for
     */
    Collection<T> getUnclosedAggregations(
            DateTime start, DateTime end, AggregationInterval interval);

    /** Create a new aggregation for the specified key */
    T createAggregation(K key);

    /** @param aggregation The aggregation to update */
    void updateAggregation(T aggregation);

    /** @param aggregations The aggregations to update */
    void updateAggregations(Iterable<T> aggregations, boolean removeFromCache);
}
