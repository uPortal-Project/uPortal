package org.jasig.portal.events.support;

import org.jasig.portal.UserProfile;
import org.jasig.portal.events.EventType;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.security.IPerson;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 */
public abstract class ChannelLayoutPortalEvent extends PortalEvent {
    private final UserProfile profile;
	private final IUserLayoutChannelDescription description;
    private final IUserLayoutNodeDescription parentNode;
	
	public ChannelLayoutPortalEvent(final Object source, final IPerson person, final UserProfile profile, 
	        final IUserLayoutChannelDescription description, final IUserLayoutNodeDescription parentNode, 
	        final EventType eventType) {
		
	    super(source, person, eventType);
		
		this.profile = profile;
		this.description = description;
		this.parentNode = parentNode;
	}

    public final UserProfile getProfile() {
		return this.profile;
	}
	public final IUserLayoutChannelDescription getChannelDescription() {
		return this.description;
	}
	public final IUserLayoutNodeDescription getParentDescription() {
	    return this.parentNode;
	}
    
    public final String getTargetFolderId() {
        return this.parentNode.getId();
    }
    public void setTargetFolderId(String id) {
        //ignore, method required for hibernate
    }
    
    public final String getChannelDefinitionId() {
        return this.description.getChannelPublishId();
    }
    public void setChannelDefinitionId(String id) {
        //ignore, method required for hibernate
    }
    
    public final String getChannelSubscribeId() {
        return this.description.getChannelSubscribeId();
    }
    public void setChannelSubscribeId(String id) {
        //ignore, method required for hibernate
    }
    
    public final int getProfileId() {
        return this.profile.getProfileId();
    }
    public void setProfileId(int id) {
        //ignore, method required for hibernate
    }
    
    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
    public String toString() {
        return this.getClass().getName() + " for Channel [" + getChannelDescription().getName() + ", "
                + getChannelDescription().getChannelPublishId() + ", "
                + getChannelDescription().getChannelSubscribeId()
                + "] in layout " + getProfile().getLayoutId()
                + " under node [" + getParentDescription().getId() + "," + getParentDescription().getName()
                + "] for " + getDisplayName() + " at " + getTimestampAsDate();
    }
}
