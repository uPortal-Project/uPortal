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
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.security.IPerson;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class TimedChannelLayoutPortalEvent extends ChannelLayoutPortalEvent {
    private final long renderTime;

    public TimedChannelLayoutPortalEvent(final Object source, final IPerson person, final UserProfile profile,
            final IUserLayoutChannelDescription description, final IUserLayoutNodeDescription parentNode, long renderTime,
            final EventType eventType) {
        super(source, person, profile, description, parentNode, eventType);
        
        this.renderTime = renderTime;
    }

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
        return this.getClass().getName() +  " for Channel " + getChannelDescriptionString()
                + " in layout " + getProfile().getLayoutId()
                + " under node " + getParentDescriptionString()
                + " by " + getDisplayName() + " at " + getTimestampAsDate() + " in " + this.getRenderTime() + "ms";
    }

}