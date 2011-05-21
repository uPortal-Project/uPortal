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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.utils.cache.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceTypeTransformerConfigurationSource extends TransformerConfigurationSourceAdapter {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getParameters(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Map<String, Object> getParameters(HttpServletRequest request, HttpServletResponse response) {
        
        final boolean isNative = Boolean.valueOf((String) request.getSession(false).getAttribute("isNativeDevice"));
        return Collections.singletonMap("NATIVE", (Object) isNative);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getCacheKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        
        return new CacheKey("LocaleTransformerConfigurationSource", request.getHeader("user-agent"));
    }

}
