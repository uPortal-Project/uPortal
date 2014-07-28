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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.jasig.portal.events.tincan.om.LrsStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.PropertyResolver;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static java.lang.String.format;


/**
 * HTTP provider that connects to a TinCan LRS service.
 *
 * @author Josh Helmer, jhelmer@unicon.net
 */
public class DefaultTinCanAPIProvider implements ITinCanAPIProvider {
    private static final String STATEMENTS_REST_ENDPOINT = "/statements";
    private static final String STATES_REST_ENDPOINT = "/activities/state";
    private static final String ACTOR_FORMAT = "{\"name\":\"%s\",\"mbox\":\"mailto:%s\",\"objectType\":\"Agent\"}";
    private static final String STATE_KEY_STATUS = "status";
    private static final String STATE_VALUE_STARTED = "started";
    private static final String XAPI_VERSION_HEADER = "X-Experience-API-Version";
    private static final String XAPI_VERSION_VALUE = "1.0.0";
    private static final String PROPERTY_FORMAT = "org.jasig.portal.tincan-api.%s.%s";

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private RestTemplate restTemplate;
    private PropertyResolver propertyResolver;
    private String LRSUrl = "";
    private boolean enabled = false;
    private String id = null;
    private String activityId = "urn:tincan:uportal:activities:state:status";
    private String stateId = "urn:tincan:uportal:activities:state:status:stateId";
    private String actorEmail = "no-reply@jasig.org";
    private String actorName = "uPortal";


    /**
     * Set the rest template object.
     *
     * @param restTemplate the rest template object
     */
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    /**
     * Property resolver used to read property values based on the provider id.
     *
     * @param propertyResolver the property resolver
     */
    @Autowired
    public void setPropertyResolver(PropertyResolver propertyResolver) {
        this.propertyResolver = propertyResolver;
    }


    /**
     * Set the id of the provider to use.  The ID will be used to read the
     * configuration for this provider.
     *
     * @param id the provider id.
     */
    @Required
    public void setId(String id) {
        this.id = id;
    }


    /**
     * If the xAPI interface is enabled or disabled.  Defaults to "false"
     *
     * @param enabled the xAPI status
     */
    @Value("${org.jasig.portal.tincan-api.enabled:false}")
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    /**
     * Initialize the API.  Just sends an initialization event to the LRS provider.
     * This uses the activities/state API to do the initial test.
     */
    @Override
    public void init() {
        loadConfig();

        if (enabled) {
            try {
                String actorStr = format(ACTOR_FORMAT, actorName, actorEmail);

                // Setup GET params...
                List<Entry<String, String>> getParams = new ArrayList<>();
                getParams.add(new SimpleImmutableEntry<>("activityId", activityId));
                getParams.add(new SimpleImmutableEntry<>("agent", actorStr));
                getParams.add(new SimpleImmutableEntry<>("stateId", stateId));

                // just post a simple:  {"status": "started"} record to the states API to verify
                // the service is up.
                Map<String, String> body = new HashMap<String, String>();
                body.put(STATE_KEY_STATUS, STATE_VALUE_STARTED);

                ResponseEntity<Object> response = sendRequest(STATES_REST_ENDPOINT, HttpMethod.POST, getParams, body, Object.class);
                if (response.getStatusCode().series() != Series.SUCCESSFUL) {
                    logger.error("LRS provider for URL " + LRSUrl + " it not configured properly, or is offline.  Disabling provider.");
                }

            } catch (HttpClientErrorException e) {
                // log some additional info in this case...
                logger.error("LRS provider for URL " + LRSUrl + " failed to contact LRS for initialization.  Disabling provider." , e);
                logger.error("  Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
                enabled = false;

            } catch (Exception e) {
                logger.error("LRS provider for URL " + LRSUrl + " failed to contact LRS for initialization.  Disabling provider" , e);
                enabled = false;
            }
        }
    }


    /**
     * Actually send an event to the provider.
     *
     * @param statement the LRS statement to send.
     */
    @Override
    public boolean sendEvent(LrsStatement statement) {
        ResponseEntity<Object> response = sendRequest(STATEMENTS_REST_ENDPOINT, HttpMethod.POST, null, statement, Object.class);
        if (response.getStatusCode().series() == Series.SUCCESSFUL) {
            logger.trace("LRS provider successfully sent to {}, statement: {}", LRSUrl, statement);
        } else {
            logger.error("LRS provider failed to send to {}, statement: {}", LRSUrl, statement);
            logger.error("- Response: {}", response);
            return false;
        }

        return true;
    }


    @Override
    public void destroy() {
    }


    /**
     * Verify that the LRSUrl is defined.  If not, just disable this provider.
     */
    private void loadConfig() {
        final String urlProp = format(PROPERTY_FORMAT, id, "url");
        LRSUrl = propertyResolver.getProperty(urlProp);
        actorName = propertyResolver.getProperty(format(PROPERTY_FORMAT, id, "actor-name"), actorName);
        actorEmail = propertyResolver.getProperty(format(PROPERTY_FORMAT, id, "actor-email"), actorEmail);
        activityId = propertyResolver.getProperty(format(PROPERTY_FORMAT, id, "activity-id"), activityId);
        stateId = propertyResolver.getProperty(format(PROPERTY_FORMAT, id, "state-id"), stateId);

        if (StringUtils.isEmpty(LRSUrl)) {
            logger.error("Disabling TinCan API interface.  Property {0} not set!", urlProp);
            enabled = false;
            return;
        }

        // strip trailing '/' if included
        LRSUrl = LRSUrl.replaceAll("/*$", "");
    }


    /**
     * Send a request to the LRS.
     *
     * @param url the URL.  Should be relative to the xAPI API root
     * @param method the method
     * @param getParams the set of params
     * @param postData the post data
     * @param returnType the type of object to expect in the response
     * @param <T> The type of object to expect in the response
     * @return The response object.
     */
    private <T> ResponseEntity<T> sendRequest(String url, HttpMethod method,
            List<Entry<String, String>> getParams, Object postData, Class<T> returnType) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(XAPI_VERSION_HEADER, XAPI_VERSION_VALUE);

        // Need to convert to a URI so that Springs REST Template doesn't
        // do path mapping and get confused by the embedded JSON in the
        // case of a activity request.
        URI fullURI = null;
        try {
            fullURI = new URI(LRSUrl + url + buildGETParams(getParams));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error creating request URI", e);
        }

        HttpEntity<?> entity = new HttpEntity<>(postData, headers);
        ResponseEntity<T> response = restTemplate.exchange(fullURI, method, entity, returnType);

        return response;
    }


    /**
     * Convert a list of key-value pairs to a GET parameter.
     * @param getParams the list of params
     * @return a string formatted as a GET parameter.  Will include the leading '?' if there are any params.
     */
    private String buildGETParams(List<Entry<String, String>> getParams) {
        if (getParams == null) {
            return "";
        }

        StringBuilder buffer = new StringBuilder();

        try {
            char delim = '?';
            if (getParams != null) {
                for (Entry<String, String> param : getParams) {
                    buffer.append(delim);
                    buffer.append(param.getKey());
                    if (param.getValue() != null) {
                        buffer.append('=');
                        buffer.append(URLEncoder.encode(param.getValue(), "UTF-8"));
                    }

                    delim = '&';
                }
            }
        } catch (UnsupportedEncodingException e) {
            // should never happen...
            throw new RuntimeException(e);
        }

        return buffer.toString();
    }
}
