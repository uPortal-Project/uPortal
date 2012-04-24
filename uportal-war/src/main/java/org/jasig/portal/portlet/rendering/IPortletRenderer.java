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

import javax.portlet.CacheControl;
import javax.portlet.Event;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.rendering.worker.HungWorkerAnalyzer;

/**
 * Provides easy API for executing methods on portlets. Takes care of all of the uPortal specific setup and tear down around portlet calls.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletRenderer {
    public static final WindowState EXCLUSIVE = new WindowState("EXCLUSIVE");
	public static final WindowState DETACHED = new WindowState("DETACHED");
	public static final WindowState DASHBOARD = new WindowState("DASHBOARD");
	public static final PortletMode ABOUT = new PortletMode("ABOUT");
	public static final PortletMode CONFIG = new PortletMode("CONFIG");
	
	/**
	 * Name of the {@link PortletRequest} attribute that the adaptor
	 * will store a Map of user info attributes that has support for multi-valued attributes.
	 */
	public static final String MULTIVALUED_USERINFO_MAP_ATTRIBUTE = "org.jasig.portlet.USER_INFO_MULTIVALUED";
	
	/**
	 * Name of the {@link PortletRequest} property that the portlet should use to set
	 * a new-item-count value.
	 */
	public static final String NEW_ITEM_COUNT_PROPERTY = "org.jasig.portlet.NEW_ITEM_COUNT";

    /**
     * Name of the {@link PortletRequest} property that the portlet should use to set
     * an external link
     */
    public static final String EXTERNAL_PORTLET_LINK_PROPERTY = "org.jasig.portlet.EXTERNAL_PORTLET_LINK";
    
    /**
     * Name of the {@link PortletRequest} property that the portlet should use to get
     * the current theme name
     */
    public static final String THEME_NAME_PROPERTY = "org.jasig.portlet.THEME_NAME";
	
	/**
	 * {@link javax.servlet.http.HttpServletRequest} attributes specific to the
	 * {@link IPortletRenderer} must be prefixed with this value to be sure they
	 * are protected from manipulation by the portlet.
	 */
	public static final String RENDERER_ATTRIBUTE_PREFIX = IPortletRenderer.class.getName();
	
	/**
	 * Attribute that the dynamic portlet title is stored using if set.
	 */
	public static final String ATTRIBUTE__PORTLET_TITLE = RENDERER_ATTRIBUTE_PREFIX + ".PORTLET_TITLE";
	
	/**
	 * Attribute that the dynamic portlet new item count is stored using if set.
	 */
	public static final String ATTRIBUTE__PORTLET_NEW_ITEM_COUNT = RENDERER_ATTRIBUTE_PREFIX + ".PORTLET_NEW_ITEM_COUNT";

    /**
     * Attribute that the dynamic external portlet link is stored using if set.
     */
	public static final String ATTRIBUTE__PORTLET_LINK = RENDERER_ATTRIBUTE_PREFIX + ".PORTLET_LINK";

	/**
	 * Attribute that the renderer stores a {@link PortletOutputHandler} or {@link PortletResourceOutputHandler} that should be used when the portlet writes out content.
	 */
	public static final String ATTRIBUTE__PORTLET_OUTPUT_HANDLER = RENDERER_ATTRIBUTE_PREFIX + ".PORTLET_OUTPUT_HANDLER";
	
	/**
     * Attribute that the renderer stores a {@link CacheControl} that should be used when the portlet writes out content.
     */
    public static final String ATTRIBUTE__PORTLET_CACHE_CONTROL = RENDERER_ATTRIBUTE_PREFIX + ".CACHE_CONTROL";
    
	
	/**
     * Executes an action in a portlet, handles all the request and response setup and teardown
     * 
     * @param portletWindowId Portlet to target with the action
     * @param httpServletRequest The portal's request
     * @param httpServletResponse The portal's response (nothing will be written to the response)
     */
    public long doAction(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
    
    /**
     * Executes an event in a portlet, handles all the request and response setup and teardown
     * 
     * @param portletWindowId Portlet to target with the action
     * @param httpServletRequest The portal's request
     * @param httpServletResponse The portal's response (nothing will be written to the response)
     */
    public long doEvent(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Event event);
    
    /**
     * Executes a render for the body of a portlet, handles all the request and response setup and teardown
     * 
     * @param portletWindowId Portlet to target with the render
     * @param httpServletRequest The portal's request
     * @param httpServletResponse The portal's response (nothing will be written to the response)
     * @param portletOutputHandler The output handler to write to
     */
    public PortletRenderResult doRenderMarkup(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, PortletOutputHandler portletOutputHandler) throws IOException;
    
    /**
     * Executes a render for the head of a portlet, handles all the request and response setup and teardown
     * 
     * @param portletWindowId Portlet to target with the render
     * @param httpServletRequest The portal's request
     * @param httpServletResponse The portal's response (nothing will be written to the response)
     * @param portletOutputHandler The output handler to write to
     */
    public PortletRenderResult doRenderHeader(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, PortletOutputHandler portletOutputHandler) throws IOException;
    
    /**
     * Executes a portlet resource request.
     * 
     * @param portletWindowId Portlet to target with the render
     * @param httpServletRequest The portal's request
     * @param httpServletResponse The portal's response (nothing will be written to the response)
     * @param portletOutputHandler The output handler to write to
     * @return The execution time for serving the resource
     */
    public long doServeResource(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, PortletResourceOutputHandler portletOutputHandler) throws IOException;
    
    /**
     * Resets a portlet's window data to the defaults and clears all portlet scoped session data
     * 
     * @param portletWindowId Portlet to target with the render
     * @param httpServletRequest The portal's request
     * @param httpServletResponse The portal's response (nothing will be written to the response)
     */
    public void doReset(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
    
    /**
     * @return HungWorkerAnalyzer that tracks hung portlet execution workers
     */
    public HungWorkerAnalyzer getHungWorkerAnalyzer();
}
