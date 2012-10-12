package org.jasig.portal.io.xml.portlet;

import org.jasig.portal.utils.ComparableExtractingComparator;

public final class ExternalPortletParameterNameComparator extends
        ComparableExtractingComparator<ExternalPortletParameter, String> {
    public static final ExternalPortletParameterNameComparator INSTANCE = new ExternalPortletParameterNameComparator();
    
    private ExternalPortletParameterNameComparator() {
    }
    
    @Override
    protected String getComparable(ExternalPortletParameter o) {
        return o.getName();
    }
}