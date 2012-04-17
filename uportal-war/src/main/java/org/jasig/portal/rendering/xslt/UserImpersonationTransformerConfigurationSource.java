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
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.utils.cache.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Injects a parameter marking if the current user is impersonating another using the ID Swapper
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class UserImpersonationTransformerConfigurationSource implements TransformerConfigurationSource {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private String userImpersonatingParameter = "userImpersonating";
    private IPersonManager personManager;
    
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
    
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final boolean impersonating = this.personManager.isImpersonating(request);
        
        return CacheKey.build(UserImpersonationTransformerConfigurationSource.class.getName(), userImpersonatingParameter, impersonating);
    }

    @Override
    public Properties getOutputProperties(HttpServletRequest request, HttpServletResponse response) {
       return null;
    }

    @Override
    public Map<String, Object> getParameters(HttpServletRequest request, HttpServletResponse response) {
        final boolean impersonating = this.personManager.isImpersonating(request);
        
        final Map<String, Object> parameters = Collections.<String, Object>singletonMap(userImpersonatingParameter, impersonating);
        this.logger.debug("Returning transformer parameters: {}", parameters);
        
        return parameters;
    }
}
