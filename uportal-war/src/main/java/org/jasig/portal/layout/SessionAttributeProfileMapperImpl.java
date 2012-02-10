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

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasig.portal.security.IPerson;

/**
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class SessionAttributeProfileMapperImpl implements IProfileMapper {

    private Map<String,String> mappings = Collections.<String,String>emptyMap();
    private String defaultProfileName = null;
    private String attributeName = "profileKey";
    
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

}
