/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils.threading;

/**
 * The WorkerTask class should be extended by any class that will be
 * executed by a ThreadPool.
 *
 * @author <a href="mailto:clajoie@vt.edu">Chad La Joie</a>
 * @version $Revision$
 */
public abstract class WorkerTask implements Runnable{

	private Exception exception = null;
	private WorkTracker tracker = null;
        private Worker worker = null;

	/**
	 * The work of a WorkerTask
	 */
	public abstract void run();

	/**
	 * Should be called in the run method to set an exception that occured while executing
	 *
	 * @param e the exception encountered in the run method
	 */
	public void setException(Exception e) {
		exception = e;
	}

	/**
	 * Returns the exception thrown in the run method, assuming it was set using setException
	 *
	 * @return the exception thrown in the run method, or null if there was, or it wasn't set
	 */
	public final Exception getException() {
		return exception;
	}

        /**
	 * Sets the worker associated with this task
	 *
	 * @param worker the Worker currently performing this task
	 */
	public final void setWorker(Worker worker) {
		this.worker = worker;
	}

        /**
	 * Gets the worker associated with this task
	 *
	 * @return the Worker currently performing this task
	 */
	public final Worker getWorker() {
		return this.worker;
	}

	/**
	 * Sets the tracker associated with this task
	 *
	 * @param tracker The WorkTracker associated with this task
	 */
	public final void setWorkTracker(WorkTracker tracker) {
		this.tracker = tracker;
	}

	/**
	 * Gets the WorkTracker associated with this task
	 *
	 * @return the WorkTracker associated with this task
	 */
	public final WorkTracker getWorkTracker() {
		return tracker;
	}

	/**
	 * Checks if this task has had an exception
	 *
	 * @return true if an exception has been set using setException(), false if not
	 */
	public final boolean hasException() {
		if(exception == null){
			return false;
		}else{
			return true;
		}
	}

	/**
	 * Disassociates the WorkerTracker associated with this task.
	 * This method should never be used, except by the Worker thread
	 */
	public final void deregisterTracker() {
		tracker = null;
	}

	/**
	 * Kills this task
	 */
	public final void kill() {
           if ( worker != null && !worker.isInterrupted() )
		worker.interrupt();
	}
}
