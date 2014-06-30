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

import java.util.HashMap;
import java.util.Map;

import org.jasig.portal.api.sso.SsoTicket;
import org.jasig.portal.api.url.BuildUrlRequest;

public class BuildUrlRequestImpl implements BuildUrlRequest{

	private Map<String,String> parameters = new HashMap<String, String>();
	private String requestUrl;  //retrieved from HttpRequest
	private String contextPath; //retrieved from HttpRequest
	private String urlTemplateName;  //determined by target
	private SsoTicket ssoTicket;
	

	public BuildUrlRequestImpl(Map<String, String> parameters, String requestUrl,
			String contextPath, String urlTemplateName, SsoTicket ssoTicket) {
		super();
		this.parameters = parameters;
		this.requestUrl = requestUrl;
		this.contextPath = contextPath;
		this.urlTemplateName = urlTemplateName;
		this.ssoTicket = ssoTicket;
	}

	@Override
	public Map<String, String> getParameters() {
		return parameters;
	}
	

	@Override
	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	@Override
	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	@Override
	public String getUrlTemplateName() {
		return urlTemplateName;
	}

	public void setUrlTemplateName(String urlTemplateName) {
		this.urlTemplateName = urlTemplateName;
	}

	@Override
	public SsoTicket getSsoTicket() {
		return ssoTicket;
	}


	public void setSsoTicket(SsoTicket ssoTicket) {
		this.ssoTicket = ssoTicket;
	}
}
