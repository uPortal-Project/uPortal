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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.portlet.CacheControl;
import javax.portlet.PortletMode;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletRenderResponseContext;
import org.apache.pluto.container.PortletURLProvider;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.ResourceURLProvider;
import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 * @version $Revision$
 */
public class PortletRenderResponseContextImpl implements
		PortletRenderResponseContext {

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRenderResponseContext#setNextPossiblePortletModes(java.util.Collection)
	 */
	@Override
	public void setNextPossiblePortletModes(Collection<PortletMode> portletModes) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRenderResponseContext#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletMimeResponseContext#flushBuffer()
	 */
	@Override
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletMimeResponseContext#getBufferSize()
	 */
	@Override
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletMimeResponseContext#getCacheControl()
	 */
	@Override
	public CacheControl getCacheControl() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletMimeResponseContext#getCharacterEncoding()
	 */
	@Override
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletMimeResponseContext#getContentType()
	 */
	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletMimeResponseContext#getLocale()
	 */
	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletMimeResponseContext#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() throws IOException,
			IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletMimeResponseContext#getPortletURLProvider(org.apache.pluto.container.PortletURLProvider.TYPE)
	 */
	@Override
	public PortletURLProvider getPortletURLProvider(TYPE type) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletMimeResponseContext#getWriter()
	 */
	@Override
	public PrintWriter getWriter() throws IOException, IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletMimeResponseContext#isCommitted()
	 */
	@Override
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletMimeResponseContext#reset()
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletMimeResponseContext#resetBuffer()
	 */
	@Override
	public void resetBuffer() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletMimeResponseContext#setBufferSize(int)
	 */
	@Override
	public void setBufferSize(int size) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletMimeResponseContext#setContentType(java.lang.String)
	 */
	@Override
	public void setContentType(String contentType) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#addProperty(javax.servlet.http.Cookie)
	 */
	@Override
	public void addProperty(Cookie cookie) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#getContainer()
	 */
	@Override
	public PortletContainer getContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#getContainerRequest()
	 */
	@Override
	public HttpServletRequest getContainerRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#getContainerResponse()
	 */
	@Override
	public HttpServletResponse getContainerResponse() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#getPortletWindow()
	 */
	@Override
	public PortletWindow getPortletWindow() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#getResourceURLProvider()
	 */
	@Override
	public ResourceURLProvider getResourceURLProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#getServletRequest()
	 */
	@Override
	public HttpServletRequest getServletRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#getServletResponse()
	 */
	@Override
	public HttpServletResponse getServletResponse() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResponseContext#init(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void init(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
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
