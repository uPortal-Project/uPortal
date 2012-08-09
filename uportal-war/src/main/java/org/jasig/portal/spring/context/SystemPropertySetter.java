package org.jasig.portal.spring.context;

import java.util.Map;
import java.util.Properties;

/**
 * Sets system properties, ignores any properties with zero length values
 * 
 * @author Eric Dalquist
 */
public final class SystemPropertySetter {
    public void setSystemProperties(Properties props) {
    	for (final Map.Entry<Object, Object> propEntry : props.entrySet()) {
    		final Object value = propEntry.getValue();
            if (value != null && String.valueOf(value).length() != 0) {
                System.setProperty(
                		String.valueOf(propEntry.getKey()),
        				String.valueOf(value));
            }
    	}
    }
}
