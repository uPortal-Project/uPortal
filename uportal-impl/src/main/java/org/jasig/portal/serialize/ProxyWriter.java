/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.serialize;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.services.HttpClientManager;
import org.jasig.portal.utils.CommonUtils;

/**
 * Appends PROXY_REWRITE_PREFIX string in front of all the references to images
 * that are on a remote location that start with http://. This allows the
 * browser to load the resources without triggering a warning about mixed
 * content. For example instead of http://www.abc.com/image.gif the URI will be
 * rewritten to https://[portaladdress]/PROXY_REWRITE_PREFIX/www.abc.com/image.gif
 *
 * This class also does the proxy rewrite in the following exceptional situations:
 *
 * 1. If the return code pointing to the image is 3XX (the image reference,
 * references is a mapping to a different location) In this case the final
 * destination address in which the image or the resource is located is e and
 * then the rewrite points to this location.
 *
 * 2. If the content of a channel is an include javascript file the file is
 * rewritten to a location on a local virtual host and at the same time the
 * image or other resources references are rewritten.
 * HttpURLConnection.HTTP_MOVED_PERM
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
	 * The list of elements which src attribute is rewritten with proxy.
	 */

	//    Only image content should be proxied
    private static final String[] _proxiableElements = { "image", "img", "input"};

	/*
	 * If enabled the references to images or any external browser loadable resources will be proxied.
	 */
	private static boolean PROXY_ENABLED = PropertiesManager
			.getPropertyAsBoolean("org.jasig.portal.serialize.ProxyWriter.resource_proxy_enabled");

	/*
	 * The URI of location on virtual host on the same server as portal. This URI is used for rewriting proxied files.
	 */
	private static String PROXIED_FILES_URI = PropertiesManager
			.getProperty("org.jasig.portal.serialize.ProxyWriter.proxy_files_uri");

	/*
	 * The path of location on virtual host on the same server as portal. This path is used for rewriting proxied files.
	 */
	private static String PROXIED_FILES_PATH = PropertiesManager
			.getProperty("org.jasig.portal.serialize.ProxyWriter.proxy_files_path");

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
	 * Examines whether or not the proxying should be done and if so handles different situations by delegating
	 * the rewrite to other methods n the class.
	 * @param name
	 * @param localName
	 * @param url
	 * @return value
	 */
	protected static String considerProxyRewrite(final String name, final String localName,
			final String url) {
		if (PROXY_ENABLED
				&& (name.equalsIgnoreCase("src") || name
						.equalsIgnoreCase("archive"))
				&& url.indexOf("http://") != -1) {

			// capture any resource redirect and set the value to the real
			// address while proxying it
			final String skip_protocol = url.substring(7);
			final String domain_only = skip_protocol.substring(0, skip_protocol.indexOf("/"));
			/**
			 * Capture 3xx return codes - specifically, if 301/302, then go to
			 * the redirected URL - note, this in turn may also be redirected.
			 * Note - do as little network connecting as possible. So as a
			 * start, assume PROXY_REWRITE_NO_REDIRECT_DOMAIN domain images will
			 * not be redirected, so skip these ones.
			 */
			if (PROXY_REWRITE_NO_REDIRECT_DOMAIN.length() == 0
					|| !domain_only.endsWith(PROXY_REWRITE_NO_REDIRECT_DOMAIN)) {
				String work_url = url;
				while (true) {
					final HttpClient client = HttpClientManager.getNewHTTPClient();
					final GetMethod get = new GetMethod(work_url);

					try {
						final int responseCode = client.executeMethod(get);
						if (responseCode != HttpStatus.SC_MOVED_PERMANENTLY
								&& responseCode != HttpStatus.SC_MOVED_TEMPORARILY) {
							// if there is a script element with a src attribute
							// the src should be rewritten
							if (localName.equalsIgnoreCase("script")) {
								return reWrite(work_url, get);
							} else {
								// handle normal proxies
								for (int i = 0; i < _proxiableElements.length; i++) {
									if (localName.equalsIgnoreCase(_proxiableElements[i])) {
										work_url = PROXY_REWRITE_PREFIX + work_url.substring(7);
										break;
									}
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
					} catch (ConnectTimeoutException cte) {
					  // Remove non-responding URL to prevent browser trying it again
					  return "";
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

	/**
	 * This method rewrites included javascript files and replaces the references in these files
	 * to images' sources to use proxy.
	 *
	 * @param scriptUri: The string representing the address of script
	 * @return value: The new address of the script file which image sources have been rewritten
	 */
	private static String reWrite(final String scriptUri, final GetMethod get) {
		final String fileName = fileNameGenerator(scriptUri);
		final String filePath = PROXIED_FILES_PATH + fileName;
		try {
			final File outputFile = new File(filePath);
			if (!outputFile.exists()
					|| (System.currentTimeMillis() - outputFile.lastModified() > 1800 * 1000)) {
				try {
					final BufferedReader in = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream()));
					try {
						final FileWriter out = new FileWriter(outputFile);
						try {
							String line;
							while ((line = in.readLine()) != null) {
								out.write(processLine(line) + "\t\n");
							}
						} finally {
							out.close();
						}
					} finally {
						in.close();
					}
				} catch (Exception e) {
					log.error(
							"ProxyWriter::rewrite():Failed to rewrite the file for: "
									+ scriptUri, e);
					outputFile.delete();
					return scriptUri;
				} // end catch
			}

			// Now make sure that we can read the modified version
			final String newScriptPath = PROXIED_FILES_URI + fileName;
			final HttpClient client = HttpClientManager.getNewHTTPClient();
			final GetMethod getTest = new GetMethod(newScriptPath);

			try {
				final int rc = client.executeMethod(getTest);
				if (rc != HttpStatus.SC_OK) {
					log.error("ProxyWriter::rewrite(): The file  " + filePath
							+ " is written but cannot be reached at "
							+ newScriptPath);
					return scriptUri;
				} else {
					return PROXY_REWRITE_PREFIX
							+ PROXIED_FILES_URI.substring(7) + fileName;
				}
			} finally {
				getTest.releaseConnection();
			}

		} catch (IOException e) {
			log.error("ProxyWriter::rewrite(): Failed to read the file at : " + filePath, e);
			return scriptUri;
		}
	}

	/**
	 * This method uses a URI and creates an HTML file name by simply omitting some characters from the URI.
	 * The purpose of using the address for the file name is that the file names will be unique and map to addresses.
	 * @param addr: is the address of the file
	 * @newName: is the name built form the address
	 */
	private static String fileNameGenerator(String addr) {
		String newName = CommonUtils.replaceText(addr, "/", "");
		newName = CommonUtils.replaceText(newName, "http:", "");
		newName = CommonUtils.replaceText(newName, "www.", "");
		newName = CommonUtils.replaceText(newName, ".", "");
		newName = CommonUtils.replaceText(newName, "?", "");
		newName = CommonUtils.replaceText(newName, "&", "");

		return newName.substring(0, Math.min(16, newName.length())) + ".html";
	}

	/**
	 * This method parses a line recursively and replaces all occurrences of image references
	 * with a proxied reference.
	 * @param line - is the portion of the line or the whole line to be processed.
	 * @return line - is the portion of the line or the line that has been processed.
	 */
	private static String processLine(String line) throws Exception {
		try {
			if (line.indexOf(" src") != -1 && line.indexOf("http://") != -1) {
				String srcValue = extractURL(line);
				String srcNewValue = createProxyURL(srcValue);
				line = CommonUtils.replaceText(line, srcValue, srcNewValue);
				int firstPartIndex = line.lastIndexOf(srcNewValue)
						+ srcNewValue.length();
				String remaining = line.substring(firstPartIndex);
				return line.substring(0, firstPartIndex) + "  "
						+ processLine(remaining);
			} else {
				return line;
			}
		} catch (Exception e) {

			log.error("Failed to process a line : " + line, e);
			throw e;
		}
	}

	/**
	 *
	 * This method takes a String (line) and parses out the value of src attribute
	 * in that string.
	 * @param line - String
	 * @return srcValue - String
	 */
	private static String extractURL(String line) {
		int URLStartIndex = 0;
		int URLEndIndex = 0;
		//need this to make sure only image paths are pointed to and not href.
		int srcIndex = line.indexOf(" src");
		if (line.indexOf("https://", srcIndex) != -1) {
			return "";
		}
		if (line.indexOf("http://", srcIndex) != -1) {
			URLStartIndex = line.indexOf("http", srcIndex);
		} else {
			return "";
		}

		URLEndIndex = line.indexOf(" ", URLStartIndex);
		String srcValue = line.substring(URLStartIndex, URLEndIndex);
		return srcValue;
	}

	/**
	 *
	 * This method receives an image source URL and modified
	 * it to be proxied.
	 * @param srcValue - String
	 * @return srcNewValue - String
	 */
	private static String createProxyURL(String srcValue) {
		String srcNewValue = "";
		if (srcValue.indexOf("https://") != -1) {
			return srcValue;
		} else if (srcValue.indexOf("http://") != -1) {
			srcNewValue = CommonUtils.replaceText(srcValue, "http://",
					PROXY_REWRITE_PREFIX);
		} else {
			srcNewValue = "";
		}
		return srcNewValue;
	}

}
