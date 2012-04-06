package org.jasig.portal.character.stream.events;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.events.StartElement;

import org.jasig.portal.character.stream.BasePlaceholderEventSource;
import org.jasig.portal.rendering.StAXSerializingComponent;
import org.jasig.portal.xml.stream.ChunkingEventReader;

/**
 * Returns an empty string event. The chunk-point element and this source are used to break up the character
 * stream created by the {@link StAXSerializingComponent} and {@link ChunkingEventReader}. The <chunk-point/>
 * element should be placed before and after sections of markup that are very user or page specific (username, 
 * page title, session key, etc..). This allows the string de-duplication logic of the {@link CharacterDataEventImpl}
 * to be more effective as more users will have common character event strings in their rendering pipeline.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ChunkPointPlaceholderEventSource extends BasePlaceholderEventSource {
    /**
     * Represents <chunk-point> layout element 
     */
    public static final String CHUNK_POINT = "chunk-point";

    @Override
    protected void generateCharacterEvents(HttpServletRequest servletRequest, StartElement event,
            Collection<CharacterEvent> eventBuffer) {
        eventBuffer.add(CharacterDataEventImpl.EMPTY_CHARACTER_DATA);
    }
}
