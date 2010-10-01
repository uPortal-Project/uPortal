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

package org.jasig.portal.events.handlers;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.events.BatchingEventHandler;
import org.jasig.portal.events.PortalEvent;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

/**
 * Queues PortalEvents in a local {@link ConcurrentLinkedQueue} and flushes the events to the configured
 * {@link BatchingEventHandler} when {@link #flush()} is called. This class must be used with some external
 * timer that will call {@link #flush()} at regular intervals
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class QueueingEventHandler extends AbstractLimitedSupportEventHandler implements DisposableBean {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final Queue<PortalEvent> eventQueue = new ConcurrentLinkedQueue<PortalEvent>();
    private final AtomicLong eventCount = new AtomicLong(0);
    private final Lock flushLock = new ReentrantLock();
    
    private int batchSize = 25;
    private BatchingEventHandler batchingEventHandler;
    
    
    /**
     * @return the batchingEventHandler
     */
    public BatchingEventHandler getBatchingEventHandler() {
        return batchingEventHandler;
    }
    /**
     * @param batchingEventHandler the batchingEventHandler to set
     */
    @Required
    public void setBatchingEventHandler(BatchingEventHandler batchingEventHandler) {
        Assert.notNull(batchingEventHandler, "batchingEventHandler can not be null");
        this.batchingEventHandler = batchingEventHandler;
    }
    
    /**
     * @return the batchSize
     */
    public int getBatchSize() {
        return batchSize;
    }
    /**
     * The maximum number of events to be flushed to the {@link BatchingEventHandler} per call.
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy() throws Exception {
        this.flush(true);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.events.EventHandler#handleEvent(org.jasig.portal.events.PortalEvent)
     */
    public void handleEvent(PortalEvent event) {
        this.eventQueue.offer(event);
        this.eventCount.incrementAndGet();
    }
    
    /**
     * @see #flush(boolean)
     */
    public void flush() {
        this.flush(false);
    }
    
    /**
     * Flushes the queued PortalEvents to the configured {@link BatchingEventHandler}. If <code>force</code> is false
     * flushing only happens if there are enough events in the queue and a flush isn't already under way. If 
     * <code>force</code> is true all queued events will be flushed and the calling thread will wait until any previously
     * executing flush call completes before flushing
     * 
     * @param ignoreFailure Ignore failures while flushing, don't log anything
     */
    public void flush(boolean ignoreFailure) {
        boolean hasMoreEvents = true;
        while (hasMoreEvents) {
            //Use a Lock instead of synchronized to avoid threads potentially waiting to flush
            if (!this.flushLock.tryLock()) {
                return;
            }
            
            try {
                final int pendingEventCount = this.eventCount.intValue();
                if (pendingEventCount == 0) {
                    return;
                }
                
                //Only flush up to the batch size with each iteration
                final int flushSize;
                if (pendingEventCount < this.batchSize) {
                    hasMoreEvents = false;
                    flushSize = pendingEventCount;
                }
                else {
                    hasMoreEvents = true;
                    flushSize = this.batchSize;
                }
                
                //Get an array of the events
                final PortalEvent[] flushedEvents = new PortalEvent[flushSize];
                for (int index = 0; index < flushedEvents.length; index++) {
                    final PortalEvent event = this.eventQueue.poll();
                    flushedEvents[index] = event;
                }
                
                //Decrement the event count
                this.eventCount.addAndGet(-flushedEvents.length);
                
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Flushing " + flushedEvents.length + " PortalEvents to " + this.batchingEventHandler);
                }
        
                try {
                    this.batchingEventHandler.handleEvents(flushedEvents);
                }
                catch (Throwable t) {
                    if (!ignoreFailure) {
                        this.logger.error("An exception was thrown while trying to flush " + flushedEvents.length + " PortalEvents to " + this.batchingEventHandler, t);
                        
                        final StringBuilder failedEvents = new StringBuilder();
                        failedEvents.append("The following is the list of events that was being flushed, some may have been persisted correctly");
                        
                        for (final PortalEvent portalEvent : flushedEvents) {
                            failedEvents.append("\n\t");
                            try {
                                failedEvents.append(portalEvent.toString());
                            }
                            catch (Exception e) {
                                failedEvents.append("toString failed on a PortalEvent of type '" + portalEvent.getClass() + "': " + e);
                            }
                        }
                        
                        this.logger.error(failedEvents, t);
                    }
                }
            }
            finally {
                this.flushLock.unlock();
            }
            
            if (hasMoreEvents && this.logger.isDebugEnabled()) {
                this.logger.debug("Has more events, looping until all pending events are flushed");
            }
        }
    }
}
