package org.jasig.portal.spring.web.flow;

import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.springframework.webflow.context.ExternalContext;

/**
 * Useful general utilities for uPortal's webflows.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public interface IPortalWebFlowUtils {

    /**
     * Return an IPerson instance representing the current user.
     * 
     * @param externalContext portlet webflow external context
     * @return
     */
    public IPerson getCurrentPerson(ExternalContext externalContext);

    /**
     * Return an IAuthorizationPrincipal instance representing the current user.
     * 
     * @param externalContext portlet webflow external context
     * @return
     */
    public IAuthorizationPrincipal getCurrentPrincipal(ExternalContext externalContext);

}