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
package org.apereo.portal.events.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apereo.portal.spring.context.FilteringApplicationListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEvent;

/**
 * Queues PortalEvents in a local {@link ConcurrentLinkedQueue} and flushes the events to the
 * configured {@link BatchingEventHandler} when {@link #flush()} is called. This class must be used
 * with some external timer that will call {@link #flush()} at regular intervals
 *
 */
public abstract class QueueingEventHandler<E extends ApplicationEvent>
        extends FilteringApplicationListener<E> implements DisposableBean {

    private final Queue<E> eventQueue = new ConcurrentLinkedQueue<E>();
    private final Lock flushLock = new ReentrantLock();
    private int batchSize = 25;

    //Used to hold events to flush, MUST only be read/written from within the flushLock
    private List<E> eventBuffer = new ArrayList<E>(this.batchSize);

    /** The maximum number of events to be flushed to the {@link BatchingEventHandler} per call. */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        eventBuffer = new ArrayList<E>(this.batchSize);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    @Override
    public final void destroy() throws Exception {
        this.flush();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.spring.context.FilteringApplicationListener#onFilteredApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected final void onFilteredApplicationEvent(E event) {
        this.eventQueue.offer(event);
    }

    /**
     * Handle a batch of application events, these events have been filtered by the parent {@link
     * FilteringApplicationListener}
     *
     * @param events Events to handle
     */
    protected abstract void onApplicationEvents(Iterable<E> events);

    /**
     * Flushes the queued PortalEvents to the configured {@link BatchingEventHandler}. If <code>
     * force</code> is false flushing only happens if there are enough events in the queue and a
     * flush isn't already under way. If <code>force</code> is true all queued events will be
     * flushed and the calling thread will wait until any previously executing flush call completes
     * before flushing
     *
     * @param force Forces flushing events to the {@link BatchingEventHandler} even if there are
     *     fewer than <code>flushCount</code> PortalEvents in the queue.
     */
    public final void flush() {
        if (eventQueue.isEmpty()) {
            //No events to flush
            logger.trace("No events to flush, returning.");
            return;
        }

        //Only one thread should be flushing at a time, try to get the flush lock and if it
        //is already held just return.
        if (!this.flushLock.tryLock()) {
            logger.trace("FlushLock already held, returning.");
            return;
        }
        try {
            while (!this.eventQueue.isEmpty()) {
                //Clear the buffer for re-use
                eventBuffer.clear();

                //Pop events off the queue into the buffer
                while (!this.eventQueue.isEmpty() && eventBuffer.size() < this.batchSize) {
                    final E event = eventQueue.poll();
                    eventBuffer.add(event);
                }

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Flushing " + eventBuffer.size() + " events");
                }

                //Write events out to batching listener
                try {
                    this.onApplicationEvents(eventBuffer);
                } catch (Throwable t) {
                    this.logger.error(
                            "An exception was thrown while trying to flush "
                                    + eventBuffer.size()
                                    + " events",
                            t);

                    final StringBuilder failedEvents = new StringBuilder();
                    failedEvents.append(
                            "The following events that were being flushed, some may have been persisted correctly");

                    for (final E portalEvent : eventBuffer) {
                        failedEvents.append("\n\t");
                        try {
                            failedEvents.append(portalEvent.toString());
                        } catch (Exception e) {
                            failedEvents
                                    .append("toString failed on a PortalEvent of type '")
                                    .append(portalEvent.getClass())
                                    .append("': ")
                                    .append(e);
                        }
                    }

                    this.logger.error(failedEvents.toString(), t);
                }
            }

        } finally {
            //Clear the buffer to avoid memory leaks
            eventBuffer.clear();

            this.flushLock.unlock();
        }
    }
}
