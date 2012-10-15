package org.jasig.portal.io.xml.eventaggr;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.collections.comparators.ComparatorChain;
import org.jasig.portal.utils.ComparableExtractingComparator;

/**
 * Sort group mappings
 * 
 * @author Eric Dalquist
 */
public class ExternalAggregatedGroupMappingComparator implements Comparator<ExternalAggregatedGroupMapping> {
    private final ComparatorChain chain;
    
    public static final ExternalAggregatedGroupMappingComparator INSTANCE = new ExternalAggregatedGroupMappingComparator();
    
    @SuppressWarnings("unchecked")
    private ExternalAggregatedGroupMappingComparator() {
        chain = new ComparatorChain(Arrays.asList(
                new ComparableExtractingComparator<ExternalAggregatedGroupMapping, String>() {
                    @Override
                    protected String getComparable(ExternalAggregatedGroupMapping o) {
                        return o.getGroupService();
                    }
                },
                new ComparableExtractingComparator<ExternalAggregatedGroupMapping, String>() {
                    @Override
                    protected String getComparable(ExternalAggregatedGroupMapping o) {
                        return o.getGroupName();
                    }
                }
        ));
    }
    
    @Override
    public int compare(ExternalAggregatedGroupMapping o1, ExternalAggregatedGroupMapping o2) {
        return chain.compare(o1, o2);
    }
}
