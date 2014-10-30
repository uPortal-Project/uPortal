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
package org.jasig.portal.layout.profile;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasig.portal.security.IPerson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;

/**
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class SessionAttributeProfileMapperImpl
    implements IProfileMapper, ApplicationListener<ProfileSelectionEvent> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Since uPortal 4.2, instead of externally relying upon this key and hoping that a runtime
     * SessionAttributeProfileMapperImpl is not configured to use a different key,
     * consider instead firing a ProfileSelectionEvent and let this class store its data itself into the session.
     */
    public static final String DEFAULT_SESSION_ATTRIBUTE_NAME = "profileKey";

    private Map<String,String> mappings = Collections.<String,String>emptyMap();
    private String defaultProfileName = null;
    private String attributeName = DEFAULT_SESSION_ATTRIBUTE_NAME;
    
    /**
     * Session profile key to database profile fname mappings.
     */
    public void setMappings(Map<String,String> mappings) {
        this.mappings = mappings;
    }
    
    /**
     * Default profile name to return if no match is found, defaults to <code>null</code>.
     */
    public void setDefaultProfileName(String defaultProfileName) {
        this.defaultProfileName = defaultProfileName;
    }

    /**
     * 
     * @param attributeName
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
    
    @Override
    public String getProfileFname(IPerson person, HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        final String requestedProfileKey = (String) session.getAttribute(attributeName);
        if (requestedProfileKey != null) {
            final String profileName = mappings.get(requestedProfileKey);
            if (profileName != null) {
                return profileName;
            }
        }

        return defaultProfileName;
    }

    /*
     * Store the requested profile key into the user Session so that this SessionAttributeProfileMapperImpl
     * can subsequently find it and use it to determine a profile mapping.
     */
    @Override
    public void onApplicationEvent(final ProfileSelectionEvent event) {
        final HttpSession session = event.getRequest().getSession(false);

        Assert.notNull(session, "Cannot store a profile selection into a null session.");

        session.setAttribute(this.attributeName, event.getRequestedProfileKey());

        logger.trace("Stored desired profile key [{}] into session (at attribute [{}]).",
                event.getRequestedProfileKey(), attributeName);

        final String fnameTheKeyMapsTo = this.mappings.get( event.getRequestedProfileKey() );

        if (null == fnameTheKeyMapsTo) {
            logger.warn("The desired profile key {} has no mapping so will have no effect.",
                    event.getRequestedProfileKey());
        }

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " which considers session attribute [" + this.attributeName +
                "] as key to mappings [" + this.mappings +
                "], falling back on default [" + this.defaultProfileName + "] .";
    }

}
