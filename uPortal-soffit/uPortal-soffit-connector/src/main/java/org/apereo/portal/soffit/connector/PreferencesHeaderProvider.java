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
package org.apereo.portal.soffit.connector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apereo.portal.soffit.Headers;
import org.apereo.portal.soffit.model.v1_0.Preferences;
import org.apereo.portal.soffit.service.PreferencesService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Prepares the custom 'X-Soffit-Preferences' HTTP header.
 *
 * @since 5.0
 */
public class PreferencesHeaderProvider extends AbstractHeaderProvider {

    public static final String INCLUDE_PREFERENCE =
            SoffitConnectorController.class.getName() + ".includePreferences";

    @Autowired private PreferencesService preferencesService;

    @Override
    public Header createHeader(RenderRequest renderRequest, RenderResponse renderResponse) {

        // Include this header?
        if (!isIncluded(renderRequest, INCLUDE_PREFERENCE)) {
            return null;
        }

        // Username
        final String username = getUsername(renderRequest);

        // PreferencesMap
        final Map<String, List<String>> preferencesMap = new HashMap<>();
        final PortletPreferences prefs = renderRequest.getPreferences();
        for (Map.Entry<String, String[]> y : prefs.getMap().entrySet()) {
            final String name = y.getKey();

            /*
             * We ignore (skip) preferences that exist for the benefit of the
             * SoffitConnectorController.
             */
            if (name.startsWith(SoffitConnectorController.CONNECTOR_PREFERENCE_PREFIX)) {
                continue;
            }

            List<String> values = Arrays.asList(prefs.getValues(name, new String[0]));
            if (!values.isEmpty()) {
                preferencesMap.put(name, values);
            }
        }

        // Preferences header
        final Preferences preferences =
                preferencesService.createPreferences(
                        preferencesMap, username, getExpiration(renderRequest));
        final Header rslt =
                new BasicHeader(Headers.PREFERECES.getName(), preferences.getEncryptedToken());
        logger.debug(
                "Produced the following Preferences header for username='{}':  {}", username, rslt);

        return rslt;
    }
}
