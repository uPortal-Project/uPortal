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
