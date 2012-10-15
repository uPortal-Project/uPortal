package org.jasig.portal.io.xml.permission;

import org.jasig.portal.utils.ComparableExtractingComparator;

/**
 * Compare ExternalActivity based on fname
 * 
 * @author Eric Dalquist
 */
public class ExternalActivityFnameComparator extends ComparableExtractingComparator<ExternalActivity, String> {
    public static final ExternalActivityFnameComparator INSTANCE = new ExternalActivityFnameComparator();
    
    private ExternalActivityFnameComparator() {
    }

    @Override
    protected String getComparable(ExternalActivity o) {
        return o.getFname();
    }
}
