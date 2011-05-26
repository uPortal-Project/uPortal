package org.jasig.portal.json.rendering;

import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import org.jasig.portal.character.stream.CharacterEventSource;
import org.jasig.portal.character.stream.events.CharacterEvent;


public class JsonLayoutPlaceholderEventSource implements CharacterEventSource {

    @Override
    public List<CharacterEvent> getCharacterEvents(
            HttpServletRequest servletRequest, XMLEventReader eventReader,
            StartElement event) throws XMLStreamException {
        return Collections.<CharacterEvent>singletonList(new JsonLayoutPlaceholderEventImpl());
    }

    @Override
    public List<CharacterEvent> getCharacterEvents(
            HttpServletRequest servletRequest, MatchResult matchResult) {
        return Collections.<CharacterEvent>singletonList(new JsonLayoutPlaceholderEventImpl());
    }
    
}
