package org.jasig.portal.character.stream.events;


/**
 * Placeholder in the character event string for analytics data
 *  
 * @author Eric Dalquist
 */
public final class PortletAnalyticsDataPlaceholderEvent implements CharacterEvent {
    public static PortletAnalyticsDataPlaceholderEvent INSTANCE = new PortletAnalyticsDataPlaceholderEvent();
    
    private static final long serialVersionUID = 1L;
    
    private PortletAnalyticsDataPlaceholderEvent() {
    }

    @Override 
    public CharacterEventTypes getEventType() {
        return CharacterEventTypes.PORTLET_ANALYTICS_DATA;
    }
}
