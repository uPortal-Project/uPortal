package org.jasig.portal.portlet.rendering;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

/**
 * PortletOutputHandler that delegates all methods directly to a {@link HttpServletResponse}
 * 
 * @author Eric Dalquist
 */
public class ResourcePortletOutputHandler implements PortletOutputHandler {
    private final HttpServletResponse response;
    
    public ResourcePortletOutputHandler(HttpServletResponse response) {
        this.response = response;
    }

    @Override
    public PrintWriter getPrintWriter() throws IOException {
        return this.response.getWriter();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.response.getOutputStream();
    }

    @Override
    public void flushBuffer() throws IOException {
        this.response.flushBuffer();
    }

    @Override
    public int getBufferSize() {
        return this.response.getBufferSize();
    }

    @Override
    public boolean isCommitted() {
        return this.response.isCommitted();
    }

    @Override
    public void reset() {
        this.response.reset();
    }

    @Override
    public void resetBuffer() {
        this.response.resetBuffer();
    }

    @Override
    public void setBufferSize(int size) {
        this.response.setBufferSize(size);
    }

    
    @Override
    public void setContentType(String contentType) {
        this.response.setContentType(contentType);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.response.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len) {
        this.response.setContentLength(len);
    }

    @Override
    public void setLocale(Locale locale) {
        this.response.setLocale(locale);
    }
}
