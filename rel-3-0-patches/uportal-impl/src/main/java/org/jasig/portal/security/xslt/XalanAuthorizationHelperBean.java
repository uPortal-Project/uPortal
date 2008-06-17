/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.security.xslt;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.springframework.beans.factory.annotation.Required;

/**
 * Spring managed version of the Xalan Elements helper class used during portal XSL
 * transformations.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XalanAuthorizationHelperBean implements IXalanAuthorizationHelper {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private IChannelRegistryStore channelRegistryStore;
    
    /**
     * @return the channelRegistryStore
     */
    public IChannelRegistryStore getChannelRegistryStore() {
        return channelRegistryStore;
    }
    /**
     * @param channelRegistryStore the channelRegistryStore to set
     */
    @Required
    public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
        Validate.notNull(channelRegistryStore, "channelRegistryStore can not be null");
        this.channelRegistryStore = channelRegistryStore;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.xslt.IAuthorizationHelper#canRender(java.lang.String, java.lang.String)
     */
    public boolean canRender(final String userName, final String channelFName) {
        if (userName == null || channelFName == null) {
            return false;
        }
        
        final IAuthorizationPrincipal userPrincipal = this.getUserPrincipal(userName);
        
        final int channelId;
        try {
            final ChannelDefinition channelDefinition = this.channelRegistryStore.getChannelDefinition(channelFName);
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
        
        final AuthorizationService authService = AuthorizationService.instance();
        final IAuthorizationPrincipal userPrincipal = authService.newPrincipal(user);
        return userPrincipal;
    }
}
