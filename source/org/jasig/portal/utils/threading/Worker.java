/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils.threading;

/**
 * A ThreadPool worker thread.
 *
 * @author <a href="mailto:clajoie@vt.edu>Chad La Joie</a>
 * @version $Revision$
 */
public final class Worker extends Thread {

	private ThreadPool pool;
	private Queue work;
	private WorkerTask task;
	private WorkTracker tracker;
	private boolean continueWorking;
        private static int iThreadID = 0;

	/**
	 * Worker Constructor
	 *
	 * @param pool the ThreadPool that this worker belongs to
	 * @param work the queue of work for this thread
	 */
	public Worker(ThreadPool pool, Queue work) {
		this.pool = pool;
                this.work = work;
                continueWorking = true;
                iThreadID++;
                setName("uPortal thread pool worker #" + iThreadID);
	}


	/**
	 * The work of a worker thread
	 */
	public void run() {
		while (continueWorking) {
			try {
				task = (WorkerTask) work.take();
                                // Lock this thread
                                pool.lockThread(this);

                                task.setWorker(this);
				tracker = task.getWorkTracker();

				//check to make sure this job hasn't been killed before we got it
				if (tracker.getState() != WorkTracker.KILLED) {
					//indicate that task is now running
					tracker.updateStatus(WorkTracker.RUNNING, false, null);
					//run task
					task.run();

					//check to see if the task had an exception while running
					if (task.hasException()) {
						//indicate task is done, and had an exception
						tracker.updateStatus(WorkTracker.DONE, false, task.getException());
					} else {
						//indicate task is done and had no exceptions
						tracker.updateStatus(WorkTracker.DONE, true, null);
					}
				}

				cleanState();
                                // Release this thread
                                pool.releaseThread(this);

			} catch (Exception ie) {
				//check to make sure we weren't interrupted while idle
				if (tracker != null) {
					//indicate that the task was killed
					tracker.updateStatus(WorkTracker.KILLED, false, null);
					cleanState();
				}
				//clear this threads interrupted status
				Worker.interrupted();

			} finally {
                            Thread.yield();
			}
		}
	}

	/**
	 * Stops a worker
	 */
	public void stopWorker() {
		continueWorking = false;
                if ( !this.isInterrupted() )
		 this.interrupt();
                pool.destroyThread(this);
	}

	private void cleanState() {
		if (tracker != null) {
			tracker.deregisterWork();
			task.deregisterTracker();
		}
		tracker = null;
		task = null;
	}
}