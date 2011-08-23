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

package org.jasig.portal.rendering;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletExecutionManager;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.xml.stream.FilteringXMLEventReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Iterators;

/**
 * Initiates portlet rendering based each encountered {@link XMLPipelineConstants#PORTLET} element in the
 * event stream
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRenderingInitiationComponent extends StAXPipelineComponentWrapper {
    public static final QName PORTLET_WINDOW_ID_ATTR_NAME = new QName("portletWindowId");
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
    private IPortletExecutionManager portletExecutionManager;
    private IUrlSyntaxProvider urlSyntaxProvider;
    private IPortletWindowRegistry portletWindowRegistry;
    
    @Autowired
    public void setPortletExecutionManager(IPortletExecutionManager portletExecutionManager) {
        this.portletExecutionManager = portletExecutionManager;
    }
    
    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }
    
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.PipelineComponent#getCacheKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        //Initiating rendering of portlets will change the stream at all
        return this.wrappedComponent.getCacheKey(request, response);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.PipelineComponent#getEventReader(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public PipelineEventReader<XMLEventReader, XMLEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final PipelineEventReader<XMLEventReader, XMLEvent> pipelineEventReader = this.wrappedComponent.getEventReader(request, response);

        final XMLEventReader eventReader = pipelineEventReader.getEventReader();
        final PortletRenderingXMLEventReader filteredEventReader = new PortletRenderingXMLEventReader(request, response, eventReader);
        
        final Map<String, String> outputProperties = pipelineEventReader.getOutputProperties();
        return new PipelineEventReaderImpl<XMLEventReader, XMLEvent>(filteredEventReader, outputProperties);
    }

    private class PortletRenderingXMLEventReader extends FilteringXMLEventReader {
        private final HttpServletRequest request;
        private final HttpServletResponse response;
        
        public PortletRenderingXMLEventReader(HttpServletRequest request, HttpServletResponse response, XMLEventReader reader) {
            super(reader);
            this.request = request;
            this.response = response;
        }

        @Override
        protected XMLEvent filterEvent(XMLEvent event, boolean peek) {
        	if (event.isStartElement()) {
                final StartElement startElement = event.asStartElement();
                
                final QName name = startElement.getName();
                final String localName = name.getLocalPart();
                if (IUserLayoutManager.CHANNEL.equals(localName)) {
                    final IPortletWindow portletWindow = getPortletWindow(request, startElement);
					if (portletWindow == null) {
						logger.warn("No portlet window found for: "  + localName);
						return null;
					}
                    
                    final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();

                    if (!portletExecutionManager.isPortletRenderRequested(portletWindowId, this.request, this.response)) {
                        portletExecutionManager.startPortletRender(portletWindowId, this.request, this.response);
                        logger.debug("Initiated portlet markup rendering for: {}", portletWindow);
                    }
                    else {
                        logger.debug("Portlet render already requested for: {}", portletWindow);
                    }
                    
                    event = addPortletWindowId(startElement, portletWindowId);
                }
                else if(IUserLayoutManager.CHANNEL_HEADER.equals(localName)) {
                	final IPortletWindow portletWindow = getPortletWindow(request, startElement);
					if (portletWindow == null) {
						logger.warn("No portlet window found for: " + localName);
						return null;
					}

                    final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();

                    if (!portletExecutionManager.isPortletRenderHeaderRequested(portletWindowId, this.request, this.response)) {
                        portletExecutionManager.startPortletHeadRender(portletWindowId, this.request, this.response);
                        logger.debug("Initiated portlet head rendering for: {}", portletWindow);
                    }
                    else {
                        logger.debug("Portlet header render already requested for: {}", portletWindow);
                    }
                     
                    event = addPortletWindowId(startElement, portletWindowId);
                }
            } 
            
            return event;
        }
    }
    
    protected XMLEvent addPortletWindowId(StartElement element, IPortletWindowId portletWindowId) {
        //If the id attribute exists don't need to do anything
        Attribute windowIdAttribute = element.getAttributeByName(PORTLET_WINDOW_ID_ATTR_NAME);
        if (windowIdAttribute != null) {
            return element;
        }
        
        windowIdAttribute = xmlEventFactory.createAttribute(PORTLET_WINDOW_ID_ATTR_NAME, portletWindowId.getStringId());
        
        //Clone the start element to add the new attribute
        final QName name = element.getName();
        final String prefix = name.getPrefix();
        final String namespaceURI = name.getNamespaceURI();
        final String localPart = name.getLocalPart();
        @SuppressWarnings("unchecked")
        final Iterator<Attribute> attributes = element.getAttributes();
        @SuppressWarnings("unchecked")
        final Iterator<Namespace> namespaces = element.getNamespaces();
        final NamespaceContext namespaceContext = element.getNamespaceContext();
        
        //Create a new iterator of the existing attributes + the new window id attribute
        final Iterator<Attribute> newAttributes = Iterators.concat(attributes, Iterators.forArray(windowIdAttribute));
        
        return xmlEventFactory.createStartElement(prefix, namespaceURI, localPart, newAttributes, namespaces, namespaceContext);
    }

    protected IPortletWindow getPortletWindow(HttpServletRequest request, StartElement element) {
        //Check if the layout node explicitly specifies the window id
        final Attribute windowIdAttribute = element.getAttributeByName(PORTLET_WINDOW_ID_ATTR_NAME);
        if (windowIdAttribute != null) {
            final String windowIdStr = windowIdAttribute.getValue();
            final IPortletWindowId portletWindowId = this.portletWindowRegistry.getPortletWindowId(request, windowIdStr);
            return this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        }
        
        //No explicit window id, look it up based on the layout node id
        final Attribute nodeIdAttribute = element.getAttributeByName(IUserLayoutManager.ID_ATTR_NAME);
        final String layoutNodeId = nodeIdAttribute.getValue();

        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(request, layoutNodeId);
        
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(request);
        switch (portalRequestInfo.getUrlState()) {
            //Handle detached portlets explicitly
            //TODO Can we ever have non-targeted portlets render in a detached request? If so should they all be stateless windows anyways? 
            case DETACHED: {
                final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
                final IPortletWindow statelessPortletWindow = this.portletWindowRegistry.getOrCreateStatelessPortletWindow(request, portletWindowId);
                return statelessPortletWindow;
            }
            default: {
                return portletWindow;
            }
        }
    }
}
