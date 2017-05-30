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

import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.security.IPerson;

/**
 * Publishes events related to layouts
 *
 */
public interface IPortalLayoutEventFactory {

    //  ********** Portlet in Layout Events **********//

    void publishPortletAddedToLayoutPortalEvent(
            HttpServletRequest request,
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String parentFolderId,
            String fname);

    void publishPortletAddedToLayoutPortalEvent(
            Object source, IPerson layoutOwner, long layoutId, String parentFolderId, String fname);

    void publishPortletMovedInLayoutPortalEvent(
            HttpServletRequest request,
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String newParentFolderId,
            String fname);

    void publishPortletMovedInLayoutPortalEvent(
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String newParentFolderId,
            String fname);

    void publishPortletDeletedFromLayoutPortalEvent(
            HttpServletRequest request,
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String fname);

    void publishPortletDeletedFromLayoutPortalEvent(
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String fname);

    void publishFolderAddedToLayoutPortalEvent(
            HttpServletRequest request,
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String newFolderId);

    //********** Folder in Layout Events **********//

    void publishFolderAddedToLayoutPortalEvent(
            Object source, IPerson layoutOwner, long layoutId, String newFolderId);

    void publishFolderMovedInLayoutPortalEvent(
            HttpServletRequest request,
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String movedFolderId);

    void publishFolderMovedInLayoutPortalEvent(
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String movedFolderId);

    void publishFolderDeletedFromLayoutPortalEvent(
            HttpServletRequest request,
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String deletedFolderId,
            String deletedFolderName);

    void publishFolderDeletedFromLayoutPortalEvent(
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String deletedFolderId,
            String deletedFolderName);
}
