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
package org.jasig.portal.portlet.rendering;

import java.util.Map;


public interface PortletExecutionManagerMXBean {

    /**
     * @param maxEventIterations The maximum number of iterations to spend dispatching events for a single event request.
     */
    void setMaxEventIterations(int maxEventIterations);
    
    /**
     * @return The maximum number of iterations to spend dispatching events for a single event request.
     */
    int getMaxEventIterations();

    /**
     * @param ignoreTimeouts If true timeouts will be ignored
     */
    void setIgnoreTimeouts(boolean ignoreTimeouts);
    
    /**
     * @return If true timeouts will be ignored
     */
    boolean isIgnoreTimeouts();

    /**
     * @param extendedTimeoutExecutions Number of executions from sever start to extend timeouts for each portlet
     */
    void setExtendedTimeoutExecutions(int extendedTimeoutExecutions);
    
    /**
     * @return Number of executions from sever start to extend timeouts for each portlet
     */
    int getExtendedTimeoutExecutions();
    
    /**
     * @param extendedTimeoutMultiplier Multiplier to use for extended timeouts
     */
    void setExtendedTimeoutMultiplier(long extendedTimeoutMultiplier);
    
    /**
     * @return Multiplier to use for extended timeouts
     */
    long getExtendedTimeoutMultiplier();
    
    /**
     * @return Number of times each portlet has been executed
     */
    Map<String, Integer> getPortletExecutionCounts();
    
}