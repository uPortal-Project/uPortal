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

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jasig.portal.events.aggr.dao.jpa.QuarterDetailsImpl;
import org.joda.time.MonthDay;

import com.google.common.collect.ImmutableSortedSet;

/**
 * Utilities for working with the various date/time data types involved in event aggregation
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class EventDateTimeUtils {
    private EventDateTimeUtils() {
    }
    
    public static SortedSet<QuarterDetails> validateQuarters(Collection<QuarterDetails> quarters) {
        final SortedSet<QuarterDetails> sortedQuarters = new TreeSet<QuarterDetails>(quarters);
        if (sortedQuarters.size() != 4) {
            throw new IllegalArgumentException("Exactly 4 QuarterDetails must be set: " + sortedQuarters);
        }
        
        MonthDay previousEnd = sortedQuarters.last().getEnd();
        final Iterator<QuarterDetails> itr = sortedQuarters.iterator();
        for (int i = 0; i < 4; i++) {
            final QuarterDetails q = itr.next();
            if (i != q.getQuarterId()) {
                throw new IllegalArgumentException("Quarter " + i + " has an illegal id of " + q.getQuarterId());
            }
            
            if (!q.getStart().equals(previousEnd)) {
                throw new IllegalArgumentException("Quarter " + i + " start date of " + q.getStart() + " is not adjacent to previous quarter's end date of " + previousEnd);
            }
            previousEnd = q.getEnd();
        }
        
        return sortedQuarters;
    }
    
    public static SortedSet<QuarterDetails> createStandardQuarters() {
        return ImmutableSortedSet.<QuarterDetails>of(
                new QuarterDetailsImpl(new MonthDay(1, 1), new MonthDay(4, 1), 0),
                new QuarterDetailsImpl(new MonthDay(7, 1), new MonthDay(10, 1), 2),
                new QuarterDetailsImpl(new MonthDay(4, 1), new MonthDay(7, 1), 1),
                new QuarterDetailsImpl(new MonthDay(10, 1), new MonthDay(1, 1), 3)
                );
    }
}
