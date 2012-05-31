package org.jasig.portal.events.aggr.tabrender;

import org.jasig.portal.events.aggr.BaseAggregationKey;

/**
 * Primary Key for a {@link TabRenderAggregation}
 * 
 * @author Eric Dalquist
 */
public interface TabRenderAggregationKey extends BaseAggregationKey {
    /**
     * Tab name used for personal (non DLM sourced) tabs
     */
    public static final String PERSONAL_TAB_NAME = "CATCH_ALL_PERSONAL_TAB";
    
    /**
     * Tab name used for renders with no targeted tab
     */
    public static final String NO_TAB_NAME = "CATCH_ALL_NULL_TAB";

    /**
     * @return The name of the tab
     */
    String getTabName();

}
