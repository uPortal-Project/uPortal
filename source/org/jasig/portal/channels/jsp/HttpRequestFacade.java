package org.jasig.portal.channels.jsp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * An HttpServletRequestWrapper that prevents changes to the underlying request
 * but provides access to most of its objects. Adding an attribute to this
 * wrapper will make that attribute available to all users of this wrapper but
 * will not make it available to any users of the underlying request. If an
 * attribute is placed on this wrapper having the same key as an attribute in
 * the underlying request the underlying request's attribute will not be
 * accessible. Cookies are available from this object but are clones of those
 * in the wrapped object so that the wrapped ones are preserved untouched. 
 * Access to the reader or input stream of the underlying object is denied to
 * prevent corruption of the wrapped request.
 * 
 * @author Mark Boyd
 * @since 2.6
 */
public class HttpRequestFacade extends HttpServletRequestWrapper
{
    /**
     * Instantiate an HttpRequestFacade wrapped around the underlying request to
     * provide access to request information but allow separation of channel 
     * specific aspects from other channels like request attributes.
     * @param request
     */
    public HttpRequestFacade(HttpServletRequest request)
    {
        super(request);
    }

    private Map attributes = new HashMap();

    /**
     * Returns the value of the specified key. If the same key has been used on
     * this object and on the wrapped request the local value for that key hides
     * the value in the wrapped object making it inaccessible.
     */
    public Object getAttribute(String key)
    {
        Object obj = attributes.get(key);
        
        if (obj == null)
            obj = super.getRequest().getAttribute(key);
        return obj;
    }
    /**
     * Returns an Enumeration of attribute names on this object and those of 
     * the wrapped request object. If both this object and the wrapped request
     * have the same key only one instance is returned enforing Set semantices.
     */
    public Enumeration getAttributeNames()
    {
        Enumeration wrapped = super.getRequest().getAttributeNames();
        Enumeration local = Collections.enumeration(attributes.keySet());
        Set keys = new HashSet();
        
        while(wrapped.hasMoreElements())
            keys.add(wrapped.nextElement());
        while(local.hasMoreElements())
            keys.add(local.nextElement());
        return Collections.enumeration(keys);

    }
    /**
     * Remove an item from the private attribute set for this wrapper. Does
     * not remove attributes from the underlying request.
     * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String arg0)
    {
        attributes.remove(arg0);
    }

    /**
     * Overridden to store attributes into the private attribute set of this 
     * object and not into the underlying request object.
     */
    public void setAttribute(String arg0, Object arg1)
    {
        attributes.put(arg0, arg1);
    }
    
    /** 
     * Return a modifiable array of cookies whose changes will be discarded and
     * not pushed into the cookies of the wrapped request.
     *  
     * @see javax.servlet.http.HttpServletRequest#getCookies()
     */
     
    public Cookie[] getCookies()
    {
        HttpServletRequest request = (HttpServletRequest) this.getRequest();
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null)
        {
            Cookie[] safeCookies = new Cookie[cookies.length];
            for ( int i=0; i<cookies.length; i++)
            {
                safeCookies[i] = (Cookie) cookies[i].clone();
            }
            cookies = safeCookies;
        }
        return cookies;
    }


    /**
     * Throws an UnsupportedOperationException since this method won't be called
     * for our usage of requests in the JSP channel type and exposing the 
     * input stream of the underlying request essentially corrupts that request 
     * object.
     */
    public ServletInputStream getInputStream() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an UnsupportedOperationException since this method won't be called
     * for our usage of requests in the JSP channel type and exposing the 
     * reader of the underlying request essentially corrupts that request 
     * object.
     */
    public BufferedReader getReader() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an UnsupportedOperationException since this method won't be called
     * for our usage of requests in the JSP channel type.
     */
    public void setCharacterEncoding(String arg0)
        throws UnsupportedEncodingException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Return the encoding specified in the wrapped request object.
     */
    public String getCharacterEncoding()
    {
        return super.getCharacterEncoding();
    }
}
