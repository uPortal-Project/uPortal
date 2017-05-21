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
package org.apereo.portal.url;

import org.apache.pluto.container.PortletURLProvider.TYPE;

/**
 * Represents the request type of the url, all available request types should be enumerated here
 *
 */
public enum UrlType {
    /* IMPORTANT, all enum keys must be completely upper case for the helper methods to not cause problems */

    /** Renders content */
    RENDER,
    /** Performs an action, the result from this type of URL will always be a redirect */
    ACTION,
    /** Portlet Resource: renders content direct from the portlet */
    RESOURCE;

    private final String lowercase;

    private UrlType() {
        this.lowercase = this.toString().toLowerCase();
    }

    public String toLowercaseString() {
        return this.lowercase;
    }

    public TYPE getPortletUrlType() {
        return UrlType.getPortletUrlType(this);
    }

    public static TYPE getPortletUrlType(UrlType t) {
        switch (t) {
            case ACTION:
                {
                    return TYPE.ACTION;
                }
            case RENDER:
                {
                    return TYPE.RENDER;
                }
            case RESOURCE:
                {
                    return TYPE.RESOURCE;
                }
        }

        throw new IllegalStateException("Unknown UrlType: " + t);
    }

    public static UrlType fromPortletUrlType(TYPE t) {
        switch (t) {
            case ACTION:
                {
                    return ACTION;
                }
            case RENDER:
                {
                    return RENDER;
                }
            case RESOURCE:
                {
                    return RESOURCE;
                }
        }

        throw new IllegalStateException("Unknown TYPE: " + t);
    }

    public static UrlType valueOfIngoreCase(String name) {
        return UrlType.valueOf(name.toUpperCase());
    }

    public static UrlType valueOfIngoreCase(String name, UrlType defaultValue) {
        if (name == null) {
            return defaultValue;
        }

        try {
            return valueOfIngoreCase(name);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}
