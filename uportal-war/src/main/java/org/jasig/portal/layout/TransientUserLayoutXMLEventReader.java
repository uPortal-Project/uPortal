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

package org.jasig.portal.layout;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.xml.stream.InjectingXMLEventReader;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TransientUserLayoutXMLEventReader extends InjectingXMLEventReader {
    private static final XMLEventFactory EVENT_FACTORY = XMLEventFactory.newFactory();

    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final TransientUserLayoutManagerWrapper userLayoutManager;
    private final String rootFolderId;
    
    
    public TransientUserLayoutXMLEventReader(TransientUserLayoutManagerWrapper userLayoutManager,
            XMLEventReader wrappedReader) {
        super(wrappedReader);
        this.userLayoutManager = userLayoutManager;
        this.rootFolderId = this.userLayoutManager.getRootFolderId();
    }
    
    @Override
    protected Deque<XMLEvent> getAdditionalEvents(XMLEvent event) {
        if (event.isStartElement()) {
            final StartElement startElement = event.asStartElement();
            
            //All following logic requires an ID attribute, ignore any element without one
            final Attribute idAttribute = startElement.getAttributeByName(IUserLayoutManager.ID_ATTR_NAME);
            if (idAttribute == null) {
                return null;
            }
            
            //Handle adding a transient folder to the root element
            if (this.rootFolderId.equals(idAttribute.getValue())) {
                final QName name = startElement.getName();
                final String namespaceURI = name.getNamespaceURI();
                final String prefix = name.getPrefix();
                
                final Deque<XMLEvent> transientEventBuffer = new LinkedList<XMLEvent>();
                
                final Collection<Attribute> transientFolderAttributes = new LinkedList<Attribute>();
                transientFolderAttributes.add(EVENT_FACTORY.createAttribute("ID", TransientUserLayoutManagerWrapper.TRANSIENT_FOLDER_ID));
                transientFolderAttributes.add(EVENT_FACTORY.createAttribute("type", "regular"));
                transientFolderAttributes.add(EVENT_FACTORY.createAttribute("hidden", "true"));
                transientFolderAttributes.add(EVENT_FACTORY.createAttribute("unremovable", "true"));
                transientFolderAttributes.add(EVENT_FACTORY.createAttribute("immutable", "true"));
                transientFolderAttributes.add(EVENT_FACTORY.createAttribute("name", "Transient Folder"));
                
                final StartElement transientFolder = EVENT_FACTORY.createStartElement(prefix, namespaceURI, IUserLayoutManager.FOLDER, transientFolderAttributes.iterator(), null);
                transientEventBuffer.add(transientFolder);

                //append channel element iff subscribeId describes a transient channel, and not a regular layout channel
                final String subscribeId = this.userLayoutManager.getFocusedId();
                if (null != subscribeId && !subscribeId.equals("") && this.userLayoutManager.isTransientChannel(subscribeId)) {
                    IPortletDefinition chanDef = null;
                    try {
                        chanDef = this.userLayoutManager.getChannelDefinition(subscribeId);
                    }
                    catch (Exception e) {
                        this.logger.error("Could not obtain IChannelDefinition for subscribe id: " + subscribeId, e);
                    }
                    
                    if (chanDef != null) {
                        //TODO Move IChannelDefinition/IPortletDefinition -> StAX events code somewhere reusable
                        final Collection<Attribute> channelAttrs = new LinkedList<Attribute>();
                        channelAttrs.add(EVENT_FACTORY.createAttribute("ID", subscribeId));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("typeID", Integer.toString(chanDef.getType().getId())));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("hidden", "false"));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("unremovable", "true"));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("name", chanDef.getName()));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("description", chanDef.getDescription()));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("title", chanDef.getTitle()));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("chanID", chanDef.getPortletDefinitionId().getStringId()));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("fname", chanDef.getFName()));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("timeout", Integer.toString(chanDef.getTimeout())));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("transient", "true"));

                        final StartElement startChannel = EVENT_FACTORY.createStartElement(prefix, namespaceURI, IUserLayoutManager.CHANNEL, channelAttrs.iterator(), null);
                        transientEventBuffer.offer(startChannel);

                        // add channel parameter elements
                        for(final IPortletDefinitionParameter parm : chanDef.getParameters())
                        {
                            final Collection<Attribute> parameterAttrs = new LinkedList<Attribute>();
                            parameterAttrs.add(EVENT_FACTORY.createAttribute("name",parm.getName()));
                            parameterAttrs.add(EVENT_FACTORY.createAttribute("value",parm.getValue()));

                            final StartElement startParameter = EVENT_FACTORY.createStartElement(prefix, namespaceURI, IUserLayoutManager.PARAMETER, parameterAttrs.iterator(), null);
                            transientEventBuffer.offer(startParameter);
                            
                            final EndElement endParameter = EVENT_FACTORY.createEndElement(prefix, namespaceURI, IUserLayoutManager.PARAMETER, null);
                            transientEventBuffer.offer(endParameter);
                        }

                        final EndElement endChannel = EVENT_FACTORY.createEndElement(prefix, namespaceURI, IUserLayoutManager.CHANNEL, null);
                        transientEventBuffer.offer(endChannel);
                    }
                }
                
                final EndElement endFolder = EVENT_FACTORY.createEndElement(prefix, namespaceURI, IUserLayoutManager.FOLDER, null);
                transientEventBuffer.offer(endFolder);
                
                return transientEventBuffer;
            }
        }
        
        return null;
    }


    @Override
    protected XMLEvent getPeekEvent(XMLEvent event) {
        //Not the most efficient way of doing this since the whole deque is built but we only need the first element.
        final Deque<XMLEvent> additionalEvents = this.getAdditionalEvents(event);
        if (additionalEvents != null) {
            return additionalEvents.pop();
        }
        return null;
    }
}
