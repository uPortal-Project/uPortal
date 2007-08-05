/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

import javax.servlet.*;
import javax.servlet.http.*;

import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.services.HttpClientManager;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
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
	if (null!=checkReferer && !checkReferer.equals("")){
		StringTokenizer  checkedReferers = new StringTokenizer (checkReferer, " ");
		boolean refOK = false;
		String referer = request.getHeader("Referer");

        if (log.isDebugEnabled()) {
            log.debug("HttpProxyServlet: HTTP Referer: " + referer);
        }
        if (null!=referer) {
            while (checkedReferers.hasMoreTokens()) {
                String goodRef = checkedReferers.nextToken();
                if (log.isDebugEnabled()) {
                    log.debug("HttpProxyServlet: checking for "+goodRef);
                }
                if (referer.startsWith(goodRef)) {
                    refOK = true;
                    if (log.isDebugEnabled()) {
                        log.debug("HttpProxyServlet: referer accepted "+goodRef);
                    }
                    break;
                }
            }
    		if (!refOK) {
                if (log.isWarnEnabled()) {
    			    log.warn("HttpProxyServlet: bad Referer: " + referer);
                }
    			response.setStatus(404);
    			return;
    		}

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
	    String qs = request.getQueryString();
	    if (qs != null) {
	        target +="?"+request.getQueryString();
	    }
	} else {
		response.setStatus(404);
		log.warn("HttpProxyServlet: getPathInfo is empty");
	    return;
	}

	try {
		final HttpClient client = HttpClientManager.getNewHTTPClient();
		final GetMethod get = new GetMethod(target);
		final int rc = client.executeMethod(get);
        if (rc != 200) {
            response.setStatus(404);
            log.info("httpProxyServlet returning response 404 after receiving response code: "+rc+" from url: "+"target");
        }
        final Header contentType = get.getResponseHeader("content-type");
        if (log.isDebugEnabled()) 
            log.debug("httpProxyServlet examining element with content type = "+contentType);
        if (!contentType.getValue().startsWith("image")){
            response.setStatus(404);
            log.info("httpProxyServlet returning response 404 after receiving element with contentType ="+contentType);
        }		response.setContentType(contentType.getValue());

		final ServletOutputStream out = response.getOutputStream();
		try {
			final InputStream is = get.getResponseBodyAsStream();
			try {
				final byte[] buf = new byte[4096];
				int bytesRead;
				while ((bytesRead = is.read(buf)) != -1) {
					out.write(buf, 0, bytesRead);
				}
			} finally {
				is.close();
			}
		} finally {
			out.close();
		}
	} catch (MalformedURLException e) {
		response.setStatus(404);
		log.warn("HttpProxyServlet: target=" + target.toString(), e);

	} catch (IOException e) {
		response.setStatus(404);
		log.warn(e, e);
	}
    }
}
