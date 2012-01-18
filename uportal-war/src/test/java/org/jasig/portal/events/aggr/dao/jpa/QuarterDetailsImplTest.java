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

package org.jasig.portal.events.aggr.dao.jpa;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.SortedSet;

import org.joda.time.DateTime;
import org.joda.time.MonthDay;
import org.junit.Test;

import com.google.common.collect.ImmutableSortedSet;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class QuarterDetailsImplTest {
    
    @Test
    public void testOrder() {
        SortedSet<QuarterDetailsImpl> quarters = createStandardQuarters();
        verifyOrder(quarters);
        
        quarters = createOffsetQuarters();
        verifyOrder(quarters);
    }

    protected void verifyOrder(SortedSet<QuarterDetailsImpl> quarters) {
        final Iterator<QuarterDetailsImpl> itr = quarters.iterator();
        assertEquals(0, itr.next().getQuarterId());
        assertEquals(1, itr.next().getQuarterId());
        assertEquals(2, itr.next().getQuarterId());
        assertEquals(3, itr.next().getQuarterId());
    }
    
    @Test
    public void testContains() {
        SortedSet<QuarterDetailsImpl> quarters = createStandardQuarters();
        
        verifyContains(new DateTime(2012, 1, 1, 0, 0),              quarters, true,  false, false, false);
        verifyContains(new DateTime(2012, 2, 29, 15, 48),           quarters, true,  false, false, false);
        verifyContains(new DateTime(2012, 4, 29, 15, 48),           quarters, false, true,  false, false);
        verifyContains(new DateTime(2012, 8, 29, 15, 48),           quarters, false, false, true,  false);
        verifyContains(new DateTime(2012, 12, 31, 23, 59, 59, 999), quarters, false, false, false, true);
        
        
        quarters = createOffsetQuarters();
        
        verifyContains(new DateTime(2012, 1, 1, 0, 0),              quarters, false, false, true,  false);
        verifyContains(new DateTime(2012, 2, 29, 15, 48),           quarters, false, false, false, true);
        verifyContains(new DateTime(2012, 4, 29, 15, 48),           quarters, false, false, false, true);
        verifyContains(new DateTime(2012, 8, 29, 15, 48),           quarters, false, true,  false, false);
        verifyContains(new DateTime(2012, 12, 31, 23, 59, 59, 999), quarters, false, false, true,  false);
    }
    protected void verifyContains(DateTime dt, SortedSet<QuarterDetailsImpl> quarters, boolean... contains) {
        final Iterator<QuarterDetailsImpl> itr = quarters.iterator();
        for (int q = 0; q < 4; q++) {
            final QuarterDetailsImpl quarter = itr.next();
            assertEquals(dt + " is not between " + quarter.getStart() + " and " + quarter.getEnd(), contains[q], quarter.contains(dt));
        }
    }

    protected SortedSet<QuarterDetailsImpl> createStandardQuarters() {
        final SortedSet<QuarterDetailsImpl> quarters = ImmutableSortedSet.of(
                new QuarterDetailsImpl(new MonthDay(1, 1), new MonthDay(4, 1), 0),
                new QuarterDetailsImpl(new MonthDay(7, 1), new MonthDay(10, 1), 2),
                new QuarterDetailsImpl(new MonthDay(4, 1), new MonthDay(7, 1), 1),
                new QuarterDetailsImpl(new MonthDay(10, 1), new MonthDay(1, 1), 3)
                );
        return quarters;
    }

    protected SortedSet<QuarterDetailsImpl> createOffsetQuarters() {
        final SortedSet<QuarterDetailsImpl> quarters = ImmutableSortedSet.of(
                new QuarterDetailsImpl(new MonthDay(5, 1), new MonthDay(8, 1), 0),
                new QuarterDetailsImpl(new MonthDay(11, 1), new MonthDay(2, 1), 2),
                new QuarterDetailsImpl(new MonthDay(8, 1), new MonthDay(11, 1), 1),
                new QuarterDetailsImpl(new MonthDay(2, 1), new MonthDay(5, 1), 3)
                );
        return quarters;
    }
}
