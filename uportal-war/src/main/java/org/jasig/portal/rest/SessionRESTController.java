package org.jasig.portal.rest;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SessionRESTController {

    private IPersonManager personManager;
    
    @Autowired(required = true)
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @RequestMapping(value="/session", method = RequestMethod.GET)
    public ModelAndView isAuthenticated(HttpServletRequest request, HttpServletResponse response) {
        final ModelAndView mv = new ModelAndView();
        
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        
        else {
            final IPerson person = personManager.getPerson(request);
            final Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("userName", person.getUserName());
            attributes.put("displayName", person.getFullName());
            mv.addObject("person", attributes);
        }
        
        return mv;

    }
}
