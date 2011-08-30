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

package org.jasig.portal.url;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Utility enum used in {@link UrlSyntaxProviderImpl} to simplify URL parsing a bit. Lists all of the portlet
 * parameters that can be suffixed with a portlet window id. Also specifies which {@link UrlType}s each parameter
 * is valid for.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
enum SuffixedPortletParameter {
    RESOURCE_ID(UrlSyntaxProviderImpl.PARAM_RESOURCE_ID, UrlType.RESOURCE),
    CACHEABILITY(UrlSyntaxProviderImpl.PARAM_CACHEABILITY, UrlType.RESOURCE),
    DELEGATE_PARENT(UrlSyntaxProviderImpl.PARAM_DELEGATE_PARENT, UrlType.RENDER, UrlType.ACTION, UrlType.RESOURCE),
    WINDOW_STATE(UrlSyntaxProviderImpl.PARAM_WINDOW_STATE, UrlType.RENDER, UrlType.ACTION),
    PORTLET_MODE(UrlSyntaxProviderImpl.PARAM_PORTLET_MODE, UrlType.RENDER, UrlType.ACTION),
    COPY_PARAMETERS(UrlSyntaxProviderImpl.PARAM_COPY_PARAMETERS, UrlType.RENDER);
    
    private final String parameterPrefix;
    private final Set<UrlType> validUrlTypes;
    
    private SuffixedPortletParameter(String parameterPrefix, UrlType validUrlType, UrlType... validUrlTypes) {
        this.parameterPrefix = parameterPrefix;
        this.validUrlTypes = Sets.immutableEnumSet(validUrlType, validUrlTypes);
    }
    
    /**
     * @return The {@link UrlType}s this parameter is valid on
     */
    public Set<UrlType> getValidUrlTypes() {
        return this.validUrlTypes;
    }

    /**
     * @return The parameter prefix
     */
    public String getParameterPrefix() {
        return this.parameterPrefix;
    }
}