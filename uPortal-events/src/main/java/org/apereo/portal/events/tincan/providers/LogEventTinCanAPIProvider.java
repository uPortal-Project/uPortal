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
package org.apereo.portal.events.tincan.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.portal.events.tincan.om.LrsStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TinCan API provider that just logs LRS statements.
 *
 */
public class LogEventTinCanAPIProvider implements ITinCanAPIProvider {
    private static Logger log = LoggerFactory.getLogger(LogEventTinCanAPIProvider.class);
    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void init() {
        log.debug("Initializing LogEventTinCanAPIProvider.");
    }

    @Override
    public boolean sendEvent(LrsStatement statement) {
        String json = null;
        try {
            json = objectMapper.writeValueAsString(statement);
            log.debug("TinCanAPI Event: " + json);
        } catch (JsonProcessingException e) {
            log.error("Error logging xAPI event", e);
        }

        return true;
    }

    @Override
    public void destroy() {
        log.debug("Destroying LogEventTinCanAPIProvider.");
    }
}
