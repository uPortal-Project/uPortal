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

package org.jasig.portal.security.xslt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class XalanGroupMembershipHelperBean implements IXalanGroupMembershipHelper {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    
    /**
     * @param portletDefinitionRegistry the portletDefinitionRegistry to set
     */
    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.security.xslt.IXalanGroupMembershipHelper#isChannelDeepMemberOf(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isChannelDeepMemberOf(String fname, String groupKey) {
        final IEntityGroup distinguishedGroup = GroupService.findGroup(groupKey);
        if (distinguishedGroup == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No group found for key '" + groupKey + "'");
            }
            
            return false;
        }
        
        final IPortletDefinition portletDefinition;
        try {
            portletDefinition = this.portletDefinitionRegistry.getPortletDefinitionByFname(fname);
        }
        catch (Exception e) {
            this.logger.warn("Caught exception while retrieving portlet definition for fname '" + fname + "'", e);
            return false;
        }
        
        if (portletDefinition == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No portlet found for key '" + fname + "'");
            }
            
            return false;
        }
        
        final String portletId = portletDefinition.getPortletDefinitionId().getStringId();
        final IEntity entity = GroupService.getEntity(portletId, IPortletDefinition.class);
        if (entity == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No portlet found for id '" + portletId + "'");
            }
            
            return false;
        }
        
        return distinguishedGroup.deepContains(entity);
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.security.xslt.IXalanGroupMembershipHelper#isUserDeepMemberOf(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isUserDeepMemberOf(String userName, String groupKey) {
        final IEntityGroup distinguishedGroup = GroupService.findGroup(groupKey);
        if (distinguishedGroup == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No group found for key '" + groupKey + "'");
            }
            
            return false;
        }
        
        final IEntity entity = GroupService.getEntity(userName, IPerson.class);
        if (entity == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No user found for key '" + userName + "'");
            }
            
            return false;
        }
        
        return distinguishedGroup.deepContains(entity);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.security.xslt.IXalanGroupMembershipHelper#isUserDeepMemberOfGroupName(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isUserDeepMemberOfGroupName(String userName, String groupName) {
        final EntityIdentifier[] results = GroupService.searchForGroups(groupName, GroupService.IS, IPerson.class);
        if (results == null || results.length == 0) {
            return false;
        }
        
        if (results.length > 1) {
            this.logger.warn(results.length + " groups were found for '" + groupName + "'. The first result will be used.");
        }
            
        final IGroupMember group = GroupService.getGroupMember(results[0]);
        
        final IEntity entity = GroupService.getEntity(userName, IPerson.class);
        if (entity == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No user found for key '" + userName + "'");
            }
            
            return false;
        }
        
        return group.deepContains(entity);
    }
}
