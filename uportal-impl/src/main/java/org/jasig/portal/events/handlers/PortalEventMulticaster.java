/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.events.handlers;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.jasig.portal.events.EventHandler;
import org.jasig.portal.events.PortalEvent;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalEventMulticaster extends AbstractLimitedSupportEventHandler {
    private final Collection<EventHandler> handlers = new LinkedHashSet<EventHandler>();
    private TaskExecutor taskExecutor = new SyncTaskExecutor();

    /**
     * Set the TaskExecutor to execute event handlers with.
     * <p>Default is a SyncTaskExecutor, executing the handlers synchronously
     * in the calling thread.
     * <p>Consider specifying an asynchronous TaskExecutor here to not block the
     * caller until all handlers have been executed. However, note that asynchronous
     * execution will not participate in the caller's thread context (class loader,
     * transaction association) unless the TaskExecutor explicitly supports this.
     * @see org.springframework.core.task.SyncTaskExecutor
     * @see org.springframework.core.task.SimpleAsyncTaskExecutor
     * @see org.springframework.scheduling.timer.TimerTaskExecutor
     */
    public final void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = (taskExecutor != null ? taskExecutor : new SyncTaskExecutor());
    }

    /**
     * Return the current TaskExecutor for this multicaster.
     */
    public final TaskExecutor getTaskExecutor() {
        return this.taskExecutor;
    }

    /**
     * @param handlers the handlers to set
     */
    public final void setHandlers(Collection<EventHandler> listeners) {
        this.handlers.clear();
        this.handlers.addAll(listeners);
    }
    public final void addListener(EventHandler listener) {
        this.handlers.add(listener);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (this.handlers.size() == 0) {
            this.logger.warn("No PortalEventListeners are configured");
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.events.EventHandler#handleEvent(org.jasig.portal.events.PortalEvent)
     */
    public void handleEvent(PortalEvent event) {
        for (final EventHandler current : this.handlers) {
            if (current.supports(event)) {
                this.handleEventInternal(event, current);
            }
        }
    }

    /**
     * sub-classes can override this method to provide different handling for each  {@link PortalEvent} and
     * {@link EventHandler}. The default implementation simply calls {@link EventHandler#supports(PortalEvent)} 
     * and {@link EventHandler#handleEvent(PortalEvent)}.
     */
    protected void handleEventInternal(final PortalEvent event, final EventHandler handler) {
        this.taskExecutor.execute(new PortalEventTask(event, handler));
    }

    protected static class PortalEventTask implements Runnable {
        private final PortalEvent event;
        private final EventHandler handler;

        public PortalEventTask(final PortalEvent event, final EventHandler handler) {
            this.event = event;
            this.handler = handler;
        }

        public void run() {
            if (this.handler.supports(this.event)) {
                this.handler.handleEvent(this.event);
            }
        }
    }
}
