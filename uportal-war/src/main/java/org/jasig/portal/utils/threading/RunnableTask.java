/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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