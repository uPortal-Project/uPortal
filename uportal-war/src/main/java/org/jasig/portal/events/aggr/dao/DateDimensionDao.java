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

package org.jasig.portal.events.aggr.dao;

import java.util.List;

import org.jasig.portal.events.aggr.DateDimension;
import org.joda.time.DateMidnight;

/**
 * DAO for creation/lookup of date dimensions
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface DateDimensionDao {
    
    /**
     * @return The newest (largest time in millis) date dimension in the persistent store
     */
    DateDimension getNewestDateDimension();
    
    /**
     * @return The oldest (smallest time in millis) date dimension in the persistent store
     */
    DateDimension getOldestDateDimension();

    /**
     * Create the date specified date dimension
     * @return The created dimension
     */
    DateDimension createDateDimension(DateMidnight cal, int quarter, String term);

    /**
     * @return A list of all date dimensions in the persistent store, chronological order
     */
    List<DateDimension> getDateDimensions();
    
    /**
     * @param start Start date (inclusive)
     * @param end End date (exclusive)
     * @return A list of the date dimensions that exist between the specified start and end
     */
    List<DateDimension> getDateDimensionsBetween(DateMidnight start, DateMidnight end);

    /**
     * @return The {@link DateDimension} corresponding to the specified id
     */
    DateDimension getDateDimensionById(long id);

    /**
     * Get the {@link DateDimension} for the specified year, month and day.
     */
    DateDimension getDateDimensionByDate(DateMidnight date);
}