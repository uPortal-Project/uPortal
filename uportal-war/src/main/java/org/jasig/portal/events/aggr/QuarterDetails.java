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

import org.joda.time.MonthDay;
import org.joda.time.ReadableInstant;

/**
 * Details about a quarter of the year. The first quarter will have ID 0, the second ID 1, etc...
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface QuarterDetails extends Comparable<QuarterDetails> {
    /**
     * @return The id of the quarter (0 - 3)
     */
    int getQuarterId();
    
    /**
     * @return Start of the quarter, inclusive
     */
    MonthDay getStart();
    
    /**
     * @return End of the quarter, exclusive
     */
    MonthDay getEnd();
    
    /**
     * @return true If the specified instant is within the date range for the quarter
     */
    boolean contains(ReadableInstant instant);
    
    /**
     * Compare to another {@link QuarterDetails}, must sort by {@link #getQuarterId()}
     */
    @Override
    int compareTo(QuarterDetails o);
}
