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
package org.apereo.portal.soffit.security;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Concrete <code>AuthenticationManager</code> implementation (Spring Security) for use with {@link
 * SoffitApiPreAuthenticatedProcessingFilter}. Use <code>
 * SoffitApiPreAuthenticatedProcessingFilter.setAuthenticationManager</code> when constructing the
 * bean.
 *
 * @since 5.1
 */
public class SoffitApiAuthenticationManager implements AuthenticationManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        logger.debug("Authenticating the following Authentication object:  {}", authentication);

        if (SoffitApiUserDetails.class.isInstance(authentication.getDetails())) {
            final SoffitApiUserDetails saud = (SoffitApiUserDetails) authentication.getDetails();
            if (StringUtils.isNotBlank(saud.getUsername())) {
                authentication.setAuthenticated(true);
            }
        }

        return authentication;
    }
}
