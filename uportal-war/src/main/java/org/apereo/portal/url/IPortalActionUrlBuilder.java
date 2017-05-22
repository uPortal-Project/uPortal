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

/**
 * Builds a portal action URL that can deal with redirects
 *
 */
public interface IPortalActionUrlBuilder extends IPortalUrlBuilder {
    /** @param location The redirect location to generate a URL for. */
    public void setRedirectLocation(String location);
    /**
     * Generates the portal render URL represented by this url builder and passes it to the redirect
     * location.
     *
     * @param location The redirect location to generate a URL for.
     * @param renderUrlParamName The parameter name to pass the render url with
     */
    public void setRedirectLocation(String location, String renderUrlParamName);
    /** The currently set redirect location, null if not set */
    public String getRedirectLocation();
    /** The currently set redirect render parameter name, null if not set */
    public String getRenderUrlParamName();
}
