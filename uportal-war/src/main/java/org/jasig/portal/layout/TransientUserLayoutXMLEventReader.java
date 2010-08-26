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
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelParameter;
import org.jasig.portal.rendering.XMLPipelineConstants;
import org.jasig.portal.xml.stream.BaseXMLEventReader;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TransientUserLayoutXMLEventReader extends BaseXMLEventReader {
    private static final XMLEventFactory EVENT_FACTORY = XMLPipelineConstants.XML_EVENT_FACTORY;

    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final TransientUserLayoutManagerWrapper userLayoutManager;
    private final XMLEventReader wrappedReader;
    private final String rootFolderId;
    
    private final Queue<XMLEvent> transientEventBuffer = new LinkedList<XMLEvent>();
    private XMLEvent previousEvent;
    
    
    public TransientUserLayoutXMLEventReader(TransientUserLayoutManagerWrapper userLayoutManager,
            XMLEventReader wrappedReader) {
        this.userLayoutManager = userLayoutManager;
        this.wrappedReader = wrappedReader;
        this.rootFolderId = this.userLayoutManager.getRootFolderId();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.xml.stream.BaseXMLEventReader#getPreviousEvent()
     */
    @Override
    protected XMLEvent getPreviousEvent() {
        return this.previousEvent;
    }

    public void close() throws XMLStreamException {
        this.wrappedReader.close();
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        return this.wrappedReader.getProperty(name);
    }

    public boolean hasNext() {
        return this.wrappedReader.hasNext();
    }

    public XMLEvent nextEvent() throws XMLStreamException {
        //Read from the buffer first
        if (!this.transientEventBuffer.isEmpty()) {
            final XMLEvent event = transientEventBuffer.poll();
            this.previousEvent = event;
            return event;
        }
        
        XMLEvent event = this.wrappedReader.nextEvent();
        
        if (event.isStartElement()) {
            final StartElement startElement = event.asStartElement();
            
            //All following logic requires an ID attribute, ignore any element without one
            final Attribute idAttribute = startElement.getAttributeByName(XMLPipelineConstants.ID_ATTR_NAME);
            if (idAttribute == null) {
                return event;
            }
            
            //Handle adding a transient folder to the root element
            if (this.rootFolderId.equals(idAttribute.getValue())) {
                final Collection<Attribute> transientFolderAttributes = new LinkedList<Attribute>();
                transientFolderAttributes.add(EVENT_FACTORY.createAttribute("ID", TransientUserLayoutManagerWrapper.TRANSIENT_FOLDER_ID));
                transientFolderAttributes.add(EVENT_FACTORY.createAttribute("type", "regular"));
                transientFolderAttributes.add(EVENT_FACTORY.createAttribute("hidden", "true"));
                transientFolderAttributes.add(EVENT_FACTORY.createAttribute("unremovable", "true"));
                transientFolderAttributes.add(EVENT_FACTORY.createAttribute("immutable", "true"));
                transientFolderAttributes.add(EVENT_FACTORY.createAttribute("name", "Transient Folder"));
                
                final StartElement transientFolder = EVENT_FACTORY.createStartElement(XMLPipelineConstants.FOLDER, transientFolderAttributes.iterator(), null);
                this.transientEventBuffer.add(transientFolder);

                //append channel element iff subscribeId describes a transient channel, and not a regular layout channel
                final String subscribeId = this.userLayoutManager.getFocusedId();
                if (null != subscribeId && !subscribeId.equals("") && this.userLayoutManager.isTransientChannel(subscribeId)) {
                    IChannelDefinition chanDef = null;
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
                        channelAttrs.add(EVENT_FACTORY.createAttribute("editable", Boolean.toString(chanDef.isEditable())));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("unremovable", "true"));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("name", chanDef.getName()));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("description", chanDef.getDescription()));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("title", chanDef.getTitle()));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("class", chanDef.getJavaClass()));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("chanID", Integer.toString(chanDef.getId())));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("fname", chanDef.getFName()));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("timeout", Integer.toString(chanDef.getTimeout())));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("hasHelp", Boolean.toString(chanDef.hasHelp())));
                        channelAttrs.add(EVENT_FACTORY.createAttribute("hasAbout", Boolean.toString(chanDef.hasAbout())));

                        final StartElement startChannel = EVENT_FACTORY.createStartElement(XMLPipelineConstants.CHANNEL, channelAttrs.iterator(), null);
                        this.transientEventBuffer.add(startChannel);

                        // add channel parameter elements
                        for(final IChannelParameter parm : chanDef.getParameters())
                        {
                            final Collection<Attribute> parameterAttrs = new LinkedList<Attribute>();
                            parameterAttrs.add(EVENT_FACTORY.createAttribute("name",parm.getName()));
                            parameterAttrs.add(EVENT_FACTORY.createAttribute("value",parm.getValue()));

                            final StartElement startParameter = EVENT_FACTORY.createStartElement(XMLPipelineConstants.PARAMETER, parameterAttrs.iterator(), null);
                            this.transientEventBuffer.add(startParameter);
                            
                            final EndElement endParameter = EVENT_FACTORY.createEndElement(XMLPipelineConstants.PARAMETER, null);
                            this.transientEventBuffer.add(endParameter);
                        }

                        final EndElement endChannel = EVENT_FACTORY.createEndElement(XMLPipelineConstants.CHANNEL, null);
                        this.transientEventBuffer.add(endChannel);
                    }
                }
                
                final EndElement endFolder = EVENT_FACTORY.createEndElement(XMLPipelineConstants.FOLDER, null);
                this.transientEventBuffer.add(endFolder);
            }
        }
        
        this.previousEvent = event;
        return event;
    }

    public XMLEvent peek() throws XMLStreamException {
        if (!this.transientEventBuffer.isEmpty()) {
            return transientEventBuffer.peek();
        }

        return this.wrappedReader.peek();
    }

    public void remove() {
        this.wrappedReader.remove();
    }
}
