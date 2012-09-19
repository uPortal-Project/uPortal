package org.jasig.portal.events.aggr.login;

import org.jasig.portal.events.aggr.AggregationIntervalInfo;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

import com.google.common.base.Function;

/**
 * MissingLoginDataCreator is a function designed for creating empty login
 * aggregations.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
public class MissingLoginDataCreator implements Function<AggregationIntervalInfo, LoginAggregation> {
    
    private final AggregatedGroupMapping aggregatedGroupMapping;
    
    public MissingLoginDataCreator(AggregatedGroupMapping aggregatedGroupMapping) {
        this.aggregatedGroupMapping = aggregatedGroupMapping;
    }

    @Override
    public LoginAggregation apply(AggregationIntervalInfo aggregationIntervalInfo) {
        return new EmptyLoginAggregation(aggregationIntervalInfo, aggregatedGroupMapping);
    }

}
