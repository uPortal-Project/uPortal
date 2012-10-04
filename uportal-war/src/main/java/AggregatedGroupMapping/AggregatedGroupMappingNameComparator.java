package AggregatedGroupMapping;

import java.util.Comparator;

import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.utils.ComparableExtractingComparator;

public class AggregatedGroupMappingNameComparator extends
        ComparableExtractingComparator<AggregatedGroupMapping, String> {
    
    public static Comparator<AggregatedGroupMapping> INSTANCE = new AggregatedGroupMappingNameComparator();

    @Override
    protected String getComparable(AggregatedGroupMapping o) {
        return o.getGroupName();
    }
}
