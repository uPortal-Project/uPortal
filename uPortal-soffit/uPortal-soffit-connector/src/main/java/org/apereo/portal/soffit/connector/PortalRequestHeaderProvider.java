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

import java.util.*;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apereo.portal.soffit.Headers;
import org.apereo.portal.soffit.model.v1_0.PortalRequest;
import org.apereo.portal.soffit.model.v1_0.PortalRequest.Attributes;
import org.apereo.portal.soffit.service.PortalRequestService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Prepares the custom 'X-Soffit-PortalRequest' HTTP header.
 *
 * @since 5.0
 */
public class PortalRequestHeaderProvider extends AbstractHeaderProvider {

    public static final String INCLUDE_PREFERENCE =
            SoffitConnectorController.class.getName() + ".includePortalRequest";
    public static final String NAMESPACE_PREFIX = "n_";

    @Autowired private PortalRequestService portalRequestService;

    @Override
    public Header createHeader(RenderRequest renderRequest, RenderResponse renderResponse) {

        // Include this header?
        if (!isIncluded(renderRequest, INCLUDE_PREFERENCE)) {
            return null;
        }

        // Username
        final String username = getUsername(renderRequest);

        // Properties
        final Map<String, String> properties = new HashMap<>();
        final Enumeration<String> names = renderRequest.getPropertyNames();
        for (String propertyName = names.nextElement();
                names.hasMoreElements();
                propertyName = names.nextElement()) {
            properties.put(propertyName, renderRequest.getProperty(propertyName));
        }

        // Attributes
        final Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(
                Attributes.NAMESPACE.getName(),
                Collections.singletonList(NAMESPACE_PREFIX + renderRequest.getWindowID()));
        attributes.put(
                Attributes.MODE.getName(),
                Collections.singletonList(renderRequest.getPortletMode().toString()));
        attributes.put(
                Attributes.WINDOW_STATE.getName(),
                Collections.singletonList(renderRequest.getWindowState().toString()));
        attributes.put(
                Attributes.PORTAL_INFO.getName(),
                Collections.singletonList(renderRequest.getPortalContext().getPortalInfo()));
        attributes.put(
                Attributes.SCHEME.getName(), Collections.singletonList(renderRequest.getScheme()));
        attributes.put(
                Attributes.SERVER_NAME.getName(),
                Collections.singletonList(renderRequest.getServerName()));
        attributes.put(
                Attributes.SERVER_PORT.getName(),
                Collections.singletonList(
                        Integer.valueOf(renderRequest.getServerPort()).toString()));
        attributes.put(
                Attributes.SECURE.getName(),
                Collections.singletonList(Boolean.valueOf(renderRequest.isSecure()).toString()));

        // Parameters
        final Map<String, List<String>> parameters = new HashMap<>();
        for (Map.Entry<String, String[]> y : renderRequest.getParameterMap().entrySet()) {
            parameters.put(y.getKey(), Arrays.asList(y.getValue()));
        }

        // PortalRequest header
        final PortalRequest portalRequest =
                portalRequestService.createPortalRequest(
                        properties, attributes, parameters, username, getExpiration(renderRequest));
        final Header result =
                new BasicHeader(
                        Headers.PORTAL_REQUEST.getName(), portalRequest.getEncryptedToken());
        logger.debug(
                "Produced the following PortalRequest header for username='{}':  {}",
                username,
                result);

        return result;
    }
}
