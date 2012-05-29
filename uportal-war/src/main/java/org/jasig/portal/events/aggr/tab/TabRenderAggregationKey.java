package org.jasig.portal.events.aggr.tab;

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
     * @return The name of the tab
     */
    String getTabName();

}
