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

package org.jasig.portal.serialize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class CachingOutputStream extends OutputStream implements CharacterCachingWriter { 

    OutputStream out;
    ByteArrayOutputStream cache;
    boolean caching;

    public CachingOutputStream(OutputStream out) {
        cache=null; caching=false;
        this.out=out;
    }

    public boolean startCaching() throws IOException {
        if(caching) return false;
        flush();
        caching=true;
        cache=new ByteArrayOutputStream();
        return true;
    }

    public String getCache(String encoding) throws UnsupportedEncodingException, IOException {
        flush();
        return cache.toString(encoding);
    }

    public boolean stopCaching() {
        if(!caching) return false;
        caching=false; return true;
    }

    public void close() throws IOException {
        out.close();
    }
    
    public void flush() throws IOException {
        if(out!=null) out.flush();
        if(cache!=null) cache.flush();
    }

    public void write(byte[] b) throws IOException {
        out.write(b);
        if(caching) cache.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b,off,len);
        if(caching) cache.write(b,off,len);
    }

    public void write(int b) throws IOException {
        out.write(b);
        if(caching) cache.write(b);
    }
}
