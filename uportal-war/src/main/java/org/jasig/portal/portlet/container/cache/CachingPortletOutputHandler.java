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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;

import javax.portlet.CacheControl;

import org.apache.commons.io.output.StringBuilderWriter;
import org.jasig.portal.portlet.rendering.PortletOutputHandler;

import com.google.common.base.Function;

/**
 * Captures the output of a portlet for later re-use. The maximumSize field allows
 * for setting a max number of bytes/chars to cache before giving up on the caching.
 * 
 * @author Eric Dalquist
 */
public class CachingPortletOutputHandler implements PortletOutputHandler {
    private final PortletOutputHandler portletOutputHandler;
    private final int maximumSize;

    private LimitingTeeWriter teeWriter;
    private PrintWriter printWriter;
    private LimitingTeeOutputStream teeStream;

    private StringBuilderWriter cachingWriter;
    private ByteArrayOutputStream cachingOutputStream;
    
    private String contentType;

    public CachingPortletOutputHandler(PortletOutputHandler portletOutputHandler, int maximumSize) {
        this.portletOutputHandler = portletOutputHandler;
        this.maximumSize = maximumSize;
    }
    
    public <T extends Serializable> CachedPortletData<T> getCachedPortletData(T portletResult, CacheControl cacheControl) {
        if ((this.teeWriter != null && this.teeWriter.isLimitReached()) || (this.teeStream != null && this.teeStream.isLimitReached())) {
            //Hit the caching limit, nothing to return 
            return null;
        }
        
        return new CachedPortletData<T>(
                portletResult, 
                this.cachingWriter != null ? this.cachingWriter.toString() : null, 
                this.cachingOutputStream != null ? this.cachingOutputStream.toByteArray() : null,
                contentType, 
                cacheControl.isPublicScope(),
                cacheControl.getETag(), 
                cacheControl.getExpirationTime());
    }
    
    public String getCachedWriterOutput() {
        if (cachingWriter == null) {
            return null;
        }
        
        return cachingWriter.toString();
    }
    
    public byte[] getCachedStreamOutput() {
        if (cachingOutputStream == null) {
            return null;
        }
        
        return cachingOutputStream.toByteArray();
    }
    
    public String getContentType() {
        return contentType;
    }

    @Override
    public PrintWriter getPrintWriter() throws IOException {
        if (teeStream != null) {
            throw new IllegalStateException("getOutputStream() has already been called");
        }

        if (this.printWriter == null) {
            final PrintWriter delegateWriter = this.portletOutputHandler.getPrintWriter();
            this.cachingWriter = new StringBuilderWriter();
            
            //Create the limiting tee writer to write to the actual PrintWriter and the cachingWriter
            this.teeWriter = new LimitingTeeWriter(this.maximumSize, delegateWriter,
                    this.cachingWriter, new Function<LimitingTeeWriter, Object>() {
                        @Override
                        public Object apply(LimitingTeeWriter input) {
                            //Limit hit, clear the cache
                            clearCachedWriter();
                            return null;
                        }
                    });
            
            //Wrap the limiting tee writer in a PrintWriter to return to the caller
            this.printWriter = new PrintWriter(this.teeWriter);
        }

        return this.printWriter;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (this.printWriter != null) {
            throw new IllegalStateException("getPrintWriter() has already been called");
        }
        
        if (this.teeStream == null) {
            final OutputStream delegateOutputStream = this.portletOutputHandler.getOutputStream();
            this.cachingOutputStream = new ByteArrayOutputStream();
            
            //Create the limiting tee output stream to the actual OutputStream and the cachingOutputStream
            this.teeStream = new LimitingTeeOutputStream(this.maximumSize, delegateOutputStream,
                    this.cachingOutputStream, new Function<LimitingTeeOutputStream, Object>() {
                        @Override
                        public Object apply(LimitingTeeOutputStream input) {
                            //Limit hit, clear the cache
                            clearCachedStream();
                            return null;
                        }
                    });
        }

        return teeStream;
    }

    @Override
    public void flushBuffer() throws IOException {
        this.portletOutputHandler.flushBuffer();
    }

    @Override
    public int getBufferSize() {
        return this.portletOutputHandler.getBufferSize();
    }

    @Override
    public boolean isCommitted() {
        return this.portletOutputHandler.isCommitted();
    }

    @Override
    public void reset() {
        this.portletOutputHandler.reset();
        
        //Parent reset worked, clear the caches
        resetCached();
        
    }

    @Override
    public void resetBuffer() {
        this.portletOutputHandler.resetBuffer();
        
        //Parent reset worked, clear the caches
        resetCached();
    }

    private void resetCached() {
        resetCachedWriter();
        resetCachedStream();
    }

    private void resetCachedWriter() {
        if (this.cachingWriter != null) {
            this.teeWriter.resetByteCount();
            this.clearCachedWriter();
        }
    }

    private void clearCachedWriter() {
        if (this.cachingWriter != null) {
            final StringBuilder builder = this.cachingWriter.getBuilder();
            if (builder.length() > 0) {
                builder.delete(0, builder.length());
            }
        }
    }

    private void resetCachedStream() {
        if (this.cachingOutputStream != null) {
            this.teeStream.resetByteCount();
            this.clearCachedStream();
        }
    }

    private void clearCachedStream() {
        if (this.cachingOutputStream != null) {
            this.cachingOutputStream.reset();
        }
    }

    @Override
    public void setBufferSize(int size) {
        this.portletOutputHandler.setBufferSize(size);
    }

    @Override
    public void setContentType(String contentType) {
        this.portletOutputHandler.setContentType(contentType);
        this.contentType = contentType;
    }
}
