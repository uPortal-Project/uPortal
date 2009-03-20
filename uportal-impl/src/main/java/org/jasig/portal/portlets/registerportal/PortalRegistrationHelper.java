/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.registerportal;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalRegistrationHelper {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPortalDataCollator portalDataCollator;
    private IPortalDataSubmitter portalDataSubmitter;
    
    /**
     * @param portalDataCollator the portalDataCollator to set
     */
    public void setPortalDataCollator(IPortalDataCollator portalDataCollator) {
        this.portalDataCollator = portalDataCollator;
    }
    
    /**
     * @param portalDataSubmitter the portalDataSubmitter to set
     */
    public void setPortalDataSubmitter(IPortalDataSubmitter portalDataSubmitter) {
        this.portalDataSubmitter = portalDataSubmitter;
    }

    /**
     * @return A new backing object for the registration request form
     */
    public PortalRegistrationRequest createRegistrationRequest() {
        final Map<String, String> dataToSubmit = new LinkedHashMap<String, String>();
        
        final Set<String> possibleDataKeys = this.portalDataCollator.getPossibleDataKeys();
        for (final String possibleDataKey : possibleDataKeys) {
            dataToSubmit.put(possibleDataKey, Boolean.TRUE.toString());
        }
        
        final PortalRegistrationRequest registrationRequest = new PortalRegistrationRequest();
        registrationRequest.setDataToSubmit(dataToSubmit);
        
        return registrationRequest;
    }
    
    /**
     * Gathers portal data for the registration request
     * 
     * @param registrationRequest Registration request containing the user's preferences
     * @return Registration data collected based on the registration request 
     */
    public PortalRegistrationData getRegistrationData(PortalRegistrationRequest registrationRequest) {
        this.logger.debug(registrationRequest);
        
        final PortalRegistrationData registrationData = new PortalRegistrationData(registrationRequest);
        
        final Set<String> dataToCollect = new LinkedHashSet<String>();
        final Map<String, String> dataToSubmit = registrationRequest.getDataToSubmit();
        for (final Map.Entry<String, String> dataToSubmitEntry : dataToSubmit.entrySet()) {
            if (Boolean.parseBoolean(dataToSubmitEntry.getValue())) {
                dataToCollect.add(dataToSubmitEntry.getKey());
            }
        }
        
        final Map<String, Map<String, String>> collectedData = this.portalDataCollator.getCollectedData(dataToCollect);
        registrationData.setCollectedData(collectedData);
        
        return registrationData;
    }
    
    /**
     * @param portalRegistrationData Registration data to submit to backend service
     */
    public boolean submitRegistration(PortalRegistrationData portalRegistrationData) {
        this.logger.debug(portalRegistrationData);
        return this.portalDataSubmitter.submitPortalData(portalRegistrationData);
    }
}
