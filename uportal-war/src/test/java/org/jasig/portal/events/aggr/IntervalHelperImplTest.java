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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Calendar;
import java.util.Date;

import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class IntervalHelperImplTest {
    @InjectMocks private IntervalHelperImpl helperImpl = new IntervalHelperImpl();
    @Mock private DateDimensionDao dateDimensionDao;
    @Mock private TimeDimensionDao timeDimensionDao;
    
    @Test
    public void testGetIntervalInfo() {
        final Calendar instant = Calendar.getInstance();
        instant.setTimeInMillis(1325881376117l);
        
        Calendar start, end;
        
        
        // TEST YEAR
        
        start = this.helperImpl.determineStart(Interval.YEAR, instant);
        assertEquals(2012, start.get(Calendar.YEAR));
        assertEquals(0, start.get(Calendar.MONTH));
        assertEquals(1, start.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, start.get(Calendar.WEEK_OF_YEAR));
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, start.get(Calendar.MINUTE));
        assertEquals(0, start.get(Calendar.SECOND));
        assertEquals(0, start.get(Calendar.MILLISECOND));
        
        end = this.helperImpl.determineEnd(Interval.YEAR, start);
        assertEquals(2013, end.get(Calendar.YEAR));
        assertEquals(0, end.get(Calendar.MONTH));
        assertEquals(1, end.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, end.get(Calendar.WEEK_OF_YEAR));
        assertEquals(0, end.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, end.get(Calendar.MINUTE));
        assertEquals(0, end.get(Calendar.SECOND));
        assertEquals(0, end.get(Calendar.MILLISECOND));
        
        
        // TEST MONTH
        
        start = this.helperImpl.determineStart(Interval.MONTH, instant);
        assertEquals(2012, start.get(Calendar.YEAR));
        assertEquals(0, start.get(Calendar.MONTH));
        assertEquals(1, start.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, start.get(Calendar.WEEK_OF_YEAR));
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, start.get(Calendar.MINUTE));
        assertEquals(0, start.get(Calendar.SECOND));
        assertEquals(0, start.get(Calendar.MILLISECOND));
        
        end = this.helperImpl.determineEnd(Interval.MONTH, start);
        assertEquals(2012, end.get(Calendar.YEAR));
        assertEquals(1, end.get(Calendar.MONTH));
        assertEquals(1, end.get(Calendar.DAY_OF_MONTH));
        assertEquals(5, end.get(Calendar.WEEK_OF_YEAR));
        assertEquals(0, end.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, end.get(Calendar.MINUTE));
        assertEquals(0, end.get(Calendar.SECOND));
        assertEquals(0, end.get(Calendar.MILLISECOND));
        
        
        // TEST WEEK
        
        start = this.helperImpl.determineStart(Interval.WEEK, instant);
        assertEquals(2012, start.get(Calendar.YEAR));
        assertEquals(0, start.get(Calendar.MONTH));
        assertEquals(1, start.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, start.get(Calendar.WEEK_OF_YEAR));
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, start.get(Calendar.MINUTE));
        assertEquals(0, start.get(Calendar.SECOND));
        assertEquals(0, start.get(Calendar.MILLISECOND));
        
        end = this.helperImpl.determineEnd(Interval.WEEK, start);
        assertEquals(2012, end.get(Calendar.YEAR));
        assertEquals(0, end.get(Calendar.MONTH));
        assertEquals(8, end.get(Calendar.DAY_OF_MONTH));
        assertEquals(2, end.get(Calendar.WEEK_OF_YEAR));
        assertEquals(0, end.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, end.get(Calendar.MINUTE));
        assertEquals(0, end.get(Calendar.SECOND));
        assertEquals(0, end.get(Calendar.MILLISECOND));
        
        
        // TEST DAY
        
        start = this.helperImpl.determineStart(Interval.DAY, instant);
        assertEquals(2012, start.get(Calendar.YEAR));
        assertEquals(0, start.get(Calendar.MONTH));
        assertEquals(6, start.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, start.get(Calendar.WEEK_OF_YEAR));
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, start.get(Calendar.MINUTE));
        assertEquals(0, start.get(Calendar.SECOND));
        assertEquals(0, start.get(Calendar.MILLISECOND));
        
        end = this.helperImpl.determineEnd(Interval.DAY, start);
        assertEquals(2012, end.get(Calendar.YEAR));
        assertEquals(0, end.get(Calendar.MONTH));
        assertEquals(7, end.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, end.get(Calendar.WEEK_OF_YEAR));
        assertEquals(0, end.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, end.get(Calendar.MINUTE));
        assertEquals(0, end.get(Calendar.SECOND));
        assertEquals(0, end.get(Calendar.MILLISECOND));
        
        
        // TEST HOUR
        
        start = this.helperImpl.determineStart(Interval.HOUR, instant);
        assertEquals(2012, start.get(Calendar.YEAR));
        assertEquals(0, start.get(Calendar.MONTH));
        assertEquals(6, start.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, start.get(Calendar.WEEK_OF_YEAR));
        assertEquals(14, start.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, start.get(Calendar.MINUTE));
        assertEquals(0, start.get(Calendar.SECOND));
        assertEquals(0, start.get(Calendar.MILLISECOND));
        
        end = this.helperImpl.determineEnd(Interval.HOUR, start);
        assertEquals(2012, end.get(Calendar.YEAR));
        assertEquals(0, end.get(Calendar.MONTH));
        assertEquals(6, end.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, end.get(Calendar.WEEK_OF_YEAR));
        assertEquals(15, end.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, end.get(Calendar.MINUTE));
        assertEquals(0, end.get(Calendar.SECOND));
        assertEquals(0, end.get(Calendar.MILLISECOND));
        
        
        // TEST FIVE_MINUTE
        
        start = this.helperImpl.determineStart(Interval.FIVE_MINUTE, instant);
        assertEquals(2012, start.get(Calendar.YEAR));
        assertEquals(0, start.get(Calendar.MONTH));
        assertEquals(6, start.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, start.get(Calendar.WEEK_OF_YEAR));
        assertEquals(14, start.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, start.get(Calendar.MINUTE));
        assertEquals(0, start.get(Calendar.SECOND));
        assertEquals(0, start.get(Calendar.MILLISECOND));
        
        end = this.helperImpl.determineEnd(Interval.FIVE_MINUTE, start);
        assertEquals(2012, end.get(Calendar.YEAR));
        assertEquals(0, end.get(Calendar.MONTH));
        assertEquals(6, end.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, end.get(Calendar.WEEK_OF_YEAR));
        assertEquals(14, end.get(Calendar.HOUR_OF_DAY));
        assertEquals(25, end.get(Calendar.MINUTE));
        assertEquals(0, end.get(Calendar.SECOND));
        assertEquals(0, end.get(Calendar.MILLISECOND));
        
        
        // TEST MINUTE
        
        start = this.helperImpl.determineStart(Interval.MINUTE, instant);
        assertEquals(2012, start.get(Calendar.YEAR));
        assertEquals(0, start.get(Calendar.MONTH));
        assertEquals(6, start.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, start.get(Calendar.WEEK_OF_YEAR));
        assertEquals(14, start.get(Calendar.HOUR_OF_DAY));
        assertEquals(22, start.get(Calendar.MINUTE));
        assertEquals(0, start.get(Calendar.SECOND));
        assertEquals(0, start.get(Calendar.MILLISECOND));
        
        end = this.helperImpl.determineEnd(Interval.MINUTE, start);
        assertEquals(2012, end.get(Calendar.YEAR));
        assertEquals(0, end.get(Calendar.MONTH));
        assertEquals(6, end.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, end.get(Calendar.WEEK_OF_YEAR));
        assertEquals(14, end.get(Calendar.HOUR_OF_DAY));
        assertEquals(23, end.get(Calendar.MINUTE));
        assertEquals(0, end.get(Calendar.SECOND));
        assertEquals(0, end.get(Calendar.MILLISECOND));
                
    }
    
}
