package org.jasig.portal.io.xml.eventaggr;

import org.jasig.portal.utils.ComparableExtractingComparator;
import org.joda.time.MonthDay;


/**
 * Compare ExternalQuarterDetail based on start MonthDay
 * 
 * @author Eric Dalquist
 */
public class ExternalQuarterDetailComparator extends ComparableExtractingComparator<ExternalQuarterDetail, MonthDay> {
    public static final ExternalQuarterDetailComparator INSTANCE = new ExternalQuarterDetailComparator();
    
    private ExternalQuarterDetailComparator() {
    }

    @Override
    protected MonthDay getComparable(ExternalQuarterDetail o) {
        return MonthDay.parse(o.getStart());
    }
}
