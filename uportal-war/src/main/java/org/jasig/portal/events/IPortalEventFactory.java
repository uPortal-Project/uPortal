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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IPortalRequestInfo;

/**
 * Used to create and publish portal events
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalEventFactory {
    
    public String getPortalEventSessionId(HttpServletRequest request, IPerson person);
    
    //********** Login/Logout Events **********//
    
    public void publishLoginEvent(HttpServletRequest request, Object source, IPerson person);
    
    public void publishLogoutEvent(HttpServletRequest request, Object source, IPerson person);
    
    
    //********** Portlet in Layout Events **********//

    public void publishPortletAddedToLayoutPortalEvent(HttpServletRequest request, Object source, 
            IPerson layoutOwner, long layoutId, String parentFolderId, String fname);
    public void publishPortletAddedToLayoutPortalEvent(Object source, 
            IPerson layoutOwner, long layoutId, String parentFolderId, String fname);
    
    public void publishPortletMovedInLayoutPortalEvent(HttpServletRequest request, Object source, 
            IPerson layoutOwner, long layoutId, String oldParentFolderId, String newParentFolderId, String fname);
    public void publishPortletMovedInLayoutPortalEvent(Object source, 
            IPerson layoutOwner, long layoutId, String oldParentFolderId, String newParentFolderId, String fname);
    
    public void publishPortletDeletedFromLayoutPortalEvent(HttpServletRequest request, Object source, 
            IPerson layoutOwner, long layoutId, String oldParentFolderId, String fname);
    public void publishPortletDeletedFromLayoutPortalEvent(Object source, 
            IPerson layoutOwner, long layoutId, String oldParentFolderId, String fname);
    
    
    //********** Folder in Layout Events **********//

    public void publishFolderAddedToLayoutPortalEvent(HttpServletRequest request, Object source, 
            IPerson layoutOwner, long layoutId, String newFolderId);
    public void publishFolderAddedToLayoutPortalEvent(Object source, 
            IPerson layoutOwner, long layoutId, String newFolderId);
    
    public void publishFolderMovedInLayoutPortalEvent(HttpServletRequest request, Object source, 
            IPerson layoutOwner, long layoutId, String oldParentFolderId, String movedFolderId);
    public void publishFolderMovedInLayoutPortalEvent(Object source, 
            IPerson layoutOwner, long layoutId, String oldParentFolderId, String movedFolderId);

    public void publishFolderDeletedFromLayoutPortalEvent(HttpServletRequest request, Object source, 
            IPerson layoutOwner, long layoutId, String oldParentFolderId, String deletedFolderId, String deletedFolderName);
    public void publishFolderDeletedFromLayoutPortalEvent(Object source, 
            IPerson layoutOwner, long layoutId, String oldParentFolderId, String deletedFolderId, String deletedFolderName);
    
    
    //********** Portlet Execution Events **********//
    
    public void publishPortletActionExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters);
    public void publishPortletEventExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters, QName eventName);
    public void publishPortletRenderHeaderExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters, boolean targeted, boolean usedPortalCache);
    public void publishPortletRenderExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters, boolean targeted, boolean usedPortalCache);
    public void publishPortletResourceExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters, String resourceId, boolean usedBrowserCache, boolean usedPortalCache);
    
    //********** Portal Rendering Pipeline Events **********//
    
    public void publishPortalRenderEvent(HttpServletRequest request, Object source, String requestPathInfo, long executionTime,
            IPortalRequestInfo portalRequestInfo);
}
