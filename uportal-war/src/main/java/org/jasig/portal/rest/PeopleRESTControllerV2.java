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
import org.jasig.portal.portlets.lookup.PersonLookupHelperImpl;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/people/v2")
public class PeopleRESTControllerV2 {

    private IPersonManager personManager;
    private ObjectMapper jsonMapper;

    @Autowired
    public void setJsonMapper(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    private PersonLookupHelperImpl lookupHelper;

    @Autowired(required = true)
    public void setPersonLookupHelper(PersonLookupHelperImpl lookupHelper) {
        this.lookupHelper = lookupHelper;
    }

    @RequestMapping(value="/", method = RequestMethod.GET)
    public void searchPeople(@RequestParam Map<String,Object> query,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        final IPerson user = personManager.getPerson((HttpServletRequest) request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        List<IPersonAttributes> people = lookupHelper.searchForPeople(user, query);

        List<Object> results = new ArrayList<Object>();
        for(IPersonAttributes p : people) {
            results.add(p.getAttributes());
        }

        response.setHeader("Content-Type", "application/json");
        jsonMapper.writeValue(response.getOutputStream(), results);
    }

    @RequestMapping(value="/{username}", method = RequestMethod.GET)
    public ModelAndView getPerson(@PathVariable String username,
            HttpServletRequest request, HttpServletResponse response) {

        final IPerson searcher = personManager.getPerson(request);
        if (searcher == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        final IPersonAttributes person = lookupHelper.findPerson(searcher, username);
        if (person == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        return new ModelAndView("json", person.getAttributes());
    }

    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public ModelAndView getMe(HttpServletRequest request, HttpServletResponse response) {
        final IPerson me = personManager.getPerson(request);

        if ( me == null ) {
            //If null, this person is not logged in.
            response.setStatus(401);
            return null;
        }

        return new ModelAndView("json", me.getAttributeMap());
    }

}
