/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.io.xml.user;

import org.jasig.portal.io.xml.BaseXsltDataUpgraderTest;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class UserDataUpgradeTest extends BaseXsltDataUpgraderTest {
    @Test
    public void testUpgradeUser26to40() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/user/upgrade-user_3-2.xsl"), 
                UserPortalDataType.IMPORT_26_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/user/test_2-6.user.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/user/test_2-6_to_4-0_expected.user.xml"),
                new ClassPathResource("/xsd/io/user/user-4.0.xsd"));
    }

    @Test
    public void testUpgradeUser30to40() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/user/upgrade-user_3-2.xsl"), 
                UserPortalDataType.IMPORT_30_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/user/test_3-0.user.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/user/test_3-0_to_4-0_expected.user.xml"),
                new ClassPathResource("/xsd/io/user/user-4.0.xsd"));
    }

    @Test
    public void testUpgradeUser32to40() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/user/upgrade-user_3-2.xsl"), 
                UserPortalDataType.IMPORT_32_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/user/test_3-2.user.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/user/test_3-2_to_4-0_expected.user.xml"),
                new ClassPathResource("/xsd/io/user/user-4.0.xsd"));
    }
}
