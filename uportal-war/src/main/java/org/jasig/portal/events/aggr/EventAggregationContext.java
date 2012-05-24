package org.jasig.portal.events.aggr;

/**
 * Context of the current event aggregation run. There is a single instance of
 * this object for each aggregation run. It can be used by aggregators to store
 * data across sessions
 * 
 * @author Eric Dalquist
 */
public interface EventAggregationContext {
    /**
     * Store an attribute in the current aggregation context
     */
    void setAttribute(Object key, Object value);
    
    /**
     * Get a value from the current aggregation context
     */
    <T> T getAttribute(Object key);
}
