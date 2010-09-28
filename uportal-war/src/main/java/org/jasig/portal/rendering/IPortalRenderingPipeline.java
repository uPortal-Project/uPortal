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

package org.jasig.portal.rendering;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.PortalException;

/**
 * Describes the entry point into the uPortal rendering pipeline.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalRenderingPipeline {
    /**
     * <code>renderState</code> method orchestrates the rendering pipeline which includes worker dispatching, and the
     * rendering process from layout access, to channel rendering, to writing content to the browser.
     * 
     * @param req the <code>HttpServletRequest</code>
     * @param res the <code>HttpServletResponse</code>
     * @exception PortalException if an error occurs
     */
    public void renderState(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException;
}
