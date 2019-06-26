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
package org.apereo.portal.soffit.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import org.junit.Test;

public class JwtEncryptorTest {

    private static final String VALID_JWT =
        "sdfSDFSD4345.sdfsdf2342534fsadf/++.sdf3w4tvav==";

    private static final String INVALID_JWT1 =
        "tqqgq5vtbyn65mutyu756muyum567mm7m";

    @Test
    public void testJwtPatternMatcher() {
        Matcher m = JwtEncryptor.JWT_PATTERN.matcher(VALID_JWT);
        assertTrue("Valid JWT matches", m.matches());

        m = JwtEncryptor.JWT_PATTERN.matcher(INVALID_JWT1);
        assertFalse("Invalid JWT matches", m.matches());
    }

}
