/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
