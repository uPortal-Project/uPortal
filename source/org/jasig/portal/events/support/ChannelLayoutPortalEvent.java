package org.jasig.portal.events.support;

import org.jasig.portal.UserProfile;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.security.IPerson;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 *
 */
public abstract class ChannelLayoutPortalEvent extends PortalEvent {

	private final UserProfile profile;
	
	private final IUserLayoutChannelDescription description;
	
	public ChannelLayoutPortalEvent(final Object source, final IPerson person, final UserProfile profile, final IUserLayoutChannelDescription description) {
		super(source, person);
		
		this.profile = profile;
		this.description = description;
	}
	
	public final UserProfile getProfile() {
		return this.profile;
	}
	
	public final IUserLayoutChannelDescription getChannelDescription() {
		return this.description;
	}
	
}
