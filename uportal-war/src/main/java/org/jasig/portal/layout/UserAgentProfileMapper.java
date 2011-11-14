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

package org.jasig.portal.layout;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.jasig.portal.security.IPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps User-Agents to profile names using the {@link UAgentInfo}class from MobileESP. Can cater for many different mobile devices
 * and tables. If no match is found the {@link #setDefaultProfileName(String)} value is used.
 * 
 * @author Eric Dalquist
 * @author Steve Swinsburg
 * @version $Revision$
 */
public class UserAgentProfileMapper implements IProfileMapper {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    private String defaultProfileName = null;
    private String userAgentHeader = "User-Agent";
    private String httpAcceptHeader = "Accept";
    
    private final String MOBILE_PROFILE_NAME = "mobileDefault";
    
    /**
     * Default profile name to return if no match is found, defaults to <code>null</code>.
     */
    public void setDefaultProfileName(String defaultProfileName) {
        this.defaultProfileName = defaultProfileName;
    }

    /**
     * {@link HttpServletRequest} header name to get the user agent string. Defaults to "User-Agent"
     */
    public void setUserAgentHeader(String userAgentHeader) {
        Validate.notNull(userAgentHeader);
        this.userAgentHeader = userAgentHeader;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.IProfileMapper#getProfileFname(org.jasig.portal.security.IPerson, javax.servlet.http.HttpServletRequest)
     */
    @Override
    public String getProfileFname(IPerson person, HttpServletRequest request) {
        final String userAgent = request.getHeader(this.userAgentHeader);
        if (userAgent == null) {
            this.logger.debug("No {} header for {} returning default profile", this.userAgentHeader, person.getUserName());
            return this.defaultProfileName;
        }
        
        String acceptHeader = request.getHeader(this.httpAcceptHeader);
        if (acceptHeader == null) {
            this.logger.debug("No {} header for {}, continuing but may not be able to determine profile correctly", this.httpAcceptHeader, person.getUserName());
            acceptHeader = "";
        }
        
        UAgentInfo agentInfo = new UAgentInfo(userAgent, acceptHeader);
        
        if(agentInfo.detectMobileQuick()) {
        	this.logger.debug("Matched {} header {} for {} returning profile {}", new Object[] {this.userAgentHeader, person.getUserName(), MOBILE_PROFILE_NAME});
        	return MOBILE_PROFILE_NAME;
        }
        
        //to send tablet devices to the mobile profile, uncomment this block
        /*
        if(agentInfo.detectTierTablet()) {
        	this.logger.debug("Matched {} header {} for {} returning profile {}", new Object[] {this.userAgentHeader, person.getUserName(), MOBILE_PROFILE_NAME});
        	return MOBILE_PROFILE_NAME;
        }
        */
        
        this.logger.debug("No matching Mapping for {} header for {} returning default profile", this.userAgentHeader, person.getUserName());
        return defaultProfileName;
    }
}
