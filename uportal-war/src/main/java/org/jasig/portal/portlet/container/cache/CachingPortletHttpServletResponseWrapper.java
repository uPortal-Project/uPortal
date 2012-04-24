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

package org.jasig.portal.portlet.container.cache;

import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.utils.web.PortletHttpServletResponseWrapper;

/**
 * Captures all outputs needed for cached-replay at a later time 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CachingPortletHttpServletResponseWrapper extends PortletHttpServletResponseWrapper {
//    private final PortletOutputHandler portletOutputHandler;
//    
//    private boolean badStatusCode = false;
//    private ServletOutputStream outputStream;
//    private PrintWriter printWriter;
    
    public CachingPortletHttpServletResponseWrapper(HttpServletResponse httpServletResponse, int cacheThresholdSize) {
        super(httpServletResponse);
        
//        this.cachedPortletData = new CachedPortletRenderData();
//        this.cacheThresholdSize = cacheThresholdSize;
    }

//    @Override
//    public ServletOutputStream getOutputStream() throws IOException {
//        ServletOutputStream os = this.outputStream;
//        if (os == null) {
//            this.cachingOutputStream = new LimitedBufferOutputStream(this.cacheThresholdSize);
//            os = new TeeServletOutputStream(super.getOutputStream(), this.cachingOutputStream);
//            this.outputStream = os;
//        }
//        return os;
//    }
//
//    @Override
//    public PrintWriter getWriter() throws IOException {
//        PrintWriter pw = this.printWriter;
//        if (pw == null) {
//            this.cachingWriter = new LimitingTeeWriter(cacheThresholdSize);
//            pw = new PrintWriter(new TeeWriter(super.getWriter(), this.cachingWriter));
//            this.printWriter = pw;
//        }
//        return pw;
//    }
//
//    @Override
//    public void setCharacterEncoding(String charset) {
//        this.cachedPortletData.setCharacterEncoding(charset);
//        super.setCharacterEncoding(charset);
//    }
//
//    @Override
//    public void setContentLength(int len) {
//        this.cachedPortletData.setContentLength(len);
//        super.setContentLength(len);
//    }
//
//    @Override
//    public void setContentType(String type) {
//        this.cachedPortletData.setContentType(type);
//        super.setContentType(type);
//    }
//
//    @Override
//    public void setLocale(Locale loc) {
//        this.cachedPortletData.setLocale(loc);
//        super.setLocale(loc);
//    }
//    
//    @Override
//    public void sendError(int sc, String msg) throws IOException {
//        this.badStatusCode = true;
//        super.sendError(sc, msg);
//    }
//
//    @Override
//    public void sendError(int sc) throws IOException {
//        this.badStatusCode = true;
//        super.sendError(sc);
//    }
//
//    @Override
//    public void setStatus(int sc) {
//        if (sc >= 200 && sc < 300) {
//            this.cachedPortletData.setStatus(sc);
//        }
//        else {
//            this.badStatusCode = true;
//        }
//        super.setStatus(sc);
//    }
//
//    @Override
//    public void setStatus(int sc, String sm) {
//        if (sc >= 200 && sc < 300) {
//            this.cachedPortletData.setStatus(sc);
//            this.cachedPortletData.setStatusMessage(sm);
//        }
//        else {
//            this.badStatusCode = true;
//        }
//        super.setStatus(sc, sm);
//    }
//
//    @Override
//    public void setDateHeader(String name, long date) {
//        super.setDateHeader(name, date);
//        safeSetHeader(name, date);
//    }
//
//    @Override
//    public void addDateHeader(String name, long date) {
//        super.addDateHeader(name, date);
//        safeAddHeader(name, date);
//    }
//
//    @Override
//    public void setHeader(String name, String value) {
//        super.setHeader(name, value);
//        safeSetHeader(name, value);
//    }
//
//    @Override
//    public void addHeader(String name, String value) {
//        super.addHeader(name, value);
//        safeAddHeader(name, value);
//    }
//
//    @Override
//    public void setIntHeader(String name, int value) {
//        super.setIntHeader(name, value);
//        safeSetHeader(name, value);
//    }
//
//    @Override
//    public void addIntHeader(String name, int value) {
//        super.addIntHeader(name, value);
//        safeAddHeader(name, value);
//    } 
//    
//    private void safeAddHeader(String name, Object value) {
//        final Map<String, List<Object>> headers = this.cachedPortletData.getHeaders();
//        
//        List<Object> values = headers.get(name);
//        if(values == null) {
//            values = new ArrayList<Object>();
//            headers.put(name, values);
//        }
//        values.add(value);
//    }
//    
//    private void safeSetHeader(String name, Object value) {
//        final Map<String, List<Object>> headers = this.cachedPortletData.getHeaders();
//        final List<Object>  values = new ArrayList<Object>();
//        values.add(value);
//        headers.put(name, values);
//    }
//    
//    public boolean isThresholdExceeded() {
//        return (this.cachingOutputStream != null && this.cachingOutputStream.isThresholdExceeded()) ||
//                (this.cachingWriter != null && this.cachingWriter.isLimitExceeded());
//    }
//    
//    /**
//     * This method returns the {@link CachedPortletRenderData} if available.
//     * It may return null if a bad statusCode was set, or if the length of the stream exceeds
//     * the cache configuration's max threshold.
//     * 
//     * @see LimitedBufferOutputStream#getCapturedContent()
//     * @return the {@link CachedPortletRenderData} for this response, or null if caching could not be completed.
//     */
//    public CachedPortletRenderData getCachedPortletData() {
//        if (this.badStatusCode) {
//            return null;
//        }
//        
//        if (this.cachingOutputStream != null) {
//            final byte[] capturedContent = this.cachingOutputStream.getCapturedContent();
//            if(capturedContent == null) {
//            	// threshold was exceeded!
//            	return null;
//            } else {
//            	this.cachedPortletData.setByteData(capturedContent);
//            }
//        }
//        
//        if (this.cachingWriter != null) {
//            final StringBuffer buffer = this.cachingWriter.getBuffer();
//            this.cachedPortletData.setStringData(buffer.toString());
//        }
//
//        return this.cachedPortletData;
//    }
}
