package org.jasig.portal.io.xml.eventaggr;

import java.util.Calendar;

import org.jasig.portal.utils.ComparableExtractingComparator;


/**
 * Compare ExternalTermDetail based on start date
 * 
 * @author Eric Dalquist
 */
public class ExternalTermDetailComparator extends ComparableExtractingComparator<ExternalTermDetail, Calendar> {
    public static final ExternalTermDetailComparator INSTANCE = new ExternalTermDetailComparator();
    
    private ExternalTermDetailComparator() {
    }

    @Override
    protected Calendar getComparable(ExternalTermDetail o) {
        return o.getStart();
    }
}
