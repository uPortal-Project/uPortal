/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channel;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelCategory;
import org.jasig.portal.IChannelRegistryStore;
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
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Default implementation of IChannelPublishingService.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 */
public class ChannelPublishingServiceImpl implements IChannelPublishingService, ApplicationContextAware {

	private Log log = LogFactory.getLog(ChannelPublishingServiceImpl.class);

	private IChannelRegistryStore channelRegistryStore;
	
	/**
	 * Set the channel persistence service
	 * 
	 * @param channelRegistryStore
	 */
	public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
		this.channelRegistryStore = channelRegistryStore;
	}

	private ApplicationEventPublisher eventPublisher;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.eventPublisher = applicationContext;
	}

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

	    if (log.isDebugEnabled()) {
	        log.debug( "Channel " + definition.getId() + " has been " + 
	                (newChannel ? "published" : "modified") + ".");
	    }

	    // Record that a channel has been published or modified
	    if (newChannel) {
	    	eventPublisher.publishEvent(new PublishedChannelDefinitionPortalEvent(definition, publisher, definition));
	    } else {
	    	eventPublisher.publishEvent(new ModifiedChannelDefinitionPortalEvent(definition, publisher, definition));
	    }

		return definition;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.IChannelPublishingService#removeChannelDefinition(org.jasig.portal.channel.IChannelDefinition, org.jasig.portal.security.IPerson)
	 */
	public void removeChannelDefinition(IChannelDefinition channelDefinition, IPerson person) {
		IChannelDefinition channelDef = channelRegistryStore.getChannelDefinition(channelDefinition.getId());

	    // Delete existing category memberships for this channel
	    String chanKey = String.valueOf(channelDefinition.getId());
	    IEntity channelDefEntity = GroupService.getEntity(chanKey, IChannelDefinition.class);
		@SuppressWarnings("unchecked")
	    Iterator iter = channelDefEntity.getAllContainingGroups();
	    while (iter.hasNext()) {
	        IEntityGroup group = (IEntityGroup) iter.next();
	        group.removeMember(channelDefEntity);
	        group.update();
	    }

	    // remove permissions
	    AuthorizationService authService = AuthorizationService.instance();
	    String target = IPermission.CHANNEL_PREFIX + channelDefinition.getId();
	    IUpdatingPermissionManager upm = authService.newUpdatingPermissionManager(FRAMEWORK_OWNER);
	    IPermission[] oldPermissions = upm.getPermissions(SUBSCRIBER_ACTIVITY, target);
	    upm.removePermissions(oldPermissions);

	    // delete the channel
		channelRegistryStore.deleteChannelDefinition(channelDef);

	    // Record that a channel has been deleted
	    eventPublisher.publishEvent(new RemovedChannelDefinitionPortalEvent(channelDef, person, channelDef));

	}

}
