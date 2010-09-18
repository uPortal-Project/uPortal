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

package org.jasig.portal.events;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
	
	private final ThreadGroup group = new ThreadGroup("uPortal");

	protected void afterPropertiesSetInternal() throws Exception {
		this.threadPool = new ThreadPoolExecutor(initialThreads, maxThreads,
				0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(),
				new PriorityThreadFactory(threadPriority, "Priority", group));
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
