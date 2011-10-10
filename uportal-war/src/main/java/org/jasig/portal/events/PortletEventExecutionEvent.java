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

package org.jasig.portal.events;

import javax.portlet.EventRequest;
import javax.xml.namespace.QName;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class PortletEventExecutionEvent extends PortletExecutionEvent {
    private static final long serialVersionUID = 1L;
    
    private final QName eventName;

    @SuppressWarnings("unused")
    private PortletEventExecutionEvent() {
        this.eventName = null;
    }

    PortletEventExecutionEvent(PortalEventBuilder eventBuilder, long executionTime, QName eventName) {
        super(eventBuilder, executionTime);
        this.eventName = eventName;
    }

    /**
     * @return the eventName
     * @see EventRequest#getEvent()
     */
    public QName getEventName() {
        return this.eventName;
    }
}
