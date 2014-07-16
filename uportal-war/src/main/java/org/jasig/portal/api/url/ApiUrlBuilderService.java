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
package org.jasig.portal.api.url;

import org.apache.commons.lang3.StringUtils;
import org.jasig.portal.security.mvc.LoginController;
import org.jasig.portal.security.sso.RemoteUserFilterBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * See design notes in {@link UrlBuilderService}
 */
@Service
public class ApiUrlBuilderService implements UrlBuilderService {

	// For building the loginUrl
	private static final String LOGIN_SERVLET_PATH = "/Login";
	
	// For building the logoutUrl
	private static final String LOGOUT_SERVLET_PATH = "/Logout";

	@Value("${environment.build.uportal.protocol}")
	private String protocol;

	@Value("${environment.build.uportal.server}")
	private String server;

	@Value("${environment.build.uportal.context}")
	private String context;

	private final Map<String, UrlTemplate> getUrlTemplates() {
		return Collections.unmodifiableMap(new HashMap<String, UrlTemplate>() {
			{
				put("default", new UrlTemplate("/", Collections.EMPTY_MAP));
				put("ea.form",
						new UrlTemplate(
								"/p/early-alert?pP_action=enterAlert&",
								Collections
										.unmodifiableMap(new HashMap<String, String>() {
											{
												put("schoolId", "pP_schoolId");
												put("formattedCourse",
														"pP_formattedCourse");
												put("sectionCode",
														"pP_sectionCode");
												put("termCode", "pP_termCode");
												put("studentUserName",
														"pP_studentUserName");
											}
										})));
				put("ea.new",
						new UrlTemplate(
								"/p/early-alert?pP_action=enterAlert&",
								Collections
										.unmodifiableMap(new HashMap<String, String>() {
											{
												put("schoolId", "pP_schoolId");
												put("formattedCourse",
														"pP_formattedCourse");
												put("sectionCode",
														"pP_sectionCode");
												put("termCode", "pP_termCode");
												put("studentUserName",
														"pP_studentUserName");
											}
										})));
				put("ea",
						new UrlTemplate("/p/early-alert?", Collections
								.unmodifiableMap(new HashMap<String, String>() {
									{
										put("formattedCourse",
												"pP_formattedCourse");
										put("sectionCode", "pP_sectionCode");
										put("termCode", "pP_termCode");
									}
								})));
				put("ea.roster",
						new UrlTemplate("/p/early-alert?", Collections
								.unmodifiableMap(new HashMap<String, String>() {
									{
										put("formattedCourse",
												"pP_formattedCourse");
										put("sectionCode", "pP_sectionCode");
										put("termCode", "pP_termCode");
									}
								})));
				put("ssp",
						new UrlTemplate("/p/ssp", Collections
								.unmodifiableMap(new HashMap<String, String>())));
				put("mygps",
						new UrlTemplate("/p/mygps", Collections
								.unmodifiableMap(new HashMap<String, String>())));
				put("reports",
						new UrlTemplate("/p/reports", Collections
								.unmodifiableMap(new HashMap<String, String>())));

			}
		});
	}

	@Override
	public String buildUrl(BuildUrlRequest buildRequest)
			throws UnsupportedEncodingException, MalformedURLException {

		final String templateName = buildRequest.getUrlTemplateName();
		if ( StringUtils.isBlank(templateName) ) {
			throw new IllegalArgumentException("URL build request did not include a template name");
		}
		final String canonicalizedTemplateName = templateName.trim().toLowerCase();

		final UrlTemplate template = getUrlTemplates().get(canonicalizedTemplateName);
		if ( template == null ) {
			throw new IllegalArgumentException("URL template identifier [" + templateName +
					"] (canonicalized to [" + canonicalizedTemplateName + "]) not recognized");
		}

		String templatedUrl = buildTemplate(template, buildRequest);

		final boolean isSsoRequest = buildRequest.getSsoTicket() != null &&
				StringUtils.isNotBlank(buildRequest.getSsoTicket().getUuid());

		if ( !(isSsoRequest) ) {
			final String baseUrl = new URL(buildServerUrl().toString()).toExternalForm();
			return new StringBuilder(baseUrl).append(templatedUrl).toString();
		}

		final String logoutUrl = new URL(buildPlatformUrlWithLogout().toString()).toExternalForm();
		final String loginUrl = buildContextWithLogin().toString();
		
		 String loginWithReferrerUrl = new StringBuilder(loginUrl)
			.append("?")
			.append(RemoteUserFilterBean.TICKET_PARAMETER)
			.append("=")
			.append(URLEncoder.encode(buildRequest.getSsoTicket()
					.getUuid(), "UTF-8")).append("&")
			.append(LoginController.REFERER_URL_PARAM).append("=")
			.append(URLEncoder.encode(templatedUrl, "UTF-8")).toString();
		 
		return new StringBuilder(logoutUrl)
					.append("?")
					.append(LoginController.REFERER_URL_PARAM).append("=")
					.append(URLEncoder.encode(loginWithReferrerUrl, "UTF-8")).toString();
	}

	private StringBuilder buildServerUrl() {
		return new StringBuilder(protocol).append("://").append(server);
	}

	private StringBuilder buildPlatformUrl() {
		return buildServerUrl().append(context);
	}

	private StringBuilder buildContextWithLogin() {
		return new StringBuilder(context).append(LOGIN_SERVLET_PATH);
	}
	
	private StringBuilder buildPlatformUrlWithLogout() {
		return buildPlatformUrl().append(LOGOUT_SERVLET_PATH);
	}

	private String buildTemplate(UrlTemplate template, BuildUrlRequest urlBuildRequest)
			throws UnsupportedEncodingException {

		Map<String, String> parameters = urlBuildRequest.getParameters();
		Map<String, String> pathParams = new HashMap<String, String>();

		final StringBuilder redirect = new StringBuilder(context).append(template.root);

		String suffix = "";
		for (String parameterKey : parameters.keySet()) {
			if(StringUtils.isBlank(parameters.get(parameterKey))){
				continue;
			}
			if (template.paramKeyTranslation.containsKey(parameterKey)) {
				redirect.append(suffix)
						.append(template.paramKeyTranslation.get(parameterKey))
						.append("=")
						.append(URLEncoder.encode(parameters.get(parameterKey),
								"UTF-8"));
				suffix = "&";
			} else {
				pathParams.put(parameterKey, parameters.get(parameterKey));
			}
		}
		buildTemplatePath(redirect, pathParams);
		String url = redirect.toString();

		// removes any path params not matched
		return url.replaceAll("(/\\{.*\\}/)", "/").split("\\{")[0];
	}

	private void buildTemplatePath(StringBuilder redirect,
			Map<String, String> pathParams) throws UnsupportedEncodingException {
		for (String parameterKey : pathParams.keySet()) {
			if(pathParams.get(parameterKey) == null)
				continue;
			String paramToken = "/{" + parameterKey + "}";
			int start = redirect.indexOf(paramToken);
			if (start < 0)
				continue;
			String encodedParam = UriUtils.encodePathSegment(pathParams.get(parameterKey), "UTF-8");
			redirect.replace(start, start + paramToken.length(), "/"
					+ encodedParam);
		}
	}

	private static class UrlTemplate {
		String root;
		Map<String, String> paramKeyTranslation;
		List<String> pathParams = null;

		UrlTemplate(String root, Map<String, String> paramKeyTranslation) {
			this.root = root;
			this.paramKeyTranslation = paramKeyTranslation;
		}
	}

}
