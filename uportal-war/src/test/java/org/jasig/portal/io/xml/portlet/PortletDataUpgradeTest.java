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

package org.jasig.portal.io.xml.portlet;

import org.jasig.portal.io.xml.BaseXsltDataUpgraderTest;
import org.jasig.portal.io.xml.XmlTestException;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXParseException;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletDataUpgradeTest extends BaseXsltDataUpgraderTest {
    @Test
    public void testUpgradeFacultyFeature31to32() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_31.xsl"), 
                PortletPortalDataType.IMPORT_26_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/faculty-feature_31.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/faculty-feature_31-32_expected.channel.xml"));
    }
    
    @Test
    public void testUpgradeFacultyFeature32to40() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_32.xsl"), 
                PortletPortalDataType.IMPORT_26_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/faculty-feature_31-32_expected.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/faculty-feature_32-40_expected.channel.xml"),
                new ClassPathResource("/xsd/io/portlet-definition/portlet-definition-4.0.xsd"));
    }
    
    @Test
    public void testUpgradeTestPortlet26to30() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_26.xsl"), 
                PortletPortalDataType.IMPORT_26_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/test-portlet-1_26.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/test-portlet-1_26-30_expected.channel.xml"));
    }

    @Test
    public void testUpgradeTestPortlet30to31() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_30.xsl"), 
                PortletPortalDataType.IMPORT_30_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/test-portlet-1_30.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/test-portlet-1_30-31_expected.channel.xml"));
    }

    @Test
    public void testUpgradeTestPortlet31to32() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_31.xsl"), 
                PortletPortalDataType.IMPORT_31_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/test-portlet-1_31.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/test-portlet-1_31-32_expected.channel.xml"));
    }

    @Test
    public void testUpgradeTestPortlet32to40() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_32.xsl"), 
                PortletPortalDataType.IMPORT_31_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/test-portlet-1_32.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/test-portlet-1_32-40_expected.channel.xml"),
                new ClassPathResource("/xsd/io/portlet-definition/portlet-definition-4.0.xsd"));
    }
    
    @Test
    public void testUpgradeGroupsManagerChannel26to30() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_26.xsl"), 
                PortletPortalDataType.IMPORT_26_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/groupsmanager_26.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/groupsmanager_26-30_expected.channel.xml"));
    }

    @Test
    public void testUpgradeGroupsManagerChannel30to31() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_30.xsl"), 
                PortletPortalDataType.IMPORT_30_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/groupsmanager_30.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/groupsmanager_30-31_expected.channel.xml"));
    }

    @Test
    public void testUpgradeGroupsManagerChannel31to32() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_31.xsl"), 
                PortletPortalDataType.IMPORT_31_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/groupsmanager_31.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/groupsmanager_31-32_expected.channel.xml"));
    }

    @Test
    public void testUpgradeGroupsManagerChannel32to40() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_32.xsl"), 
                PortletPortalDataType.IMPORT_31_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/groupsmanager_32.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/groupsmanager_32-40_expected.channel.xml"),
                new ClassPathResource("/xsd/io/portlet-definition/portlet-definition-4.0.xsd"));
    }

    @Test
    public void testUpgradeIdentitySwapperChannel32to40() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_32.xsl"), 
                PortletPortalDataType.IMPORT_31_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/IdentitySwapper_32.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/IdentitySwapper_32-40_expected.channel.xml"),
                new ClassPathResource("/xsd/io/portlet-definition/portlet-definition-4.0.xsd"));
    }

    @Test
    public void testUpgradeCInlineFrame26to30() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_26.xsl"), 
                PortletPortalDataType.IMPORT_26_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/mywebspace-demo_26.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/mywebspace-demo_26-30_expected.channel.xml"));
    }

    @Test
    public void testUpgradeCInlineFrame30to31() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_30.xsl"), 
                PortletPortalDataType.IMPORT_30_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/mywebspace-demo_26-30_expected.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/mywebspace-demo_30-31_expected.channel.xml"));
    }

    @Test
    public void testUpgradeCInlineFrame31to32() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_31.xsl"), 
                PortletPortalDataType.IMPORT_31_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/mywebspace-demo_30-31_expected.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/mywebspace-demo_31-32_expected.channel.xml"));
    }

    @Test
    public void testUpgradeCInlineFrame32to40() throws Exception {
        testXsltUpgrade(
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/upgradeChannel_32.xsl"), 
                PortletPortalDataType.IMPORT_31_DATA_KEY, 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/mywebspace-demo_31-32_expected.channel.xml"), 
                new ClassPathResource("/org/jasig/portal/io/xml/portlet/mywebspace-demo_32-40_expected.channel.xml"),
                new ClassPathResource("/xsd/io/portlet-definition/portlet-definition-4.0.xsd"));
    }
}
