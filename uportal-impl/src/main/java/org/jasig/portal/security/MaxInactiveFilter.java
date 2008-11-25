package org.jasig.portal.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.provider.AuthorizationImpl;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

/**
 * This <code>Filter</code> sets the HttpSession MaxInactiveInterval based on 
 * permissions, if applicable.
 * 
 * @author awills
 */
public class MaxInactiveFilter implements Filter {
    // Instance Members.
    protected final Log log = LogFactory.getLog(getClass());
    
    private IPersonManager personManager;
    
    /*
     * Public API.
     */

    /**
     * @return the personManager
     */
    public IPersonManager getPersonManager() {
        return personManager;
    }
    /**
     * @param personManager the personManager to set
     */
    @Required
    public void setPersonManager(IPersonManager personManager) {
        Assert.notNull(personManager);
        this.personManager = personManager;
    }

    public void init(FilterConfig filterConfig) { /* Nothing to do...*/ }
    
    public void destroy() { /* Nothing to do...*/ }
    
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException {
        
        // First perform the login...
        chain.doFilter(req, res);
        
        // Now see if authentication was successful...
        final IPerson person = this.personManager.getPerson((HttpServletRequest) req);
        if (person == null) {
            return;
        }
        
        final ISecurityContext securityContext = person.getSecurityContext();
        if (securityContext != null && securityContext.isAuthenticated()) {
            // We have an authenticated user... let's see if any MAX_INACTIVE settings apply...
            IAuthorizationService authServ = AuthorizationImpl.singleton();
            IAuthorizationPrincipal principal = authServ.newPrincipal((String) person.getAttribute(IPerson.USERNAME), IPerson.class);
            Integer rulingGrant = null;
            Integer rulingDeny = null;
            IPermission[] permissions = authServ.getAllPermissionsForPrincipal(principal, null, "MAX_INACTIVE", null);
            for (IPermission p : permissions) {
                // First be sure the record applies currently...
                long now = System.currentTimeMillis();
                if (p.getEffective() != null && p.getEffective().getTime() > now) {
                    // It's *TOO EARLY* for this record... move on.
                    continue;
                }
                if (p.getExpires() != null && p.getExpires().getTime() < now) {
                    // It's *TOO LATE* for this record... move on.
                    continue;
                }
                if (p.getType().equals(IPermission.PERMISSION_TYPE_GRANT)) {
                    try {
                        Integer grantEntry = Integer.valueOf(p.getTarget());
                        if (rulingGrant == null 
                                        || grantEntry.intValue() < 0 /* Any negative number trumps all */
                                        || rulingGrant.intValue() < grantEntry.intValue()) {
                            rulingGrant = grantEntry;
                        }
                    } catch (NumberFormatException nfe) {
                        log.warn("Invalid MAX_INACTIVE permission grant '" 
                                        + p.getTarget() 
                                        + "';  target must be an integer value.");
                    }
                } else if (p.getType().equals(IPermission.PERMISSION_TYPE_DENY)) {
                    try {
                        Integer denyEntry = Integer.valueOf(p.getTarget());
                        if (rulingDeny == null || rulingDeny.intValue() > denyEntry.intValue()) {
                            rulingDeny = denyEntry;
                        }
                    } catch (NumberFormatException nfe) {
                        log.warn("Invalid MAX_INACTIVE permission deny '" 
                                        + p.getTarget() 
                                        + "';  target must be an integer value.");
                    }
                } else {
                    log.warn("Unknown permission type:  " + p.getType());
                }
            }

            if (rulingDeny != null && rulingDeny.intValue() < 0) {
                // Negative MaxInactiveInterval values mean the session never 
                // times out, so a negative DENY is somewhat nonsensical... just 
                // clear it.
                log.warn("A MAX_INACTIVE DENY entry improperly specified a negative target:  " 
                        + rulingDeny.intValue());
                rulingDeny = null;
            }
            if (rulingGrant != null || rulingDeny != null) {
                // We only want to intervene if there's some actual value 
                // specified... otherwise we'll just let the container settings 
                //govern.
                int maxInactive = rulingGrant != null 
                                    ? rulingGrant.intValue() 
                                    : 0;    // If rulingGrant is null, rulingDeny won't be...
                if (rulingDeny != null) {
                    // Applying DENY entries is tricky b/c GRANT entries may be negative...
                    int limit = rulingDeny.intValue();
                    if (maxInactive >= 0) {
                        maxInactive = limit < maxInactive ? limit : maxInactive;
                    } else {
                        // The best grant was negative (unlimited), so go with limit...
                        maxInactive = limit;
                    }
                }
                // Apply the specified setting...
                HttpSession session = ((HttpServletRequest) req).getSession();
                session.setMaxInactiveInterval(maxInactive);
                if (log.isInfoEnabled()) {
                    log.info("Setting maxInactive to '" + maxInactive 
                                + "' for user '" 
                                + person.getAttribute(IPerson.USERNAME) + "'");
                }
            }
            
        }

    }
    
}
