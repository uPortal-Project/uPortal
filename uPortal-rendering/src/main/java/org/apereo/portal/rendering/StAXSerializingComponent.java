/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.rendering;

import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import org.apereo.portal.character.stream.CharacterEventBufferReader;
import org.apereo.portal.character.stream.CharacterEventReader;
import org.apereo.portal.character.stream.CharacterEventSource;
import org.apereo.portal.character.stream.events.CharacterEvent;
import org.apereo.portal.utils.cache.CacheKey;
import org.apereo.portal.xml.XmlUtilities;
import org.apereo.portal.xml.stream.ChunkingEventReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Converts a StAX event stream into a {@link CharacterEvent} stream. Breaking up the stream into
 * chunks based on specific elements
 *
 */
public class StAXSerializingComponent implements CharacterPipelineComponent {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private XmlUtilities xmlUtilities;

    private StAXPipelineComponent wrappedComponent;
    private Map<String, CharacterEventSource> chunkingElements;
    private Map<Pattern, CharacterEventSource> chunkingPatternEventSources;
    private Pattern[] chunkingPatterns;

    @Autowired
    public void setXmlUtilities(XmlUtilities xmlUtilities) {
        this.xmlUtilities = xmlUtilities;
    }

    public void setWrappedComponent(StAXPipelineComponent wrappedComponent) {
        this.wrappedComponent = wrappedComponent;
    }

    public void setChunkingElements(Map<String, CharacterEventSource> chunkingElements) {
        this.chunkingElements = chunkingElements;
    }

    public void setChunkingPatterns(Map<String, CharacterEventSource> chunkingPatterns) {
        final Map<Pattern, CharacterEventSource> compiledChunkingPatternEventSources =
                new LinkedHashMap<Pattern, CharacterEventSource>();

        for (final Map.Entry<String, CharacterEventSource> chunkingPatternEntry :
                chunkingPatterns.entrySet()) {
            final String key = chunkingPatternEntry.getKey();
            final Pattern pattern = Pattern.compile(key);
            final CharacterEventSource value = chunkingPatternEntry.getValue();
            compiledChunkingPatternEventSources.put(pattern, value);
        }

        this.chunkingPatternEventSources = compiledChunkingPatternEventSources;
        this.chunkingPatterns =
                this.chunkingPatternEventSources
                        .keySet()
                        .toArray(new Pattern[this.chunkingPatternEventSources.size()]);
    }

    @Override
    public PipelineEventReader<CharacterEventReader, CharacterEvent> getEventReader(
            HttpServletRequest request, HttpServletResponse response) {
        final PipelineEventReader<XMLEventReader, XMLEvent> eventReader =
                this.wrappedComponent.getEventReader(request, response);

        //Writer shared by the ChunkingEventReader and the StAX Serializer
        final StringWriter writer = new StringWriter();

        final XMLOutputFactory outputFactory = this.xmlUtilities.getHtmlOutputFactory();
        final XMLEventWriter xmlEventWriter;
        try {
            xmlEventWriter = outputFactory.createXMLEventWriter(writer);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to create XMLEventWriter", e);
        }

        //Add the chunking wrapper to the XMLEventReader
        final XMLEventReader xmlEventReader = eventReader.getEventReader();
        final ChunkingEventReader chunkingEventReader =
                new ChunkingEventReader(
                        request,
                        this.chunkingElements,
                        this.chunkingPatternEventSources,
                        this.chunkingPatterns,
                        xmlEventReader,
                        xmlEventWriter,
                        writer);

        try {
            xmlEventWriter.add(chunkingEventReader);
            xmlEventWriter.flush();
            xmlEventWriter.close();
            chunkingEventReader.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write events to Writer", e);
        }

        //Return the chunked data
        final List<CharacterEvent> characterEvents = chunkingEventReader.getCharacterEvents();
        final CharacterEventBufferReader characterEventReader =
                new CharacterEventBufferReader(characterEvents.listIterator());
        final Map<String, String> outputProperties = eventReader.getOutputProperties();
        return new PipelineEventReaderImpl<CharacterEventReader, CharacterEvent>(
                characterEventReader, outputProperties);
    }

    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        return this.wrappedComponent.getCacheKey(request, response);
    }
}
