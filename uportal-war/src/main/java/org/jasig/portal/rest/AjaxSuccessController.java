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
package org.jasig.portal.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This controller and the endpoint it responds to provide a cheap, general-purpose 
 * way for a portlet controller responding to an ajax-based action request to 
 * send a redirect to something that returns HTTP 200.  We like to use renderURLs 
 * for portlet ajaxt requests, but there are some things that can only be done 
 * in an action request.  Normally action handlers are followed by a complete 
 * page lifecycle -- event/resource/render phases plus rendering other portlets 
 * and assembling a complete page.  These tasks are wasteful for an ajax-based 
 * request, so action handler that wish to avoid that waste may redirect to this 
 * endpoint.
 * 
 * @author awills
 */
@Controller
public class AjaxSuccessController {

    public static final String SUCCESS_URL = "/ajax-success";
    public static final String SUCCESS_RESPONSE = "{ 'success': 'true' }";

    @RequestMapping(SUCCESS_URL)
    public @ResponseBody String sendJsonSuccess(HttpServletRequest request, HttpServletResponse response) {
        return SUCCESS_RESPONSE;
//        return "jsp/ajax-success";
    }


}
