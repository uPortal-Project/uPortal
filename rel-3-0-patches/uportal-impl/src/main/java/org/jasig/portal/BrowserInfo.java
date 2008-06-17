/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/*
 * This class contains basic information about the browser.
 * It stores all of the headers and cookies
 */
public class BrowserInfo {
    protected Cookie[] cookies;
    protected Map headers;

    /**
     * Constructs a new empty browser info
     */
    public BrowserInfo() {
      // Note: A default constructor is needed
      // in order to use Apache Axis's BeanSerializer to 
      // serialize this object, so please don't remove it
    }
        
    /**
     * Constructs a new browser info with supplied cookies and header info
     * @param cookies an array of cookies
     * @param headers a Map of headers
     */
    public BrowserInfo(Cookie[] cookies, Map headers) {
      this.cookies = cookies;
      this.headers = headers;
    }

    /**
     * Construct a new browser info based on HTTP request.
     * @param req a <code>HttpServletRequest</code> value
     */
    public BrowserInfo (HttpServletRequest req) {
        headers = new HashMap();
        for (Enumeration e = req.getHeaderNames(); e.hasMoreElements();) {
            String hName = (String)e.nextElement();
            // Request header names are case insensitive, so BrowserInfo must be too!!
            headers.put(hName.toLowerCase(), req.getHeader(hName));
        }
        cookies = req.getCookies();
    }

    public Cookie[] getCookies() {
        return  cookies;
    }

    public void setCookies(Cookie[] cookies) {
        this.cookies = cookies;
    }

    /**
     * Get value of a particular header.
     * @param hName a <code>String</code> value
     * @return a <code>String</code> value
     */
    public String getHeader(String hName) {
        return (String)headers.get(hName.toLowerCase());
    }

    /**
     * Get the headers as a Map.
     * @return a map of headers
     */
    public Map getHeaders() {
        return headers;
    }

    /**
     * Sets the headers.
     * @param headers a Map of headers
     */
    public void setHeaders(Map headers) {
        this.headers = headers;
    }    

    /**
     * Obtain a "user-agent" header contained in the request.
     * @return a <code>String</code> value
     */
    public String getUserAgent () {
        String ua=(String)headers.get("user-agent");
        if(ua==null || ua.equals("")) { 
            ua=MediaManager.NULL_USER_AGENT; 
        }
        return ua;
    }

    /**
     * Overrides Object's toString().  The string form of this object is
     * sometimes used to generate a key for caching objects in the portal.
     * @return state the state of this object in string form
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(1024);
        // Skip cookies for now and just print out headers
        Iterator iter = headers.keySet().iterator();
        while (iter.hasNext()) {
            String header = (String)iter.next();
            sb.append("[").append(header).append("]");
            sb.append("=");
            sb.append("[").append(headers.get(header)).append("] ");
        }
        return sb.toString();
    }
}



