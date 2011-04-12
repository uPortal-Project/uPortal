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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.jasig.portal.portlet.PortletUtils;

/**
 * Utility enum used in {@link UrlSyntaxProviderImpl} to simplify URL parsing a bit. Lists all of the portlet
 * parameters that can be suffixed with a portlet window id and 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
enum SuffixedPortletParameter {
    RESOURCE_ID(UrlSyntaxProviderImpl.PARAM_RESOURCE_ID, UrlType.RESOURCE),
    CACHEABILITY(UrlSyntaxProviderImpl.PARAM_CACHEABILITY, UrlType.RESOURCE),
    WINDOW_STATE(UrlSyntaxProviderImpl.PARAM_WINDOW_STATE, UrlType.RENDER, UrlType.ACTION),
    PORTLET_MODE(UrlSyntaxProviderImpl.PARAM_PORTLET_MODE, UrlType.RENDER, UrlType.ACTION);
    
    private final String parameterPrefix;
    private final Set<UrlType> validUrlTypes;
    
    private SuffixedPortletParameter(String parameterPrefix, UrlType validUrlType, UrlType... validUrlTypes) {
        this.parameterPrefix = parameterPrefix;
        this.validUrlTypes = Collections.unmodifiableSet(EnumSet.of(validUrlType, validUrlTypes));
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
    
    /**
     * Stores the parameter value on the {@link PortletRequestInfoImpl} based on the parameter type.
     */
    public void storeParameter(PortletRequestInfoImpl portletRequestInfo, List<String> values) {
        switch (this) {
            case RESOURCE_ID: {
                portletRequestInfo.setResourceId(values.get(0));
                break;
            }
            case CACHEABILITY: {
                portletRequestInfo.setCacheability(values.get(0));
                break;
            }
            case WINDOW_STATE: {
                portletRequestInfo.setWindowState(PortletUtils.getWindowState(values.get(0)));
                break;
            }
            case PORTLET_MODE: {
                portletRequestInfo.setPortletMode(PortletUtils.getPortletMode(values.get(0)));
                break;
            }
            default: {
                throw new IllegalStateException("Unknown SuffixedPortletParameter: " + this);
            }
        }
    }
}