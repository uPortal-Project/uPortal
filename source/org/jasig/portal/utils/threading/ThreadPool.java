/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or withoutu
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.utils.threading;

/**
 * An interface for a generic pool of worker threads.
 *
 * @author <a href="mailto:clajoie@vt.edu>Chad La Joie</a>
 * @version $Revision$
 */

public interface ThreadPool {

	/**
	 * Queues up a task to be executed.  The queue use FIFO ordering.
	 *
	 * @param task the task to be executed
	 *
	 * @return the WorkerTracker for used to track and interact with this task
	 *
	 * @exception IllegalStateException - thrown if the pool has been destroyed
	 */
	public WorkTracker execute(WorkerTask task) throws IllegalStateException;

	/**
	 * Gives the total number of threads in the pool
	 *
	 * @retur the total number of worker threads (busy and idle) in the pool
	 */
	public int totalThreads() throws IllegalStateException;

	/**
	 * Gives the number of idle threads in the pool
	 *
	 * @return the number of idle worker threads in the pool
	 */
	public int idleThreads() throws IllegalStateException;

	/**
	 * Gives the number of busy (working) worker threads in the pool
	 *
	 * @return the number of busy workers in the pool
	 */
	public int busyThreads() throws IllegalStateException;

	/**
	 * Release the thread to the pool
	 */
	public void releaseThread(Thread thread) throws Exception;

        /**
	 * Locks the thread to the pool
	 */
	public void lockThread(Thread thread) throws Exception;

	/**
	 * Gets the pooled thread
	 */
	public Thread getPooledThread() throws Exception;

        /**
	 * Destroys the pooled thread
	 */
	public void destroyThread(Thread thread);

	/**
	 * Destroys the pool and all it's threads.  A destroyed pool thread can not be used
	 * after is has been destroyed.  Threads running when a pool is destroyed are interrupted
	 * and then destroyed.
	 */
	public void destroy();

}
