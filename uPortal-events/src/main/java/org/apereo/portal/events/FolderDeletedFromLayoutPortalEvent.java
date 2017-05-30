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

public final class FolderDeletedFromLayoutPortalEvent extends LayoutPortalEvent {
    private static final long serialVersionUID = 1L;

    private final String oldParentFolderId;
    private final String deletedFolderId;
    private final String deletedFolderName;

    @SuppressWarnings("unused")
    private FolderDeletedFromLayoutPortalEvent() {
        super();
        this.oldParentFolderId = null;
        this.deletedFolderId = null;
        this.deletedFolderName = null;
    }

    FolderDeletedFromLayoutPortalEvent(
            PortalEventBuilder portalEventBuilder,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String deletedFolderId,
            String deletedFolderName) {
        super(portalEventBuilder, layoutOwner, layoutId);
        Validate.notNull(oldParentFolderId, "oldParentFolderId");
        Validate.notNull(deletedFolderId, "deletedFolderId");

        this.oldParentFolderId = oldParentFolderId;
        this.deletedFolderId = deletedFolderId;
        this.deletedFolderName = deletedFolderName;
    }

    /** @return the deletedFolderName */
    public String getDeletedFolderName() {
        return this.deletedFolderName;
    }

    /** @return the oldParentFolderId */
    public String getOldParentFolderId() {
        return this.oldParentFolderId;
    }

    /** @return the deletedFolderId */
    public String getDeletedFolderId() {
        return this.deletedFolderId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString()
                + ", oldParentFolderId="
                + this.oldParentFolderId
                + ", deletedFolderId="
                + this.deletedFolderId
                + ", deletedFolderName="
                + this.deletedFolderName
                + "]";
    }
}
