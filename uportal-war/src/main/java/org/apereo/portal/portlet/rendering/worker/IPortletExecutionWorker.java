/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.portlet.rendering.worker;

/**
 * @param <V>
 */
public interface IPortletExecutionWorker<V> extends IPortletExecutionContext {

    /**
     * Submit the worker for execution. The worker may start immediately or submit itself to a
     * thread pool or service for execution at a future time.
     *
     * <p>Submit should only be called ONCE
     */
    public void submit();

    /**
     * Wait for the worker to start.
     *
     * @param timeout The maximum time in ms to wait for the worker to start
     * @return The time in ms that the worker actually started, 0 if it has not started
     */
    public long waitForStart(long timeout) throws InterruptedException;

    /**
     * Get the object returned by the worker's execution
     *
     * @param timeout The maximum time in ms to wait for the worker to complete, if the worker has
     *     not started yet this includes time spent waiting to start
     * @return The object returned by the portlet execution
     * @throws Exception The exception thrown by the portlet during execution if any
     */
    public V get(long timeout) throws Exception;

    /** Cancel the worker, interrupting the thread that is executing the worker */
    public void cancel();

    /** @return The number of times that cancel has been called. */
    public int getCancelCount();

    /** @return true If {@link #get(long)} has been called */
    public boolean isRetrieved();
}
