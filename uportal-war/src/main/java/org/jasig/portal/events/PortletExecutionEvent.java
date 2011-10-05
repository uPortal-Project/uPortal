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

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class PortletExecutionEvent extends PortalEvent {
    private static final long serialVersionUID = 1L;
    
    private final long executionTime;

    PortletExecutionEvent() {
        super();
        this.executionTime = -1;
    }

    PortletExecutionEvent(PortalEventBuilder eventBuilder, long executionTime) {
        super(eventBuilder);
        this.executionTime = executionTime;
    }

    /**
     * @return the executionTime
     */
    public long getExecutionTime() {
        return this.executionTime;
    }
}
