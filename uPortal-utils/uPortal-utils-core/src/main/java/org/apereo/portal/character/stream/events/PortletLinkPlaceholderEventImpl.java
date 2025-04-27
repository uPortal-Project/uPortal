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

/** */
public final class PortletLinkPlaceholderEventImpl extends PortletPlaceholderEventImpl
        implements PortletLinkPlaceholderEvent {
    private static final long serialVersionUID = 1L;

    private final String defaultPortletUrl;

    public PortletLinkPlaceholderEventImpl(
            IPortletWindowId portletWindowId, String defaultPortletUrl) {
        super(portletWindowId);
        this.defaultPortletUrl = defaultPortletUrl;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.character.stream.events.CharacterEvent#getEventType()
     */
    @Override
    public CharacterEventTypes getEventType() {
        return CharacterEventTypes.PORTLET_LINK;
    }

    @Override
    public String getDefaultPortletUrl() {
        return this.defaultPortletUrl;
    }

    @Override
    protected int internalHashCode() {
        final int prime = 31;
        int result = super.internalHashCode();
        result =
                prime * result
                        + ((this.getDefaultPortletUrl() == null)
                                ? 0
                                : this.getDefaultPortletUrl().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!PortletNewItemCountPlaceholderEvent.class.isAssignableFrom(obj.getClass()))
            return false;
        PortletLinkPlaceholderEvent other = (PortletLinkPlaceholderEvent) obj;
        if (this.getEventType() == null) {
            if (other.getEventType() != null) return false;
        } else if (!this.getEventType().equals(other.getEventType())) return false;
        if (this.getPortletWindowId() == null) {
            if (other.getPortletWindowId() != null) return false;
        } else if (!this.getPortletWindowId().equals(other.getPortletWindowId())) return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletLinkPlaceholderEvent ["
                + "portletWindowId="
                + this.getPortletWindowId()
                + ", "
                + "defaultPortletUrl="
                + this.getDefaultPortletUrl()
                + "]";
    }
}
