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

import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Defines methods for initiating and managing portlet rendering
 * 
 * Note from  Portlet 2.0 specification: 
 * <ul>
 * <li>PLT 10.4.2 Runtime Option javax.portlet.renderHeaders</li>
 * <li>PLT 11.1.4.3 The Render Part Request Attribute for Setting Headers in the Render Phase</li>
 * </ul>
 * These sections only require that the portlet container issue the render method of 
 * the portlet twice; once with the RENDER_PART request attribute set to RENDER_HEADERS, 
 * once with the RENDER_PART request attribute set to RENDER_MARKUP.
 * 
 * There is no mention of the order of these 2 render calls; we assume it is acceptable
 * to invoke them in either order.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletExecutionManager {

    public void doPortletAction(IPortletEntityId portletEntityId, HttpServletRequest request, HttpServletResponse response);

    public void doPortletAction(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response);
    
    /**
     * @see #startPortletHeaderRender(IPortletWindowId, HttpServletRequest, HttpServletResponse)
     */
    public void startPortletHeaderRender(String subscribeId, HttpServletRequest request, HttpServletResponse response);

    /**
     * Initiates the rendering worker for the portlet's HEAD output.
     * Returns immediately.
     * 
     * @param portletWindowId
     * @param request
     * @param response
     */
    public void startPortletHeaderRender(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response);
    
    /**
     * @see #startPortletRender(IPortletWindowId, HttpServletRequest, HttpServletResponse)
     */
    public void startPortletRender(String subscribeId, HttpServletRequest request, HttpServletResponse response);

    /**
     * Initiates the rendering worker for the portlet's BODY output.
     * Returns immediately.
     * 
     * @param portletWindowId
     * @param request
     * @param response
     */
    public void startPortletRender(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response);
    
    /**
     * 
     * @param portletWindowId
     * @param request
     * @param response
     */
    public void doPortletServeResource(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response);
    
    /**
     * @see #isPortletRenderHeaderRequested(IPortletWindowId, HttpServletRequest, HttpServletResponse)
     */
    public boolean isPortletRenderHeaderRequested(String subscribeId, HttpServletRequest request, HttpServletResponse response);
    
    /**
     * @return true if the specified portlet been requested to render it's output for the HEAD during this request.
     */
    public boolean isPortletRenderHeaderRequested(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response);
    
    /**
     * @see #isPortletRenderRequested(IPortletWindowId, HttpServletRequest, HttpServletResponse)
     */
    public boolean isPortletRenderRequested(String subscribeId, HttpServletRequest request, HttpServletResponse response);
    
    /**
     * @return true if the specified portlet been requested to render it's output for the BODY during this request.
     */
    public boolean isPortletRenderRequested(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response);

    /**
     * @see #getPortletHeadOutput(IPortletWindowId, HttpServletRequest, HttpServletResponse)
     * @param subscribeId
     * @param request
     * @param response
     * @return the HEAD output for the specified portlet
     */
    public String getPortletHeadOutput(String subscribeId, HttpServletRequest request, HttpServletResponse response);
    
    /**
     * 
     * @param portletWindowId
     * @param request
     * @param response
     * @return the HEAD output for the specified portlet
     */
    public String getPortletHeadOutput(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response);
    
    /**
     * @see #outputPortlet(IPortletWindowId, HttpServletRequest, HttpServletResponse, Writer)
     */
    public String getPortletOutput(String subscribeId, HttpServletRequest request, HttpServletResponse response);

    /**
     * Writes the specified portlet content to the Writer. If the portlet was already rendering due to a previous call to
     * {@link #startPortletRender(IPortletWindowId, HttpServletRequest, HttpServletResponse)} the output from that render will
     * be used. If the portlet is not already rendering it will be started.
     */
    public String getPortletOutput(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response);


    /**
     * @see #getPortletTitle(IPortletWindowId, HttpServletRequest, HttpServletResponse)
     */
    public String getPortletTitle(String subscribeId, HttpServletRequest request, HttpServletResponse response);

    /**
     * Gets the title for the specified portlet
     */
    public String getPortletTitle(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response);

    public int getPortletNewItemCount(String subscribeId, HttpServletRequest request, HttpServletResponse response);

    public int getPortletNewItemCount(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response);

    public String getPortletLink(IPortletWindowId portletWindowId, String defaultUrl, HttpServletRequest request, HttpServletResponse response);
}