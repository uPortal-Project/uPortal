/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.portlets.lookup.PersonLookupHelperImpl;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PeopleRESTController {

    private IPersonManager personManager;

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    private PersonLookupHelperImpl lookupHelper;

    @Autowired(required = true)
    public void setPersonLookupHelper(PersonLookupHelperImpl lookupHelper) {
        this.lookupHelper = lookupHelper;
    }

    @RequestMapping(value = "/people.json", method = RequestMethod.GET)
    public ModelAndView getPeople(
            @RequestParam("searchTerms[]") List<String> searchTerms,
            HttpServletRequest request,
            HttpServletResponse response) {

        final IPerson person = personManager.getPerson((HttpServletRequest) request);
        if (person == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        // build a search query from the request parameters
        Map<String, Object> query = new HashMap<>();
        for (String term : searchTerms) {
            String search = request.getParameter(term);
            if (StringUtils.isNotBlank(search)) {
                query.put(term, search);
            }
        }

        List<IPersonAttributes> people = lookupHelper.searchForPeople(person, query);

        ModelAndView mv = new ModelAndView();
        mv.addObject("people", people);
        mv.setViewName("json");

        return mv;
    }

    @RequestMapping(value = "/people/{username}.json", method = RequestMethod.GET)
    public ModelAndView getPerson(
            @PathVariable String username,
            HttpServletRequest request,
            HttpServletResponse response) {

        final IPerson searcher = personManager.getPerson((HttpServletRequest) request);
        if (searcher == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        final IPersonAttributes person = lookupHelper.findPerson(searcher, username);

        final ModelAndView mv = new ModelAndView();
        mv.addObject("person", person);
        mv.setViewName("json");

        return mv;
    }
}
