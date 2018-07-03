/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.portlets.dynamicskin;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import javax.portlet.PortletPreferences;

/**
 * {@link DynamicSkinUniqueTokenGenerator} implementation that generates a token by computing a
 * hashcode using the values for the Dynamic Skin configurable portlet preferences. To ensure that
 * when using the same {@link PortletPreferences} that multiple calls to generate the token will
 * return identical result, the portlet preferences are ordered by name prior to hashcode
 * calculation.
 */
public class ConfigurablePreferencesBasedTokenGenerator implements DynamicSkinUniqueTokenGenerator {

    /**
     * Returns a String hashcode of the values for the portlet preferences that are configurable by
     * the Dynamic Skin portlet. The hashcode is generated in a repeatable fashion by calculating it
     * based on sorted portlet preference names. Though hashcode does not guarantee uniqueness, from
     * a practical perspective we'll have so few different values we can reasonably assume
     * preference value combinations will be unique.
     *
     * @see DynamicSkinUniqueTokenGenerator#generateToken(DynamicSkinInstanceData)
     */
    @Override
    public String generateToken(final DynamicSkinInstanceData data) {
        final PortletPreferences preferences = data.getPortletRequest().getPreferences();
        int hash = 0;
        // Add the list of preference names to an ordered list so we can get reliable hashcode
        // calculations.
        final Map<String, String[]> prefs = preferences.getMap();
        final TreeSet<String> orderedNames = new TreeSet<String>(prefs.keySet());
        final Iterator<String> iterator = orderedNames.iterator();
        while (iterator.hasNext()) {
            final String preferenceName = iterator.next();
            if (preferenceName.startsWith(DynamicRespondrSkinConstants.CONFIGURABLE_PREFIX)) {
                hash = hash * 31 + preferences.getValue(preferenceName, "").trim().hashCode();
            }
        }
        return Integer.toString(hash);
    }
}
