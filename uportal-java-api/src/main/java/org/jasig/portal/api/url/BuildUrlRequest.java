package org.jasig.portal.api.url;

import java.util.Map;

import org.jasig.portal.api.sso.SsoTicket;


public interface BuildUrlRequest {

	Map<String, String> getParameters();
	
	String getRequestUrl();

	String getContextPath();

	String getUrlTemplateName();
	
	SsoTicket getSsoTicket();

}
