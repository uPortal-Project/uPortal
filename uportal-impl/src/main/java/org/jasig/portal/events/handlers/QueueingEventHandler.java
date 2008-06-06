/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.events.handlers;

import java.util.ArrayList;
import java.util.List;
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
 * {@link BatchingEventHandler} when the <code>flushCount</code> is reached or when {@link #flush()} is called.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class QueueingEventHandler extends AbstractLimitedSupportEventHandler implements DisposableBean {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final Queue<PortalEvent> eventQueue = new ConcurrentLinkedQueue<PortalEvent>();
    private final AtomicLong eventCount = new AtomicLong(0);
    private final Lock flushLock = new ReentrantLock();
    
    private int flushCount = 50;
    private BatchingEventHandler batchingEventHandler;
    
    /**
     * @return the flushCount
     */
    public int getFlushCount() {
        return this.flushCount;
    }
    /**
     * @param flushCount the flushCount to set
     */
    public void setFlushCount(int flushCount) {
        this.flushCount = flushCount;
    }

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
        this.flush(false);
    }
    
    /**
     * Calls {@link #flush(boolean)} passing <code>true</code> to force a flush.
     * @see #flush(boolean)
     */
    public void flush() {
        this.flush(true);
    }
    
    /**
     * Flushes the queued PortalEvents to the configured {@link BatchingEventHandler}. If <code>force</code> is false
     * flushing only happens if there are enough events in the queue and a flush isn't already under way. If 
     * <code>force</code> is true all queued events will be flushed and the calling thread will wait until any previously
     * executing flush call completes before flushing
     * 
     * @param force Forces flushing events to the {@link BatchingEventHandler} even if there are fewer than <code>flushCount</code> PortalEvents in the queue.
     */
    public void flush(boolean force) {
        final int eventCount = this.eventCount.intValue();
        
        //If the force parameter is set get (waiting if nessesary) the lock
        if (force) {
            this.flushLock.lock();
        }
        //If not a forced flush and there are enough events in the que try to get the lock, if this fails just return.
        else if (eventCount < this.flushCount || !this.flushLock.tryLock()) {
            return;
        }

        //Get an array of the events
        final List<PortalEvent> flushedEventList = new ArrayList<PortalEvent>(eventCount);
        try {
            for (int index = 0; index < eventCount; index++) {
                final PortalEvent event = this.eventQueue.poll();
                if (event == null) {
                    break;
                }
                flushedEventList.add(event);
            }
            
            //Decrement the event count
            this.eventCount.addAndGet(-flushedEventList.size());
        }
        finally {
            this.flushLock.unlock();
        }
        
        if (flushedEventList.size() > 0) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Flushing " + flushedEventList.size() + " PortalEvents to " + this.batchingEventHandler);
            }

            this.batchingEventHandler.handleEvents(flushedEventList.toArray(new PortalEvent[eventCount]));
        }
    }
}
