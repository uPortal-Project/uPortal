/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.portal.events.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.events.PortalEvent;
import org.springframework.util.Assert;

/**
 * Instance of Event Handler that delegates to Commons Logging and writes out
 * events at the INFO level.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 */
public final class LoggingEventHandler extends AbstractLimitedSupportEventHandler {
    private Log eventLogger = this.logger;
    
    /**
     * @param logCategory A custom log category to use
     */
    public void setLogCategory(String logCategory) {
        Assert.notNull(logCategory);
        this.eventLogger = LogFactory.getLog(logCategory);
    }
    
    public void handleEvent(final PortalEvent event) {
		if (this.eventLogger.isInfoEnabled()) {
		    this.eventLogger.info(event.toString());
		}
	}
}
