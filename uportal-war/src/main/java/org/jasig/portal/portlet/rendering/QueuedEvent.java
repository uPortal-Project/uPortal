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

package org.jasig.portal.portlet.rendering;

import javax.portlet.Event;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Tracks an event and the window id that generated it
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class QueuedEvent {
	private final IPortletWindowId portletWindowId;
	private final Event event;

	public QueuedEvent(IPortletWindowId portletWindowId, Event event) {
		this.portletWindowId = portletWindowId;
		this.event = event;
	}
	/**
	 * @return the portletWindowId that created the event
	 */
	public IPortletWindowId getPortletWindowId() {
		return portletWindowId;
	}
	/**
	 * @return the event
	 */
	public Event getEvent() {
		return event;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((event == null) ? 0 : event.hashCode());
		result = prime * result
				+ ((portletWindowId == null) ? 0 : portletWindowId.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueuedEvent other = (QueuedEvent) obj;
		if (event == null) {
			if (other.event != null)
				return false;
		} else if (!event.equals(other.event))
			return false;
		if (portletWindowId == null) {
			if (other.portletWindowId != null)
				return false;
		} else if (!portletWindowId.equals(other.portletWindowId))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QueuedEvent [portletWindowId=" + portletWindowId + ", event=" + event + "]";
	}
}
