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