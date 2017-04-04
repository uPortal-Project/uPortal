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
package org.apereo.portal.portlets.dynamicskin.storage.s3;

import javax.portlet.PortletPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.portlets.dynamicskin.DynamicSkinException;
import org.apereo.portal.portlets.dynamicskin.DynamicSkinInstanceData;
import org.apereo.portal.portlets.dynamicskin.storage.DynamicSkinCssFileNamer;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * {@link DynamicSkinCssFileNamer} class that uses a the value of a specific {@link PortletPreferences} preference as
 * the Dynamic Skin CSS file name.  Since the name does not vary based on the values of the portlet preferences used 
 * for LESS variables, this class must be used in conjunction with a strategy for storing the 'unique' token somewhere
 * other than the filename (such as file metadata).
 */
@Service
public class PrefValueDynamicSkinCssFileNamer implements DynamicSkinCssFileNamer {

    public static final String SKIN_CSS_FILE_NAME_PREFERENCE_NAME = "dynamicSkinCssFileName";

    private String preferenceName = SKIN_CSS_FILE_NAME_PREFERENCE_NAME;

    public PrefValueDynamicSkinCssFileNamer() {
    }

    public PrefValueDynamicSkinCssFileNamer(final String prefName) {
        Assert.hasText(prefName);
        this.preferenceName = prefName;
    }

    /**
     * Returns preference value as the CSS filename.  If preference value is not found or if the value is empty, then
     * a {@link DynamicSkinException} will be thrown.
     * @see DynamicSkinCssFileNamer#generateCssFileName(DynamicSkinInstanceData)
     */
    @Override
    public String generateCssFileName(final DynamicSkinInstanceData data) {
        final PortletPreferences preferences = data.getPortletRequest().getPreferences();
        return getCssFileName(preferences);
    }

    private String getCssFileName(final PortletPreferences prefs) {
        final String result = prefs.getValue(this.preferenceName, null);
        if (result == null) {
            throw new DynamicSkinException("Dynamic Skin CSS filename preference not found: " + this.preferenceName);
        }
        if (StringUtils.isEmpty(result)) {
            throw new DynamicSkinException(
                "Dynamic Skin CSS filename preference value is empty. Pref: [" + this.preferenceName + "] Value: [" + result + "]");
        }
        return result;
    }

}
