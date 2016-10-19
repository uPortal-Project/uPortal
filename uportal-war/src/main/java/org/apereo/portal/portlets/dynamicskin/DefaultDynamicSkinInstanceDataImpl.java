/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.portal.portlets.dynamicskin;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Default {@link DynamicSkinInstanceData} implementation that pulls required data from the provided
 * {@link PortletRequest}.
 */
public class DefaultDynamicSkinInstanceDataImpl implements DynamicSkinInstanceData {

    private String portletAbsolutePathRoot;
    private String skinName;
    private PortletRequest portletRequest;
    private Map<String, String> variableNameToValueMap;

    public DefaultDynamicSkinInstanceDataImpl(final PortletRequest request) {
        this.pullDataFromPortletPreferences(request.getPreferences());
        this.pullDataFromPortletContext(request.getPortletSession().getPortletContext());
        this.portletRequest = request;
    }

    /**
     * @see DynamicSkinInstanceData#getPortletAbsolutePathRoot()
     */
    @Override
    public String getPortletAbsolutePathRoot() {
       return this.portletAbsolutePathRoot;
    }

    /**
     * @see DynamicSkinInstanceData#getSkinName()
     */
    @Override
    public String getSkinName() {
        return this.skinName;
    }

    @Override
    public PortletRequest getPortletRequest() {
        return this.portletRequest;
    }

    /**
     * @see DynamicSkinInstanceData#getVariablesValuesMap()
     */
    @Override
    public Map<String, String> getVariableNameToValueMap() {
        return this.variableNameToValueMap;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    private void pullDataFromPortletPreferences(final PortletPreferences prefs) {
        this.skinName = prefs.getValue(
                DynamicRespondrSkinConstants.PREF_SKIN_NAME, DynamicRespondrSkinConstants.DEFAULT_SKIN_NAME);

        this.variableNameToValueMap = new HashMap<String, String>();
        final Enumeration<String> prefNames =  prefs.getNames();
        while (prefNames.hasMoreElements()) {
            final String prefName = prefNames.nextElement();
            if (prefName.startsWith(DynamicRespondrSkinConstants.CONFIGURABLE_PREFIX)) {
                final String nameWithoutPrefix = prefName.substring(DynamicRespondrSkinConstants.CONFIGURABLE_PREFIX.length());
                final String value = prefs.getValue(prefName, "");
                this.variableNameToValueMap.put(nameWithoutPrefix, value);
            }
        }
    }

    private void pullDataFromPortletContext(final PortletContext ctx) {
        this.portletAbsolutePathRoot = ctx.getRealPath("/");
    }

}
