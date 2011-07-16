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

package org.jasig.portal.events.support;

import org.jasig.portal.events.EventType;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.security.IPerson;

public final class PublishedPortletDefinitionPortalEvent extends PortletPortalEvent {
    private static final long serialVersionUID = 1L;

    public PublishedPortletDefinitionPortalEvent(final Object source, final IPerson person, final IPortletDefinition portletDefinition) {
		super(source, person, portletDefinition, EventType.getEventType("PORTLET_DEFINITION_PUBLISHED"));
	}

	/* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
    public String toString() {
		return "Portlet '" + getPortletDefinition().getName()
				+ "' was published by " + getDisplayName() + " at "
				+ getTimestampAsDate();
	}
}
