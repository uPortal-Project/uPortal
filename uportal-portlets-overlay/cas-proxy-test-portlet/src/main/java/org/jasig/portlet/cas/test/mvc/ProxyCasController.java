/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.cas.test.mvc;

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;

/**
 *
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
@RequestMapping("VIEW")
public final class ProxyCasController {

	private Log log = LogFactory.getLog(ProxyCasController.class);

	private TicketValidator validator;

	/**
	 * Set the ticket validator to use for proxy ticket validation.
	 *
	 * @param validator
	 */
	@Autowired(required = true)
	public void setTicketValidator(TicketValidator validator) {
		this.validator = validator;
	}

	private String serviceUrl = "http://localhost:8080/cas-proxy-test-portlet";

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	private String proxyTicketKey = "casProxyTicket";


	/**
	 * Attempt to validate the proxy ticket supplied to the UserInfo map and
	 * display the result in the main view of the portlet.  If the ticket is
	 * not found or cannot be validated, a short debugging message will be
	 * displayed in the portlet.
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping
	public ModelAndView validateProxyCas(RenderRequest request) {
		Map<String,Object> model = new HashMap<String,Object>();

		// get the proxy ticket from the UserInfo map
		@SuppressWarnings("unchecked")
		Map<String,String> userInfo = (Map<String,String>) request.getAttribute(PortletRequest.USER_INFO);
		String proxyTicket = userInfo.get(proxyTicketKey);
		if (proxyTicket == null){
			model.put("success", false);
			model.put("message", "No proxy ticket in UserInfo map");
			return new ModelAndView("/proxyPortlet", model);
		}

		// attempt to validate the proxy ticket
		try {
			Assertion assertion = validator.validate(proxyTicket, serviceUrl);

			// make sure we can proxy other sites
			@SuppressWarnings("unused")
            String proxyTicket2 = assertion.getPrincipal().getProxyTicketFor(serviceUrl);
			model.put("success", true);
		} catch (TicketValidationException e) {
			log.error("Exception attempting to validate proxy ticket", e);
			model.put("success", false);
			model.put("message", "Unable to validate proxy ticket");
		}

		return new ModelAndView("/proxyPortlet", model);
	}

}
