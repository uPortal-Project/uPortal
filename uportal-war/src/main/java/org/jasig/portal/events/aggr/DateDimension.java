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

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeFieldType;


/**
 * Describes a dimension in time (hours). Each object represents one minute in a 24 period
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface DateDimension {
    /**
     * @return The ID of the dimension
     */
    long getId();
    
    /**
     * @return The fully built date for this date dimension
     */
    DateMidnight getDate();
    
    /**
     * @return The year the day exists in 
     * @see DateTimeFieldType#year()
     */
    int getYear();

    /**
     * Will only return values between 0 and 3 inclusive
     * 
     * @return The quarter of the year the day exists in 
     */
    int getQuarter();
    
    /**
     * Will only return values between 0 and 11 inclusive
     * 
     * @return The month of the year the day exists in 
     * @see DateTimeFieldType#monthOfYear()
     */
    int getMonth();
    
    /**
     * @return The week of the year the day exists in 
     * @see DateTimeFieldType#weekOfWeekyear()
     */
    int getWeek();
    
    /**
     * Will only return values between 0 and 31 inclusive
     * 
     * @return Day in the month
     * @see DateTimeFieldType#dayOfMonth()
     */
    int getDay();
    
    /**
     * @return The optional designation of the current term the day exists in, may be null
     */
    String getTerm();
}
