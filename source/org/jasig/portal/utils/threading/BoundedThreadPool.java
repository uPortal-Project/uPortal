/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils.threading;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A thread pool with a maxium number of possible worker threads
 *
 * @author <a href="mailto:mvi@immagic.com>Mike Ivanov</a>
 * @version $Revision$
 */

public class BoundedThreadPool extends AbstractPool{
    private static final Log LOG = LogFactory.getLog(BoundedThreadPool.class);

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
                  LOG.error("Failed to execute task: " + task, e);
                  IllegalStateException ise = new IllegalStateException("Failed to execute task: " + task + " With message: " + e.getMessage());
                  ise.initCause(e);
                  throw ise;
              }


	}
}