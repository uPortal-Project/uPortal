package org.jasig.portal.events.aggr;

import java.util.Comparator;

/**
 * Sorts {@link BaseAggregation} instaces by Date and Time
 * 
 * @author Eric Dalquist
 */
public class BaseAggregationDateTimeComparator implements Comparator<BaseAggregation<?>> {
    public static final Comparator<BaseAggregation<?>> INSTANCE = new BaseAggregationDateTimeComparator();
    

    @Override
    public int compare(BaseAggregation<?> o1, BaseAggregation<?> o2) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        final int dataCmp = o1.getDateDimension().getDate().compareTo(o2.getDateDimension().getDate());
        if (dataCmp != 0) {
            return dataCmp;
        }

        return o1.getTimeDimension().getTime().compareTo(o2.getTimeDimension().getTime());
    }

}
