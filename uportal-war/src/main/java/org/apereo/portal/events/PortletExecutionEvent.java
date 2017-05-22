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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import org.apache.commons.lang.Validate;
import org.apereo.portal.dao.usertype.FunctionalNameType;
import org.apereo.portal.portlet.om.IPortletWindowId;

/**
 * Constructor assumes that Map passed in is completely immutable
 *
 */
public abstract class PortletExecutionEvent extends PortalEvent {
    private static final long serialVersionUID = 1L;

    @JsonIgnore private final IPortletWindowId portletWindowId;
    private final String fname;
    private final WindowState windowState;
    private final PortletMode portletMode;
    /**
     * Still here to support deserializing old event json
     *
     * @deprecated use {@link #executionTimeNano} instead
     */
    @JsonInclude(Include.NON_NULL)
    @Deprecated
    private Long executionTime;

    private long executionTimeNano;
    private final Map<String, List<String>> parameters;

    PortletExecutionEvent() {
        super();
        this.portletWindowId = null;
        this.fname = null;
        this.executionTimeNano = -1;
        this.parameters = Collections.emptyMap();
        this.windowState = null;
        this.portletMode = null;
    }

    PortletExecutionEvent(PortletExecutionEventBuilder portletEventBuilder) {
        super(portletEventBuilder.portalEventBuilder);

        this.portletWindowId = portletEventBuilder.portletWindowId;
        this.fname = portletEventBuilder.fname;
        this.executionTimeNano = portletEventBuilder.executionTimeNano;
        this.parameters = portletEventBuilder.parameters;
        this.windowState = portletEventBuilder.windowState;
        this.portletMode = portletEventBuilder.portletMode;
    }

    /**
     * @return The windowId of the portlet that was executed, may return null if this event was
     *     loaded from a persistent store
     */
    public IPortletWindowId getPortletWindowId() {
        return portletWindowId;
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

    /** @return the fname */
    public String getFname() {
        return this.fname;
    }

    public Map<String, List<String>> getParameters() {
        return Collections.unmodifiableMap(this.parameters);
    }

    public WindowState getWindowState() {
        return windowState;
    }

    public PortletMode getPortletMode() {
        return portletMode;
    }

    @Override
    public String toString() {
        return super.toString()
                + ", fname="
                + this.fname
                + ", executionTimeNano="
                + this.getExecutionTimeNano()
                + ", parameters="
                + this.parameters.size();
    }

    protected static class PortletExecutionEventBuilder {
        private final PortalEventBuilder portalEventBuilder;
        private final IPortletWindowId portletWindowId;
        private final String fname;
        private final long executionTimeNano;
        private final Map<String, List<String>> parameters;
        private final WindowState windowState;
        private final PortletMode portletMode;

        public PortletExecutionEventBuilder(PortletExecutionEventBuilder portletEventBuilder) {
            this(
                    portletEventBuilder.portalEventBuilder,
                    portletEventBuilder.portletWindowId,
                    portletEventBuilder.fname,
                    portletEventBuilder.executionTimeNano,
                    portletEventBuilder.parameters,
                    portletEventBuilder.windowState,
                    portletEventBuilder.portletMode);
        }

        public PortletExecutionEventBuilder(
                PortalEventBuilder portalEventBuilder,
                IPortletWindowId portletWindowId,
                String fname,
                long executionTimeNano,
                Map<String, List<String>> parameters,
                WindowState windowState,
                PortletMode portletMode) {

            Validate.notNull(portalEventBuilder, "portalEventBuilder");
            FunctionalNameType.validate(fname);
            Validate.notNull(parameters, "parameters");

            this.portalEventBuilder = portalEventBuilder;
            this.portletWindowId = portletWindowId;
            this.fname = fname;
            this.executionTimeNano = executionTimeNano;
            this.parameters = parameters;
            this.windowState = windowState;
            this.portletMode = portletMode;
        }
    }
}
