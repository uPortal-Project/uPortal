/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.serialize;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.services.HttpClientManager;

/**
 * This Class allows appending PROXY_REWRITE_PREFIX String in front of all the references to images, javascript files, etc..
 * that are on a remote location. This allows the browser while portal is running in https assume that these resources
 * are secure resources(are referenced by https rather than http). This is because the resource URI insteadof
 * http://www.abc.com/image.gif will be rewriten as as https://[portal address]/PROXY_REWRITE_PREFIX/www.abc.com/image.gif
 * This class does the proxy rewrite in the following exceptional situations as well:
 * 1. If the return code poting to the image is 3XX (the image refrence, refrences is a mapping to a diffrent location)
 *    In this case the final destination iddress n which the image or the resource is located is e
 *    and then the rewrite points to this location.
 * 2. If the content of a channel is an include javascript file the file is rewriten to a location on a local virtual host
 *    and at the same time the image or other resources references are rewritten.
 *HttpURLConnection.HTTP_MOVED_PERM
 *
 * @author <a href="mailto:kazemnaderi@yahoo.ca">Kazem Naderi</a>
 * @version $Revision$
 * @since uPortal 2.2
 */

public class ProxyWriter {

	private static final Log log = LogFactory.getLog(ProxyWriter.class);

	/**
	 * True if allow rewriting certain elements for proxying.
	 */
	protected boolean _proxying;

	/**
	 * The list of elemnets which src attribute is rewritten with proxy.
	 */
	private static final String[] _proxiableElements = { "image", "img",
			"script", "input", "applet", "iframe" };

	/*
	 * If enabled the references to images or any external browser loadable resources will be proxied.
	 */
	private static boolean PROXY_ENABLED = PropertiesManager
			.getPropertyAsBoolean("org.jasig.portal.serialize.ProxyWriter.resource_proxy_enabled");

	/*
	 * The prefix used for proxying
	 */
	private static final String PROXY_REWRITE_PREFIX = PropertiesManager
			.getProperty("org.jasig.portal.serialize.ProxyWriter.resource_proxy_rewrite_prefix");

	/*
	 * The local domain that does not do redirection
	 */
	private static final String PROXY_REWRITE_NO_REDIRECT_DOMAIN = PropertiesManager
			.getProperty("org.jasig.portal.serialize.ProxyWriter.no_redirect_domain");
	
	/**
	 * This will enable the generated proxied urls to be stateful.  This is used
	 * in conjunction with uPortal's Proxy servlet (/servlet/ProxyServlet/) and
	 * if enabled with another proxy servlet (i.e. external), will not work.
	 */
	private static boolean STATEFUL_PROXY_URLS = PropertiesManager
            .getPropertyAsBoolean("org.jasig.portal.serialize.ProxyWriter.stateful_proxy_urls", false);


	/**
	 * Examines whther or not the proxying should be done and if so handles differnt situations by delegating
	 * the rewrite to other methods n the class.
	 * @param name
	 * @param localName
	 * @param url
	 * @return value
	 */
	protected static String considerProxyRewrite(final String name, final String localName,
			final String url, ProxyResourceMap proxyResourceMap) {
		if (PROXY_ENABLED
		        && !localName.equalsIgnoreCase("script")
				&& (name.equalsIgnoreCase("src") || name
						.equalsIgnoreCase("archive"))
				&& url.indexOf("http://") != -1) {

			// capture any resource redirect and set the value to the real
			// address while proxying it
		    
			final String skip_protocol = url.substring(7);
			final String domain_only = skip_protocol.substring(0, skip_protocol.indexOf("/"));
			/**
			 * Capture 3xx return codes - specifically, if 301/302, then go to the
			 * redirected url - note, this in turn may also be redirected. Note - do as
			 * little network connecting as possible. So as a start, assume "ubc.ca"
			 * domain images will not be redirected so skip these ones.
			 */
			if (PROXY_REWRITE_NO_REDIRECT_DOMAIN.length() > 0
					&& !domain_only.endsWith(PROXY_REWRITE_NO_REDIRECT_DOMAIN)) {
				String work_url = url;
				while (true) {
					final HttpClient client = HttpClientManager.getNewHTTPClient();
					final GetMethod get = new GetMethod(work_url);

					try {
						final int responseCode = client.executeMethod(get);
						if (responseCode != HttpStatus.SC_MOVED_PERMANENTLY
								&& responseCode != HttpStatus.SC_MOVED_TEMPORARILY) {
							// handle normal proxies
							for (int i = 0; i < _proxiableElements.length; i++) {
								if (localName.equalsIgnoreCase(_proxiableElements[i])) {
								    if (STATEFUL_PROXY_URLS) {
										work_url = generateMappedProxyUrl(work_url.substring(7), proxyResourceMap);
								    } else {
										work_url = PROXY_REWRITE_PREFIX + work_url.substring(7);
								    }
									break;
								}
							}
							return work_url;
						}

						/* At this point we will have a redirect directive */
						final Header location = get.getResponseHeader("location");
						if (location != null) {
							work_url = location.getValue();
						} else {
							return url;
						}

						// According to httpClient documentation we have to read the body
						final InputStream in = get.getResponseBodyAsStream();
						try {
							final byte buff[] = new byte[4096];
							while (in.read(buff) > 0) {};
						} finally {
							in.close();
						}
					} catch (IOException ioe) {
						return url;
					} finally {
						get.releaseConnection();
					}
				}
			}
		}
		return url;
	}

	private static String generateMappedProxyUrl(final String url,
        final ProxyResourceMap proxyResourceMap) {

        int resourceId = proxyResourceMap.getNextResourceId();
        proxyResourceMap.put(resourceId, url);

        StringBuffer sb = new StringBuffer(PROXY_REWRITE_PREFIX);
        sb.append("?resourceId=").append(resourceId);
        return sb.toString();
    }
	
}
