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


package  org.jasig.portal;

import  javax.servlet.*;
import  javax.servlet.http.*;
import  java.util.Hashtable;
import  java.util.Enumeration;


/*
 * This class contains basic information about the browser.
 * It stores all of the headers and cookies
 */
public class BrowserInfo {
  protected Cookie[] cookies;
  protected Hashtable headers;

    
    /**
     * Construct a new browser info based on HTTP request.
     *
     * @param req a <code>HttpServletRequest</code> value
     */
    public BrowserInfo (HttpServletRequest req) {
        headers = new Hashtable();
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

    
    /**
     * Get value of a particular header.
     *
     * @param hName a <code>String</code> value
     * @return a <code>String</code> value
     */
    public String getHeader(String hName) {
        return  (String)headers.get(hName.toLowerCase());
    }

    /**
     * Obtain all header names.
     *
     * @return an <code>Enumeration</code> value
     */
    public Enumeration getHeaderNames() {
        return  headers.keys();
    }

    /**
     * Obtain a "user-agent" header contained in the request.
     *
     * @return a <code>String</code> value
     */
    public String getUserAgent () {
        return  (String)headers.get("user-agent");
    }

    /**
     * Overrides Object's toString().  The string form of this object is
     * sometimes used to generate a key for caching objects in the portal.
     * @return state the state of this object in string form
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(1024);
        // Skip cookies for now and just print out headers
        Enumeration e = headers.keys();
        while (e.hasMoreElements()) {
            String header = (String)e.nextElement();
            sb.append("[").append(header).append("]");
            sb.append("=");
            sb.append("[").append(headers.get(header)).append("] ");
        }
        return sb.toString();
    }
}



