package org.apereo.portal.security.mvc;

import javax.servlet.http.HttpServletRequest;

public interface ILoginRedirect {

    /*
     * Return redirect URL or null to bypass redirect.
     */
    String redirectTarget(HttpServletRequest request);
}
