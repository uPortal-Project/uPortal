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
import java.util.concurrent.TimeUnit;

import javax.portlet.CacheControl;

import org.jasig.portal.portlet.rendering.PortletOutputHandler;

/**
 * Java bean to represent the data cached for a portlet request via 
 * {@link CacheControl}s.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 * @version $Id$
 */
public class CachedPortletData<T extends Serializable> implements CachedPortletResultHolder<T>, Serializable {
	private static final long serialVersionUID = 5509299103587289000L;
	
	private final T portletResult;
	
	private final String cachedWriterOutput;
	private final byte[] cachedStreamOutput;

    private final String contentType;

    private final boolean publicScope;
    private final String etag;
    private final long timeStored;
    private long expirationTime;
    
    
    /**
     * @param expirationTime Time in seconds the content is valid for (from now)
     */
    public CachedPortletData(T portletResult, String cachedWriterOutput, byte[] cachedStreamOutput, String contentType,
            boolean publicScope, String etag, int expirationTime) {
        
        if (cachedWriterOutput != null && cachedStreamOutput != null) {
            throw new IllegalArgumentException("Both cachedWriterOutput and cachedStreamOutput have been specified");
        }
        
        this.portletResult = portletResult;
        this.cachedWriterOutput = cachedWriterOutput;
        this.cachedStreamOutput = cachedStreamOutput;
        
        this.contentType = contentType;
        
        this.publicScope = publicScope;
        this.etag = etag;
        this.timeStored = System.currentTimeMillis();
        this.updateExpirationTime(expirationTime);
    }
    
    public void replay(PortletOutputHandler portletOutputHandler) throws IOException {
        if (contentType != null) {
            portletOutputHandler.setContentType(contentType);
        }
        
        if (this.cachedWriterOutput != null) {
            final PrintWriter printWriter = portletOutputHandler.getPrintWriter();
            printWriter.write(this.cachedWriterOutput);
        }
        else if (this.cachedStreamOutput != null) {
            final OutputStream outputStream = portletOutputHandler.getOutputStream();
            outputStream.write(cachedStreamOutput);
        }
    }

    /**
     * The time since the epoch in milliseconds that this content expires. -1 if it never expires
     */
    public long getExpirationTime() {
        return expirationTime;
    }

    /**
     * @param expirationTime Time in seconds the content is valid for (from now)
     */
    public void updateExpirationTime(int expirationTime) {
        if (expirationTime == -1) {
            this.expirationTime = -1;
        }
        else {
            this.expirationTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expirationTime);
        }
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
    
    public String getEtag() {
        return etag;
    }

    public long getTimeStored() {
        return timeStored;
    }

    public boolean isPublicScope() {
        return publicScope;
    }
}
