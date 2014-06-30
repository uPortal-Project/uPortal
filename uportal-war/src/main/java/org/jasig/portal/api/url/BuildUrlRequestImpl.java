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
