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

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

/**
 * Statistics about an aggregation that includes timing info. All values are returned in nanoseconds
 *
 */
public interface TimedAggregationStatistics extends StatisticalSummary {
    /**
     * Returns the sum of the squares of the values that have been added.
     *
     * <p>Double.NaN is returned if no values have been added.
     *
     * @return The sum of squares
     */
    double getSumsq();

    /**
     * Returns the <a href="http://en.wikibooks.org/wiki/Statistics/Summary/Variance">population
     * variance</a> of the values that have been added.
     *
     * <p>Double.NaN is returned if no values have been added.
     *
     * @return the population variance
     */
    double getPopulationVariance();

    /**
     * Returns the geometric mean of the values that have been added.
     *
     * <p>Double.NaN is returned if no values have been added.
     *
     * @return the geometric mean
     */
    double getGeometricMean();

    /**
     * Returns the sum of the logs of the values that have been added.
     *
     * <p>Double.NaN is returned if no values have been added.
     *
     * @return the sum of logs
     */
    double getSumOfLogs();

    /**
     * Returns a statistic related to the Second Central Moment. Specifically, what is returned is
     * the sum of squared deviations from the sample mean among the values that have been added.
     *
     * <p>Returns <code>Double.NaN</code> if no data values have been added and returns <code>0
     * </code> if there is just one value in the data set.
     *
     * <p>
     *
     * @return second central moment statistic
     */
    double getSecondMoment();
}
