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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.Validate;
import org.apereo.portal.url.UrlState;
import org.apereo.portal.url.UrlType;

/**
 * Constructor assumes that Map passed in is completely immutable
 *
 */
public final class PortalRenderEvent extends PortalEvent {
    private static final long serialVersionUID = 1L;

    private final String requestPathInfo;
    private final UrlState urlState;
    private final UrlType urlType;
    private final Map<String, List<String>> parameters;
    private final String targetedLayoutNodeId;
    /**
     * Still here to support deserializing old event json
     *
     * @deprecated use {@link #executionTimeNano} instead
     */
    @JsonInclude(Include.NON_NULL)
    @Deprecated
    private Long executionTime;

    private long executionTimeNano;

    PortalRenderEvent() {
        super();
        this.requestPathInfo = null;
        this.urlState = null;
        this.urlType = null;
        this.parameters = Collections.emptyMap();
        this.targetedLayoutNodeId = null;
        this.executionTimeNano = -1;
    }

    PortalRenderEvent(
            PortalEventBuilder eventBuilder,
            String requestPathInfo,
            long executionTimeNano,
            UrlState urlState,
            UrlType urlType,
            Map<String, List<String>> parameters,
            String targetedLayoutNodeId) {
        super(eventBuilder);
        Validate.notNull(urlState, "urlType");
        Validate.notNull(urlType, "urlType");
        Validate.notNull(parameters, "parameters");

        this.requestPathInfo = requestPathInfo;
        this.urlState = urlState;
        this.urlType = urlType;
        this.parameters = parameters;
        this.targetedLayoutNodeId = targetedLayoutNodeId;
        this.executionTimeNano = executionTimeNano;
    }

    /** @return the executionTime in milliseconds */
    public long getExecutionTime() {
        if (this.executionTime == null) {
            this.executionTime = TimeUnit.NANOSECONDS.toMillis(this.executionTimeNano);
        }

        return this.executionTime;
    }

    /** @return the executionTime in nanoseconds */
    public long getExecutionTimeNano() {
        if (this.executionTimeNano == -1 && this.executionTime != null) {
            this.executionTimeNano = TimeUnit.MILLISECONDS.toNanos(this.executionTime);
        }

        return this.executionTimeNano;
    }

    /** @return the requestPathInfo */
    public String getRequestPathInfo() {
        return this.requestPathInfo;
    }

    /** @return the urlState */
    public UrlState getUrlState() {
        return this.urlState;
    }

    /** @return the urlType */
    public UrlType getUrlType() {
        return this.urlType;
    }

    /** @return the parameters */
    public Map<String, List<String>> getParameters() {
        return this.parameters;
    }

    /** @return the targetedLayoutNodeId */
    public String getTargetedLayoutNodeId() {
        return this.targetedLayoutNodeId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString()
                + ", requestPathInfo="
                + this.requestPathInfo
                + ", urlState="
                + this.urlState
                + ", urlType="
                + this.urlType
                + ", parameters="
                + this.parameters.size()
                + ", targetedLayoutNodeId="
                + this.targetedLayoutNodeId
                + ", executionTimeNano="
                + this.getExecutionTimeNano()
                + "]";
    }
}
