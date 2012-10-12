package org.jasig.portal.io.xml.portlet;

import org.jasig.portal.utils.ComparableExtractingComparator;

public final class ExternalPortletPreferenceNameComparator extends
        ComparableExtractingComparator<ExternalPortletPreference, String> {
    public static final ExternalPortletPreferenceNameComparator INSTANCE = new ExternalPortletPreferenceNameComparator();
    
    private ExternalPortletPreferenceNameComparator() {
    }
    
    @Override
    protected String getComparable(ExternalPortletPreference o) {
        return o.getName();
    }
}