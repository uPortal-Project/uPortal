package org.jasig.portal.events.aggr;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic context impl
 * 
 * @author Eric Dalquist
 */
class EventAggregationContextImpl implements EventAggregationContext {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final Map<Object, Object> attributes = new HashMap<Object, Object>();
    
    @Override
    public void setAttribute(Object key, Object value) {
        final Object old = this.attributes.put(key, value);
        if (old != null) {
            logger.warn("Replaced existing event aggr context for key={}", key);
        }
    }

    @Override
    public <T> T getAttribute(Object key) {
        return (T)attributes.get(key);
    }

}
