/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.portal.events.handlers;

import org.jasig.portal.events.PortalEvent;

public abstract class RawTimestampEventHandler extends
    AbstractLimitedSupportEventHandler {

    protected String getMessage(PortalEvent event) {
        return event.getEvent() + FOR + event.getDisplayName()
            + AT + event.getTimestamp();
    }

}
