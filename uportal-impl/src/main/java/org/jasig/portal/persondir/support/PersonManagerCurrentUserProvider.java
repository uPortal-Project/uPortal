/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.persondir.support;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.services.persondir.support.ICurrentUserProvider;

/**
 * Provides the username of the current portal user
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PersonManagerCurrentUserProvider implements ICurrentUserProvider {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPersonManager personManager;
    private IPortalRequestUtils portalRequestUtils;
    

    public IPersonManager getPersonManager() {
        return personManager;
    }
    /**
     * @param personManager the personManager to set
     */
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    public IPortalRequestUtils getPortalRequestUtils() {
        return portalRequestUtils;
    }
    /**
     * @param portalRequestUtils the portalRequestUtils to set
     */
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }



    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.ICurrentUserProvider#getCurrentUserName()
     */
    public String getCurrentUserName() {
        final HttpServletRequest portalRequest;
        try {
            portalRequest = this.portalRequestUtils.getCurrentPortalRequest();
        }
        catch (IllegalStateException ise) {
            this.logger.warn("No current portal request available, cannot determine current user name.");
            return null;
        }
        
        final IPerson person = this.personManager.getPerson(portalRequest);
        if (person == null) {
            this.logger.warn("IPersonManager returned no IPerson for request, cannot determine current user name. " + portalRequest);
            return null;
        }
        
        return person.getUserName();
    }
}
