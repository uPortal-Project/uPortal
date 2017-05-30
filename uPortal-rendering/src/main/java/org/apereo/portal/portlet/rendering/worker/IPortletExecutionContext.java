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
package org.apereo.portal.portlet.rendering.worker;

import java.util.Map;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletWindowId;

/**
 * Data about a portlet execution.
 *
 */
public interface IPortletExecutionContext {
    public enum ExecutionType {
        ACTION,
        EVENT,
        RENDER_HEADER,
        RENDER,
        RESOURCE,
        FAILURE;
    }

    /** @return The type of portlet request the execution is happening for. */
    public ExecutionType getExecutionType();

    /**
     * Set an attribute about the execution.
     *
     * @see Map#put(Object, Object)
     */
    public Object setExecutionAttribute(String name, Object value);

    /**
     * Get an attribute about the execution.
     *
     * @see Map#get(Object)
     */
    public Object getExecutionAttribute(String name);

    /** @return The ID of the portlet window this context is for */
    public IPortletWindowId getPortletWindowId();

    /** @return The fname of the portlet window (from {@link IPortletDefinition#getFName()}) */
    public String getPortletFname();

    /**
     * @return The timeout setting in milliseconds for the operation in process, or -1 for no
     *     timeout.
     */
    public long getApplicableTimeout();

    /** @return true If the worker has been submitted */
    public boolean isSubmitted();

    /** @return true If the worker has been started */
    public boolean isStarted();

    /** @return true If the worker is complete */
    public boolean isComplete();

    /** @return time that the worker was submitted */
    public long getSubmittedTime();

    /** @return time that the execution started */
    public long getStartedTime();

    /** @return time that the execution completed */
    public long getCompleteTime();

    /** @return time that to wait from being submitted until being started */
    public long getWait();

    /** @return time for the execution to complete (difference between started and completed) */
    public long getDuration();
}
