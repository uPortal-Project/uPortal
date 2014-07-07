package org.jasig.portal.api.url;

import java.util.Map;

import org.jasig.portal.api.sso.SsoTicket;

public class BuildUrlRequest {

	private Map<String, String> parameters;
	private String urlTemplateName;
	private SsoTicket ssoTicket;

	public BuildUrlRequest(Map<String, String> parameters,
			String urlTemplateName, SsoTicket ssoTicket) {
		this.urlTemplateName = urlTemplateName;
		this.parameters = parameters;
		this.ssoTicket = ssoTicket;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public String getUrlTemplateName() {
		return urlTemplateName;
	}

	public SsoTicket getSsoTicket() {
		return ssoTicket;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public void setUrlTemplateName(String urlTemplateName) {
		this.urlTemplateName = urlTemplateName;
	}

	public void setSsoTicket(SsoTicket ssoTicket) {
		this.ssoTicket = ssoTicket;
	}

}
