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

import org.apache.commons.lang.Validate;
import org.apereo.portal.portlet.rendering.worker.IPortletExecutionWorker;

/**
 * Used to signal that a portlet hung execution has completed
 *
 */
public class PortletHungCompleteEvent extends PortalEvent {
    private static final long serialVersionUID = 1L;

    private final transient IPortletExecutionWorker<?> worker;

    private final String fname;

    @SuppressWarnings("unused")
    private PortletHungCompleteEvent() {
        this.worker = null;
        this.fname = null;
    }

    PortletHungCompleteEvent(PortalEventBuilder eventBuilder, IPortletExecutionWorker<?> worker) {
        super(eventBuilder);

        Validate.notNull(worker, "worker");

        this.worker = worker;
        this.fname = worker.getPortletFname();
    }

    /** @return The portlet worker that is hung. Not available during event aggregation */
    public IPortletExecutionWorker<?> getWorker() {
        return worker;
    }

    /** @return The functional name of the hung portlet */
    public String getFname() {
        return fname;
    }

    @Override
    public String toString() {
        return super.toString() + ", fname=" + this.fname + "]";
    }
}
