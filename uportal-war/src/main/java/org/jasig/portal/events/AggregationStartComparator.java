package org.jasig.portal.events;

import java.util.Comparator;

import org.jasig.portal.events.aggr.BaseAggregation;
import org.joda.time.DateTime;


public class AggregationStartComparator implements Comparator<BaseAggregation<?>> {

	@Override
	public int compare(BaseAggregation<?> aggr1, BaseAggregation<?> aggr2) {
        final DateTime entryDate1 = aggr1.getDateTime();
        final DateTime entryDate2 = aggr2.getDateTime();

		return entryDate1.compareTo(entryDate2);
	}

}
