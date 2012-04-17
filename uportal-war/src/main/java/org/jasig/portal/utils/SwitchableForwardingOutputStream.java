package org.jasig.portal.utils;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang.Validate;

/**
 * Delegates to another {@link OutputStream} which can be modified
 * at any time by calling {@link #setOutputStream(OutputStream)}
 * 
 * @author Eric Dalquist
 */
public class SwitchableForwardingOutputStream extends OutputStream {
    private OutputStream outputStream;
    
    public SwitchableForwardingOutputStream(OutputStream outputStream) {
        Validate.notNull(outputStream);
        this.outputStream = outputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        Validate.notNull(outputStream);
        this.outputStream = outputStream;
    }

    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    public int hashCode() {
        return outputStream.hashCode();
    }

    public void write(byte[] b) throws IOException {
        outputStream.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
    }

    public boolean equals(Object obj) {
        return outputStream.equals(obj);
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    public void close() throws IOException {
        outputStream.close();
    }

    public String toString() {
        return outputStream.toString();
    }
}
