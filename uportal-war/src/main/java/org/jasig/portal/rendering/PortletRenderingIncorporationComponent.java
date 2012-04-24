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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.character.stream.CharacterEventReader;
import org.jasig.portal.character.stream.FilteringCharacterEventReader;
import org.jasig.portal.character.stream.events.CharacterDataEventImpl;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.PortletContentPlaceholderEvent;
import org.jasig.portal.character.stream.events.PortletHeaderPlaceholderEvent;
import org.jasig.portal.character.stream.events.PortletLinkPlaceholderEvent;
import org.jasig.portal.character.stream.events.PortletNewItemCountPlaceholderEvent;
import org.jasig.portal.character.stream.events.PortletTitlePlaceholderEvent;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.rendering.IPortletExecutionManager;
import org.jasig.portal.utils.cache.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Inserts the results of portlet's rendering into the character stream
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRenderingIncorporationComponent extends CharacterPipelineComponentWrapper {
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
        /*
         * TODO do all the portlet cache keys need to be included here?
         * Probably for this to be useful
         */
        return this.wrappedComponent.getCacheKey(request, response);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.PipelineComponent#getEventReader(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public PipelineEventReader<CharacterEventReader, CharacterEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final PipelineEventReader<CharacterEventReader, CharacterEvent> pipelineEventReader = this.wrappedComponent.getEventReader(request, response);
        
        final CharacterEventReader eventReader = pipelineEventReader.getEventReader();
        final PortletIncorporatingEventReader portletIncorporatingEventReader = new PortletIncorporatingEventReader(eventReader, request, response);
        
        final Map<String, String> outputProperties = pipelineEventReader.getOutputProperties();
        return new PipelineEventReaderImpl<CharacterEventReader, CharacterEvent>(portletIncorporatingEventReader, outputProperties);
    }

    private class PortletIncorporatingEventReader extends FilteringCharacterEventReader {
        private final HttpServletRequest request;
        private final HttpServletResponse response;
        
        public PortletIncorporatingEventReader(CharacterEventReader delegate, HttpServletRequest request, HttpServletResponse response) {
            super(delegate);
            this.request = request;
            this.response = response;
        }

        @Override
        protected CharacterEvent filterEvent(CharacterEvent event, boolean peek) {
            switch (event.getEventType()) {
            	case PORTLET_HEADER: {
            		final PortletHeaderPlaceholderEvent headerPlaceholderEvent = (PortletHeaderPlaceholderEvent) event;
            		final IPortletWindowId portletWindowId = headerPlaceholderEvent.getPortletWindowId();
            		
            		final String output = portletExecutionManager.getPortletHeadOutput(portletWindowId, this.request, this.response);
            		
            		return CharacterDataEventImpl.create(output);
            	}
                case PORTLET_CONTENT: {
                    final PortletContentPlaceholderEvent contentPlaceholderEvent = (PortletContentPlaceholderEvent)event;
                    final IPortletWindowId portletWindowId = contentPlaceholderEvent.getPortletWindowId();
                    
                    final String output = portletExecutionManager.getPortletOutput(portletWindowId, this.request, this.response);
                    
                    return CharacterDataEventImpl.create(output);
                }
                case PORTLET_TITLE: {
                    final PortletTitlePlaceholderEvent titlePlaceholderEvent = (PortletTitlePlaceholderEvent)event;
                    final IPortletWindowId portletWindowId = titlePlaceholderEvent.getPortletWindowId();
                    
                    final String title = portletExecutionManager.getPortletTitle(portletWindowId, this.request, this.response);
                    
                    return CharacterDataEventImpl.create(title);
                }
                case PORTLET_NEW_ITEM_COUNT: {
                    final PortletNewItemCountPlaceholderEvent newItemCountPlaceholderEvent = (PortletNewItemCountPlaceholderEvent)event;
                    final IPortletWindowId portletWindowId = newItemCountPlaceholderEvent.getPortletWindowId();
                    
                    final int newItemCount = portletExecutionManager.getPortletNewItemCount(portletWindowId, this.request, this.response);
                    
                    return CharacterDataEventImpl.create(String.valueOf(newItemCount));
                }
                case PORTLET_LINK: {
                    final PortletLinkPlaceholderEvent linkPlaceholderEvent = (PortletLinkPlaceholderEvent)event;
                    final IPortletWindowId portletWindowId = linkPlaceholderEvent.getPortletWindowId();
                    final String defaultPortletUrl = linkPlaceholderEvent.getDefaultPortletUrl();
                    
                    final String link = portletExecutionManager.getPortletLink(portletWindowId, defaultPortletUrl, this.request, this.response);
                    
                    return CharacterDataEventImpl.create(link);
                }
            }

            
            return event;
        }
    }
}
