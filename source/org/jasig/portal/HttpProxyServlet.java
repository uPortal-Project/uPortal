/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
		+ ".checkReferer", null);

	String target;
	
	// if checking referer then only supply proxied content for specific referer
	// Ensures requests come from pages in the portal
	if (null!=checkReferer){
		String referer = request.getHeader("Referer");
        if (log.isDebugEnabled())
            log.debug("HttpProxyServlet: HTTP Referer: " + referer);
		if (null!=referer && !referer.startsWith(checkReferer)) {
			if (log.isWarnEnabled())
			    log.warn("HttpProxyServlet: bad Referer: " + referer);
			response.setStatus(404);
			return;
		}
	}
	
	if (request.getSession(false)==null) {
		if (log.isWarnEnabled())
		    log.warn("HttpProxyServlet: no session");
		response.setStatus(404);
		return;		
	}
	
	// pathinfo is "/host/url"
	if(request.getPathInfo() != null && !request.getPathInfo().equals("")) {
	    target = "http:/" + request.getPathInfo();
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
