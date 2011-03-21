/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.events.support;

import org.apache.commons.lang.Validate;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.events.EventType;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.security.IPerson;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 */
public abstract class ChannelLayoutPortalEvent extends PortalEvent {
    private final IUserProfile profile;
	private final IUserLayoutChannelDescription description;
    private final IUserLayoutNodeDescription parentNode;
	
	public ChannelLayoutPortalEvent(final Object source, final IPerson person, final IUserProfile profile, 
	        final IUserLayoutChannelDescription description, final IUserLayoutNodeDescription parentNode, 
	        final EventType eventType) {
		
	    super(source, person, eventType);
	    
	    Validate.notNull(description, "IUserLayoutChannelDescription can not be null");
		
		this.profile = profile;
		this.description = description;
		this.parentNode = parentNode;
	}

    public final IUserProfile getProfile() {
		return this.profile;
	}
	public final IUserLayoutChannelDescription getChannelDescription() {
		return this.description;
	}
	public final IUserLayoutNodeDescription getParentDescription() {
	    return this.parentNode;
	}
    
    public final String getTargetFolderId() {
        return this.parentNode != null ? this.parentNode.getId() : TransientUserLayoutManagerWrapper.TRANSIENT_FOLDER_ID;
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
    
    protected String getChannelDescriptionString() {
        final IUserLayoutChannelDescription channelDescription = getChannelDescription();
        
        if (channelDescription == null) {
            return "[transient channel]";
        }
        
        return "[" + channelDescription.getName() + ", "
                + channelDescription.getChannelPublishId() + ", "
                + channelDescription.getChannelSubscribeId()
                + "]";
    }
    
    protected String getParentDescriptionString() {
        final IUserLayoutNodeDescription parentDescription = getParentDescription();
        
        if (parentDescription == null) {
            return "[transient parent]";
        }
        
        return "[" + parentDescription.getId() + "," + parentDescription.getName() + "]";
    }
    
    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
    public String toString() {
        return this.getClass().getName() + " for Channel " + getChannelDescriptionString()
                + " in layout " + getProfile().getLayoutId()
                + " under node " + getParentDescriptionString()
                + " for " + getDisplayName() + " at " + getTimestampAsDate();
    }
}
