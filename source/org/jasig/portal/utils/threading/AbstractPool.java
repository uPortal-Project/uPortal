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
 * An abstract implementation of the ThreadPool interface which
 * implements the totalThreads(), idleThreads(), signalThreadIdle(), 
 * signalThreadWorking(), and destroy() methods.
 * 
 * @author <a href="mailto:clajoie@vt.edu">Chad La Joie</a>
 * @version $Revision$
 */

public abstract class AbstractPool implements ThreadPool {
	protected UnboundedQueue workQueue;
	protected int priority;
	protected boolean isDestroyed;
	private Object[] workerThreads;
	private int totalWorkers = 0;
	private int idleWorkers = 0;
	private Object idleThreadCountLock = new Object();

	/**
	* Queues up a task to be executed.  The queue use FIFO ordering.
	* 
	* @param task the task to be executed
	* 
	* @return the WorkerTracker for used to track and interact with this task
	* 
	* @exception IllegalStateException - thrown if the pool has been destroyed
	*/
	public abstract WorkTracker execute(WorkerTask task)
		throws IllegalStateException;

	/**
	 * Gives the total number of threads in the pool
	 * 
	 * @retur the total number of worker threads (busy and idle) in the pool
	 */
	public int totalThreads() throws IllegalStateException {
		if (isDestroyed) {
			throw new IllegalStateException("This pool has been destroyed!");
		}

		return totalWorkers;
	}

	/**
	 * Gives the number of idle threads in the pool
	 * 
	 * @return the number of idle worker threads in the pool
	 */
	public int idleThreads() throws IllegalStateException {
		if (isDestroyed) {
			throw new IllegalStateException("This pool has been destroyed!");
		}

		return idleWorkers;
	}

	/**
	 * Gives the number of busy (working) worker threads in the pool
	 * 
	 * @return the number of busy workers in the pool
	 */
	public int busyThreads() throws IllegalStateException{
		if (isDestroyed) {
			throw new IllegalStateException("This pool has been destroyed!");
		}
		
		return totalWorkers - idleWorkers;
	}
	
	/**
	 * Called by a worker thread to notify the pool that it is now idle
	 */
	public void signalThreadIdle() throws IllegalStateException {
		if (isDestroyed) {
			throw new IllegalStateException("This pool has been destroyed!");
		}

		synchronized (idleThreadCountLock) {
			idleWorkers++;
		}
	}

	/**
	 * Called by a worker thread to notify that pool that it is now busy
	 */
	public void signalThreadWorking() throws IllegalStateException {
		if (isDestroyed) {
			throw new IllegalStateException("This pool has been destroyed!");
		}

		synchronized (idleThreadCountLock) {
			idleWorkers--;
		}
	}

	/**
	 * Destroys the pool and all it's threads.  A destroyed pool thread can not be used
	 * after is has been destroyed.  Threads running when a pool is destroyed are interrupted
	 * and then destroyed.
	 */
	public void destroy() {
		if (isDestroyed) {
			return;
		}

		Worker worker;
		for (int i = workerThreads.length - 1; i >= 0; i--) {
			worker = (Worker) workerThreads[i];
			worker.stopWorker();
			workerThreads[i] = null;
		}
		workerThreads = null;
		workQueue = null;
	}

	/**
	 * Creates, sets the priority, and starts a given number of worker threads.
	 * 
	 * @param numberOfThreads the number of worker threads to be added to the pool
	 */
	protected void initWorkers(int numberOfThreads) {
		Worker worker;
		workerThreads = new Object[numberOfThreads];

		for (int i = 0; i < numberOfThreads; i++) {
			totalWorkers++;
			worker = new Worker(this, workQueue);
			workerThreads[i] = worker;
			worker.setDaemon(true);
			worker.setPriority(priority);
			worker.start();
		}
	}

}