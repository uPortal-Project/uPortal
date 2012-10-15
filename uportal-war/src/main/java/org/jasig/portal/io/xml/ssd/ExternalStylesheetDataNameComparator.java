package org.jasig.portal.io.xml.ssd;

import org.jasig.portal.utils.ComparableExtractingComparator;

/**
 * Compare ExternalStylesheetData by name
 * 
 * @author Eric Dalquist
 */
public class ExternalStylesheetDataNameComparator extends ComparableExtractingComparator<ExternalStylesheetData, String> {
    public static final ExternalStylesheetDataNameComparator INSTANCE = new ExternalStylesheetDataNameComparator();
    
    private ExternalStylesheetDataNameComparator() {
    }
    
    @Override
    protected String getComparable(ExternalStylesheetData o) {
        return o.getName();
    }
}
