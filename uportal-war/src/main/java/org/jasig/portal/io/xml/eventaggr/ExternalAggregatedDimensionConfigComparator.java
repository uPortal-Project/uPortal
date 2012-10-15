package org.jasig.portal.io.xml.eventaggr;

import org.jasig.portal.utils.ComparableExtractingComparator;

/**
 * Compare dimension configs by type
 * 
 * @author Eric Dalquist
 */
public class ExternalAggregatedDimensionConfigComparator extends
        ComparableExtractingComparator<ExternalAggregatedDimensionConfig, String> {
    
    public static final ExternalAggregatedDimensionConfigComparator INSTANCE = new ExternalAggregatedDimensionConfigComparator();
    
    private ExternalAggregatedDimensionConfigComparator() {
    }

    @Override
    protected String getComparable(ExternalAggregatedDimensionConfig o) {
        return o.getAggregatorType();
    }
}
