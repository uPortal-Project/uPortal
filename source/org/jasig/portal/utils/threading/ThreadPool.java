/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
	 * @return the total number of worker threads (busy and idle) in the pool
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
