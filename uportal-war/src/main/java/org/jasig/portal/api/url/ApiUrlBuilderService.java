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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jasig.portal.security.mvc.LoginController;
import org.jasig.portal.security.sso.RemoteUserFilterBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

/**
 * See design notes in {@link iUrlBuilderService}
 */
@Service
public class ApiUrlBuilderService implements UrlBuilderService {

	// For building the loginUrl
	private static final String LOGIN_SERVLET_PATH = "/Login";

	@Value("${environment.build.uportal.protocol}")
	private String protocol;

	@Value("${environment.build.uportal.server}")
	private String server;

	@Value("${environment.build.uportal.context}")
	private String context;

	private final Map<String, UrlTemplate> getUrlTemplates() {
		return Collections.unmodifiableMap(new HashMap<String, UrlTemplate>() {
			{
				put("ea.form",
						new UrlTemplate(
								"p/early-alert?pP_action=enterAlert&",
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
								"p/early-alert?pP_action=enterAlert&",
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
						new UrlTemplate("p/early-alert?", Collections
								.unmodifiableMap(new HashMap<String, String>() {
									{
										put("formattedCourse",
												"pP_formattedCourse");
										put("sectionCode", "pP_sectionCode");
										put("termCode", "pP_termCode");
									}
								})));
				put("ea.roster",
						new UrlTemplate("p/early-alert?", Collections
								.unmodifiableMap(new HashMap<String, String>() {
									{
										put("formattedCourse",
												"pP_formattedCourse");
										put("sectionCode", "pP_sectionCode");
										put("termCode", "pP_termCode");
									}
								})));
				put("ssp",
						new UrlTemplate("p/ssp", Collections
								.unmodifiableMap(new HashMap<String, String>())));
				put("mygps",
						new UrlTemplate("p/mygps", Collections
								.unmodifiableMap(new HashMap<String, String>())));
				put("reports",
						new UrlTemplate("p/reports", Collections
								.unmodifiableMap(new HashMap<String, String>())));

			}
		});
	}

	@Override
	public String issueUrl(BuildUrlRequest buildRequest)
			throws UnsupportedEncodingException, MalformedURLException {

		URL url = new URL(buildBaseUrlWithLogin());

		final StringBuilder login = new StringBuilder();
		if (url != null && buildRequest.getSsoTicket() != null) {
			login.append(url.toExternalForm())
					.append("?")
					.append(RemoteUserFilterBean.TICKET_PARAMETER)
					.append("=")
					.append(URLEncoder.encode(buildRequest.getSsoTicket()
							.getUuid(), "UTF-8")).append("&")
					.append(LoginController.REFERER_URL_PARAM).append("=")
					.append(URLEncoder.encode(buildTemplate(buildRequest), "UTF-8"));
			return login.toString();
		}

		return  protocol + "://" + server + buildTemplate(buildRequest);
	}

	private String buildBaseUrlWithLogin() {
		return protocol + "://" + server + context + LOGIN_SERVLET_PATH;
	}

	String buildTemplate(BuildUrlRequest urlBuildRequest)
			throws UnsupportedEncodingException {
		UrlTemplate template = getUrlTemplates().get(
				urlBuildRequest.getUrlTemplateName());
		Map<String, String> parameters = urlBuildRequest.getParameters();
		Map<String, String> pathParams = new HashMap<String, String>();

		final StringBuilder redirect = new StringBuilder(context + "/"
				+ template.root);

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
