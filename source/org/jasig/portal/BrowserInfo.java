/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
     * @param cookies, an array of cookies
     * @param headers, a Map of headers
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
     * @param headers, a Map of headers
     */
    public void setHeaders(Map headers) {
        this.headers = headers;
    }    

    /**
     * Obtain a "user-agent" header contained in the request.
     * @return a <code>String</code> value
     */
    public String getUserAgent() {
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



