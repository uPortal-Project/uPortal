package org.jasig.portal.character.stream.events;


/**
 * Placeholder in the character event string for page analytics data
 *  
 * @author Eric Dalquist
 */
public final class PageAnalyticsDataPlaceholderEvent implements CharacterEvent {
    public static PageAnalyticsDataPlaceholderEvent INSTANCE = new PageAnalyticsDataPlaceholderEvent();
    
    private static final long serialVersionUID = 1L;
    
    private PageAnalyticsDataPlaceholderEvent() {
    }

    @Override 
    public CharacterEventTypes getEventType() {
        return CharacterEventTypes.PAGE_ANALYTICS_DATA;
    }
}
