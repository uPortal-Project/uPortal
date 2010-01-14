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

package org.jasig.portal;


/**
 * An interface for worker request processors.
 * Note: workers are required for functionality that requires complete
 * control over the servlet output stream and, at the same time, requires
 * access to the internal structures.
 *
 * @version $Revision$
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @deprecated IChannel rendering code will be replaced with portlet specific rendering code in a future release
 */
@Deprecated
public interface IWorkerRequestProcessor {

    /**
     * Process a worker request.
     *
     * @param pcs a <code>PortalControlStructures</code> object
     * @exception PortalException if an error occurs
     */
    public void processWorkerDispatch(PortalControlStructures pcs) throws PortalException;

}
