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

import java.util.Collection;
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
 * This class and all data structures returned by it are thread safe
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletEventQueue implements Iterable<IPortletWindowId> {
    /**
     * Queue of portlet events that have not been resolved to target specific portlet windows 
     */
    private final Queue<QueuedEvent> rawEventQueue = new ConcurrentLinkedQueue<QueuedEvent>();
    
    /**
     * Map for portlet windows where the value is a queue of events that need to be dispatched to the portlet
     */
    private final ConcurrentMap<IPortletWindowId, Queue<QueuedEvent>> resolvedEventQueues = new ConcurrentHashMap<IPortletWindowId, Queue<QueuedEvent>>();
    
    PortletEventQueue() {
        //Only allow code in the same package to create the queue
    }
    
    /**
     * Add the collection of events to the raw event queue
     */
    public void addEvents(Collection<? extends QueuedEvent> events) {
        this.rawEventQueue.addAll(events);
    }
    
    /**
     * @return The Queue of unresolved events
     */
    public Queue<QueuedEvent> getUnresolvedEvents() {
        return this.rawEventQueue;
    }
    
    /**
     * Queue an {@link Event} for the specified {@link IPortletWindowId}
     */
    public void offerEvent(IPortletWindowId portletWindowId, QueuedEvent event) {
        Queue<QueuedEvent> events = resolvedEventQueues.get(portletWindowId);
        if (events == null) {
            events = ConcurrentMapUtils.putIfAbsent(resolvedEventQueues, portletWindowId, new ConcurrentLinkedQueue<QueuedEvent>());
        }
        
        events.offer(event);
    }
    
    /**
     * Remove the oldest {@link Event} from the Queue for the specified {@link IPortletWindowId}
     */
    public QueuedEvent pollEvent(IPortletWindowId portletWindowId) {
        final Queue<QueuedEvent> queue = this.resolvedEventQueues.get(portletWindowId);
        return queue != null ? queue.poll() : null;
    }

    /**
     * Get an {@link Iterator} of all {@link IPortletWindowId}s that have {@link Event}s queued.
     */
    @Override
    public Iterator<IPortletWindowId> iterator() {
        return this.resolvedEventQueues.keySet().iterator();
    }
}
