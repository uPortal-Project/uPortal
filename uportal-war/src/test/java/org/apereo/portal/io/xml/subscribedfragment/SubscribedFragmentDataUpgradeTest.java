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
package org.apereo.portal.io.xml.subscribedfragment;

import org.apereo.portal.io.xml.BaseXsltDataUpgraderTest;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class SubscribedFragmentDataUpgradeTest extends BaseXsltDataUpgraderTest {
    @Test
    public void testUpgradeUser32to40() throws Exception {
        testXsltUpgrade(
                new ClassPathResource(
                        "/org/apereo/portal/io/xml/subscribed-fragment/upgrade-subscribed-fragment_3-2.xsl"),
                SubscribedFragmentPortalDataType.IMPORT_32_DATA_KEY,
                new ClassPathResource(
                        "/org/apereo/portal/io/xml/subscribed-fragment/test_3-2.subscribed-fragment.xml"),
                new ClassPathResource(
                        "/org/apereo/portal/io/xml/subscribed-fragment/test_4-0.subscribed-fragment.xml"),
                new ClassPathResource("/xsd/subscribed-fragment-4.0.xsd"));
    }
}
