package org.apereo.portal.services;

import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.security.IPerson;

public interface IAuthenticationExt {

    /**
     * Run custom code after user has successfully authenticated.
     *
     * @param request web request for authentication
     * @param person person object after core authentication process is done
     */
    void postAttributeResolution(HttpServletRequest request, IPerson person);
}
