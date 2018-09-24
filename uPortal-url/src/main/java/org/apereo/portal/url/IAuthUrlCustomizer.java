package org.apereo.portal.url;

import javax.servlet.http.HttpServletRequest;

/** Customize CAS or any other authentication URL. */
public interface IAuthUrlCustomizer {
    /**
     * Does this customizer supports the current HTTP request.
     *
     * @param request The current servlet request
     * @param url The url to customize
     * @return True if the customizer apply, else false.
     */
    boolean supports(HttpServletRequest request, String url);

    /**
     * Customize the supplied external login URL.
     *
     * @param request The current servlet request
     * @param url The url to customize
     * @return The url customized.
     */
    String customizeUrl(HttpServletRequest request, String url);
}
