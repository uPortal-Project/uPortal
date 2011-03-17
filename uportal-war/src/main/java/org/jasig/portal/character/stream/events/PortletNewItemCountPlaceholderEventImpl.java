package org.jasig.portal.character.stream.events;

/**
 * @author Jen Bourey
 * @version $Revision$
 */
public class PortletNewItemCountPlaceholderEventImpl implements PortletNewItemCountPlaceholderEvent {
    private static final long serialVersionUID = 1L;

    private final String portletWindowId;
    
    public PortletNewItemCountPlaceholderEventImpl(String portletWindowId) {
        this.portletWindowId = portletWindowId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.events.PortletPlaceholderEvent#getPortletWindowId()
     */
    @Override
    public String getPortletSubscribeId() {
        return this.portletWindowId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.events.CharacterEvent#getEventType()
     */
    @Override
    public CharacterEventTypes getEventType() {
        return CharacterEventTypes.PORTLET_NEW_ITEM_COUNT;
    }

    @Override
    public String toString() {
        return "PortletNewItemCountPlaceholderEventImpl [portletWindowId=" + this.portletWindowId + "]";
    }
}
