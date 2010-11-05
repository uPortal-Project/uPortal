package org.jasig.portal.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.portlets.lookup.PersonLookupHelperImpl;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

    @RequestMapping(value="/people.json", method = RequestMethod.GET)
    public ModelAndView getPeople(@RequestParam("searchTerms[]") List<String> searchTerms,
            HttpServletRequest request, HttpServletResponse response) {

        final IPerson person = personManager.getPerson((HttpServletRequest) request);
        if (person == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        // build a search query from the request parameters
        Map<String,Object> query = new HashMap<String,Object>();
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

}
