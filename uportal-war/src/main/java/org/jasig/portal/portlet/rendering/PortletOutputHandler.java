package org.jasig.portal.portlet.rendering;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.portlet.MimeResponse;
import javax.servlet.ServletResponse;

/**
 * Used to handle output from portlet render and resource requests.
 * 
 * @author Eric Dalquist
 */
public interface PortletOutputHandler {
    /**
     * @see MimeResponse#getWriter()
     * @see ServletResponse#getWriter() 
     */
    PrintWriter getPrintWriter() throws IOException;
    
    /**
     * @see MimeResponse#getPortletOutputStream()
     * @see ServletResponse#getOutputStream() 
     */
    OutputStream getOutputStream() throws IOException ;
    
    /**
     * @see MimeResponse#flushBuffer()
     * @see ServletResponse#flushBuffer()
     */
    void flushBuffer() throws IOException;
    
    /**
     * @see MimeResponse#getBufferSize()
     * @see ServletResponse#getBufferSize()
     */
    int getBufferSize();
    
    /**
     * @see MimeResponse#isCommitted()
     * @see ServletResponse#isCommitted()
     */
    boolean isCommitted();
    
    /**
     * @see MimeResponse#reset()
     * @see ServletResponse#reset()
     */
    void reset();
    
    /**
     * @see MimeResponse#resetBuffer()
     * @see ServletResponse#resetBuffer()
     */
    void resetBuffer();
    
    /**
     * @see MimeResponse#setBufferSize(int)
     * @see ServletResponse#setBufferSize(int)
     */
    void setBufferSize(int size);
    
    /**
     * @see MimeResponse#setContentType(String)
     * @see ServletResponse#setContentType(String)
     */
    void setContentType(String contentType);
}
