/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
