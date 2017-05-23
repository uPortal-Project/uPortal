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

import static java.lang.String.format;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apereo.portal.events.tincan.om.LrsStatement;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP provider that connects to a TinCan LRS service.
 *
 * <p>Each provider is must be configured with an id. The id will be used to load the configuration
 * for the provider. The id must be injected as a spring property.*
 *
 * <p>Additional configuration is available by setting properties in the portal.properties or your
 * local overrides.properties file. The additional properties that may be configured are:
 *
 * <table>
 *     <tr>
 *         <th>property</th>
 *         <th>required</th>
 *         <th>default value</th>
 *         <th>description</th>
 *     </tr>
 *     <tr>
 *         <td>org.apereo.portal.tincan-api.{ID}.url</td>
 *         <td>true</td>
 *         <td>&nbsp;</td>
 *         <td>The root of the LRS REST API.</td>
 *     </tr>
 *     <tr>
 *         <td>org.apereo.portal.tincan-api.{ID}.form-encode-activity-data</td>
 *         <td>false unless the LRS is LearningLocker</td>
 *         <td>false</td>
 *         <td>
 *             By default, the activities/state API accepts JSON in the POST body.
 *             LearningLocker requires that the content be form encoded instead.
 *             This setting converts the request to a multipart form.
 *
 *             For LearningLocker, this should always be set to true.
 *             For ScormCloud, this should always be set to false or left to the default.
 *             Installations will need to experiment with other LRSs, but I believe
 *             that "false" more closely matches the spec
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>org.apereo.portal.tincan-api.{ID}.activity-form-param-name</td>
 *         <td>false</td>
 *         <td>content</td>
 *         <td>
 *             If the "org.apereo.portal.tincan-api.{ID}.form-encode-activity-data"
 *             property is set, this property controls the property name to use
 *             when posting the form data.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>org.apereo.portal.tincan-api.{ID}.actor-name</td>
 *         <td>false</td>
 *         <td>uPortal</td>
 *         <td>
 *              The LRS will attempt to POST to the activities/state API on startup
 *              in order to test if the LRS is available.  The activities/state API
 *              requires and agent element.  This name is the name of the agent to
 *              use.  Since this is only for testing connectivity, this is not
 *              critical and in most cases should be left as the default.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>org.apereo.portal.tincan-api.{ID}.actor-email</td>
 *         <td>false</td>
 *         <td>no-reply@jasig.org</td>
 *         <td>
 *             The email address of the agent making the initial activities request.
 *             Not critical and should be left as default in most cases.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>org.apereo.portal.tincan-api.{ID}.activityId</td>
 *         <td>false</td>
 *         <td>activityId</td>
 *         <td>
 *             The activity ID passed to the initial activities/state request.
 *             Since the initial request is just a connectivity test, can be
 *             left as the default in most cases.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>org.apereo.portal.tincan-api.{ID}.stateId</td>
 *         <td>false</td>
 *         <td>stateId</td>
 *         <td>
 *             The state ID passed to the initial activities/state request.
 *             Since the initial request is just a connectivity test, can be
 *             left as the default in most cases.
 *         </td>
 *     </tr>
 * </table>
 *
 */
public class DefaultTinCanAPIProvider implements ITinCanAPIProvider {
    protected static final String STATEMENTS_REST_ENDPOINT = "/statements";
    protected static final String STATES_REST_ENDPOINT = "/activities/state";
    private static final String ACTOR_FORMAT =
            "{\"name\":\"%s\",\"mbox\":\"mailto:%s\",\"objectType\":\"Agent\"}";
    private static final String STATE_FORMAT = "{\"%s\":\"%s\"}";
    private static final String STATE_KEY_STATUS = "status";
    private static final String STATE_VALUE_STARTED = "started";
    private static final String XAPI_VERSION_HEADER = "X-Experience-API-Version";
    private static final String XAPI_VERSION_VALUE = "1.0.0";
    private static final String PROPERTY_FORMAT = "org.apereo.portal.tincan-api.%s.%s";

    private static final String PARAM_ACTIVITY_ID = "activityId";
    private static final String PARAM_AGENT = "agent";
    private static final String PARAM_STATE_ID = "stateId";

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
    private boolean formEncodeActivityData = false;
    private String activitiesFormParamName = "content";

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
     * Set the id of the provider to use. The ID will be used to read the configuration for this
     * provider.
     *
     * @param id the provider id.
     */
    @Required
    public void setId(String id) {
        this.id = id;
    }

    /**
     * If the xAPI interface is enabled or disabled. Defaults to "false"
     *
     * @param enabled the xAPI status
     */
    @Value("${org.apereo.portal.tincan-api.enabled:false}")
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Check if the LRS provider is enabled.
     *
     * @return true if the LRS provider is enable, else false
     */
    protected boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the base LRS URL.
     *
     * @return the base URL
     */
    protected String getLRSUrl() {
        return LRSUrl;
    }

    /**
     * Initialize the API. Just sends an initialization event to the LRS provider. This uses the
     * activities/state API to do the initial test.
     */
    @Override
    public void init() {
        loadConfig();

        if (!isEnabled()) {
            return;
        }

        try {
            String actorStr = format(ACTOR_FORMAT, actorName, actorEmail);

            // Setup GET params...
            List<BasicNameValuePair> getParams = new ArrayList<>();
            getParams.add(new BasicNameValuePair(PARAM_ACTIVITY_ID, activityId));
            getParams.add(new BasicNameValuePair(PARAM_AGENT, actorStr));
            getParams.add(new BasicNameValuePair(PARAM_STATE_ID, stateId));

            Object body = null;
            if (formEncodeActivityData) {
                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                String json = format(STATE_FORMAT, STATE_KEY_STATUS, STATE_VALUE_STARTED);
                map.add(activitiesFormParamName, json);
                body = map;

            } else {
                // just post a simple:  {"status": "started"} record to the states API to verify
                // the service is up.
                Map<String, String> data = new HashMap<String, String>();
                data.put(STATE_KEY_STATUS, STATE_VALUE_STARTED);
                body = data;
            }

            ResponseEntity<Object> response =
                    sendRequest(
                            STATES_REST_ENDPOINT, HttpMethod.POST, getParams, body, Object.class);
            if (response.getStatusCode().series() != Series.SUCCESSFUL) {
                logger.error(
                        "LRS provider for URL "
                                + LRSUrl
                                + " it not configured properly, or is offline.  Disabling provider.");
            }

            // todo: Need to think through a strategy for handling errors submitting
            // to the LRS.
        } catch (HttpClientErrorException e) {
            // log some additional info in this case...
            logger.error(
                    "LRS provider for URL "
                            + LRSUrl
                            + " failed to contact LRS for initialization.  Disabling provider.",
                    e);
            logger.error(
                    "  Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            enabled = false;

        } catch (Exception e) {
            logger.error(
                    "LRS provider for URL "
                            + LRSUrl
                            + " failed to contact LRS for initialization.  Disabling provider",
                    e);
            enabled = false;
        }
    }

    /**
     * Actually send an event to the provider.
     *
     * @param statement the LRS statement to send.
     */
    @Override
    public boolean sendEvent(LrsStatement statement) {
        if (!isEnabled()) {
            return false;
        }

        ResponseEntity<Object> response =
                sendRequest(
                        STATEMENTS_REST_ENDPOINT, HttpMethod.POST, null, statement, Object.class);
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
    public void destroy() {}

    /**
     * Read the LRS config.
     *
     * <p>"url" is the only required property. If not set, will disable this LRS provider.
     *
     * <p>Rather than asking installations to write a lot of XML, this pushes most of the
     * configuration out to portal.properties. It reads dynamically named properties based on the
     * "id" of this LRS provider. This is similar to the way that the TinCan configuration is
     * handled for Sakai.
     */
    protected void loadConfig() {
        if (!isEnabled()) {
            return;
        }

        final String urlProp = format(PROPERTY_FORMAT, id, "url");
        LRSUrl = propertyResolver.getProperty(urlProp);
        actorName =
                propertyResolver.getProperty(format(PROPERTY_FORMAT, id, "actor-name"), actorName);
        actorEmail =
                propertyResolver.getProperty(
                        format(PROPERTY_FORMAT, id, "actor-email"), actorEmail);
        activityId =
                propertyResolver.getProperty(
                        format(PROPERTY_FORMAT, id, "activity-id"), activityId);
        stateId = propertyResolver.getProperty(format(PROPERTY_FORMAT, id, "state-id"), stateId);
        formEncodeActivityData =
                propertyResolver.getProperty(
                        format(PROPERTY_FORMAT, id, "form-encode-activity-data"),
                        Boolean.class,
                        formEncodeActivityData);
        activitiesFormParamName =
                propertyResolver.getProperty(
                        format(PROPERTY_FORMAT, id, "activity-form-param-name"),
                        activitiesFormParamName);

        if (StringUtils.isEmpty(LRSUrl)) {
            logger.error("Disabling TinCan API interface.  Property {} not set!", urlProp);
            enabled = false;
            return;
        }

        // strip trailing '/' if included
        LRSUrl = LRSUrl.replaceAll("/*$", "");
    }

    /**
     * Send a request to the LRS.
     *
     * @param pathFragment the URL. Should be relative to the xAPI API root
     * @param method the HTTP method
     * @param getParams the set of GET params
     * @param postData the post data.
     * @param returnType the type of object to expect in the response
     * @param <T> The type of object to expect in the response
     * @return The response object.
     */
    protected <T> ResponseEntity<T> sendRequest(
            String pathFragment,
            HttpMethod method,
            List<? extends NameValuePair> getParams,
            Object postData,
            Class<T> returnType) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(XAPI_VERSION_HEADER, XAPI_VERSION_VALUE);

        // make multipart data is handled correctly.
        if (postData instanceof MultiValueMap) {
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        }

        URI fullURI = buildRequestURI(pathFragment, getParams);

        HttpEntity<?> entity = new HttpEntity<>(postData, headers);
        ResponseEntity<T> response = restTemplate.exchange(fullURI, method, entity, returnType);

        return response;
    }

    /**
     * Build a URI for the REST request.
     *
     * <p>Note: this converts to URI instead of using a string because the activities/state API
     * requires you to pass JSON as a GET parameter. The {...} confuses the RestTemplate path
     * parameter handling. By converting to URI, I skip that.
     *
     * @param pathFragment The path fragment relative to the LRS REST base URL
     * @param params The list of GET parameters to encode. May be null.
     * @return The full URI to the LMS REST endpoint
     */
    private URI buildRequestURI(String pathFragment, List<? extends NameValuePair> params) {
        try {
            String queryString = "";
            if (params != null && !params.isEmpty()) {
                queryString = "?" + URLEncodedUtils.format(params, "UTF-8");
            }

            URI fullURI = new URI(LRSUrl + pathFragment + queryString);
            return fullURI;
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error creating request URI", e);
        }
    }
}
