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
 * A thread pool with a maxium number of possible worker threads
 *
 * @author <a href="mailto:mvi@immagic.com>Mike Ivanov</a>
 * @version $Revision$
 */

public class BoundedThreadPool extends AbstractPool{

	/**
	 * BoundedThreadPool Construcutor
	 *
	 * @param minThreads the min number of worker threads that can be in this pool
	 * @param maxThreads the max number of worker threads that can be in this pool
	 * @param threadPriority the priority these worker threads should have
	 */
	public BoundedThreadPool(int minThreads, int maxThreads, int threadPriority) {
                super(minThreads,maxThreads,threadPriority);
	}


        /**
	 * Creates a new thread
	 */
	protected Thread createNewThread() throws Exception {
			Worker worker = new Worker(this,workQueue);
			worker.setDaemon(true);
			worker.setPriority(priority);
            return worker;
	}


        /**
	 * Destroyed a pooled thread
	 */
	public synchronized void destroyThread( Thread thread ) {
		if (isDestroyed) {
			return;
		}

		Worker worker = (Worker) thread;
        worker.stopWorker();
		worker = null;
	}


	/**
	* Queues up a task to be executed.  The queue use FIFO ordering.
	*
	* @param task the task to be executed
	*
	* @return the WorkerTracker for used to track and interact with this task
	*
	* @exception IllegalStateException - thrown if the pool has been destroyed
	*/
	public WorkTracker execute(WorkerTask task) throws IllegalStateException {
               if(isDestroyed){
			throw new IllegalStateException ("This thread pool has been destroyed, no additional tasks may be executed.");
               }

              try {

                adjustThreadPool();

		        WorkTracker tracker = new WorkTracker(task);
		        task.setWorkTracker(tracker);

                workQueue.put(task);

                return tracker;

              } catch ( Exception e ) {
                  throw new IllegalStateException(e.getMessage());
                }


	}
}