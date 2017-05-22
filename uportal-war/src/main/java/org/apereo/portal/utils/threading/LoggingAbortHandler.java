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
package org.apereo.portal.utils.threading;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Logs a more useful error message when rejecting a runnable
 *
 */
public class LoggingAbortHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        throw new RejectedExecutionException(
                "Rejecting execution of "
                        + r
                        + ". activeCount="
                        + executor.getActiveCount()
                        + ". corePoolSize="
                        + executor.getCorePoolSize()
                        + ". poolSize="
                        + executor.getPoolSize()
                        + ". maxPoolSize="
                        + executor.getMaximumPoolSize()
                        + ". queueSize="
                        + executor.getQueue().size()
                        + ". taskCount="
                        + executor.getTaskCount()
                        + ". completedTaskCount="
                        + executor.getCompletedTaskCount());
    }
}
