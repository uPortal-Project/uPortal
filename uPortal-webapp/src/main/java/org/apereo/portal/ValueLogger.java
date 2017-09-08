package org.apereo.portal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueLogger {

    private static final Logger logger = LoggerFactory.getLogger(ValueLogger.class);

    private String name = "";
    private String value = "";

    public void setName(String name) {
        this.name = name;
        logger.debug("name = {}", name);
    }

    public void setValue(String value) {
        this.value = value;
        logger.debug("value = {}", value);
    }
}