package org.jasig.portal.portlet.rendering;

import java.util.Locale;

import javax.portlet.ResourceResponse;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Used to handle output from portlet resource requests.
 * 
 * @author Eric Dalquist
 */
public interface PortletResourceOutputHandler extends PortletOutputHandler {
    
    /**
     * @see ResourceResponse#setCharacterEncoding(String)
     * @see ServletResponse#setCharacterEncoding(String)
     */
    void setCharacterEncoding(String charset);
    
    /**
     * @see ResourceResponse#setContentLength(int)
     * @see ServletResponse#setContentLength(int)
     */
    void setContentLength(int len);
    
    /**
     * @see ResourceResponse#setLocale(Locale)
     * @see ServletResponse#setLocale(Locale)
     */
    void setLocale(Locale locale);
    
    /**
     * @see HttpServletResponse#setStatus(int)
     */
    void setStatus(int status);
    
    /**
     * @see HttpServletResponse#setDateHeader(String, long)
     */
    void setDateHeader(String name, long date);
    
    /**
     * @see HttpServletResponse#addDateHeader(String, long)
     */
    void addDateHeader(String name, long date);
    
    /**
     * @see HttpServletResponse#setHeader(String, String)
     */
    void setHeader(String name, String value);
    
    /**
     * @see HttpServletResponse#addHeader(String, String)
     */
    void addHeader(String name, String value);

    /**
     * @see HttpServletResponse#setIntHeader(String, int)
     */
    void setIntHeader(String name, int value);

    /**
     * @see HttpServletResponse#addIntHeader(String, int)
     */
    void addIntHeader(String name, int value);
}
