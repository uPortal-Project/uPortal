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

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Defines methods for initiating and managing portlet rendering
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletExecutionManager {

    public void doPortletAction(IPortletEntityId portletEntityId, HttpServletRequest request, HttpServletResponse response);

    public void doPortletAction(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response);
    
    /**
     * Starts the specified portlet rendering, returns immediately.
     */
    public void startPortletRender(String subscribeId, HttpServletRequest request, HttpServletResponse response);

    /**
     * Starts the specified portlet rendering, returns immediately.
     */
    public void startPortletRender(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response);
    
    public boolean isPortletRenderRequested(String subscribeId, HttpServletRequest request, HttpServletResponse response);
    
    public boolean isPortletRenderRequested(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response);

    public void outputPortlet(String subscribeId, HttpServletRequest request, HttpServletResponse response, Writer writer) throws IOException;

    /**
     * Writes the specified portlet content to the Writer. If the portlet was already rendering due to a previous call to
     * {@link #startPortletRender(IPortletWindowId, HttpServletRequest, HttpServletResponse)} the output from that render will
     * be used. If the portlet is not already rendering it will be started.
     */
    public void outputPortlet(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response, Writer writer) throws IOException;

    /**
     * Gets the title for the specified portlet
     */
    public String getPortletTitle(String subscribeId, HttpServletRequest request, HttpServletResponse response);

    public String getPortletTitle(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response);

}