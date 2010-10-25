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

package org.jasig.portal.portlet.rendering.worker;

import org.jasig.portal.portlet.rendering.PortletRenderResult;

/**
 * Worker that executes a portlet render request 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletRenderExecutionWorker extends IPortletExecutionWorker<PortletRenderResult> {

    /**
     * @return The output written by the portlet
     */
    public String getOutput(long timeout) throws Exception;
}