package org.jasig.portal.rendering;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.events.StartElement;

import org.jasig.portal.character.stream.BasePlaceholderEventSource;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.PageAnalyticsDataPlaceholderEvent;

/**
 * Replaces <page-analytics-data> elements in the XML stream with {@link PageAnalyticsDataPlaceholderEvent}
 */
public class PageAnalyticsDataPlaceholderEventSource extends BasePlaceholderEventSource {
    /**
     * Represents <page-analytics-data> layout element 
     */
    public static final String PAGE_ANALYTICS_SCRIPT = "page-analytics-data";

    @Override
    protected void generateCharacterEvents(HttpServletRequest servletRequest, StartElement event, Collection<CharacterEvent> eventBuffer) {
        eventBuffer.add(PageAnalyticsDataPlaceholderEvent.INSTANCE);
    }
}
