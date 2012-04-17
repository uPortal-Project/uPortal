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
/**
 * 
 */
package org.jasig.portal.portlet.container.cache;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Locale;

import javax.portlet.CacheControl;

import org.jasig.portal.portlet.rendering.PortletOutputHandler;

/**
 * Java bean to represent the data cached for a portlet request via 
 * {@link CacheControl}s.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 * @version $Id$
 */
public class CachedPortletData<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = 5509299103587289000L;
	
	private final T portletResult;
	
	private final String cachedWriterOutput;
	private final byte[] cachedStreamOutput;

    private final String contentType;
    private final String characterEncoding;
    private final Integer contentLength;
    private final Locale locale;

    private final String etag;
    private final long timeStored;
    private long expirationTime;
    
    
    public CachedPortletData(T portletResult, String cachedWriterOutput, byte[] cachedStreamOutput, String contentType,
            String characterEncoding, Integer contentLength, Locale locale, String etag, int expirationTime) {
        
        if (cachedWriterOutput != null && cachedStreamOutput != null) {
            throw new IllegalArgumentException("Both cachedWriterOutput and cachedStreamOutput have been specified");
        }
        
        this.portletResult = portletResult;
        this.cachedWriterOutput = cachedWriterOutput;
        this.cachedStreamOutput = cachedStreamOutput;
        
        this.contentType = contentType;
        this.characterEncoding = characterEncoding;
        this.contentLength = contentLength;
        this.locale = locale;
        
        //TODO HEADERS
        
        this.etag = etag;
        this.timeStored = System.currentTimeMillis();
        if (expirationTime == -1) {
            this.expirationTime = -1;
        }
        else {
            this.expirationTime = this.timeStored + expirationTime;
        }
    }
    
    public void replay(PortletOutputHandler portletOutputHandler) throws IOException {
        if (contentType != null) {
            portletOutputHandler.setContentType(contentType);
        }
        if (characterEncoding != null) {
            portletOutputHandler.setCharacterEncoding(characterEncoding);
        }
        if (contentLength != null) {
            portletOutputHandler.setContentLength(contentLength);
        }
        if (locale != null) {
            portletOutputHandler.setLocale(locale);
        }
        
        //TODO HEADERS
        
        if (this.cachedWriterOutput != null) {
            final PrintWriter printWriter = portletOutputHandler.getPrintWriter();
            printWriter.write(this.cachedWriterOutput);
        }
        else if (this.cachedStreamOutput != null) {
            final OutputStream outputStream = portletOutputHandler.getOutputStream();
            outputStream.write(cachedStreamOutput);
        }
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public T getPortletResult() {
        return portletResult;
    }

    public String getCachedWriterOutput() {
        return cachedWriterOutput;
    }

    public byte[] getCachedStreamOutput() {
        return cachedStreamOutput;
    }

    public String getContentType() {
        return contentType;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public int getContentLength() {
        return contentLength;
    }

    public Locale getLocale() {
        return locale;
    }
    
    public String getEtag() {
        return etag;
    }

    public long getTimeStored() {
        return timeStored;
    }
}
