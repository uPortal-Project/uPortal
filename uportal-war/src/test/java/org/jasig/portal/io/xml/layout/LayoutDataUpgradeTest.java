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

package org.jasig.portal.io.xml.layout;

import org.jasig.portal.io.xml.BaseXsltDataUpgraderTest;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LayoutDataUpgradeTest extends BaseXsltDataUpgraderTest {
    @Test
    public void testUpgrade30to32() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/layout/upgradeLayout_v3-2.xsl"), 
                LayoutPortalDataType.IMPORT_30_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/layout/test_30.layout.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/layout/test_30-32_expected.layout.xml"));
    }
    
    @Test
    public void testFragmentUpgrade30to32() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/layout/upgradeLayout_v3-2.xsl"), 
                LayoutPortalDataType.IMPORT_30_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/layout/developer-lo_30.fragment-layout.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/layout/developer-lo_30-32_expected.fragment-layout.xml"));
    }
}
