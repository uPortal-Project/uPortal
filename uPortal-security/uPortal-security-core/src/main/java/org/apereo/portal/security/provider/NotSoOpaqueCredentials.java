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
package org.apereo.portal.security.provider;

import org.apereo.portal.security.IOpaqueCredentials;

/**
 * This is a provider-specific extension to the standard opaque credentials that is designed to work
 * with the caching security context. This interface adds a method that removes a bit of the opacity
 * of the original by providing means of retrieving a stored credential. See the warnings and
 * caveats associated with the CacheSecurityContext provider.
 */
public interface NotSoOpaqueCredentials extends IOpaqueCredentials {

    /**
     * Returns the stored credentials as a String.
     *
     * @return the stored credentials.
     */
    public String getCredentials();
}
