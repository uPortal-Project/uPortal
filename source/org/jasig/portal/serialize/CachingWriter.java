/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.serialize;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class CachingWriter extends Writer implements CharacterCachingWriter { 

    Writer out;
    StringWriter cache;
    boolean caching;

    public CachingWriter(Writer out) {
        cache=null; caching=false;
        this.out=out;
    }

    public boolean startCaching() throws IOException {
        if(caching) return false;
        flush();
        caching=true;
        cache=new StringWriter();
        return true;
    }

    public String getCache(String encoding) throws IOException {
        flush();
        return cache.toString();
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

    public void write(char[] c) throws IOException {
        out.write(c);
        if(caching) cache.write(c);
    }

    public void write(char[] c, int off, int len) throws IOException {
        out.write(c,off,len);
        if(caching) cache.write(c,off,len);
    }

    public void write(int c) throws IOException {
        out.write(c);
        if(caching) cache.write(c);
    }

    public void write(String str) throws IOException {
        out.write(str);
        if(caching) cache.write(str);
    }

    public void write(String str, int off, int len) throws IOException {
        out.write(str,off,len);
        if(caching) cache.write(str,off,len);
    }
}
