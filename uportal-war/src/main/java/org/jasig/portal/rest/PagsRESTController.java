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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jasig.portal.portlets.groupadmin.AdHocPagsForm;
import org.jasig.portal.portlets.groupadmin.PagsAdministrationHelper;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.RuntimeAuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * REST Controller that leverages PagsAdministrationHelper and related form classes
 * to provide a RESTful API for AJAX management of PAGS.
 * 
 * @author Benito Gonzalez, bgonzalez@unicon.net
 * @see org.jasig.portal.portlets.groupadmin.PagsAdministrationHelper
 * @see org.jasig.portal.portlets.groupadmin.AdHocPagsForm
 */
@Controller
public class PagsRESTController {
    /*
      Tried to use ResponseEntities but the ran into conflicts when using <mvc:annotation-driven/>
     */

    @Autowired
    private PagsAdministrationHelper pagsAdministrationHelper;

    @Autowired
    private IPersonManager personManager;

    @Autowired
    private ObjectMapper objectMapper;

    // Just for testing!
    @RequestMapping(value = "/entities/pagstest/{pagsGroupName}.json", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    public @ResponseBody String findPagsGroupDetails(HttpServletRequest request, HttpServletResponse response,
                                              @PathVariable("pagsGroupName") String pagsGroupName) {

        try {
            return objectMapper.writeValueAsString(pagsAdministrationHelper.getPagsGroupDefByName(pagsGroupName));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            return "{ 'error': '" + e.toString() + "' }";
        }
    }
    @RequestMapping(value = "/entities/pags/{pagsGroupName}.json", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    public @ResponseBody String findPagsGroup(HttpServletRequest request, HttpServletResponse response,
                                              @PathVariable("pagsGroupName") String pagsGroupName) {

        IPerson person = personManager.getPerson(request);
        return respondPagsGroupJson(response, pagsGroupName, person, HttpServletResponse.SC_FOUND);
    }

    @RequestMapping(value = "/entities/pags/{pagsParentName}.json", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public @ResponseBody String createPagsGroup(HttpServletRequest request,
                                                HttpServletResponse response,
                                                @PathVariable("pagsParentName") String pagsParentName,
                                                @RequestBody String json) {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        AdHocPagsForm group;
        try {
            group = objectMapper.readValue(json, AdHocPagsForm.class);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{ 'error': '" + e.toString() + "' }"; // should be escaped
        }
        IPerson person = personManager.getPerson(request);
        try {
            pagsAdministrationHelper.createGroup(pagsParentName, group, person);
        } catch (IllegalArgumentException iae) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "{ 'error': '" + iae.getMessage() + "' }"; // should be escaped
        } catch (RuntimeAuthorizationException rae) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "{ 'error': 'not authorized' }";
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{ 'error': '" + e.toString() + "' }";
        }
        return respondPagsGroupJson(response, group.getName(), person, HttpServletResponse.SC_CREATED);
    }

    @RequestMapping(value = "/entities/pags/{pagsGroupName}.json", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    public @ResponseBody String updatePagsGroup(HttpServletRequest request,
                                                HttpServletResponse response,
                                                @PathVariable("pagsGroupName") String pagsGroupName,
                                                @RequestBody String json) {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        AdHocPagsForm group;
        try {
            group = objectMapper.readValue(json, AdHocPagsForm.class);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{ 'error': '" + e.toString() + "' }"; // should be escaped
        }
        if (!pagsGroupName.equals(group.getName())) {
             return "{ 'error': 'Group name in URL parameter must match name in JSON payload' }";
        }
        IPerson person = personManager.getPerson(request);
        try {
            pagsAdministrationHelper.updateGroup(person, group);
        } catch (IllegalArgumentException iae) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "{ 'error': '" + iae.getMessage() + "' }"; // should be escaped
        } catch (RuntimeAuthorizationException rae) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "{ 'error': 'not authorized' }";
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{ 'error': '" + e.toString() + "' }";
        }
        return respondPagsGroupJson(response, group.getName(), person, HttpServletResponse.SC_ACCEPTED);
    }

    @RequestMapping(value = "/entities/pags/{pagsGroupName}.json", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
    public @ResponseBody String deletePagsGroup(HttpServletRequest request,
                                                HttpServletResponse response,
                                                @PathVariable("pagsGroupName") String pagsGroupName) {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        StringBuilder result = new StringBuilder("{ 'delete': '");
        result.append(pagsGroupName);
        result.append("', 'success': ");
        IPerson person = personManager.getPerson(request);
        try {
            pagsAdministrationHelper.deleteGroup(pagsGroupName, person);
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            result.append("'true' }");
        } catch (IllegalArgumentException iae) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            result.append("'false', 'reason': '" + iae.getMessage() + "' }"); // should be escaped
        } catch (RuntimeAuthorizationException rae) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            result.append("'false', 'reason': 'un-authorized' }");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.append("'false', 'reason': 'unknown exception' }");
        }
        return result.toString();
    }

    private String respondPagsGroupJson(HttpServletResponse response, String pagsGroupName, IPerson person, int status) {
        AdHocPagsForm group;
        try {
            group = pagsAdministrationHelper.initializeAdHocPagsFormForUpdate(person, pagsGroupName);
        } catch (IllegalArgumentException iae) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "{ 'error': '" + iae.getMessage() + "' }";
        } catch (RuntimeAuthorizationException rae) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "{ 'error': 'not authorized' }";
        }
        try {
            response.setStatus(status);
            return objectMapper.writeValueAsString(group);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{ 'error': '" + e.toString() + "' }";
        }
    }
}
