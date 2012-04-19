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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.CacheControl;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.rendering.PortletResourceOutputHandler;

/**
 * Extention of {@link CachingPortletOutputHandler} that adds support for resource requests
 * 
 * @author Eric Dalquist
 */
public class CachingPortletResourceOutputHandler extends CachingPortletOutputHandler implements
        PortletResourceOutputHandler {

    private final PortletResourceOutputHandler portletResourceOutputHandler;

    private final Map<String, List<Serializable>> headers = new LinkedHashMap<String, List<Serializable>>();
    private Integer status;
    private String characterEncoding;
    private Integer contentLength;
    private Locale locale;
    
    
    public CachingPortletResourceOutputHandler(PortletResourceOutputHandler portletResourceOutputHandler, int maximumSize) {
        super(portletResourceOutputHandler, maximumSize);
        this.portletResourceOutputHandler = portletResourceOutputHandler;
    }
    
    public <T extends Serializable> CachedPortletResourceData<T> getCachedPortletResourceData(T portletResult, CacheControl cacheControl) {
        if (status != null && status != HttpServletResponse.SC_OK) {
            //Only cache OK responses
            return null;
        }
        
        final CachedPortletData<T> cachedPortletData = super.getCachedPortletData(portletResult, cacheControl);
        if (cachedPortletData == null) {
            //Hit the caching limit, nothing to return 
            return null;
        }
        
        return new CachedPortletResourceData<T>(cachedPortletData, headers, status, characterEncoding, contentLength, locale);
    }

    public Map<String, List<Serializable>> getHeaders() {
        return headers;
    }

    public int getStatus() {
        return status;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public Integer getContentLength() {
        return contentLength;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.portletResourceOutputHandler.setCharacterEncoding(charset);
        this.characterEncoding = charset;
    }

    @Override
    public void setContentLength(int len) {
        this.portletResourceOutputHandler.setContentLength(len);
        this.contentLength = len;
    }

    @Override
    public void setLocale(Locale locale) {
        this.portletResourceOutputHandler.setLocale(locale);
        this.locale = locale;
    }
    
    @Override
    public void setStatus(int status) {
        this.portletResourceOutputHandler.setStatus(status);
        this.status = status;
    }

    @Override
    public void setDateHeader(String name, long date) {
        this.portletResourceOutputHandler.setDateHeader(name, date);
        this.setGenericHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        this.portletResourceOutputHandler.addDateHeader(name, date);
        this.addGenericHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        this.portletResourceOutputHandler.setHeader(name, value);
        this.setGenericHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        this.portletResourceOutputHandler.addHeader(name, value);
        this.addGenericHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        this.portletResourceOutputHandler.setIntHeader(name, value);
        this.setGenericHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        this.portletResourceOutputHandler.addIntHeader(name, value);
        this.addGenericHeader(name, value);
    }
    
    protected final void addGenericHeader(String name, Serializable value) {
        List<Serializable> values = this.headers.get(name);
        if (values == null) {
            values = new LinkedList<Serializable>();
            this.headers.put(name, values);
        }
        values.add(value);
    }
    
    protected final void setGenericHeader(String name, Serializable value) {
        final List<Serializable> values = new LinkedList<Serializable>();
        values.add(value);
        this.headers.put(name, values);
    }
}
