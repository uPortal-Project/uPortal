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
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.xml.stream.InjectingXMLEventReader;

/**
 * Injects an {@link DTD} event to the XML Event Reader. The default DTD is:
 * <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DTDSettingPipelineComponent extends StAXPipelineComponentWrapper {
    private final static XMLEventFactory EVENT_FACTORY = XMLEventFactory.newFactory();
    
    private DTD dtdEvent = EVENT_FACTORY.createDTD("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
    
    public void setDTD(String dtd) {
        this.dtdEvent = EVENT_FACTORY.createDTD(dtd);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.PipelineComponent#getCacheKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        return this.wrappedComponent.getCacheKey(request, response);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.PipelineComponent#getEventReader(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public PipelineEventReader<XMLEventReader, XMLEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final PipelineEventReader<XMLEventReader, XMLEvent> pipelineEventReader = this.wrappedComponent.getEventReader(request, response);
        final DTDAddingXMLEventReader eventReader = new DTDAddingXMLEventReader(pipelineEventReader.getEventReader());
        final Map<String, String> outputProperties = pipelineEventReader.getOutputProperties();
        return new PipelineEventReaderImpl<XMLEventReader, XMLEvent>(eventReader, outputProperties);
    }

    private class DTDAddingXMLEventReader extends InjectingXMLEventReader {
        private boolean documentStarted = false;
        private boolean dtdWritten = false;

        public DTDAddingXMLEventReader(XMLEventReader reader) {
            super(reader);
        }

        @Override
        protected XMLEvent getAdditionalEvent(XMLEvent event) {
            if (this.documentStarted && !this.dtdWritten) {
                this.dtdWritten = true;
                return dtdEvent;
            }
            
            if (event.isStartDocument()) {
                this.documentStarted = true;
            }
            
            return null;
        }


        @Override
        protected XMLEvent getPeekEvent(XMLEvent event) {
            if (this.documentStarted && !this.dtdWritten) {
                return dtdEvent;
            }
            
            return null;
        }
    }
}
