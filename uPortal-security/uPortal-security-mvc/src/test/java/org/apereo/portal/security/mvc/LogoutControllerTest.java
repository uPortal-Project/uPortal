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
package org.apereo.portal.security.mvc;

import static org.junit.Assert.*;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

public class LogoutControllerTest {

    @Test
    public void testGetRedirectionUrlForSecurityContext() {

        final String casSecurityContextName = "cas";
        final String missingSecurityContextName = "missing";

        final String rootRedirectUrl = "https://www.apereo.org";
        final String casRedirectUrl = "https://www.apereo.org/projects/cas";

        /*
         * Set up the Spring Environment
         */
        final MockEnvironment env = new MockEnvironment();
        env.setProperty(
                LogoutController.LOGOUT_REDIRECT_PREFIX + LogoutController.LOGOUT_REDIRECT_ROOT,
                rootRedirectUrl);
        env.setProperty(
                LogoutController.LOGOUT_REDIRECT_PREFIX + casSecurityContextName,
                casRedirectUrl);

        LogoutController logoutController = new LogoutController();
        logoutController.setEnvironment(env);

        // Defined
        assertEquals(
                "Response should be " + casRedirectUrl
                        + " where security context name is '" + casSecurityContextName + "'",
                casRedirectUrl,
                logoutController.getRedirectionUrlForSubContext(casSecurityContextName));

        // Not defined
        assertNull(
                "Response should be null where logoutRedirect not defined",
                logoutController.getRedirectionUrlForSubContext(missingSecurityContextName));

    }

}
