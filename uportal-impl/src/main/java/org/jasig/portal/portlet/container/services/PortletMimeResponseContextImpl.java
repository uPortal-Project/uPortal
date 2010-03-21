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
import java.util.Locale;

import javax.portlet.CacheControl;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletMimeResponseContext;
import org.apache.pluto.container.PortletURLProvider;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.apache.pluto.container.util.PrintWriterServletOutputStream;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider;
import org.jasig.portal.portlet.url.PortletURLProviderImpl;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 * @version $Revision$
 */
abstract class PortletMimeResponseContextImpl extends PortletResponseContextImpl implements PortletMimeResponseContext
{

	private CacheControl cacheControl;
	private OutputStream outputStream;

	public PortletMimeResponseContextImpl(PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window, IPortletUrlSyntaxProvider portletUrlSyntaxProvider,
			IPortletWindowRegistry portletWindowRegistry, IPortletEntityRegistry portletEntityRegistry)
	{
		super(container, containerRequest, containerResponse, window);
	}

	public void close()
	{
		cacheControl = null;
		outputStream = null;
		super.close();
	}

	public void flushBuffer() throws IOException
	{
		if (!isClosed())
		{
			getServletResponse().flushBuffer();
		}
	}

	public int getBufferSize()
	{
		return getServletResponse().getBufferSize();
	}

	public CacheControl getCacheControl()
	{
		if (isClosed())
		{
			return null;
		}
		if (cacheControl == null)
		{
			cacheControl = new CacheControlImpl();
		}
		return cacheControl;
	}

	public String getCharacterEncoding()
	{
		return isClosed() ? null : getServletResponse().getCharacterEncoding();
	}

	public String getContentType()
	{
		return isClosed() ? null : getServletResponse().getContentType();
	}

	public Locale getLocale()
	{
		return isClosed() ? null : getServletResponse().getLocale();
	}

	public OutputStream getOutputStream() throws IOException, IllegalStateException {
		if (isClosed())
		{
			return null;
		}
		if (outputStream == null)
		{
			try
			{
				outputStream = getServletResponse().getOutputStream();
			}
			catch (IllegalStateException e)
			{
				// handle situation where underlying ServletResponse its getWriter()
				// has been called already anyway: return a wrapped PrintWriter in that case
				outputStream = new PrintWriterServletOutputStream(getServletResponse().getWriter(),
						getServletResponse().getCharacterEncoding());
			}
		}
		return outputStream;
	}

	public PrintWriter getWriter() throws IOException, IllegalStateException {
		return isClosed() ? null : getServletResponse().getWriter();
	}

	public boolean isCommitted() {
		return getServletResponse().isCommitted();
	}

	public void reset() {
		getServletResponse().reset();
	}

	public void resetBuffer() {
		if (!isClosed())
		{
			getServletResponse().resetBuffer();
		}
	}

	public void setBufferSize(int size) {
		if (!isClosed())
		{
			getServletResponse().setBufferSize(size);
		}
	}

	public void setContentType(String contentType) {
		if (!isClosed())
		{
			getServletResponse().setContentType(contentType);
		}
	}


	public PortletURLProvider getPortletURLProvider(TYPE type) {
		if(!isClosed()) {
			PortletURLProvider provider = new PortletURLProviderImpl(getPortalURL(), type, getPortletWindow());
			return provider;
		} else {
			return null;
		}
	}

	private static class CacheControlImpl implements CacheControl
	{
		private String eTag;
		private int expirationTime;
		private boolean publicScope;
		private boolean cachedContent;

		public CacheControlImpl()
		{
		}

		public boolean useCachedContent()
		{
			return cachedContent;
		}

		public String getETag()
		{
			return this.eTag;
		}

		public int getExpirationTime()
		{
			return expirationTime;
		}

		public boolean isPublicScope()
		{
			return publicScope;
		}

		public void setETag(String eTag)
		{
			this.eTag = eTag;
		}

		public void setExpirationTime(int expirationTime)
		{
			this.expirationTime = expirationTime;
		}

		public void setPublicScope(boolean publicScope)
		{
			this.publicScope = publicScope;
		}

		public void setUseCachedContent(boolean cachedContent)
		{
			this.cachedContent = cachedContent;
		}
	}
}
