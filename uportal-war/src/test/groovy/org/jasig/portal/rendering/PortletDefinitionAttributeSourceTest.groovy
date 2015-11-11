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
package org.jasig.portal.rendering

import org.jasig.portal.portlet.dao.IPortletDefinitionDao
import org.jasig.portal.portlet.dao.jpa.PortletDefinitionImpl
import org.jasig.portal.portlet.dao.jpa.PortletTypeImpl

import javax.xml.stream.XMLEventFactory

/**
 * Tests PortletDefinitionAttributeSource.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

class PortletDefinitionAttributeSourceTest extends GroovyTestCase {
    void testGetAdditionalAttributesPortlet() {
        def portletDefn = new PortletDefinitionImpl(new PortletTypeImpl('name', 'cpdUri'),
            'theFname', 'name', 'title', 'webappName', 'portletName', false)
        def portletDefinitionDao = [
                getPortletDefinitionByFname: { String fname ->
                    assert 'theFname' == fname
                    portletDefn }] as IPortletDefinitionDao
        def testClass = new PortletDefinitionAttributeSource()
        testClass.setPortletDefinitionDao(portletDefinitionDao)
        def factory = XMLEventFactory.newFactory()
        def attr = factory.createAttribute('fname', 'theFname')
        def element = factory.createStartElement('', '', 'channel',attr.iterator(), null)
        def attrIterator = testClass.getAdditionalAttributes(null, null, element)
        def attributeMap = [:]
        while (attrIterator.hasNext()) {
            def attrItem = attrIterator.next()
            attributeMap[attrItem.getName().getLocalPart()] = attrItem.getValue()
        }
        assert attributeMap.size() == 2
        assert attributeMap[PortletDefinitionAttributeSource.PORTLET_NAME_ATTRIBUTE] == 'portletName'
        assert attributeMap[PortletDefinitionAttributeSource.WEBAPP_NAME_ATTRIBUTE] == 'webappName'
    }

    void testGetAdditionalAttributesFrameworkPortlet() {
        def portletDefn = new PortletDefinitionImpl(new PortletTypeImpl('name', 'cpdUri'),
                'theFname', 'name', 'title', null, 'portletName', true)
        def portletDefinitionDao = [
                getPortletDefinitionByFname: { String fname ->
                    assert 'theFname' == fname
                    portletDefn }] as IPortletDefinitionDao
        def testClass = new PortletDefinitionAttributeSource()
        testClass.setPortletDefinitionDao(portletDefinitionDao)
        def factory = XMLEventFactory.newFactory()
        def attr = factory.createAttribute('fname', 'theFname')
        def element = factory.createStartElement('', '', 'channel',attr.iterator(), null)
        def attrIterator = testClass.getAdditionalAttributes(null, null, element)
        def attributeMap = [:]
        while (attrIterator.hasNext()) {
            def attrItem = attrIterator.next()
            attributeMap[attrItem.getName().getLocalPart()] = attrItem.getValue()
        }
        assert attributeMap.size() == 2
        assert attributeMap[PortletDefinitionAttributeSource.PORTLET_NAME_ATTRIBUTE] == 'portletName'
        assert attributeMap[PortletDefinitionAttributeSource.FRAMEWORK_PORTLET_ATTRIBUTE] == 'true'
    }
}
