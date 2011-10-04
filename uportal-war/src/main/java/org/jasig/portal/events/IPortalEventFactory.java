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

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.security.IPerson;

/**
 * Used to create and publish portal events
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalEventFactory {
    public void publishLoginEvent(HttpServletRequest request, Object source, IPerson person);
    
    public void publishLogoutEvent(HttpServletRequest request, Object source, IPerson person);

    public void publishPortletAddedToLayoutPortalEvent(HttpServletRequest request, Object source, 
            IPerson person, long layoutId, String parentFolderId, String fname);
    public void publishPortletAddedToLayoutPortalEvent(Object source, 
            IPerson person, long layoutId, String parentFolderId, String fname);
    
    public void publishPortletMovedInLayoutPortalEvent(HttpServletRequest request, Object source, 
            IPerson person, long layoutId, String oldParentFolderId, String newParentFolderId, String fname);
    public void publishPortletMovedInLayoutPortalEvent(Object source, 
            IPerson person, long layoutId, String oldParentFolderId, String newParentFolderId, String fname);

    public void publishFolderAddedToLayoutPortalEvent(HttpServletRequest request, Object source, 
            IPerson person, long layoutId, String folderId);
    public void publishFolderAddedToLayoutPortalEvent(Object source, 
            IPerson person, long layoutId, String folderId);
    
    public void publishFolderMovedInLayoutPortalEvent(HttpServletRequest request, Object source, 
            IPerson person, long layoutId, String oldParentFolderId, String newParentFolderId, String folderId);
    public void publishFolderMovedInLayoutPortalEvent(Object source, 
            IPerson person, long layoutId, String oldParentFolderId, String newParentFolderId, String folderId);
}
