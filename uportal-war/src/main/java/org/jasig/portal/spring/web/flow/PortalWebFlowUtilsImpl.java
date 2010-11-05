package org.jasig.portal.spring.web.flow;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.webflow.context.ExternalContext;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Component("portalWebFlowUtils")
public class PortalWebFlowUtilsImpl implements IPortalWebFlowUtils {
    
    private IPortalRequestUtils portalRequestUtils;

    @Autowired(required = true)
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    private IPersonManager personManager;

    @Autowired(required = true)
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.spring.web.flow.IPortalWebFlowUtils#getCurrentPerson(org.springframework.webflow.context.ExternalContext)
     */
    public IPerson getCurrentPerson(final ExternalContext externalContext) {
        final HttpServletRequest servletRequest = getServletRequestFromExternalContext(externalContext);
        return personManager.getPerson(servletRequest);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.spring.web.flow.IPortalWebFlowUtils#getCurrentPrincipal(org.springframework.webflow.context.ExternalContext)
     */
    public IAuthorizationPrincipal getCurrentPrincipal(final ExternalContext externalContext) {
        final IPerson person = getCurrentPerson(externalContext);
        final EntityIdentifier ei = person.getEntityIdentifier();
        return AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
    }
    
    /**
     * Get the HttpServletRequest associated with the supplied ExternalContext.
     * 
     * @param externalContext
     * @return
     */
    protected HttpServletRequest getServletRequestFromExternalContext(ExternalContext externalContext) {
        Object request = externalContext.getNativeRequest();
        
        if (request instanceof PortletRequest) {
            return portalRequestUtils.getOriginalPortalRequest((PortletRequest) externalContext.getNativeRequest());
        } 
        
        else if (request instanceof HttpServletRequest) {
            return (HttpServletRequest) request;
        } 
        
        // give up and throw an error
        else {
            throw new IllegalArgumentException("Unable to recognize the native request associated with the supplied external context");
        }

    }

}
