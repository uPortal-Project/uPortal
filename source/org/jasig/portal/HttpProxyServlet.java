/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.serialize.ProxyResourceMap;
import org.jasig.portal.services.HttpClientManager;

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
    private static boolean STATEFUL_PROXY_URLS = PropertiesManager
        .getPropertyAsBoolean("org.jasig.portal.serialize.ProxyWriter.stateful_proxy_urls", false);
    
    private String statefulProxiedResourceUrlPart;

    
    public HttpProxyServlet() {
        super();
        statefulProxiedResourceUrlPart = PropertiesManager
            .getProperty("org.jasig.portal.serialize.ProxyWriter.resource_proxy_rewrite_prefix", "not set")
            + "?resourceId=";

    }

/**
 * Returns content retreived from location following context (Path Info)
 * If no content found returns 404
 */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {

	// check referrer property - return 404 if incorrect.
	final String checkReferer = PropertiesManager.getProperty(HttpProxyServlet.class.getName()
		+ ".checkReferer", null);

	StringBuffer target = new StringBuffer();
	StringBuffer resource = new StringBuffer();
	String queryString = request.getQueryString();
	boolean appendPath = true;
	
	if (STATEFUL_PROXY_URLS) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            if (log.isWarnEnabled()) {
                log.warn("Proxy attempt with no session.");
            }
            response.setStatus(404);
            return;
        }
        
        ProxyResourceMap resourceMap = (ProxyResourceMap)session.getAttribute("proxyResourceMap");
        if (resourceMap == null) {
            if (log.isWarnEnabled()) {
                log.warn("Proxy attempt with no resourceMap.");
            }
            response.setStatus(404);
            return;
        }
        
        String resourceIdStr = request.getParameter("resourceId");
        if (resourceIdStr == null) {
            // check if referrer is a stateful proxied resource
            String referer = request.getHeader("referer");
            if (referer.contains(statefulProxiedResourceUrlPart)) {
                resourceIdStr = referer.substring(referer.indexOf("resourceId=") + 11);
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Proxy attempt with no resourceId.");
                }
                response.setStatus(404);
                return;
            }
        } else {
            appendPath = false;
        }
        
        Integer resourceId;
        
        try {
            resourceId = new Integer(resourceIdStr);
        } catch (NumberFormatException nfe) {
            response.setStatus(404);
            return;
        }
        
        String s = (String)resourceMap.get(resourceId);
        if (s == null) {
            if (log.isWarnEnabled()) {
                log.warn("Proxy attempt with no resourceId mapping.");
            }
            response.setStatus(404);
            return;
        }
        
        resource.append('/').append(s);
        
        if (log.isDebugEnabled()) {
            log.debug("Serving up resourceId/resource: " + resourceId + "/" + resource);
        }
	}
    
    if (appendPath) {
        resource.append(request.getPathInfo());
    }
            
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
    
    target.append("http:/").append(resource);

	String qs = request.getQueryString();
	if (qs != null) {
	    target.append('?').append(qs);
	}

	try {
		final HttpClient client = HttpClientManager.getNewHTTPClient();
		final GetMethod get = new GetMethod(target.toString());
		final int rc = client.executeMethod(get);
		final Header contentType = get.getResponseHeader("content-type");
		response.setContentType(contentType.getValue());

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
