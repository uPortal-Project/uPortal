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
package org.jasig.portal.portlet.container.services;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletResponseContext;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.ResourceURLProvider;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 * @version $Revision$
 */
class PortletResponseContextImpl implements PortletResponseContext {

	private PortletContainer container;
	private HttpServletRequest containerRequest;
	private HttpServletResponse containerResponse;
	private HttpServletRequest servletRequest;
	private HttpServletResponse servletResponse;
	private PortletWindow window;

	PortletResponseContextImpl(PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		this.container = container;
		this.containerRequest = containerRequest;
		this.containerResponse = containerResponse;
		this.window = window;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#addProperty(javax.servlet.http.Cookie)
	 */
	@Override
	public void addProperty(Cookie cookie) {
		servletResponse.addCookie(cookie);
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#addProperty(java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void addProperty(String key, Element element) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#addProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public void addProperty(String key, String value) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#createElement(java.lang.String)
	 */
	@Override
	public Element createElement(String tagName) throws DOMException {
		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			final DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			final Document doc = builder.newDocument();
			Element element = doc.createElement(tagName);
			return element;
		} catch (ParserConfigurationException e) {
			throw new DOMException((short) 0, "failed to create a DocumentBuilder: " + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#getContainer()
	 */
	@Override
	public PortletContainer getContainer() {
		return this.container;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#getContainerRequest()
	 */
	@Override
	public HttpServletRequest getContainerRequest() {
		return this.containerRequest;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#getContainerResponse()
	 */
	@Override
	public HttpServletResponse getContainerResponse() {
		return this.containerResponse;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#getPortletWindow()
	 */
	@Override
	public PortletWindow getPortletWindow() {
		return this.window;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#getResourceURLProvider()
	 */
	@Override
	public ResourceURLProvider getResourceURLProvider() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#getServletRequest()
	 */
	@Override
	public HttpServletRequest getServletRequest() {
		return this.servletRequest;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#getServletResponse()
	 */
	@Override
	public HttpServletResponse getServletResponse() {
		return this.servletResponse;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#init(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void init(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		this.servletRequest = servletRequest;
		this.servletResponse = servletResponse;
	}

	/**
	 * From Pluto 2 javadoc:
	 * "Releasing the response context means its internal storage can be released as well. If any outstanding changes have not been processed yet, those will be lost."
	 *  (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#release()
	 */
	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#setProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public void setProperty(String key, String value) {
		// TODO Auto-generated method stub

	}

}
