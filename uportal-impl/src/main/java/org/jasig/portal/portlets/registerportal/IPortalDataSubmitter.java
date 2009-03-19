/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.registerportal;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalDataSubmitter {
    /**
     * Submits the registration data
     * 
     * @param portalRegistrationData Data to submit for registration
     * @return True if registration succeded
     */
    public boolean submitPortalData(PortalRegistrationData portalRegistrationData);
}
