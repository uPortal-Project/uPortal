package org.jasig.portal.events.aggr;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic context impl
 * 
 * @author Eric Dalquist
 */
class EventAggregationContextImpl implements EventAggregationContext {
    private final Map<Object, Object> attributes = new HashMap<Object, Object>();
    
    @Override
    public void setAttribute(Object key, Object value) {
        this.attributes.put(key, value);
    }

    @Override
    public <T> T getAttribute(Object key) {
        return (T)attributes.get(key);
    }

}
