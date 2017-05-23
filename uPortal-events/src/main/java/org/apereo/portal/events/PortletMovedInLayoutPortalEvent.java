/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.events;

import org.apache.commons.lang.Validate;
import org.apereo.portal.security.IPerson;

public final class PortletMovedInLayoutPortalEvent extends PortletLayoutPortalEvent {
    private static final long serialVersionUID = 1L;

    private final String oldParentFolderId;
    private final String newParentFolderId;

    @SuppressWarnings("unused")
    private PortletMovedInLayoutPortalEvent() {
        super();
        this.oldParentFolderId = null;
        this.newParentFolderId = null;
    }

    PortletMovedInLayoutPortalEvent(
            PortalEventBuilder portalEventBuilder,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String newParentFolderId,
            String fname) {
        super(portalEventBuilder, layoutOwner, layoutId, fname);
        Validate.notNull(oldParentFolderId, "oldParentFolderId");
        Validate.notNull(newParentFolderId, "newParentFolderId");

        this.oldParentFolderId = oldParentFolderId;
        this.newParentFolderId = newParentFolderId;
    }

    /** @return the oldParentFolderId */
    public String getOldParentFolderId() {
        return this.oldParentFolderId;
    }

    /** @return the newParentFolderId */
    public String getNewParentFolderId() {
        return this.newParentFolderId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString()
                + ", oldParentFolderId="
                + this.oldParentFolderId
                + ", newParentFolderId="
                + this.newParentFolderId
                + ", fname="
                + this.getFname()
                + "]";
    }
}
