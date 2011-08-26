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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.character.stream.CharacterEventBufferReader;
import org.jasig.portal.character.stream.CharacterEventReader;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.CharacterEventTypes;
import org.jasig.portal.character.stream.events.PortletContentPlaceholderEvent;
import org.jasig.portal.character.stream.events.PortletHeaderPlaceholderEvent;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.rendering.IPortletExecutionManager;
import org.jasig.portal.utils.cache.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initiates portlet rendering based each encountered {@link CharacterEventTypes#PORTLET_HEADER} and
 * {@link CharacterEventTypes#PORTLET_CONTENT} element in the event stream
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRenderingInitiationCharacterComponent extends CharacterPipelineComponentWrapper {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private IPortletExecutionManager portletExecutionManager;
    
    @Autowired
    public void setPortletExecutionManager(IPortletExecutionManager portletExecutionManager) {
        this.portletExecutionManager = portletExecutionManager;
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
    public PipelineEventReader<CharacterEventReader, CharacterEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final PipelineEventReader<CharacterEventReader, CharacterEvent> pipelineEventReader = this.wrappedComponent.getEventReader(request, response);

        final CharacterEventReader eventReader = pipelineEventReader.getEventReader();
        
        final List<CharacterEvent> eventBuffer = new LinkedList<CharacterEvent>();
        while (eventReader.hasNext()) {
            final CharacterEvent event = eventReader.next();
            
            switch (event.getEventType()) {
                case PORTLET_HEADER: {
                    final PortletHeaderPlaceholderEvent headerEvent = (PortletHeaderPlaceholderEvent)event;
                    final IPortletWindowId portletWindowId = headerEvent.getPortletWindowId();
                    
                    if (!this.portletExecutionManager.isPortletRenderHeaderRequested(portletWindowId, request, response)) {
                        this.portletExecutionManager.startPortletHeaderRender(portletWindowId, request, response);
                    }
                    
                    break;
                }
                case PORTLET_CONTENT: {
                    final PortletContentPlaceholderEvent headerEvent = (PortletContentPlaceholderEvent)event;
                    final IPortletWindowId portletWindowId = headerEvent.getPortletWindowId();
                    
                    if (!this.portletExecutionManager.isPortletRenderRequested(portletWindowId, request, response)) {
                        this.portletExecutionManager.startPortletRender(portletWindowId, request, response);
                    }
                    
                    break;
                }
            }
            
            eventBuffer.add(event);
        }
        
        final Map<String, String> outputProperties = pipelineEventReader.getOutputProperties();
        final CharacterEventBufferReader bufferEventReader = new CharacterEventBufferReader(eventBuffer.listIterator());
        return new PipelineEventReaderImpl<CharacterEventReader, CharacterEvent>(bufferEventReader, outputProperties);
    }
}
