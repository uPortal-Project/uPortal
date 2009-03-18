/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.registerportal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalRegistrationHelper {
    private IPortalDataCollator portalDataCollator;
    
    /**
     * @param portalDataCollator the portalDataCollator to set
     */
    public void setPortalDataCollator(IPortalDataCollator portalDataCollator) {
        this.portalDataCollator = portalDataCollator;
    }

    public PortalRegistrationRequest createRegistrationRequest() {
        final Map<String, String> dataToSubmit = new LinkedHashMap<String, String>();
        
        final Set<String> possibleDataKeys = this.portalDataCollator.getPossibleDataKeys();
        for (final String possibleDataKey : possibleDataKeys) {
            dataToSubmit.put(possibleDataKey, null);
        }
        
        final PortalRegistrationRequest registrationRequest = new PortalRegistrationRequest();
        registrationRequest.setDataToSubmit(dataToSubmit);
        
        return registrationRequest;
    }
}
