package org.jasig.portal.rendering;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.events.StartElement;

import org.jasig.portal.character.stream.BasePlaceholderEventSource;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.PortletAnalyticsDataPlaceholderEvent;

/**
 * Replaces <portlet-analytics-data> elements in the XML stream with {@link PortletAnalyticsDataPlaceholderEvent}
 */
public class PortletAnalyticsDataPlaceholderEventSource extends BasePlaceholderEventSource {
    /**
     * Represents <portlet-analytics-data> layout element 
     */
    public static final String PORTLET_ANALYTICS_SCRIPT = "portlet-analytics-data";

    @Override
    protected void generateCharacterEvents(HttpServletRequest servletRequest, StartElement event, Collection<CharacterEvent> eventBuffer) {
        eventBuffer.add(PortletAnalyticsDataPlaceholderEvent.INSTANCE);
    }
}
