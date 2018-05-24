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
package org.apereo.portal.soffit.model.v1_0;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Provides some information about the user's request to which the portal will respond using output
 * from the soffit. This information is organized into three collections: properties, attributes,
 * and parameters.
 *
 * @since 5.0
 */
public class PortalRequest extends AbstractTokenizable {

    /**
     * Additional information that the portal (via the Soffit Connector) shares with the remote
     * Soffit will be set as attributes on the {@link PortalRequest}.
     */
    public enum Attributes {

        /**
         * A soffit may rely on this String to construct element IDs and selectors that are unique
         * on the page. The portal is responsible for issuing a namespace that does not conflict
         * with any other content on the same user's layout.
         *
         * <p><strong>WARNING:</strong> Soffits that use the GLOBAL cache option (cache across
         * users) may not rely on this strategy. There is no strategy that will work in such
         * circumstances short of generating a namespace in Javascript, since the same HTML output
         * can be placed into the DOM in multiple places.
         *
         * @since 5.0
         */
        NAMESPACE("namespace"),

        /**
         * Provides a hint about how the user is currently interacting with the content; normally
         * VIEW, but could be EDIT, HELP, CONFIG, or a custom mode.
         *
         * @since 5.0
         */
        MODE("mode"),

        /**
         * Provides a hint about the screen real estate allocated to the content; could be NORMAL,
         * MAXIMIZED, MINIMIZED, or a custom value.
         *
         * @since 5.0
         */
        WINDOW_STATE("windowState"),

        /**
         * Identifying platform and version information about the calling portal.
         *
         * @since 5.0
         */
        PORTAL_INFO("portalInfo"),

        /** @since 5.0 */
        SCHEME("scheme"),

        /** @since 5.0 */
        SERVER_NAME("serverName"),

        /** @since 5.0 */
        SERVER_PORT("serverPort"),

        /** @since 5.0 */
        SECURE("secure");

        /*
         * Implementation
         */

        private final String name;

        Attributes(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final Map<String, String> properties;
    private final Map<String, List<String>> attributes;
    private final Map<String, List<String>> parameters;

    public PortalRequest(
            String encryptedToken,
            Map<String, String> properties,
            Map<String, List<String>> attributes,
            Map<String, List<String>> parameters) {
        super(encryptedToken);
        this.properties = Collections.unmodifiableMap(properties);
        this.attributes = Collections.unmodifiableMap(attributes);
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    /**
     * Supports proxying a missing data model element.
     *
     * @since 5.1
     */
    protected PortalRequest() {
        super(null);
        this.properties = null;
        this.attributes = null;
        this.parameters = null;
    }

    /**
     * These are typically HTTP headers from the original HttpServletRequest sent by the user's
     * browser.
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * These are metadata about the original request, and the calling portal, managed by the
     * container.
     */
    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    /** These are querystring parameters, or form data in the case of a POST. */
    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        PortalRequest other = (PortalRequest) obj;
        if (attributes == null) {
            if (other.attributes != null) return false;
        } else if (!attributes.equals(other.attributes)) return false;
        if (parameters == null) {
            if (other.parameters != null) return false;
        } else if (!parameters.equals(other.parameters)) return false;
        if (properties == null) {
            if (other.properties != null) return false;
        } else if (!properties.equals(other.properties)) return false;
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("properties", this.properties)
                .append("attributes", this.attributes)
                .append("parameters", this.parameters)
                .append("getEncryptedToken()", this.getEncryptedToken())
                .toString();
    }
}
