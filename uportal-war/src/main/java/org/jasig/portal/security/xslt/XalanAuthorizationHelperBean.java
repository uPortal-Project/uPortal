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

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Spring managed version of the Xalan Elements helper class used during portal XSL
 * transformations.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class XalanAuthorizationHelperBean implements IXalanAuthorizationHelper {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private IChannelRegistryStore channelRegistryStore;
    
    /**
     * @param channelRegistryStore the channelRegistryStore to set
     */
    @Autowired
    public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
        Validate.notNull(channelRegistryStore, "channelRegistryStore can not be null");
        this.channelRegistryStore = channelRegistryStore;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.xslt.IAuthorizationHelper#canRender(java.lang.String, java.lang.String)
     */
    @Override
    public boolean canRender(final String userName, final String channelFName) {
        if (userName == null || channelFName == null) {
            return false;
        }
        
        final IAuthorizationPrincipal userPrincipal = this.getUserPrincipal(userName);
        if (userPrincipal == null) {
            return false;
        }
        
        final int channelId;
        try {
            final IChannelDefinition channelDefinition = this.channelRegistryStore.getChannelDefinition(channelFName);
            if (channelDefinition == null) {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("No ChannelDefinition for fname='" + channelFName + "', returning false.");
                }

                return false;
            }
            
            channelId = channelDefinition.getId();
        }
        catch (Exception e) {
            this.logger.warn("Could not find ChannelDefinition for fname='" + channelFName + "' while checking if user '" + userName + "' can render it. Returning FALSE.", e);
            return false;
        }
        
        return userPrincipal.canRender(channelId);
    }
    
    protected IAuthorizationPrincipal getUserPrincipal(final String userName) {
        final IEntity user = GroupService.getEntity(userName, IPerson.class);
        if (user == null) {
            return null;
        }
        
        final AuthorizationService authService = AuthorizationService.instance();
        return authService.newPrincipal(user);
    }
}
