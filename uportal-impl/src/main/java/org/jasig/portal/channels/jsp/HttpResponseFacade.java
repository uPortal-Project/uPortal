/*
 * Created on May 19, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jasig.portal.channels.jsp;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Mark Boyd
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HttpResponseFacade extends HttpServletResponseWrapper
{
    private static final Log LOG = LogFactory
            .getLog(HttpServletResponseWrapper.class);

    private StringWriter writerBuffer;
    private PrintWriter writer;
    private String encoding;
    private int errorCode = -1;
    private String errorMsg = null;

    public char[] getCharacters()
    {
        if (writer != null)
        {
            writer.flush();
            return writerBuffer.toString().toCharArray();
        }
        return "No content written by JSP".toCharArray();
    }

    /**
     * @param arg0
     */
    public HttpResponseFacade(HttpServletResponse arg0)
    {
        super(arg0);
    }
    

    /**
     * Override to prevent corruption of underlying request.
     */
    public void addCookie(Cookie arg0)
    {
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void addDateHeader(String arg0, long arg1)
    {
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void addHeader(String arg0, String arg1)
    {
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void addIntHeader(String arg0, int arg1)
    {
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void sendError(int arg0, String arg1) throws IOException
    {
        if ( LOG.isDebugEnabled() )
            LOG.debug("sendError(" + arg0 + ", '" + arg1 + "') called");

        errorCode = arg0;
        errorMsg = arg1;
    }

    public boolean isSuccessful()
    {
        return errorCode == -1;
    }
    
    /**
     * Provides access to error code set on this wrapper and not on the state
     * of the underlying response.
     * @return
     */
    public int getErrorCode()
    {
        return errorCode;
    }

    /**
     * Provides access to error message set on this wrapper and not on the state
     * of the underlying response.
     * @return
     */
    public String getErrorMessage()
    {
        return errorMsg;
    }
    
    /**
     * Override to prevent corruption of underlying request.
     */
    public void sendError(int arg0) throws IOException
    {
        if ( LOG.isDebugEnabled() )
            LOG.debug("sendError(" + arg0 + ") called");

        errorCode = arg0;
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void sendRedirect(String arg0) throws IOException
    {
        if ( LOG.isDebugEnabled() )
            LOG.debug("sendRedirect(" + arg0 + ") called");
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void setDateHeader(String arg0, long arg1)
    {
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void setHeader(String arg0, String arg1)
    {
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void setIntHeader(String arg0, int arg1)
    {
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void setStatus(int arg0, String arg1)
    {
        if ( LOG.isDebugEnabled() )
            LOG.debug("setStatus(" + arg0 + ", " + arg1 + ") called");
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void setStatus(int arg0)
    {
        if ( LOG.isDebugEnabled() )
            LOG.debug("setStatus(" + arg0 + ") called");
    }

    /**
     * Override to flush internal buffer and not that of underlying response.
     */
    public void flushBuffer() throws IOException
    {
        if ( LOG.isDebugEnabled() )
            LOG.debug("flushBuffer() called");

        if( writer != null )
            writer.flush();
    }

    /**
     * This is an unsupported method for our use in the JSP channel type and
     * results in an UnsupportedOperationException being thrown.
     */
    public ServletOutputStream getOutputStream() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Override to return the internal writer and not that of underlying
     * response.
     */
    public PrintWriter getWriter() throws IOException
    {
        if ( LOG.isDebugEnabled() )
            LOG.debug("getWriter() called");
                    
        if (writer == null)
        {
            synchronized(this)
            {
                if (writer == null)
                {
                    writerBuffer = new StringWriter();
                    writer = new PrintWriter(writerBuffer);
                }
            }
        }
        return writer;
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void reset()
    {
        if ( LOG.isDebugEnabled() )
            LOG.debug("reset() called");
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void resetBuffer()
    {
        if ( LOG.isDebugEnabled() )
            LOG.debug("resetBuffer() called");
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void setBufferSize(int arg0)
    {
    }

    /**
     * Sets the character encoding on this object and not the underlying
     * response.
     */
    public void setCharacterEncoding(String arg0)
    {
        if ( LOG.isDebugEnabled() )
            LOG.debug("setCharacterEncoding('"+ arg0 +"') called");
        encoding = arg0;
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void setContentLength(int arg0)
    {
    }

    /**
     * Only looks for the character encoding aspect of the specified content
     * type and sets that on this object and not the underlying response.
     */
    public void setContentType(String arg0)
    {
        if ( LOG.isDebugEnabled() )
            LOG.debug("setContentType('"+ arg0 +"') called");
        String moniker = "charset=";
        int charEncIdx = arg0.toLowerCase().indexOf(moniker);
        
        if (charEncIdx != -1)
            setCharacterEncoding(arg0.substring(charEncIdx + moniker.length()));
    }

    /**
     * Override to prevent corruption of underlying request.
     */
    public void setLocale(Locale arg0)
    {
    }

   
    /**
     * Returns the character encoding set on this object if any. Otherwise it
     * delegates to the wrapped response.
     */
    public String getCharacterEncoding()
    {
        if ( LOG.isDebugEnabled() )
            LOG.debug("getCharacterEncoding() called");
        if ( encoding != null)
            return encoding;
            
        return super.getCharacterEncoding();
    }

    /**
     * Always returns false since this is a wrapper that enlessly buffers the
     * output written to it.
     */
    public boolean isCommitted()
    {
        if ( LOG.isDebugEnabled() )
            LOG.debug("isCommited() called");
        return false;
    }
}
