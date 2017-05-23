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

import org.joda.time.DateMidnight;
import org.joda.time.MonthDay;
import org.joda.time.ReadableInstant;

/**
 * Details about a quarter of the year. The first quarter will have ID 0, the second ID 1, etc...
 *
 */
public interface QuarterDetail extends DateRange<MonthDay>, Comparable<QuarterDetail> {

    /**
     * @return Start of the range, inclusive. Resolved into year/month/day based on the specified
     *     instant
     */
    DateMidnight getStartDateMidnight(ReadableInstant instant);

    /**
     * @return End of the range, exclusive. Resolved into year/month/day based on the specified
     *     instant
     */
    DateMidnight getEndDateMidnight(ReadableInstant instant);

    /** @return The id of the quarter (0 - 3) */
    int getQuarterId();

    /** Compare to another {@link QuarterDetail}, must sort by {@link #getQuarterId()} */
    @Override
    int compareTo(QuarterDetail o);
}
