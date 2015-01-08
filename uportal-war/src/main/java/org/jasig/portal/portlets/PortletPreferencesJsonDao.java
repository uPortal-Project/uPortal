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
package org.jasig.portal.portlets;

import java.io.IOException;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import net.sf.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * DAO That stores/retrieves objects as JSON in portlet preferences
 * 
 * @author Eric Dalquist
 */
public interface PortletPreferencesJsonDao {

    /**
     * Stores a Java object as JSON in a portlet preference
     * 
     * @param prefs Preferences to persist to
     * @param key Preference key to store in
     * @param data Object to store as JSON
     */
    void storeJson(PortletPreferences prefs, String key, Object data)
            throws IOException, ReadOnlyException, ValidatorException;

    /**
     * Read the specified portlet preference and parse it as a JSON string into the specified type.
     * If the preference is null returns null. 
     * 
     * @param prefs Preferences to read from
     * @param key Preference key to read from
     * @param type The class type parse the JSON into
     */
    <E> E getJson(PortletPreferences prefs, String key, Class<E> type)
            throws IOException;

    /**
     * Read the specified portlet preference and parse it as a JSON string into a {@link JsonNode}
     * If the preference is null returns a {@link JSONObject}.
     * 
     * @param prefs Preferences to read from
     * @param key Preference key to read from
     */
    JsonNode getJsonNode(PortletPreferences prefs, String key)
            throws IOException;

}