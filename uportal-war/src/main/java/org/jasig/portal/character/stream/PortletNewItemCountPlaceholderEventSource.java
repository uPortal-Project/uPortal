package org.jasig.portal.character.stream;

import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;

import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.PortletNewItemCountPlaceholderEvent;
import org.jasig.portal.character.stream.events.PortletNewItemCountPlaceholderEventImpl;

/**
 * Generates a {@link PortletNewItemCountPlaceholderEvent} for a regular expression match
 * 
 * @author Jen Bourey
 * @version $Revision$
 */
public class PortletNewItemCountPlaceholderEventSource extends PortletPlaceholderEventSource {
    
    @Override
    protected List<CharacterEvent> getCharacterEvents(String subscribeId, MatchResult matchResult) {
        return Arrays.asList((CharacterEvent)new PortletNewItemCountPlaceholderEventImpl(subscribeId));
    }
}
