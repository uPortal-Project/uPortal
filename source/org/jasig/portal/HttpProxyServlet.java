/**
 * Copyright ? 2003 The JA-SIG Collaborative.  All rights reserved.
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

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Proxy embedded content such as images for portal sessions.
 * When portal is running over ssl, HttpProxyServlet can be used
 * to deliver insecure content such as images over ssl to avoid 
 * mixed content in the browser window.
 * 
 * @author Drew Mazurek (drew.mazurek@yale.edu)
 * @author Susan Bramhall (susan.bramhall@yale.edu)
 * @version 1.0
 * @since uPortal 2.2
 */
public class HttpProxyServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(HttpProxyServlet.class);
    
/**
 * Returns content retreived from location following context (Path Info)
 * If no content found returns 404
 */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {

	// check referrer property - return 404 if incorrect.
	final String checkReferer = PropertiesManager.getProperty(HttpProxyServlet.class.getName()
		+ ".checkReferer");

	String target;
	
	// if checking referer then only supply proxied content for specific referer
	// Ensures requests come from pages in the portal
	if (null!=checkReferer){
		String referer = request.getHeader("Referer");
		log.debug("HttpProxyServlet: HTTP Referer: " + referer);
		if (null!=referer && !referer.startsWith(checkReferer)) {
			// log referrer 
			log.warn("HttpProxyServlet: bad Referer: " + referer);
			response.setStatus(404);
			return;
		}
	}
	
	if (request.getSession(false)==null) {
		// log referrer in debugging mode
		log.warn("HttpProxyServlet: no session");
		response.setStatus(404);
		return;		
	}
	
	// pathinfo is "/host/url"
	if(request.getPathInfo() != null && !request.getPathInfo().equals("")) {
	    target = "http:/" + request.getPathInfo();
	    String qs = request.getQueryString();
	    if (qs != null)
	        target +="?"+request.getQueryString(); 	
	} else {
		response.setStatus(404);
	    return;
	}

	InputStream is = null;
    ServletOutputStream out = null;
    
    try {
        URL url = new URL(target);
        URLConnection uc = url.openConnection();
        
        response.setContentType(uc.getContentType());
        
        is = uc.getInputStream();
    
	    out = response.getOutputStream();

	byte[] buf = new byte[4096];
	int bytesRead;
	while ((bytesRead = is.read(buf)) != -1) {
	    out.write(buf, 0, bytesRead);
	}
    } catch (MalformedURLException e) {
		response.setStatus(404);
		
    } catch (IOException e) {
		response.setStatus(404);
		
    } finally {
        if(is != null)
        is.close();
        if(out != null)
        out.close();
    }
    }
}
