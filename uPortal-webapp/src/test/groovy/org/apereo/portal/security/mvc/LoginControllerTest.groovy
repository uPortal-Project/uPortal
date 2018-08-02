/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.portal.security.mvc

import javax.servlet.http.HttpServletRequest

import org.junit.Test

class LoginControllerTest extends GroovyTestCase {

    @Test
    void testParseLocalRefUrl() {

        final HttpServletRequest req = [
            getRequestURL: { return new StringBuffer('http://localhost:8080/uPortal'); }
        ] as HttpServletRequest

        final LoginController loginController = new LoginController()
        assertNull(''' is blank', loginController.parseLocalRefUrl(req, ""))
        assertNotNull(''/' is local', loginController.parseLocalRefUrl(req, '/'))
        assertNotNull(''/foo' is local', loginController.parseLocalRefUrl(req, '/foo'))
        assertNotNull(''/foo/bar' is local', loginController.parseLocalRefUrl(req, '/foo/bar'))
        assertNull(''http://www.apereo.org/' is not local', loginController.parseLocalRefUrl(req, 'http://www.apereo.org/'))
        assertNull(''//www.apereo.org/' is not local', loginController.parseLocalRefUrl(req, '//www.apereo.org/'))

    }
}
