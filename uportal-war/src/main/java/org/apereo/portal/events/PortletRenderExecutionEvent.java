/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 */
public final class PortletRenderExecutionEvent extends PortletExecutionEvent {
    private static final long serialVersionUID = 1L;

    private final boolean targeted;

    /**
     * Still here to support deserializing old event json
     *
     * @deprecated use {@link #usedPortalCache} instead
     */
    @JsonInclude(Include.NON_NULL)
    @Deprecated
    private Boolean cached;

    private Boolean usedPortalCache;

    @SuppressWarnings("unused")
    private PortletRenderExecutionEvent() {
        this.targeted = false;
    }

    PortletRenderExecutionEvent(
            PortletExecutionEventBuilder eventBuilder, boolean targeted, boolean usedPortalCache) {
        super(eventBuilder);
        this.targeted = targeted;
        this.usedPortalCache = usedPortalCache;
    }

    /**
     * @return If the portlet was explicitly targeted by the request that resulted in it rendering
     */
    public boolean isTargeted() {
        return this.targeted;
    }

    /** @deprecated use {@link #isUsedPortalCache()} */
    @Deprecated
    public boolean isCached() {
        return this.isUsedPortalCache();
    }

    /** @return true If the rendering was from the portal's cache */
    public boolean isUsedPortalCache() {
        if (usedPortalCache == null) {
            usedPortalCache = cached != null ? cached : false;
        }
        return this.usedPortalCache;
    }

    @Override
    public String toString() {
        return super.toString()
                + ", targeted="
                + this.targeted
                + ", cached="
                + this.isUsedPortalCache()
                + "]";
    }
}
