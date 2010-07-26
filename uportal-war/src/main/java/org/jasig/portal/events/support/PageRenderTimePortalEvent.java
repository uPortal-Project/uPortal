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
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PageRenderTimePortalEvent extends LayoutPortalEvent {
    private static final long serialVersionUID = 1L;

    private final long renderTime;
    
    
    public PageRenderTimePortalEvent(Object source, IPerson person, final UserProfile profile,
            final IUserLayoutFolderDescription folder, long renderTime) {
        super(source, person, profile, folder, EventType.getEventType("PAGE_RENDER_TIME"));
        
        this.renderTime = renderTime;
    }


    /**
     * @return the renderTime
     */
    public long getRenderTime() {
        return renderTime;
    }
    public void setRenderTime(long renderTime) {
        //ignore, method required for hibernate
    }

    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
    public String toString() {
        return "Folder " + getFolderString()
                + " was rendered for layout " + getProfile().getLayoutId()
                + " by " + getDisplayName() + " at " + getTimestampAsDate() + " in " + this.renderTime + "ms";
    }
}
