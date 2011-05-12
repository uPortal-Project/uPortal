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

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import javax.portlet.Event;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.utils.ConcurrentMapUtils;

/**
 * Used to track events generated for a specific portlet window in a thread safe manner. The event
 * source adds events to the queue for a specific {@link IPortletWindowId} and the event consumer
 * can iterate over the {@link IPortletWindowId}s that have events and get a {@link Queue} of
 * un-processed events for each {@link IPortletWindowId}
 * 
 * This class is thread safe
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletEventQueue implements Iterable<IPortletWindowId> {
    private final ConcurrentMap<IPortletWindowId, Queue<Event>> portletEvents = new ConcurrentHashMap<IPortletWindowId, Queue<Event>>();
    
    /**
     * Queue an {@link Event} for the specified {@link IPortletWindowId}
     */
    public void offerEvent(IPortletWindowId portletWindowId, Event event) {
        Queue<Event> events = portletEvents.get(portletWindowId);
        if (events == null) {
            events = ConcurrentMapUtils.putIfAbsent(portletEvents, portletWindowId, new ConcurrentLinkedQueue<Event>());
        }
        
        events.offer(event);
    }
    
    /**
     * Remove the oldest {@link Event} from the Queue for the specified {@link IPortletWindowId}
     */
    public Event pollEvent(IPortletWindowId portletWindowId) {
        final Queue<Event> queue = this.portletEvents.get(portletWindowId);
        return queue != null ? queue.poll() : null;
    }

    /**
     * Get an {@link Iterator} of all {@link IPortletWindowId}s that have {@link Event}s queued.
     */
    @Override
    public Iterator<IPortletWindowId> iterator() {
        return this.portletEvents.keySet().iterator();
    }
}
