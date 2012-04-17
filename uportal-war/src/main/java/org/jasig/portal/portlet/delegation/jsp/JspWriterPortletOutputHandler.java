package org.jasig.portal.portlet.delegation.jsp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.jsp.JspWriter;

import org.jasig.portal.portlet.rendering.PortletOutputHandler;

/**
 * Output handler that delegates to a {@link JspWriter}
 * 
 * @author Eric Dalquist
 */
public class JspWriterPortletOutputHandler implements PortletOutputHandler {
    private final JspWriter jspWriter;
    private final PrintWriter printWriter;
    
    public JspWriterPortletOutputHandler(JspWriter jspWriter) {
        this.jspWriter = jspWriter;
        this.printWriter = new PrintWriter(this.jspWriter);
    }

    @Override
    public PrintWriter getPrintWriter() throws IOException {
        return this.printWriter;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IllegalStateException("getWriter has already been called");
    }

    @Override
    public void flushBuffer() throws IOException {
        this.printWriter.flush();
    }

    @Override
    public int getBufferSize() {
        return this.jspWriter.getBufferSize();
    }

    @Override
    public boolean isCommitted() {
        return true;
    }

    @Override
    public void reset() {
        throw new IllegalStateException("Response has already been committed");
    }

    @Override
    public void resetBuffer() {
        try {
            this.jspWriter.clear();
        }
        catch (IOException e) {
            throw new IllegalStateException("Response has already been committed", e);
        }
    }

    @Override
    public void setBufferSize(int size) {
    }

    @Override
    public void setContentType(String contentType) {
    }
}
