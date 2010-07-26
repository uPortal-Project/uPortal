/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
