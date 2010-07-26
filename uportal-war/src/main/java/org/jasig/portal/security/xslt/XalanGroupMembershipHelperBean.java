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
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XalanGroupMembershipHelperBean implements IXalanGroupMembershipHelper {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IChannelRegistryStore channelRegistryStore;
    
    public IChannelRegistryStore getChannelRegistryStore() {
        return this.channelRegistryStore;
    }
    /**
     * @param channelRegistryStore the channelRegistryStore to set
     */
    @Required
    public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
        this.channelRegistryStore = channelRegistryStore;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.security.xslt.IXalanGroupMembershipHelper#isChannelDeepMemberOf(java.lang.String, java.lang.String)
     */
    public boolean isChannelDeepMemberOf(String fname, String groupKey) {
        final IEntityGroup distinguishedGroup = GroupService.findGroup(groupKey);
        if (distinguishedGroup == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No group found for key '" + groupKey + "'");
            }
            
            return false;
        }
        
        final IChannelDefinition channelDefinition;
        try {
            channelDefinition = this.channelRegistryStore.getChannelDefinition(fname);
        }
        catch (Exception e) {
            this.logger.warn("Caught exception while retrieving channel definition for fname '" + fname + "'", e);
            return false;
        }
        
        if (channelDefinition == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No channel found for key '" + fname + "'");
            }
            
            return false;
        }
        
        final Integer channelId = channelDefinition.getId();
        final IEntity entity = GroupService.getEntity(channelId.toString(), IChannelDefinition.class);
        if (entity == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No channel found for id '" + channelId + "'");
            }
            
            return false;
        }
        
        return distinguishedGroup.deepContains(entity);
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.security.xslt.IXalanGroupMembershipHelper#isUserDeepMemberOf(java.lang.String, java.lang.String)
     */
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
