package org.jasig.portal.events;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.security.IPerson;

/**
 * Publishes events related to layouts
 * 
 * @author Eric Dalquist
 */
public interface IPortalLayoutEventFactory {

    //  ********** Portlet in Layout Events **********//
    
    void publishPortletAddedToLayoutPortalEvent(HttpServletRequest request, Object source, IPerson layoutOwner,
            long layoutId, String parentFolderId, String fname);

    void publishPortletAddedToLayoutPortalEvent(Object source, IPerson layoutOwner, long layoutId,
            String parentFolderId, String fname);

    void publishPortletMovedInLayoutPortalEvent(HttpServletRequest request, Object source, IPerson layoutOwner,
            long layoutId, String oldParentFolderId, String newParentFolderId, String fname);

    void publishPortletMovedInLayoutPortalEvent(Object source, IPerson layoutOwner, long layoutId,
            String oldParentFolderId, String newParentFolderId, String fname);

    void publishPortletDeletedFromLayoutPortalEvent(HttpServletRequest request, Object source, IPerson layoutOwner,
            long layoutId, String oldParentFolderId, String fname);

    void publishPortletDeletedFromLayoutPortalEvent(Object source, IPerson layoutOwner, long layoutId,
            String oldParentFolderId, String fname);

    void publishFolderAddedToLayoutPortalEvent(HttpServletRequest request, Object source, IPerson layoutOwner,
            long layoutId, String newFolderId);

    
    //********** Folder in Layout Events **********//

    void publishFolderAddedToLayoutPortalEvent(Object source, IPerson layoutOwner, long layoutId, String newFolderId);

    void publishFolderMovedInLayoutPortalEvent(HttpServletRequest request, Object source, IPerson layoutOwner,
            long layoutId, String oldParentFolderId, String movedFolderId);

    void publishFolderMovedInLayoutPortalEvent(Object source, IPerson layoutOwner, long layoutId,
            String oldParentFolderId, String movedFolderId);

    void publishFolderDeletedFromLayoutPortalEvent(HttpServletRequest request, Object source, IPerson layoutOwner,
            long layoutId, String oldParentFolderId, String deletedFolderId, String deletedFolderName);

    void publishFolderDeletedFromLayoutPortalEvent(Object source, IPerson layoutOwner, long layoutId,
            String oldParentFolderId, String deletedFolderId, String deletedFolderName);

}