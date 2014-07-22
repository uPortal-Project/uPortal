/*
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
package org.jasig.portal.events.tincan.providers;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jasig.portal.events.tincan.UrnBuilder;
import org.jasig.portal.events.tincan.om.LocalizedString;
import org.jasig.portal.events.tincan.om.LrsActor;
import org.jasig.portal.events.tincan.om.LrsObject;
import org.jasig.portal.events.tincan.om.LrsStatement;
import org.jasig.portal.events.tincan.om.LrsVerb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


/**
 * HTTP provider that connects to a TinCan LRS service.
 *
 * @author Josh Helmer, jhelmer@unicon.net
 */
public class DefaultTinCanAPIProvider implements ITinCanAPIProvider {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private RestTemplate restTemplate;
    private String LRSUrl = "";
    private boolean enabled = false;


    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    /**
     * Set the LRS URL.  Note:  Since, for now, this interface only handles creating statements,
     * the URL should include the *full* REST endpoint for creating statements.  In most cases
     * that means you need to tack /statements on to the end of the base URL provided by the provider.
     *
     * @param url the statements REST endpoint
     */
    public void setLRSUrl(String url) {
        this.LRSUrl = url;
    }


    @Value("${org.jasig.portal.tincan-api.enabled:false}")
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    /**
     * Verify that the LRSUrl is defined.  If not, just disable this provider.
     */
    public void checkConfig() {
        if (StringUtils.isEmpty(LRSUrl)) {
            logger.warn("Disabling TinCan API interface.  Missing LRSUrl!");
            enabled = false;
        }
    }


    /**
     * Initialize the API.  Just sends an initialization event to the LRS provider.
     */
    @Override
    public void init() {
        checkConfig();

        if (enabled) {
            final UrnBuilder objectIdBuilder = new UrnBuilder("UTF-8", "tincan", "uportal", "activities");
            objectIdBuilder.add("startup");

            LrsActor actor = new LrsActor("mailto:no-reply@jasig.org", "uPortal user");
            Map<String, LocalizedString> definition = new HashMap<>();
            definition.put("name", new LocalizedString(Locale.US, "uPortal Instance"));
            LrsObject object = new LrsObject(
                    objectIdBuilder.getUri(),
                    "Activity",
                    definition);

            LrsStatement statement = new LrsStatement(actor, LrsVerb.INITIALIZED, object);
            sendEvent(statement);
        }
    }


    /**
     * Actually send an event to the provider.
     *
     * @param statement the LRS statement to send.
     */
    @Override
    public void sendEvent(LrsStatement statement) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Experience-API-Version", "1.0");

        HttpEntity<LrsStatement> entity = new HttpEntity<>(statement, headers);

        ResponseEntity<Object> response = restTemplate.exchange(LRSUrl, HttpMethod.POST, entity, Object.class);
        if (response.getStatusCode().series() == Series.SUCCESSFUL) {
            logger.debug("LRS provider successfully sent to {}, statement: {}", LRSUrl, statement);
        } else {
            logger.warn("LRS provider failed to send to {}, statement: {}\n\tResponse:", LRSUrl, statement, response);
        }
    }


    @Override
    public void destroy() {
    }
}
