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
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.utils.cache.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs the Character events
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LoggingCharacterComponent extends CharacterPipelineComponentWrapper {
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    public void setLoggerName(String loggerName) {
        logger = LoggerFactory.getLogger(loggerName);
    }

    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        return this.wrappedComponent.getCacheKey(request, response);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.PipelineComponent#getEventReader(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public PipelineEventReader<CharacterEventReader, CharacterEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final PipelineEventReader<CharacterEventReader, CharacterEvent> pipelineEventReader = this.wrappedComponent.getEventReader(request, response);
        
        final CharacterEventReader eventReader = pipelineEventReader.getEventReader();
        
        final LoggingCharacterEventReader loggingEventReader = new LoggingCharacterEventReader(eventReader);
        
        final Map<String, String> outputProperties = pipelineEventReader.getOutputProperties();
        return new PipelineEventReaderImpl<CharacterEventReader, CharacterEvent>(loggingEventReader, outputProperties);
    }
    
    private class LoggingCharacterEventReader extends FilteringCharacterEventReader {
        
        public LoggingCharacterEventReader(CharacterEventReader delegate) {
            super(delegate);
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.character.stream.FilteringCharacterEventReader#filterEvent(org.jasig.portal.character.stream.events.CharacterEvent, boolean)
         */
        @Override
        protected CharacterEvent filterEvent(CharacterEvent event, boolean peek) {
            if (logger.isDebugEnabled()) {
                logger.debug(event.toString());
            }

            return event;
        }
    }
}
