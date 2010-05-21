/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlet.url;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletURLProvider;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletPortalUrl;
import org.jasig.portal.utils.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Contains the logic and string constants for generating and parsing portlet URL parameters.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Component
public class PortletUrlSyntaxProviderImpl implements IPortletUrlSyntaxProvider {

	private static final String SEPARATOR = "_";
	private static final String PORTLET_CONTROL_PREFIX = "pltc" + SEPARATOR;
	private static final String PORTLET_PARAM_PREFIX = "pltp" + SEPARATOR;

	private static final Pattern URL_PARAM_NAME = Pattern.compile("&([^&?=\n]*)");

	protected final Log logger = LogFactory.getLog(this.getClass());

	private String defaultEncoding = "UTF-8";
	private IPortalUrlProvider portalUrlProvider;

	/**
	 * @param defaultEncoding the defaultEncoding to set
	 */
	public void setDefaultEncoding(String defaultEncoding) {
		Validate.notEmpty(defaultEncoding, "defaultEncoding cannot be empty");
		this.defaultEncoding = defaultEncoding;
	}
	/**
	 * @param portalUrlProvider the portalUrlProvider to set
	 */
	@Autowired
	public void setPortalUrlProvider(final IPortalUrlProvider portalUrlProvider) {
		this.portalUrlProvider = portalUrlProvider;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider#generatePortletUrl(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, org.jasig.portal.portlet.url.PortletUrl)
	 */
	public String generatePortletUrl(HttpServletRequest request,
			IPortletWindow portletWindow, PortletUrl portletUrl) {
		IPortletPortalUrl portalPortletUrl = portalUrlProvider.getPortletUrl(request, portletWindow.getPortletWindowId());
		portalPortletUrl = mergeWithPortletUrl(portalPortletUrl, portletUrl);
		return portalPortletUrl.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider#parsePortletUrl(javax.servlet.http.HttpServletRequest)
	 */
	public PortletUrl parsePortletUrl(HttpServletRequest request) {
		IPortalRequestInfo requestInfo = portalUrlProvider.getPortalRequestInfo(request);
		if(null == requestInfo.getTargetedPortletWindowId()) {
			return null;
		} else {
			IPortletWindowId portletWindowId = requestInfo.getTargetedPortletWindowId(); 
			IPortletPortalUrl portalPortletUrl = portalUrlProvider.getPortletUrl(request, portletWindowId);
			return toPortletUrl(portletWindowId, portalPortletUrl);
		}
	}

	/**
	 * Convert a {@link IPortalPortletUrl} into a {@link PortletUrl}.
	 * 
	 * @param portalPortletUrl
	 * @return
	 */
	protected static PortletUrl toPortletUrl(IPortletWindowId portletWindowId, IPortletPortalUrl portalPortletUrl) {
		PortletUrl result = new PortletUrl(portletWindowId);
		Map<String, List<String>> parameters = portalPortletUrl.getPortletParameters();
		result.setParameters(parameters);

		result.setPortletMode(portalPortletUrl.getPortletMode());

		if(portalPortletUrl.isAction()) {
			result.setRequestType(PortletURLProvider.TYPE.ACTION);
		} else {
			result.setRequestType(PortletURLProvider.TYPE.RENDER);
		}

		// null is the default value for the secure field
		//result.setSecure(null);

		result.setWindowState(portalPortletUrl.getWindowState());
		return result;
	}

	/**
	 * The purpose of this method is to port the fields of the {@link PortletUrl} argument
	 * to the appropriate fields of the {@link IPortletPortalUrl} argument.
	 * 
	 * This method mutates the {@link IPortletPortalUrl} argument and return it.
	 * 
	 * Neither argument can be null.
	 * 
	 * @param original
	 * @param mergeWith
	 * @return the updated original {@link IPortalPortletUrl}
	 */
	protected static IPortletPortalUrl mergeWithPortletUrl(IPortletPortalUrl original, PortletUrl mergeWith) {
		Validate.notNull(original, "original IPortalPortletUrl must not be null");
		Validate.notNull(mergeWith, "mergeWith PortletUrl must not be null");
		if (PortletURLProvider.TYPE.ACTION == mergeWith.getRequestType()) {
			original.setAction(true);
		}
		original.setPortletMode(mergeWith.getPortletMode());

		original.setWindowState(mergeWith.getWindowState());

		final Map<String, List<String>> mergeParameters = mergeWith.getParameters();
		for (final Map.Entry<String, List<String>> mergeParameter : mergeParameters.entrySet()) {
			original.setPortalParameter(mergeParameter.getKey(), mergeParameter.getValue());
		}
		return original;
	}

	/**
	 * Parses the request URL to return a Set of the parameter names that appeared on the URL string.
	 * 
	 * @param request The request to look at.
	 * @return The Set of parameter names from the URL.
	 */
	protected Set<String> getUrlParameterNames(HttpServletRequest request) {
		// Only posts can have parameters not in the URL, ignore non-post requests.
		final String method = request.getMethod();
		if (!"POST".equals(method)) {
			return null;
		}

		final Set<String> urlParameterNames = new HashSet<String>();

		final String queryString = request.getQueryString();
		final Matcher paramNameMatcher = URL_PARAM_NAME.matcher("&" + queryString);

		final String encoding = this.getEncoding(request);

		while (paramNameMatcher.find()) {
			final String paramName = paramNameMatcher.group(1);
			String decParamName;
			try {
				decParamName = URLDecoder.decode(paramName, encoding);
			}
			catch (UnsupportedEncodingException uee) {
				decParamName = paramName;
			}

			urlParameterNames.add(decParamName);
		}

		return urlParameterNames;
	}

	/**
	 * Tries to determine the encoded from the request, if not available falls back to configured default.
	 * 
	 * @param request The current request.
	 * @return The encoding to use.
	 */
	protected String getEncoding(HttpServletRequest request) {
		final String encoding = request.getCharacterEncoding();
		if (encoding != null) {
			return encoding;
		}

		return this.defaultEncoding;
	}

}
