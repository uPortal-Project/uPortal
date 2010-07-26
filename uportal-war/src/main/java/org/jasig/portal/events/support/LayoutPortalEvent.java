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

import org.jasig.portal.UserProfile;
import org.jasig.portal.events.EventType;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
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
        return this.folder != null ? this.folder.getId() : TransientUserLayoutManagerWrapper.TRANSIENT_FOLDER_ID;
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
    
    protected String getFolderString() {
        final IUserLayoutFolderDescription folder = getFolder();
        if (folder == null) {
            return "[transient folder]";
        }
        
        return "[" + folder.getName() + ", " + folder.getId() + "]";
    }

    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
    public String toString() {
        return this.getClass().getName() + " for Folder " + getFolderString()
                + " in layout " + getProfile().getLayoutId()
                + " by " + getDisplayName() + " at " + getTimestampAsDate();
    }
}
