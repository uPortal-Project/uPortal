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
