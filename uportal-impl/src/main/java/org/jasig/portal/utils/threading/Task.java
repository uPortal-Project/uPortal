/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.utils.threading;

/**
 * A task which can be executed asynchronously
 * @author Aaron Hamid (arh14 at cornell dot edu)
 */
public interface Task extends Runnable {
  /**
   * Returns the exception that occurred during execution, if any
   * @return the exception that occurred during execution, if any
   */
  Exception getException();
}