/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channel;

import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelCategory;
import org.jasig.portal.ChannelRegistryManager;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.events.EventPublisherLocator;
import org.jasig.portal.events.support.ModifiedChannelDefinitionPortalEvent;
import org.jasig.portal.events.support.PublishedChannelDefinitionPortalEvent;
import org.jasig.portal.events.support.RemovedChannelDefinitionPortalEvent;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IUpdatingPermissionManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;

/**
 * Default implementation of IChannelPublishingService.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 */
public class ChannelPublishingServiceImpl implements IChannelPublishingService {
	
	private IChannelRegistryStore channelRegistryStore;
	
	/**
	 * Set the channel persistence service
	 * 
	 * @param channelRegistryStore
	 */
	public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
		this.channelRegistryStore = channelRegistryStore;
	}


	private Log log = LogFactory.getLog(ChannelPublishingServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.IChannelPublishingService#saveChannelDefinition(org.jasig.portal.channel.IChannelDefinition, org.jasig.portal.security.IPerson, org.jasig.portal.channel.ChannelLifecycleState, java.util.Date, java.util.Date, org.jasig.portal.ChannelCategory[], org.jasig.portal.groups.IGroupMember[])
	 */
	public IChannelDefinition saveChannelDefinition(IChannelDefinition definition, IPerson publisher, ChannelCategory[] categories, IGroupMember[] groupMembers) {
		boolean newChannel = (definition.getId() < 0);

	    // save the channel
	    channelRegistryStore.saveChannelDefinition(definition);
	    definition = channelRegistryStore.getChannelDefinition(definition.getFName());

	    // Delete existing category memberships for this channel
	    String chanKey = String.valueOf(definition.getId());
	    IEntity channelDefEntity = GroupService.getEntity(chanKey, IChannelDefinition.class);
		@SuppressWarnings("unchecked")
	    Iterator iter = channelDefEntity.getAllContainingGroups();
	    while (iter.hasNext()) {
	        IEntityGroup group = (IEntityGroup) iter.next();
	        group.removeMember(channelDefEntity);
	        group.update();
	    }

	    // For each category ID, add channel to category
	    for (ChannelCategory category : categories) {
	      channelRegistryStore.addChannelToCategory(definition, category);
	    }

	    // Set groups
	    AuthorizationService authService = AuthorizationService.instance();
	    String target = IPermission.CHANNEL_PREFIX + definition.getId();
	    IUpdatingPermissionManager upm = authService.newUpdatingPermissionManager(FRAMEWORK_OWNER);
	    IPermission[] permissions = new IPermission[groupMembers.length];
	    for (int i = 0; i < groupMembers.length; i++) {
	      IAuthorizationPrincipal authPrincipal = authService.newPrincipal(groupMembers[i]);
	      permissions[i] = upm.newPermission(authPrincipal);
	      permissions[i].setType(GRANT_PERMISSION_TYPE);
	      permissions[i].setActivity(SUBSCRIBER_ACTIVITY);
	      permissions[i].setTarget(target);
	    }

	    // If modifying the channel, remove the existing permissions before adding the new ones
	    if (!newChannel) {
	      IPermission[] oldPermissions = upm.getPermissions(SUBSCRIBER_ACTIVITY, target);
	      upm.removePermissions(oldPermissions);
	    }
	    upm.addPermissions(permissions);

	    if (log.isInfoEnabled()) {
	        log.info( "Channel " + definition.getId() + " has been " + 
	                (newChannel ? "published" : "modified") + ".");
	    }

	    // Record that a channel has been published or modified
	    if (newChannel) {
	    	EventPublisherLocator.getApplicationEventPublisher().publishEvent(new PublishedChannelDefinitionPortalEvent(definition, publisher, definition));
	    } else {
	    	EventPublisherLocator.getApplicationEventPublisher().publishEvent(new ModifiedChannelDefinitionPortalEvent(definition, publisher, definition));
	    }

	    // Expire the cached channel registry
	    ChannelRegistryManager.expireCache();
	    
		return definition;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.IChannelPublishingService#removeChannelDefinition(org.jasig.portal.channel.IChannelDefinition, org.jasig.portal.security.IPerson)
	 */
	public void removeChannelDefinition(IChannelDefinition channelDefinition, IPerson person) {
		IChannelDefinition channelDef = channelRegistryStore.getChannelDefinition(channelDefinition.getId());
		
		// TODO
		// The following is a temporary fix to allow "deleted" channels to be re-created
		// with the same fname.  We are appending the existing fname with the current time
		// so that subsequent channel creations won't result in a duplicate fname error.
		channelDef.setFName(channelDef.getFName() + "_" + (new Date()).getTime());
		
	    channelRegistryStore.disapproveChannelDefinition(channelDef);

	    // Record that a channel has been deleted
	    EventPublisherLocator.getApplicationEventPublisher().publishEvent(new RemovedChannelDefinitionPortalEvent(channelDef, person, channelDef));
	    
	    // Expire the cached channel registry
	    ChannelRegistryManager.expireCache();
	}

}
