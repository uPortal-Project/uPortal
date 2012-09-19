package org.jasig.portal.events.aggr;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.login.EmptyLoginAggregation;
import org.jasig.portal.events.aggr.login.LoginAggregation;
import org.jasig.portal.events.aggr.login.MissingLoginDataCreator;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class AggregationIntervalHelperImplTest {

	@Spy AggregationIntervalHelperImpl helper = new AggregationIntervalHelperImpl();
	@Mock AggregatedGroupMapping groupMapping;
	AggregationInterval interval = AggregationInterval.HOUR;
	DateTime start = new DateTime(2012, 1, 1, 0, 0);
	DateTime end = start.plusHours(6);
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		doAnswer(new Answer() {
		    public Object answer(InvocationOnMock invocation) {
    	        final Object[] args = invocation.getArguments();
    	        final DateTime date = (DateTime) args[1];
    	        final AggregationIntervalInfo interval = getHourInfo(date);
    	        return interval;
    	    }

		}).when(helper).getIntervalInfo(any(AggregationInterval.class), any(DateTime.class));
	}
	
	@Test
	public void testEmptyDataSet() {
		
		final List<LoginAggregation> incomplete = new ArrayList<LoginAggregation>();
		
		List<LoginAggregation> complete = helper.fillInBlanks(interval, start, end, incomplete, new MissingLoginDataCreator(groupMapping));
		assertEquals(6, complete.size());
	}

	@Test
	public void testFullDataSet() {
		
		final List<LoginAggregation> incomplete = new ArrayList<LoginAggregation>();
		incomplete.add(new EmptyLoginAggregation(getHourInfo(start), groupMapping));
		incomplete.add(new EmptyLoginAggregation(getHourInfo(start.plusHours(1)), groupMapping));
		incomplete.add(new EmptyLoginAggregation(getHourInfo(start.plusHours(2)), groupMapping));
		incomplete.add(new EmptyLoginAggregation(getHourInfo(start.plusHours(3)), groupMapping));
		incomplete.add(new EmptyLoginAggregation(getHourInfo(start.plusHours(4)), groupMapping));
		incomplete.add(new EmptyLoginAggregation(getHourInfo(start.plusHours(5)), groupMapping));
		
		List<LoginAggregation> complete = helper.fillInBlanks(interval, start, end, incomplete, new MissingLoginDataCreator(groupMapping));
		assertEquals(6, complete.size());
	}

	@Test
	public void testPartialDataSet() {
		
		final List<LoginAggregation> incomplete = new ArrayList<LoginAggregation>();
		incomplete.add(new EmptyLoginAggregation(getHourInfo(start.plusHours(3)), groupMapping));
		
		List<LoginAggregation> complete = helper.fillInBlanks(interval, start, end, incomplete, new MissingLoginDataCreator(groupMapping));
		assertEquals(6, complete.size());
	}

	@Test
	public void testPartialDataSet2() {
		
		final List<LoginAggregation> incomplete = new ArrayList<LoginAggregation>();
		incomplete.add(new EmptyLoginAggregation(getHourInfo(start.plusHours(1)), groupMapping));
		incomplete.add(new EmptyLoginAggregation(getHourInfo(start.plusHours(4)), groupMapping));
		
		List<LoginAggregation> complete = helper.fillInBlanks(interval, start, end, incomplete, new MissingLoginDataCreator(groupMapping));
		assertEquals(6, complete.size());
	}

	@Test
	public void testSinglePointDataSet() {
		
		end = start.plusHours(1);
		
		final List<LoginAggregation> incomplete = new ArrayList<LoginAggregation>();
		incomplete.add(new EmptyLoginAggregation(getHourInfo(start), groupMapping));
		
		List<LoginAggregation> complete = helper.fillInBlanks(interval, start, end, incomplete, new MissingLoginDataCreator(groupMapping));
		assertEquals(1, complete.size());
	}

	@Test
	public void testEmptySinglePointDataSet() {
		
		end = start.plusHours(1);
		
		final List<LoginAggregation> incomplete = new ArrayList<LoginAggregation>();
		
		List<LoginAggregation> complete = helper.fillInBlanks(interval, start, end, incomplete, new MissingLoginDataCreator(groupMapping));
		assertEquals(1, complete.size());
	}



	private AggregationIntervalInfo getHourInfo(DateTime date) {
        final DateTime start = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), 0);
        final DateTime end = start.plusHours(1);
        
        final TimeDimension startTimeDimension = when(mock(TimeDimension.class).getTime()).thenReturn(start.toLocalTime()).getMock();
        final DateDimension startDateDimension = when(mock(DateDimension.class).getDate()).thenReturn(start.toDateMidnight()).getMock();
        
        return new AggregationIntervalInfo(AggregationInterval.HOUR, start, end, startDateDimension, startTimeDimension);

	}
	
}
