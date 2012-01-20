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
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jasig.portal.events.aggr.EventDateTimeUtils;
import org.joda.time.DateMidnight;
import org.junit.Test;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EventDateTimeUtilsTest {
    @Test
    public void testFindAcademicTerms() {
        final AcademicTermDetailImpl spring2012 = new AcademicTermDetailImpl(new DateMidnight(2012, 1, 1), new DateMidnight(2012, 6, 1), "Spring 2012");
        final AcademicTermDetailImpl summer2012 = new AcademicTermDetailImpl(new DateMidnight(2012, 6, 1), new DateMidnight(2012, 9, 1), "Summer 2012");
        final AcademicTermDetailImpl fall2012 = new AcademicTermDetailImpl(new DateMidnight(2012, 9, 1), new DateMidnight(2013, 1, 1), "Fall 2012");
        
        List<AcademicTermDetailImpl> terms = Arrays.asList(
                spring2012,
                summer2012,
                fall2012
        );
        
        
        AcademicTermDetailImpl result;
        Collections.sort(terms);
        
        result = EventDateTimeUtils.findDateRangeSorted(new DateMidnight(2011, 3, 1), terms);
        assertNull(result);
        
        result = EventDateTimeUtils.findDateRangeSorted(new DateMidnight(2012, 3, 1), terms);
        assertEquals(spring2012, result);
        
        result = EventDateTimeUtils.findDateRangeSorted(new DateMidnight(2012, 7, 1), terms);
        assertEquals(summer2012, result);
        
        result = EventDateTimeUtils.findDateRangeSorted(new DateMidnight(2012, 12, 31), terms);
        assertEquals(fall2012, result);
    }
}
