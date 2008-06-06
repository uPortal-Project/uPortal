package org.jasig.portal.events.support;

import org.jasig.portal.UserProfile;
import org.jasig.portal.events.EventType;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.security.IPerson;

public final class ChannelRenderedInLayoutPortalEvent extends TimedChannelLayoutPortalEvent {
    private static final long serialVersionUID = 1L;
    
    private final boolean renderedFromCache;

	public ChannelRenderedInLayoutPortalEvent(final Object source, final IPerson person, final UserProfile profile, 
            final IUserLayoutChannelDescription description, final IUserLayoutNodeDescription parentNode,
            final long renderTime, final boolean renderedFromCache) {
        super(source, person, profile, description, parentNode, renderTime, EventType.getEventType("CHANNEL_RENDERED"));
        
        this.renderedFromCache = renderedFromCache;
    }

    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
	public String toString() {
		return "Channel [" + getChannelDescription().getName() + ", "
				+ getChannelDescription().getChannelPublishId() + ", "
				+ getChannelDescription().getChannelSubscribeId()
				+ "] was rendered in layout " + getProfile().getLayoutId()
				+ " under node [" + getParentDescription().getId() + "," + getParentDescription().getName()
                + "] by " + getDisplayName() + " at " + getTimestampAsDate() + " in " + this.getRenderTime() + "ms "
                + (this.renderedFromCache ? "" : "not ") + "using cache";
	}
    
    /**
     * @return the renderedFromCache
     */
    public boolean isRenderedFromCache() {
        return renderedFromCache;
    }
    public void setRenderedFromCache(boolean renderedFromCache) {
        //ignore, method required for hibernate
    }
}
