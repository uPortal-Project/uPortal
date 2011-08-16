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

package org.jasig.portal.utils.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Portlet response wrapper. Makes sure the portlet doesn't screw with the portal's response 
 * 
 * Also provides facilities for overriding the {@link #getOutputStream()} implementation and capturing
 * headers.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletHttpServletResponseWrapper extends AbstractHttpServletResponseWrapper {

	private final ServletOutputStream alternate;
	private final boolean captureHeaders;
	private final Map<String, List<String>> capturedHeaders = Collections.synchronizedMap(new HashMap<String, List<String>>());
	/**
	 * Default constructor.
	 * @param httpServletResponse
	 */
    public PortletHttpServletResponseWrapper(HttpServletResponse httpServletResponse) {
        this(httpServletResponse, null, false);
    }    
    
    /**
     * 
     * @param httpServletResponse
     * @param outputStream
     */
    public PortletHttpServletResponseWrapper(HttpServletResponse httpServletResponse, ServletOutputStream alternate) {
        this(httpServletResponse, alternate, false);
    }
    
    /**
     * 
     * @param httpServletResponse
     * @param alternate
     * @param captureHeaders
     */
    public PortletHttpServletResponseWrapper(HttpServletResponse httpServletResponse, ServletOutputStream alternate, boolean captureHeaders) {
        super(httpServletResponse);
        this.alternate = alternate;
        this.captureHeaders = captureHeaders;
    }

	/* (non-Javadoc)
	 * @see org.jasig.portal.utils.web.AbstractHttpServletResponseWrapper#getOutputStream()
	 */
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if(this.alternate != null) {
			return this.alternate;
		}
		return super.getOutputStream();
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.utils.web.AbstractHttpServletResponseWrapper#setDateHeader(java.lang.String, long)
	 */
	@Override
	public void setDateHeader(String name, long date) {
		super.setDateHeader(name, date);
		safeCaptureHeader(name, Long.toString(date));
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.utils.web.AbstractHttpServletResponseWrapper#addDateHeader(java.lang.String, long)
	 */
	@Override
	public void addDateHeader(String name, long date) {
		super.addDateHeader(name, date);
		safeCaptureHeader(name, Long.toString(date));
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.utils.web.AbstractHttpServletResponseWrapper#setHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public void setHeader(String name, String value) {
		super.setHeader(name, value);
		safeCaptureHeader(name, value);
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.utils.web.AbstractHttpServletResponseWrapper#addHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public void addHeader(String name, String value) {
		super.addHeader(name, value);
		safeCaptureHeader(name, value);
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.utils.web.AbstractHttpServletResponseWrapper#setIntHeader(java.lang.String, int)
	 */
	@Override
	public void setIntHeader(String name, int value) {
		super.setIntHeader(name, value);
		safeCaptureHeader(name, Integer.toString(value));
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.utils.web.AbstractHttpServletResponseWrapper#addIntHeader(java.lang.String, int)
	 */
	@Override
	public void addIntHeader(String name, int value) {
		super.addIntHeader(name, value);
		safeCaptureHeader(name, Integer.toString(value));
	} 
    
	/**
	 * 
	 * @param name
	 * @param value
	 */
	private void safeCaptureHeader(String name, String value) {
		if(!captureHeaders) {
			return;
		}
		
		List<String> values = capturedHeaders.get(name);
		if(values == null) {
			values = new ArrayList<String>();
			capturedHeaders.put(name, values);
		}
		values.add(value);
	}
	/**
	 * 
	 * @return
	 */
	public Map<String, String[]> getCapturedHeaders() {
		if(!captureHeaders) {
			return Collections.emptyMap();
		}
		Map<String, String[]> result = new HashMap<String, String[]>();
		for(Entry<String, List<String>> entry : capturedHeaders.entrySet()) {
			result.put(entry.getKey(), entry.getValue().toArray(new String [] {}));
		}
		
		return result;
	}
}
