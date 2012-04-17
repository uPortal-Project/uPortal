package org.jasig.portal.portlet.delegation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.portlet.MimeResponse;

import org.jasig.portal.portlet.rendering.PortletOutputHandler;

/**
 * Delegates to a {@link MimeResponse}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MimeResponsePortletOutputHandler implements PortletOutputHandler {
    private final MimeResponse mimeResponse;
    
    public MimeResponsePortletOutputHandler(MimeResponse mimeResponse) {
        this.mimeResponse = mimeResponse;
    }

    @Override
    public PrintWriter getPrintWriter() throws IOException {
        return mimeResponse.getWriter();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return mimeResponse.getPortletOutputStream();
    }

    @Override
    public void flushBuffer() throws IOException {
        mimeResponse.flushBuffer();
    }

    @Override
    public int getBufferSize() {
        return mimeResponse.getBufferSize();
    }

    @Override
    public boolean isCommitted() {
        return mimeResponse.isCommitted();
    }

    @Override
    public void reset() {
        mimeResponse.reset();
    }

    @Override
    public void resetBuffer() {
        mimeResponse.resetBuffer();
    }

    @Override
    public void setBufferSize(int size) {
        mimeResponse.setBufferSize(size);
    }

    @Override
    public void setContentType(String contentType) {
        mimeResponse.setContentType(contentType);
    }
}
