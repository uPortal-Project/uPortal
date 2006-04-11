package org.jasig.portal.events.support;

import org.jasig.portal.UserProfile;
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
	
	public LayoutPortalEvent(final Object source, final IPerson person, final UserProfile profile, final IUserLayoutFolderDescription folder) {
		super(source, person);
		
		this.profile = profile;
		this.folder = folder;
	}
	
	public final UserProfile getProfile() {
		return this.profile;
	}
	
	public final IUserLayoutFolderDescription getFolder() {
		return this.folder;
	}
}
