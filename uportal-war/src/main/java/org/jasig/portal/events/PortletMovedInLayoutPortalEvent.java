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

package org.jasig.portal.events;

import org.apache.commons.lang.Validate;
import org.jasig.portal.dao.usertype.FunctionalNameType;
import org.jasig.portal.security.IPerson;



public final class PortletMovedInLayoutPortalEvent extends LayoutPortalEvent {
    private static final long serialVersionUID = 1L;

    private final String oldParentFolderId;
    private final String newParentFolderId;
    private final String fname;
    
    @SuppressWarnings("unused")
    private PortletMovedInLayoutPortalEvent() {
        super();
        this.oldParentFolderId = null;
        this.newParentFolderId = null;
        this.fname = null;
    }

    PortletMovedInLayoutPortalEvent(PortalEventBuilder portalEventBuilder, IPerson layoutOwner, long layoutId,
            String oldParentFolderId, String newParentFolderId, String fname) {
        super(portalEventBuilder, layoutOwner, layoutId);
        Validate.notNull(oldParentFolderId, "oldParentFolderId");
        Validate.notNull(newParentFolderId, "newParentFolderId");
        FunctionalNameType.validate(fname);
        
        this.oldParentFolderId = oldParentFolderId;
        this.newParentFolderId = newParentFolderId;
        this.fname = fname;
    }

    /**
     * @return the oldParentFolderId
     */
    public String getOldParentFolderId() {
        return this.oldParentFolderId;
    }

    /**
     * @return the newParentFolderId
     */
    public String getNewParentFolderId() {
        return this.newParentFolderId;
    }

    /**
     * @return the fname
     */
    public String getFname() {
        return this.fname;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString() + 
                ", oldParentFolderId=" + this.oldParentFolderId + 
                ", newParentFolderId=" + this.newParentFolderId + 
                ", fname=" + this.fname + "]";
    }
    
    
}
