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

package org.jasig.portal.url.processing;

import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.url.IWritableHttpServletRequest;

/**
 * IRequestParameterController presents an interface that is capable of processing an incoming
 * request and creating URL-generating objects, all according to the internal syntax.
 *
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision: 11911 $
 */
public interface IRequestParameterProcessorController {
    /**
     * Analyze current request, process necessary URL parameters,
     * and deliver information to the appropriate components.
     * 
     * @param req the incoming request
     * @param res the outgoing response
     * @throws IllegalArgumentException if req or res are null.
     */
    public void processParameters(IWritableHttpServletRequest req, HttpServletResponse res);
}
