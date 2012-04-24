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
package org.jasig.portal.rendering.xslt;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Maps a user attribute to a skin. The user's attribute named by {@link #setSkinAttributeName(String)} is used to
 * look up a skin name via the {@link #setAttributeToSkinMap(Map)} map and the skin name is set to in the transformer
 * using the {@link #setSkinParameterName(String)} parameter.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class UserAttributeSkinMappingTransformerConfigurationSource extends SkinMappingTransformerConfigurationSource {
    private IUserInstanceManager userInstanceManager;
    private IPersonAttributeDao personAttributeDao;

    private String skinAttributeName = "skinOverride";
    private Map<Pattern, String> attributeToSkinMap = Collections.emptyMap();
    
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setPersonAttributeDao(IPersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
    }

    /**
     * The name of the user attribute to use to look up the skin name, defaults to "skinOverride"
     */
    public void setSkinAttributeName(String skinAttributeName) {
        this.skinAttributeName = skinAttributeName;
    }

    /**
     * Map of user attribute values to skin names. Defaults to an empty map.
     */
    public void setAttributeToSkinMap(Map<Pattern, String> attributeToSkinMap) {
        this.attributeToSkinMap = attributeToSkinMap;
    }

    protected String getSkinName(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IPerson person = userInstance.getPerson();
        final IPersonAttributes personAttrs = this.personAttributeDao.getPerson(person.getUserName());
        if (personAttrs == null) {
            logger.debug("No user attributes found for {} no skin override will be done", person.getUserName());
            return null;
        }
        
        final Object attributeValue = personAttrs.getAttributeValue(this.skinAttributeName);
        if (attributeValue == null) {
            logger.debug("No user {} does not have attribute {} defined, no skin override will be done", person.getUserName(), this.skinAttributeName);
            return null;
        }
        
        final String mappedSkinName = this.getMappedSkinName(attributeValue.toString());
        if (mappedSkinName == null) {
            logger.debug("No skin is mapped for attribute {}, no skin override will be done", attributeValue);
            return null;
        }
        
        logger.debug("Overidding skin to {}", mappedSkinName);
        
        return mappedSkinName;
    }
    
    private String getMappedSkinName(String attributeValue) {
        for (final Map.Entry<Pattern, String> attributeMapEntry : this.attributeToSkinMap.entrySet()) {
            final Pattern attributePattern = attributeMapEntry.getKey();
            final Matcher attributeMatcher = attributePattern.matcher(attributeValue);
            if (attributeMatcher.matches()) {
                return attributeMapEntry.getValue();
            }
        }
        
        return null;
    }
}
