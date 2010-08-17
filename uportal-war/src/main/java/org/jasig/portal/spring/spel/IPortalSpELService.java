package org.jasig.portal.spring.spel;

import org.springframework.web.context.request.WebRequest;

/**
 * IPortalSpELService provides an interface for parsing strings using the
 * Spring expression language.  Strings are assumed to be a potential mixture
 * of SpEL expressions contained inside a ${ } and text string content.  For 
 * example, a portal-relative URL path might be formatted as 
 * ${request.contextPath}/my/path.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public interface IPortalSpELService {

    /**
     * Parse the supplied string by replacing any ${ } blocks with the 
     * SpEL-evaluated value.
     * 
     * @param string  string to be evaluated
     * @param request request (may be either a portlet or servlet request)
     * @return        evaluated string
     */
    public String parseString(String string, WebRequest request);

}
