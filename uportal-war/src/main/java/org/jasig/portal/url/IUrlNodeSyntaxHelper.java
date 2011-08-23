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

package org.jasig.portal.url;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.dao.usertype.FunctionalNameType;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Provides information to the URL generation code about layout nodes for navigation.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IUrlNodeSyntaxHelper {
    /**
     * @return The unique name of this url node syntax helper. Must be a valid {@link FunctionalNameType#VALID_FNAME_PATTERN}
     */
    public String getName();
    
    /**
     * Get the default layout nodeId for the current request, may return null IFF the default url shouldn't target any node.
     */
    public String getDefaultLayoutNodeId(HttpServletRequest httpServletRequest);
    
    /**
     * Get one or more path folder names to include in the generated url for the target layout node, will never be null.
     */
    public List<String> getFolderNamesForLayoutNode(HttpServletRequest request, String layoutNodeId);
    
    /**
     * Determine the targeted layout id from the specified list of folder names
     */
    public String getLayoutNodeForFolderNames(HttpServletRequest request, List<String> folderNames);
    
    /**
     * Get a path folder name to include in the generated url for the targeted portlet window
     */
    public String getFolderNameForPortlet(HttpServletRequest request, IPortletWindowId portletWindowId);
    /**
     * Determine the targeted portlet window from the specified folder name
     * 
     * @param targetedLayoutNodeId Layout node id targeted by the current request, null if no layout node was targeted
     * @param folderName the folder from the URL 
     */
    public IPortletWindowId getPortletForFolderName(HttpServletRequest request, String targetedLayoutNodeId, String folderName);

}
