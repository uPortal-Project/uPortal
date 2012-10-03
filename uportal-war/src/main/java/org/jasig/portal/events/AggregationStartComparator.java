package org.jasig.portal.events;

import java.util.Comparator;

import org.jasig.portal.events.aggr.BaseAggregation;
import org.joda.time.DateTime;


public class AggregationStartComparator implements Comparator<BaseAggregation> {

	@Override
	public int compare(BaseAggregation aggr1, BaseAggregation aggr2) {
        final DateTime entryDate1 = aggr1.getTimeDimension().getTime().toDateTime(aggr1.getDateDimension().getDate());
        final DateTime entryDate2 = aggr2.getTimeDimension().getTime().toDateTime(aggr2.getDateDimension().getDate());

		return entryDate1.compareTo(entryDate2);
	}

}
