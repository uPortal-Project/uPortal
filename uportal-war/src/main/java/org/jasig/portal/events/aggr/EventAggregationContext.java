package org.jasig.portal.events.aggr;

/**
 * Context of the current event aggregation run. There is a single instance of
 * this object for each aggregation run. It can be used by aggregators to store
 * data across sessions
 * <br/>
 * NOTE: Attributes are globally scoped, aggregates must make sure the key is appropriate to prevent stepping on toes.
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
