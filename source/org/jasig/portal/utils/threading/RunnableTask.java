/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.utils.threading;

/**
 * This class exists as a convenience to execute Runnables. Although it
 * may seem redundant, it is useful in that the superclass run() will
 * appropriately catch and store Exceptions, so that callers need not worry
 * about catching and handling them in their Runnable implementation.
 * @author Aaron Hamid (arh14 at cornell dot edu)
 */
public class RunnableTask extends BaseTask {
	protected Runnable runnable;

	/**
	 * Constructs a RunnableTask with a Runnable object
	 * @param runnable a runnable implementation
	 */
	public RunnableTask(Runnable runnable) {
		this.runnable = runnable;
	}

	/**
	 * Simply calls the run() on the runnable that this
	 * RunnableTask was constructed with.  BaseTask
	 * will catch and store any Exception the runnable,
	 * and hence this method, throws.
	 */
	public void execute() throws Exception {
		// may throw RuntimeExceptions
		// BaseTask will catch and expose them
		// through getException
		this.runnable.run();
	}
}