package org.jasig.portal.utils;

import java.util.Map;
import java.util.Properties;

/**
 * Populator that targets a Properties object 
 * 
 * @author Eric Dalquist
 */
public class PropertiesPopulator implements Populator<String, String> {
    private final Properties properties;
    
    public PropertiesPopulator() {
        this.properties = new Properties();
    }

    public PropertiesPopulator(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Populator<String, String> put(String k, String v) {
        this.properties.put(k, v);
        return this;
    }
    
    @Override
    public Populator<String, String> putAll(Map<? extends String, ? extends String> m) {
        this.properties.putAll(m);
        return this;
    }

    public Properties getProperties() {
        return properties;
    }
}