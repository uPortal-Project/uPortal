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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collections;
import java.util.List;
import org.apereo.portal.events.aggr.dao.DateDimensionDao;
import org.apereo.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.apereo.portal.events.aggr.dao.TimeDimensionDao;
import org.apereo.portal.events.handlers.db.IPortalEventDao;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Ignore // Breaks on move to Gradle
@RunWith(MockitoJUnitRunner.class)
public class PortalEventDimensionPopulatorImplTest {
    @InjectMocks
    private PortalEventDimensionPopulatorImpl portalEventDimensionPopulator =
            new PortalEventDimensionPopulatorImpl() {
                @Override
                DateTime getNow() {
                    return new DateTime(1325881376117l);
                }
            };

    @Mock TimeDimensionDao timeDimensionDao;
    @Mock DateDimensionDao dateDimensionDao;
    @Mock IPortalEventDao portalEventDao;
    @Mock AggregationIntervalHelper aggregationIntervalHelper;
    @Mock IEventAggregationManagementDao eventAggregationManagementDao;

    protected TimeDimension createMockTimeDimension(LocalTime time) {
        final TimeDimension td = mock(TimeDimension.class);

        when(td.getTime())
                .thenReturn(time.minuteOfHour().roundFloorCopy()); // truncate at minute level
        when(td.getHour()).thenReturn(time.getHourOfDay());
        when(td.getMinute()).thenReturn(time.getMinuteOfHour());
        when(td.getFiveMinuteIncrement()).thenReturn(time.getMinuteOfHour() / 5);

        return td;
    }

    @Test
    public void populateAllTimeDimensions() {
        when(timeDimensionDao.getTimeDimensions())
                .thenReturn(Collections.<TimeDimension>emptyList());

        this.portalEventDimensionPopulator.doPopulateTimeDimensions();

        verify(timeDimensionDao).getTimeDimensions();
        verify(timeDimensionDao, times(60 * 24))
                .createTimeDimension(ArgumentMatchers.<LocalTime>any());
        verifyNoMoreInteractions(timeDimensionDao, dateDimensionDao);
    }

    @Test
    public void populateSomeTimeDimensions() {
        final Builder<Object> existingTimeDimensionBuilder = ImmutableList.builder();

        TimeDimension td = mock(TimeDimension.class);
        when(td.getTime()).thenReturn(new LocalTime(0, 1));
        existingTimeDimensionBuilder.add(td);

        final List<TimeDimension> existingTimeDimensions =
                ImmutableList.of(
                        createMockTimeDimension(new LocalTime(0, 1)),
                        createMockTimeDimension(new LocalTime(0, 2)),
                        createMockTimeDimension(new LocalTime(0, 3)),
                        createMockTimeDimension(new LocalTime(0, 4)),
                        createMockTimeDimension(new LocalTime(0, 7)),
                        createMockTimeDimension(new LocalTime(0, 8)),
                        createMockTimeDimension(new LocalTime(0, 9)),
                        createMockTimeDimension(new LocalTime(1, 23)),
                        createMockTimeDimension(new LocalTime(23, 58)));

        when(timeDimensionDao.getTimeDimensions()).thenReturn(existingTimeDimensions);

        this.portalEventDimensionPopulator.doPopulateTimeDimensions();

        verify(timeDimensionDao).getTimeDimensions();
        verify(timeDimensionDao, times((60 * 24) - existingTimeDimensions.size()))
                .createTimeDimension(ArgumentMatchers.<LocalTime>any());
        verifyNoMoreInteractions(timeDimensionDao, dateDimensionDao);
    }

    @Test
    public void populateDefaultDateDimensions() {
        final DateTime now = new DateTime(1325881376117l);
        final Period dimensionBuffer = Period.days(30);
        final DateTime oldestPortalEvent = now.minusDays(30);
        final DateTime newestPortalEvent = now;
        final DateTime dimensionStart = oldestPortalEvent.minus(dimensionBuffer);
        final DateTime dimensionEnd = newestPortalEvent.plus(dimensionBuffer);

        when(dateDimensionDao.getDateDimensionsBetween(
                        any(DateMidnight.class), any(DateMidnight.class)))
                .thenReturn(Collections.<DateDimension>emptyList());
        when(portalEventDao.getOldestPortalEventTimestamp()).thenReturn(oldestPortalEvent);
        when(portalEventDao.getOldestPortalEventTimestamp()).thenReturn(oldestPortalEvent);

        final AggregationIntervalInfo startIntervalInfo = mock(AggregationIntervalInfo.class);
        when(startIntervalInfo.getStart()).thenReturn(dimensionStart.year().roundFloorCopy());

        final AggregationIntervalInfo endIntervalInfo = mock(AggregationIntervalInfo.class);
        when(endIntervalInfo.getEnd()).thenReturn(dimensionStart.year().roundCeilingCopy());

        when(aggregationIntervalHelper.getIntervalInfo(AggregationInterval.YEAR, dimensionStart))
                .thenReturn(startIntervalInfo);
        when(aggregationIntervalHelper.getIntervalInfo(AggregationInterval.YEAR, dimensionEnd))
                .thenReturn(endIntervalInfo);

        when(eventAggregationManagementDao.getAcademicTermDetails())
                .thenReturn(Collections.<AcademicTermDetail>emptyList());
        when(eventAggregationManagementDao.getQuartersDetails())
                .thenReturn(EventDateTimeUtils.createStandardQuarters());

        portalEventDimensionPopulator.setDimensionBuffer(dimensionBuffer);
        portalEventDimensionPopulator.doPopulateDateDimensions();

        verify(dateDimensionDao)
                .getDateDimensionsBetween(any(DateMidnight.class), any(DateMidnight.class));

        verify(dateDimensionDao, times(365))
                .createDateDimension(ArgumentMatchers.<DateMidnight>any(), anyInt(), anyString());
        verifyNoMoreInteractions(timeDimensionDao, dateDimensionDao);
    }
}
