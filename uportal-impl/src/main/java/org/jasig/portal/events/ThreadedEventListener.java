/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.portal.events;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.utils.threading.PriorityThreadFactory;

/**
 * Implementation of <code>EventListener</code> that assumes that
 * EventHandlers will be doing something I/O Intensive that they may
 * block users from seeing a "snappy" response, and thus call the <code>EventHandler</code>s in their own thread.
 * <p>This uses The Backport Concurrent Library to provide threading and has the following defaults:
 * <ul>
 * <li>Initial Threads: 15</li>
 * <li>Max Threads: 30</li>
 * <li>Thread Priority: 5</li>
 * </ul>
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 * @deprecated Use {@link PortalEventListener} and {@link org.jasig.portal.events.handlers.PortalEventMulticaster}
 */
@Deprecated
public final class ThreadedEventListener extends AbstractEventListener {

	/** Default value for initial thread size. */
	private static final int DEFAULT_INITIAL_THREADS = 15;

	/** Default value of the maximum number of threads. */
	private static final int DEFAULT_MAX_THREADS = 30;

	/** Default thread priority. */
	private static final int DEFAULT_THREAD_PRIORITY = 5;

	/** Instance of thread pool. */
	private ExecutorService threadPool;

	/** Initial size of ThreadPool. */
	private int initialThreads = DEFAULT_INITIAL_THREADS;

	/** Maximum size of ThreadPool. */
	private int maxThreads = DEFAULT_MAX_THREADS;

	/** Priority of Threads. */
	private int threadPriority = DEFAULT_THREAD_PRIORITY;

	protected void afterPropertiesSetInternal() throws Exception {
		this.threadPool = new ThreadPoolExecutor(initialThreads, maxThreads,
				0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(),
				new PriorityThreadFactory(threadPriority, "Priority", PortalSessionManager.getThreadGroup()));
	}

	protected void onApplicationEventInternal(final PortalEvent event,
			final EventHandler handler) {
		this.threadPool.execute(new Task(event, handler));
	}


	public void setInitialThreads(final int initialThreads) {
		this.initialThreads = initialThreads;
	}

	public void setMaxThreads(final int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public void setThreadPriority(final int threadPriority) {
		this.threadPriority = threadPriority;
	}

	private class Task implements Runnable {

		private final PortalEvent event;

		private final EventHandler handler;

		public Task(final PortalEvent event, final EventHandler handler) {
			this.event = event;
			this.handler = handler;
		}

		public void run() {
			this.handler.handleEvent(event);
		}
	}
}
