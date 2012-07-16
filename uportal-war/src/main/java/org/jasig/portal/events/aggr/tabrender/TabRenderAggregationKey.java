package org.jasig.portal.events.aggr.tabrender;

import org.jasig.portal.events.aggr.BaseAggregationKey;
import org.jasig.portal.events.aggr.tabs.AggregatedTabMapping;

/**
 * Primary Key for a {@link TabRenderAggregation}
 * 
 * @author Eric Dalquist
 */
public interface TabRenderAggregationKey extends BaseAggregationKey {

    /**
     * @return The name of the tab
     */
    AggregatedTabMapping getTabMapping();

}
