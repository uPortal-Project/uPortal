package org.jasig.portal.portlets.login;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.jasig.portal.security.mvc.LoginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;

@Controller
@RequestMapping("VIEW")
public class LoginPortletController {
    
    @RequestMapping
    public ModelAndView getLoginForm(PortletRequest request) {

        Map<String,Object> map = new HashMap<String,Object>();
        
        PortletSession session = request.getPortletSession();
        
        String authenticationAttempted = (String) session.getAttribute(LoginController.AUTH_ATTEMPTED_KEY, PortletSession.APPLICATION_SCOPE);
        map.put("attempted", Boolean.valueOf(authenticationAttempted));
        
        String attemptedUserName = (String)session.getAttribute(LoginController.ATTEMPTED_USERNAME_KEY, PortletSession.APPLICATION_SCOPE);
        map.put("attemtpedUsername", attemptedUserName);

        return new ModelAndView("/jsp/Login/login", map);
    }

}
