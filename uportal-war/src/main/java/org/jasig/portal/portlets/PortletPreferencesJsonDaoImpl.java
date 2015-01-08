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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Repository
public class PortletPreferencesJsonDaoImpl implements PortletPreferencesJsonDao {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private ObjectMapper mapper;

    @Autowired
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Stores a Java object as JSON in a portlet preference
     * 
     * @param prefs Preferences to persist to
     * @param key Preference key to store in
     * @param data Object to store as JSON
     */
    @Override
    public void storeJson(PortletPreferences prefs, String key, Object data) throws IOException, ReadOnlyException, ValidatorException {
        final String configData = mapper.writeValueAsString(data);
        prefs.setValue(key, configData);
        prefs.store();
    }
    
    /**
     * Read the specified portlet preference and parse it as a JSON string into the specified type.
     * If the preference is null returns null. 
     * 
     * @param prefs Preferences to read from
     * @param key Preference key to read from
     * @param type The class type parse the JSON into
     */
    @Override
    public <E> E getJson(PortletPreferences prefs, String key, Class<E> type) throws IOException {
        final String prefValue = StringUtils.trimToNull(prefs.getValue(key, null));
        if (prefValue == null) {
            return null;
        }
        return mapper.readValue(prefValue, type);
    }
    
    /**
     * Read the specified portlet preference and parse it as a JSON string into a {@link JsonNode}
     * If the preference is null returns a {@link JSONObject}.
     * 
     * @param prefs Preferences to read from
     * @param key Preference key to read from
     */
    @Override
    public JsonNode getJsonNode(PortletPreferences prefs, String key) throws IOException {
        final String prefValue = StringUtils.trimToNull(prefs.getValue(key, null));
        if (prefValue == null) {
            //Default to an empty object
            return mapper.createObjectNode();
        }
        return mapper.readTree(prefValue);
    }
}
