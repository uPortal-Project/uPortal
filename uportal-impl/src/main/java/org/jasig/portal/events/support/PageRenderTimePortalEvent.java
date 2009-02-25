/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
