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

import javax.portlet.ResourceRequest;


/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class PortletResourceExecutionEvent extends PortletExecutionEvent {
    private static final long serialVersionUID = 1L;
    
    private final String resourceId;
    private final boolean cached;

    @SuppressWarnings("unused")
    private PortletResourceExecutionEvent() {
        this.resourceId = null;
        this.cached = false;
    }

    PortletResourceExecutionEvent(PortalEventBuilder eventBuilder, long executionTime, String resourceId,
            boolean cached) {
        super(eventBuilder, executionTime);
        this.resourceId = resourceId;
        this.cached = cached;
    }

    /**
     * @return the resourceId
     * @see ResourceRequest#getResourceID()
     */
    public String getResourceId() {
        return this.resourceId;
    }

    /**
     * @return the cached
     */
    public boolean isCached() {
        return this.cached;
    }
}
