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
 * This class allows WorkerTasks to be tracked and stopped.
 * 
 * @author <a href="mailto:clajoie@vt.edu>Chad La Joie</a>
 * @version $Revision$
 */
public final class WorkTracker {
	
	final public static int READY = 0;
	final public static int RUNNING = 1;
	final public static int DONE = 2;
	final public static int KILLED = 3;

	private WorkerTask work;
	private int state;
	private boolean jobSuccessful;
	private Exception exception;

	/**
	 * WorkTracker Constructor
	 * 
	 * @param task the task this tracker is associated with
	 */	
	public WorkTracker(WorkerTask task){
		work = task;
		state = 0;
		jobSuccessful = false;
		exception = null;
	}
	
	/**
	 * Updates the status of this tracker, and notifies all threads waiting on this object.
	 * 
	 * @param state the current state of the task associated with this tracker
	 * @param succcessful whether the task associated with this tracker has been completed succesfully
	 * @param exception the exception thrown by the task associated with this tracker
	 */
	public synchronized void updateStatus(int state, boolean succcessful, Exception exception){
		this.state = state;
		jobSuccessful = succcessful;
		this.exception = exception;
                this.notifyAll();
	}
	
	/**
	 * Kills the task associated with tracker
	 */
	public synchronized void killJob(){
		if(state == READY || state == RUNNING){
			work.kill();
		}
	}
	
	/**
	 * Gets the exception encountered by the task this tracker is associated with
	 * 
	 * @return the exception encountered by the task this tracker is associated with
	 */
	public Exception getException(){
		return exception;
	}
	
	/**
	 * Checks if task this tracker is associated with has completed successfully
	 * Note, a job will never have a chance to be successful until it has completed.
	 * 
	 * @return true if the task has completed successfully, false if not
	 */
	public boolean isJobSuccessful(){
		return jobSuccessful;
	}
	
	/**
	 * Checks to see if the task associated with this tracker is complete
	 * 
	 * @return true if the task associated with this tracker is complete, false if not
	 */
	public boolean isJobComplete(){
		if(state == DONE || state == KILLED){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Gets the state of the task associated with this tracker
	 * 
	 * @return the state of the task associated with this tracker
	 */
	public int getState(){
		return state;
	}
	
	/**
	 * De-associated this tracker with it's current task.
	 * This method should never be used, except by the Worker thread 
	 */
	public void deregisterWork(){
		work = null;
	}
	
	/**
	 * Returns a string representaiton of this task giving it's state, the job success status, 
	 * and the exception if there is any.
	 * 
	 * @return a string representaiton of this task
	 */	
	public String toString(){
		String str = new String();
		
		switch(state){
			case 0:
				str +="State: READY";
				break;
			case 1:
				str +="State: RUNNING";
				break;
			case 2:
				str +="State: DONE";
				break;
			case 3:
				str +="State: KILLED";
				break;
		}
		
		str += ", Job Successful: " + jobSuccessful;
		
		if(exception != null){
			str += ", Exception: " + exception.getMessage();
		}
		
		return str;
	}	
}
