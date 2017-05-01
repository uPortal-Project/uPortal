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

import javax.portlet.ResourceRequest;

/**
 */
public final class PortletResourceExecutionEvent extends PortletExecutionEvent {
    private static final long serialVersionUID = 1L;

    private final String resourceId;
    private final boolean usedBrowserCache;
    private final boolean usedPortalCache;

    @SuppressWarnings("unused")
    private PortletResourceExecutionEvent() {
        this.resourceId = null;
        this.usedBrowserCache = false;
        this.usedPortalCache = false;
    }

    PortletResourceExecutionEvent(
            PortletExecutionEventBuilder eventBuilder,
            String resourceId,
            boolean usedBrowserCache,
            boolean usedPortalCache) {
        super(eventBuilder);
        this.resourceId = resourceId;
        this.usedBrowserCache = usedBrowserCache;
        this.usedPortalCache = usedPortalCache;
    }

    /**
     * @return the resourceId
     * @see ResourceRequest#getResourceID()
     */
    public String getResourceId() {
        return this.resourceId;
    }

    /** @deprecated use {@link #usedPortalCache} or {@link #usedBrowserCache} */
    @Deprecated
    public boolean isCached() {
        return this.usedBrowserCache;
    }

    /** @return true if the browser's cached content was used */
    public boolean isUsedBrowserCache() {
        return usedBrowserCache;
    }

    /** @return true if the portal's cached content was used */
    public boolean isUsedPortalCache() {
        return usedPortalCache;
    }

    @Override
    public String toString() {
        return super.toString()
                + ", resourceId="
                + this.resourceId
                + ", usedBrowserCache="
                + this.usedBrowserCache
                + ", usedPortalCache="
                + this.usedPortalCache
                + "]";
    }
}
