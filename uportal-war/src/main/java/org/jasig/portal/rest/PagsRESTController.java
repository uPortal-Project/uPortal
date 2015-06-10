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

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.jasig.portal.groups.pags.dao.PagsService;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.RuntimeAuthorizationException;
import org.jasig.portal.services.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private PagsService pagsService;

    @Autowired
    private IPersonManager personManager;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value="/v4-3/pags/{pagsGroupName}.json", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    public @ResponseBody String findPagsGroup(HttpServletRequest request, HttpServletResponse response,
                                              @PathVariable("pagsGroupName") String pagsGroupName) {
        IPerson person = personManager.getPerson(request);
        IPersonAttributesGroupDefinition pagsGroup = this.pagsService.getPagsDefinitionByName(person, pagsGroupName);
        return respondPagsGroupJson(response, pagsGroup, person, HttpServletResponse.SC_FOUND);
    }

    @RequestMapping(value="/v4-3/pags/{parentGroupName}.json", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public @ResponseBody String createPagsGroup(HttpServletRequest request,
                                                HttpServletResponse res,
                                                @PathVariable("parentGroupName") String parentGroupName,
                                                @RequestBody String json) {

        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        IPersonAttributesGroupDefinition inpt;
        try {
            inpt = objectMapper.readValue(json, IPersonAttributesGroupDefinition.class);
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{ 'error': '" + e.toString() + "' }"; // should be escaped
        }
        IPerson person = personManager.getPerson(request);
        EntityIdentifier[] eids = GroupService.searchForGroups(parentGroupName, IGroupConstants.IS, IPerson.class);
        if (eids.length == 0) {
            throw new IllegalArgumentException("Parent group does not exist: " + parentGroupName);
        }
        IEntityGroup parentGroup = GroupService.findGroup(eids[0].toString());  // Names must be unique

        IPersonAttributesGroupDefinition rslt;
        try {
            // A little weird that we need to do both;  need some PAGS DAO/Service refactoring
            rslt = pagsService.createPagsDefinition(person, parentGroup, inpt.getName(), inpt.getDescription());
            pagsService.updatePagsDefinition(person, rslt);
        } catch (RuntimeAuthorizationException rae) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "{ 'error': 'not authorized' }";
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{ 'error': '" + e.toString() + "' }";
        }
        return respondPagsGroupJson(res, rslt, person, HttpServletResponse.SC_CREATED);
    }

    @RequestMapping(value="/v4-3/pags/{pagsGroupName}.json", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    public @ResponseBody String updatePagsGroup(HttpServletRequest req,
                                                HttpServletResponse res,
                                                @PathVariable("pagsGroupName") String pagsGroupName,
                                                @RequestBody String json) {

        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        IPersonAttributesGroupDefinition inpt;
        try {
            inpt = objectMapper.readValue(json, IPersonAttributesGroupDefinition.class);
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{ 'error': '" + e.toString() + "' }"; // should be escaped
        }
        if (inpt == null) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "{ 'error': 'Not found' }";
        }
        if (!pagsGroupName.equals(inpt.getName())) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{ 'error': 'Group name in URL parameter must match name in JSON payload' }";
        }

        IPerson person = personManager.getPerson(req);
        IPersonAttributesGroupDefinition rslt;
        try {
            rslt = pagsService.updatePagsDefinition(person, inpt);
        } catch (IllegalArgumentException iae) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "{ 'error': '" + iae.getMessage() + "' }"; // should be escaped
        } catch (RuntimeAuthorizationException rae) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "{ 'error': 'not authorized' }";
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{ 'error': '" + e.toString() + "' }";
        }
        return respondPagsGroupJson(res, rslt, person, HttpServletResponse.SC_ACCEPTED);
    }

    @RequestMapping(value="/v4-3/pags/{pagsGroupName}.json", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
    public @ResponseBody String deletePagsGroup(HttpServletRequest request,
                                                HttpServletResponse response,
                                                @PathVariable("pagsGroupName") String pagsGroupName) {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        StringBuilder result = new StringBuilder("{ 'delete': '");
        result.append(pagsGroupName);
        result.append("', 'success': ");
        IPerson person = personManager.getPerson(request);
        try {
            IPersonAttributesGroupDefinition group = pagsService.getPagsDefinitionByName(person, pagsGroupName);
            pagsService.deletePagsDefinition(person, group);
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

    private String respondPagsGroupJson(HttpServletResponse response, IPersonAttributesGroupDefinition pagsGroup, IPerson person, int status) {
        if (pagsGroup == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "{ 'error': 'Not Found' }";
        }
        try {
            response.setStatus(status);
            return objectMapper.writeValueAsString(pagsGroup);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{ 'error': '" + e.toString() + "' }";
        }
    }

}
