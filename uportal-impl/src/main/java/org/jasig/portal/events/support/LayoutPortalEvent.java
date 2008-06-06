package org.jasig.portal.events.support;

import org.jasig.portal.UserProfile;
import org.jasig.portal.events.EventType;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 *
 */
public abstract class LayoutPortalEvent extends PortalEvent {
	private final UserProfile profile;
	private final IUserLayoutFolderDescription folder;
	
	public LayoutPortalEvent(final Object source, final IPerson person, final UserProfile profile, 
	        final IUserLayoutFolderDescription folder, final EventType eventType) {        

		super(source, person, eventType);
		
		this.profile = profile;
		this.folder = folder;
	}
	
	public final UserProfile getProfile() {
		return this.profile;
	}
	
	public final IUserLayoutFolderDescription getFolder() {
		return this.folder;
	}
    
    public final String getFolderId() {
        return this.folder.getId();
    }
    public void setFolderId(String id) {
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
        return this.getClass().getName() + " for Folder [" + getFolder().getName() + ", " + getFolder().getId()
                + "] in layout " + getProfile().getLayoutId()
                + " by " + getDisplayName() + " at " + getTimestampAsDate();
    }
}
