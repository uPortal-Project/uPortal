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
package org.apereo.portal.character.stream.events;

import org.apereo.portal.portlet.om.IPortletWindowId;

/** Base portlet placeholder event */
public abstract class PortletPlaceholderEventImpl implements PortletPlaceholderEvent {
    private static final long serialVersionUID = 1L;

    private int hash = 0;

    private final IPortletWindowId portletWindowId;

    public PortletPlaceholderEventImpl(IPortletWindowId portletWindowId) {
        this.portletWindowId = portletWindowId;
    }

    @Override
    public final IPortletWindowId getPortletWindowId() {
        return this.portletWindowId;
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            h = internalHashCode();
            hash = h;
        }
        return h;
    }

    protected int internalHashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((this.getPortletWindowId() == null)
                                ? 0
                                : this.getPortletWindowId().hashCode());
        result =
                prime * result
                        + ((this.getEventType() == null) ? 0 : this.getEventType().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + " ["
                + "portletWindowId="
                + this.getPortletWindowId()
                + "]";
    }
}
