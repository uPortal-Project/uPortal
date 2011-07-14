package org.jasig.portal.character.stream.events;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class PortletLinkPlaceholderEventImpl extends PortletPlaceholderEventImpl implements PortletLinkPlaceholderEvent {
    private static final long serialVersionUID = 1L;

    private final String defaultPortletUrl;
    
    public PortletLinkPlaceholderEventImpl(IPortletWindowId portletWindowId, String defaultPortletUrl) {
        super(portletWindowId);
        this.defaultPortletUrl = defaultPortletUrl;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.events.CharacterEvent#getEventType()
     */
    @Override
    public CharacterEventTypes getEventType() {
        return CharacterEventTypes.PORTLET_LINK;
    }
    
    @Override
    public String getDefaultPortletUrl() {
        return this.defaultPortletUrl;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getPortletWindowId() == null) ? 0 : this.getPortletWindowId().hashCode());
        result = prime * result + ((this.getEventType() == null) ? 0 : this.getEventType().hashCode());
        result = prime * result + ((this.getDefaultPortletUrl() == null) ? 0 : this.getDefaultPortletUrl().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (PortletNewItemCountPlaceholderEvent.class.isAssignableFrom(obj.getClass()))
            return false;
        PortletLinkPlaceholderEvent other = (PortletLinkPlaceholderEvent) obj;
        if (this.getEventType() == null) {
            if (other.getEventType() != null)
                return false;
        }
        else if (!this.getEventType().equals(other.getEventType()))
            return false;
        if (this.getPortletWindowId() == null) {
            if (other.getPortletWindowId() != null)
                return false;
        }
        else if (!this.getPortletWindowId().equals(other.getPortletWindowId()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletLinkPlaceholderEvent [" +
                "eventType=" + this.getEventType() + ", " +
                "portletWindowId=" + this.getPortletWindowId() + "]";
    }
}
