package org.jasig.portal.xml.stream;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.character.stream.CharacterEventSource;
import org.jasig.portal.character.stream.events.CharacterDataEvent;
import org.jasig.portal.character.stream.events.CharacterDataEventImpl;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.CharacterEventTypes;

/**
 * Used with code that serializes StAX events into a string. Watches for specific XML tags in a StAX
 * stream and chunks the string data at each XML tag occurrence.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ChunkingEventReader extends BaseXMLEventReader implements XMLEventReader {
    private final List<CharacterEvent> characterEvents = new LinkedList<CharacterEvent>();

    private final Map<QName, CharacterEventSource> chunkingElements;
    private final Map<Pattern, CharacterEventSource> chunkingPatterns;
    private final XMLEventReader xmlEventReader;
    private final StringWriter writer;
    
    //Declare this at the class level to reduce excess object creation
    private final StringBuffer characterChunkingBuffer = new StringBuffer();
    
    //to handle peek() calls
    private XMLEvent peekedEvent = null;
    private XMLEvent previousEvent = null;

    public ChunkingEventReader(Map<QName, CharacterEventSource> chunkingElements,
            Map<Pattern, CharacterEventSource> chunkingPatterns, 
            XMLEventReader xmlEventReader, StringWriter writer) {

        this.chunkingElements = chunkingElements;
        this.chunkingPatterns = chunkingPatterns;
        this.xmlEventReader = xmlEventReader;
        this.writer = writer;
    }

    public List<CharacterEvent> getCharacterEvents() {
        return this.characterEvents;
    }
    
    @Override
    protected XMLEvent getPreviousEvent() {
        return this.previousEvent;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLEvent peek() throws XMLStreamException {
        if (this.peekedEvent == null) {
            this.peekedEvent = this.nextEvent();
        }
        
        return this.peekedEvent;
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        XMLEvent event = null;
        
        //Read from the buffer if there was a peek
        if (this.peekedEvent != null) {
            event = this.peekedEvent;
            this.peekedEvent = null;
            return event;
        }
        
        event = this.xmlEventReader.nextEvent();
        if (event.isStartElement()) {
            final StartElement startElement = event.asStartElement();
            
            final QName elementName = startElement.getName();
            final CharacterEventSource characterEventSource = this.chunkingElements.get(elementName);
            if (characterEventSource != null) {
                //Capture the characters written out to this point then clear the buffer
                this.captureCharacterDataEvent();
                
                //Get the generated events for the element
                final List<CharacterEvent> generatedCharacterEvents = characterEventSource.getCharacterEvents(this.xmlEventReader, startElement);
                this.characterEvents.addAll(generatedCharacterEvents);
                
                //Read the next event off the reader
                event = this.xmlEventReader.nextEvent();
            }
        }
        
        this.previousEvent = event;
        return event;
    }

    protected void captureCharacterDataEvent() {
        //Add character chunk to events
        final String chunk = this.writer.toString();
        
        final List<CharacterEvent> characterEvents = this.chunkString(chunk);
        
        //Add all the characterEvents
        this.characterEvents.addAll(characterEvents);
        
        //Clear character buffer
        final StringBuffer buffer = this.writer.getBuffer();
        buffer.delete(0, buffer.length());
    }

    /**
     * Breaks up the String into a List of CharacterEvents based on the configured Map of Patterns to 
     * CharacterEventSources
     */
    protected List<CharacterEvent> chunkString(final String chunk) {
        final List<CharacterEvent> characterEvents = new LinkedList<CharacterEvent>();
        characterEvents.add(new CharacterDataEventImpl(chunk));

        //Iterate over the chunking patterns
        for (final Map.Entry<Pattern, CharacterEventSource> chunkingPatternEntry : this.chunkingPatterns.entrySet()) {
            final Pattern pattern = chunkingPatternEntry.getKey();

            //Iterate over the events that have been chunked so far
            for (final ListIterator<CharacterEvent> characterDataEventItr = characterEvents.listIterator(); characterDataEventItr.hasNext(); ) {
                final CharacterEvent characterDataEvent = characterDataEventItr.next();
                
                //If it is a character event it may need further chunking
                if (CharacterEventTypes.CHARACTER == characterDataEvent.getEventType()) {
                    final String data = ((CharacterDataEvent)characterDataEvent).getData();
                    
                    final Matcher matcher = pattern.matcher(data);
                    if (matcher.find()) {
                        //Found a match, replacing the CharacterDataEvent that was found
                        characterDataEventItr.remove();

                        do {
                            //Add all of the text up to the match as a new chunk
                            characterChunkingBuffer.delete(0, characterChunkingBuffer.length());
                            matcher.appendReplacement(characterChunkingBuffer, "");
                            characterDataEventItr.add(new CharacterDataEventImpl(characterChunkingBuffer.toString()));

                            //Get the generated CharacterEvents for the match
                            final CharacterEventSource eventSource = chunkingPatternEntry.getValue();
                            final MatchResult matchResult = matcher.toMatchResult();
                            final List<CharacterEvent> generatedCharacterEvents = eventSource.getCharacterEvents(matchResult);
                            
                            //Add the generated CharacterEvents to the list
                            for (final CharacterEvent generatedCharacterEvent : generatedCharacterEvents) {
                                characterDataEventItr.add(generatedCharacterEvent);
                            }
                        } while (matcher.find());
                            
                        //Add any remaining text from the original CharacterDataEvent
                        characterChunkingBuffer.delete(0, characterChunkingBuffer.length());
                        matcher.appendTail(characterChunkingBuffer);
                        characterDataEventItr.add(new CharacterDataEventImpl(characterChunkingBuffer.toString()));
                    }
                }
            }
        }
        return characterEvents;
    }

    @Override
    public boolean hasNext() {
        if (this.peekedEvent != null) {
            return true;
        }
        
        if (!this.xmlEventReader.hasNext()) {
            return false;
        }

        try {
            this.peekedEvent = this.nextEvent();
        }
        catch (NoSuchElementException e) {
            return false;
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        
        return true;
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return this.xmlEventReader.getProperty(name);
    }

    @Override
    public void close() throws XMLStreamException {
        captureCharacterDataEvent();
        this.xmlEventReader.close();
    }
}