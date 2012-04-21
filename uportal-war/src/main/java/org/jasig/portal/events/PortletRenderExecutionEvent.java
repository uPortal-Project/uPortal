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

import java.util.List;
import java.util.Map;


/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class PortletRenderExecutionEvent extends PortletExecutionEvent {
    private static final long serialVersionUID = 1L;
    
    private final boolean targeted;
    private final boolean usedPortalCache;

    @SuppressWarnings("unused")
    private PortletRenderExecutionEvent() {
        this.targeted = false;
        this.usedPortalCache = false;
    }

    PortletRenderExecutionEvent(PortalEventBuilder eventBuilder, String fname, long executionTime, Map<String, List<String>> parameters, boolean targeted, boolean usedPortalCache) {
        super(eventBuilder, fname, executionTime, parameters);
        this.targeted = targeted;
        this.usedPortalCache = usedPortalCache;
    }

    /**
     * @return If the portlet was explicitly targeted by the request that resulted in it rendering
     */
    public boolean isTargeted() {
        return this.targeted;
    }

    /**
     * @deprecated use {@link #isUsedPortalCache()}
     */
    @Deprecated
    public boolean isCached() {
        return this.usedPortalCache;
    }
    
    /**
     * @return true If the rendering was from the portal's cache
     */
    public boolean isUsedPortalCache() {
        return usedPortalCache;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString() + 
                ", targeted=" + this.targeted + 
                ", cached=" + this.usedPortalCache + "]";
    }
}
