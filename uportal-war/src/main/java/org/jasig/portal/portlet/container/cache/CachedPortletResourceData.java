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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.CacheControl;

import org.jasig.portal.portlet.rendering.PortletResourceOutputHandler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * Java bean to represent the data cached for a portlet resource request via 
 * {@link CacheControl}s.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CachedPortletResourceData<T extends Serializable> implements CachedPortletResultHolder<T>, Serializable {
    private static final long serialVersionUID = 1L;

    private final CachedPortletData<T> cachedPortletData;

    private final Map<String, List<Serializable>> headers;
    private final Integer status;
    private final String characterEncoding; 
    private final Integer contentLength; 
    private final Locale locale;
    
    
    public CachedPortletResourceData(CachedPortletData<T> cachedPortletData, Map<String, List<Serializable>> headers,
            Integer status, String characterEncoding, Integer contentLength, Locale locale) {
        
        this.cachedPortletData = cachedPortletData;
        
        final Builder<String, List<Serializable>> headersBuilder = ImmutableMap.builder();
        for (final Entry<String, List<Serializable>> headerEntry : headers.entrySet()) {
            final String name = headerEntry.getKey();
            final List<Serializable> values = headerEntry.getValue();
            headersBuilder.put(name, ImmutableList.copyOf(values));
        }
        this.headers = headersBuilder.build();
        
        this.status = status;
        this.characterEncoding = characterEncoding;
        this.contentLength = contentLength;
        this.locale = locale;
    }

    public final void replay(PortletResourceOutputHandler portletOutputHandler) throws IOException {
        //Write status
        if (status != null) {
            portletOutputHandler.setStatus(status);
        }

        //Write out headers
        for (final Entry<String, List<Serializable>> headerEntry : headers.entrySet()) {
            final String name = headerEntry.getKey();
            for (final Serializable value : headerEntry.getValue()) {
                if (value instanceof Long) {
                    portletOutputHandler.addDateHeader(name, (Long)value);
                }
                else if (value instanceof Long) {
                    portletOutputHandler.addIntHeader(name, (Integer)value);
                }
                else {
                    portletOutputHandler.addHeader(name, (String)value);
                }
            }
        }
        
        //Set explicit parameters
        if (characterEncoding != null) {
            portletOutputHandler.setCharacterEncoding(characterEncoding);
        }
        if (contentLength != null) {
            portletOutputHandler.setContentLength(contentLength);
        }
        if (locale != null) {
            portletOutputHandler.setLocale(locale);
        }
        
        //Set the caching related headers
        PortletCachingHeaderUtils.setCachingHeaders(cachedPortletData, portletOutputHandler);
        
        this.cachedPortletData.replay(portletOutputHandler);
    }

    @Override
    public T getPortletResult() {
        return this.cachedPortletData.getPortletResult();
    }
    
    @Override
    public long getExpirationTime() {
        return this.cachedPortletData.getExpirationTime();
    }

    @Override
    public String getEtag() {
        return this.cachedPortletData.getEtag();
    }

    @Override
    public long getTimeStored() {
        return this.cachedPortletData.getTimeStored();
    }

    public CachedPortletData<T> getCachedPortletData() {
        return cachedPortletData;
    }

    public Map<String, List<Serializable>> getHeaders() {
        return headers;
    }

    public Integer getStatus() {
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
}
