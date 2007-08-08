/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.utils.threading;

/**
 * A convenience base task class for subclasses.
 * Introduces an execute() method which declares <code>throws Exception</code>,
 * which subclasses may override.  Any Exception thrown from <code>execute</code>
 * will be caught and stored, to be accessible through <code>getException</code>
 * NOTE: Throwable is not caught to avoid catching <code>Error</code>s
 * @author Aaron Hamid (arh14 at cornell dot edu)
 */

public abstract class BaseTask implements Task {
  protected Exception exception;

  /**
   * Run implementation which delegates to <code>execute()</code>.
   * Catches and stores any exception <code>execute()</code> throws.
   * @see #execute()
   */
  public void run() {
    try {
      execute();
    } catch (Exception e) {
      setException(e);
    }
  }

  /**
   * Only subclasses are allowed to use this
   * @param e exception to set
   */
  protected void setException(Exception e) {
    this.exception = e;
  }

  /**
   * Subclasses should implement this method
   * NOTE: not declaring throws Throwable.  We shouldn't really catch Errors, should we?
   * @throws Exception
   */
  public abstract void execute() throws Exception;

  /**
   * Returns the exception that was thrown during execution, if any
   * @return the exception that was thrown during execution, if any
   */
  public Exception getException() {
    return this.exception;
  }
}