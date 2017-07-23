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
package org.apereo.portal.rendering

import org.apereo.portal.portlet.dao.IPortletDefinitionDao
import org.apereo.portal.portlet.dao.jpa.PortletDefinitionImpl
import org.apereo.portal.portlet.dao.jpa.PortletTypeImpl
import org.apereo.portal.portlet.om.PortletLifecycleState
import javax.xml.stream.XMLEventFactory
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.StartElement

/**
 * Tests PortletDefinitionAttributeSource.
 *
 */

class PortletDefinitionAttributeSourceTest extends GroovyTestCase {
    void testGetAdditionalAttributesPortlet() {
        PortletDefinitionImpl portletDefn = new PortletDefinitionImpl(new PortletTypeImpl('name', 'cpdUri'),
            'theFname', 'name', 'title', 'webappName', 'portletName', false)
        IPortletDefinitionDao portletDefinitionDao = [
                getPortletDefinitionByFname: { String fname ->
                    assert 'theFname' == fname
                    portletDefn },
                ] as IPortletDefinitionDao
        PortletDefinitionAttributeSource testClass = new PortletDefinitionAttributeSource()
        testClass.setPortletDefinitionDao(portletDefinitionDao)
        XMLEventFactory factory = XMLEventFactory.newFactory()
        Attribute attr = factory.createAttribute('fname', 'theFname')
        StartElement element = factory.createStartElement('', '', 'channel',attr.iterator(), null)
        Iterator attrIterator = testClass.getAdditionalAttributes(null, null, element)
        Map attributeMap = [:]
        while (attrIterator.hasNext()) {
            Attribute attrItem = attrIterator.next()
            attributeMap[attrItem.getName().getLocalPart()] = attrItem.getValue()
        }
        assert attributeMap.size() == 3
        assert attributeMap[PortletDefinitionAttributeSource.PORTLET_NAME_ATTRIBUTE] == 'portletName'
        assert attributeMap[PortletDefinitionAttributeSource.WEBAPP_NAME_ATTRIBUTE] == 'webappName'
        assert attributeMap[PortletDefinitionAttributeSource.PORTLET_LIFECYCLE_ATTRIBUTE] == PortletLifecycleState.CREATED.toString()
    }

    void testGetAdditionalAttributesFrameworkPortlet() {
        PortletDefinitionImpl portletDefn = new PortletDefinitionImpl(new PortletTypeImpl('name', 'cpdUri'),
                'theFname', 'name', 'title', null, 'portletName', true,)
        IPortletDefinitionDao portletDefinitionDao = [
                getPortletDefinitionByFname: { String fname ->
                    assert 'theFname' == fname
                    portletDefn },] as IPortletDefinitionDao
        PortletDefinitionAttributeSource testClass = new PortletDefinitionAttributeSource()
        testClass.setPortletDefinitionDao(portletDefinitionDao)
        XMLEventFactory factory = XMLEventFactory.newFactory()
        Attribute attr = factory.createAttribute('fname', 'theFname')
        StartElement element = factory.createStartElement('', '', 'channel',attr.iterator(), null)
        Iterator attrIterator = testClass.getAdditionalAttributes(null, null, element)
        Map attributeMap = [:]
        while (attrIterator.hasNext()) {
            Attribute attrItem = attrIterator.next()
            attributeMap[attrItem.getName().getLocalPart()] = attrItem.getValue()
        }
        assert attributeMap.size() == 3
        assert attributeMap[PortletDefinitionAttributeSource.PORTLET_NAME_ATTRIBUTE] == 'portletName'
        assert attributeMap[PortletDefinitionAttributeSource.FRAMEWORK_PORTLET_ATTRIBUTE] == 'true'
    }

    void testGetAdditionalAttributesDoesNotThrowExceptionIfNoPortletDefinitionFound() {
        Object portletDefn = null
        IPortletDefinitionDao portletDefinitionDao = [
                getPortletDefinitionByFname: { String fname ->
                    assert 'theFname' == fname
                    portletDefn },] as IPortletDefinitionDao
        PortletDefinitionAttributeSource testClass = new PortletDefinitionAttributeSource()
        testClass.setPortletDefinitionDao(portletDefinitionDao)
        XMLEventFactory factory = XMLEventFactory.newFactory()
        Attribute attr = factory.createAttribute('fname', 'theFname')
        StartElement element = factory.createStartElement('', '', 'channel',attr.iterator(), null)
        Iterator attrIterator = testClass.getAdditionalAttributes(null, null, element)
    }

}
