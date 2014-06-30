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

import org.jasig.portal.api.sso.SsoTicketService;
import org.jasig.portal.api.url.BuildUrlRequest;
import org.jasig.portal.api.url.BuiltUrl;
import org.jasig.portal.api.url.UrlBuilderSerivce;
import org.jasig.portal.security.mvc.LoginController;
import org.jasig.portal.security.sso.RemoteUserFilterBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * See design notes in {@link SsoTicketService}, esp re the {@code secret}
 * field/arg.
 */
@Service
public class ApiUrlBuilderService implements UrlBuilderSerivce, InitializingBean {


    // For building the loginUrl
    private static final String LOGIN_SERVLET_PATH = "/Login";


    private static final Map<String,UrlTemplate> URL_TEMPLATES = Collections.unmodifiableMap(new HashMap<String, UrlTemplate>() {{ 
        put("sspEarlyAlertNew", new UrlTemplate("/p/early-alert", Collections.unmodifiableMap(new HashMap<String, String>() {{ 
        	put("schoolId","pP_schoolId");
        	put("formattedCourse","pP_formattedCourse");
        	put("sectionCode","pP_sectionCode");
        	put("termCode","pP_termCode");
        	put("studentUserName","pP_studentUserName");
        	put("action","pP_action");
        	}})));
        put("sspEarlyAlertRoster", new UrlTemplate("/p/early-alert", Collections.unmodifiableMap(new HashMap<String, String>() {{ 
        	put("formattedCourse","pP_formattedCourse");
        	put("sectionCode","pP_sectionCode");
        	put("termCode","pP_termCode");
        	}})));
        
    }});


	@Override
	public void afterPropertiesSet() throws Exception {
	
	}

	@Override
	public BuiltUrl issueUrl(BuildUrlRequest buildRequest) throws UnsupportedEncodingException, MalformedURLException {
		
		

        URL contextUrl = null;
		try {
			contextUrl = new URL(buildRequest.getRequestUrl().toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        URL loginUrl = null;
		loginUrl = new URL(contextUrl, buildRequest.getContextPath() + LOGIN_SERVLET_PATH);
		
        final StringBuilder login = new StringBuilder();
        if(loginUrl != null)
				login.append(loginUrl.toExternalForm())
				    .append("?").append(RemoteUserFilterBean.TICKET_PARAMETER).append("=").append(buildRequest.getSsoTicket().getUuid())
				    .append("&").append(LoginController.REFERER_URL_PARAM).append("=").append(URLEncoder.encode(buildTemplate(buildRequest), "UTF-8"));
			

        final BuiltUrlImp rslt = new BuiltUrlImp();
        rslt.setSuccess(true);
		rslt.setUrl(login.toString());
        return rslt;
	}
	
	String buildTemplate(BuildUrlRequest urlBuildRequest) throws UnsupportedEncodingException{
		UrlTemplate  template = URL_TEMPLATES.get(urlBuildRequest.getUrlTemplateName());
		final StringBuilder redirect = new StringBuilder(template.root);
		String suffix="/";
		if(template.pathParams != null){
			for(String parameterKey:template.pathParams){
				redirect.append(suffix).
				append(URLEncoder.encode(urlBuildRequest.getParameters().get(parameterKey).toString(), "UTF-8"));
			}
		}
		suffix="?";
		for(String parameterKey:urlBuildRequest.getParameters().keySet()){
			if(template.paramKeyTranslation.containsKey(parameterKey)){
				redirect.append(suffix).
					append(template.paramKeyTranslation.get(parameterKey)).
					append("=").
					append(URLEncoder.encode(urlBuildRequest.getParameters().get(parameterKey).toString(), "UTF-8"));
				suffix = "&";
			}
		}
		return redirect.toString();
	}
	
	static class UrlTemplate {
		String root;
		Map<String,String> paramKeyTranslation;
		List<String> pathParams = null;
		
		UrlTemplate(String root, Map<String,String> paramKeyTranslation){
			this.root = root;
			this.paramKeyTranslation = paramKeyTranslation;
		}
		
		UrlTemplate(String root, Map<String,String> paramKeyTranslation, List<String> pathParams){
			this.root = root;
			this.paramKeyTranslation = paramKeyTranslation;
			this.pathParams = pathParams;
		}
		
		
	}

	
}
