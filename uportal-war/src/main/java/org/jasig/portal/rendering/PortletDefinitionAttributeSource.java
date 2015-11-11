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
package org.jasig.portal.rendering;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDescriptorKey;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.utils.cache.CacheKey.CacheKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base implementation of portlet definition source that adds the
 * portlet webapp, name, and framework portlet info
 * 
 * @author James Wennmacher jwennmacher@unicon.net
 */
public class PortletDefinitionAttributeSource implements AttributeSource, BeanNameAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static final QName PORTLET_FNAME_ATTR_NAME = new QName("fname");
    public static final String WEBAPP_NAME_ATTRIBUTE = "webAppName";
    public static final String PORTLET_NAME_ATTRIBUTE = "portletName";
    public static final String FRAMEWORK_PORTLET_ATTRIBUTE = "frameworkPortlet";

    private final XMLEventFactory xmlEventFactory = XMLEventFactory.newFactory();
    private IPortletDefinitionDao portletDefinitionDao;
    private IPortletWindowRegistry portletWindowRegistry;
    private String name;

    @Autowired
    public void setPortletDefinitionDao(IPortletDefinitionDao portletDefinitionDao) {
        this.portletDefinitionDao = portletDefinitionDao;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    @Override
    public final Iterator<Attribute> getAdditionalAttributes(HttpServletRequest request, HttpServletResponse response, StartElement event) {

        final QName eventName = event.getName();
        final String localEventName = eventName.getLocalPart();

        //Only pay attention to channel events
        if (!IUserLayoutManager.CHANNEL.equals(localEventName)) {
            return null;
        }

        final Collection<Attribute> attributes = new LinkedList<Attribute>();

        // Add the portlet's portlet name and either the webapp URL or framework flag to the list of attributes.
        final Attribute fnameAttribute = event.getAttributeByName(PORTLET_FNAME_ATTR_NAME);
        if (fnameAttribute != null) {
            final String fname = fnameAttribute.getValue();
            IPortletDefinition def = portletDefinitionDao.getPortletDefinitionByFname(fname);
            IPortletDescriptorKey descriptorKey = def.getPortletDescriptorKey();
            attributes.add(xmlEventFactory.createAttribute(PORTLET_NAME_ATTRIBUTE, descriptorKey.getPortletName()));
            if (descriptorKey.isFrameworkPortlet()) {
                attributes.add(xmlEventFactory.createAttribute(FRAMEWORK_PORTLET_ATTRIBUTE, "true"));
            } else {
                attributes.add(xmlEventFactory.createAttribute(WEBAPP_NAME_ATTRIBUTE, descriptorKey.getWebAppName()));
            }
        }

        return attributes.iterator();
    }

    @Override
    public final CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final CacheKeyBuilder cacheKeyBuilder = CacheKey.builder(this.name);

        // We need something that makes for a unique cache key.  For lack of anything else useful,
        // use the set of all layout portlet window ids in the user's layout.
        final Set<IPortletWindow> portletWindows = this.portletWindowRegistry.getAllLayoutPortletWindows(request);

        for (final IPortletWindow portletWindow : portletWindows) {
            if(portletWindow != null) {
                final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
                cacheKeyBuilder.add(portletWindowId);
            } else {
                this.logger.warn("portletWindowRegistry#getAllLayoutPortletWindows() returned a null portletWindow");
            }
        }

        return cacheKeyBuilder.build();
    }

}