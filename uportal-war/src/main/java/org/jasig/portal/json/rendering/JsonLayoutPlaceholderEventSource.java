package org.jasig.portal.json.rendering;

import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.events.StartElement;

import org.jasig.portal.character.stream.BasePlaceholderEventSource;
import org.jasig.portal.character.stream.events.CharacterEvent;


public class JsonLayoutPlaceholderEventSource extends BasePlaceholderEventSource {

    @Override
    protected List<CharacterEvent> getCharacterEvents(HttpServletRequest servletRequest, StartElement event) {
        return Collections.<CharacterEvent>singletonList(new JsonLayoutPlaceholderEventImpl());
    }

    @Override
    public List<CharacterEvent> getCharacterEvents(HttpServletRequest servletRequest, MatchResult matchResult) {
        return Collections.<CharacterEvent>singletonList(new JsonLayoutPlaceholderEventImpl());
    }
}
