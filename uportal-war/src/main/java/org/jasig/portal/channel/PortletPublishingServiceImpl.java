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

package org.jasig.portal.channel;

import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.events.support.ModifiedPortletDefinitionPortalEvent;
import org.jasig.portal.events.support.PublishedPortletDefinitionPortalEvent;
import org.jasig.portal.events.support.RemovedPortletDefinitionPortalEvent;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.io.portlet.xml.ExternalPortletParameterRepresentation;
import org.jasig.portal.io.portlet.xml.ExternalPortletPreferenceRepresentation;
import org.jasig.portal.io.portlet.xml.ExternalPortletRepresentation;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletTypeRegistry;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IUpdatingPermissionManager;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.EntityNameFinderService;
import org.jasig.portal.services.GroupService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Default implementation of IChannelPublishingService.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 */
@Service("portletPublishingService")
public class PortletPublishingServiceImpl implements IPortletPublishingService, ApplicationContextAware {

	private Log log = LogFactory.getLog(PortletPublishingServiceImpl.class);

	private IPortletDefinitionRegistry portletDefinitionRegistry;
	
	/**
	 * Set the channel persistence service
	 * 
	 * @param channelRegistryStore
	 */
	@Autowired(required = true)
	public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
		this.portletDefinitionRegistry = portletDefinitionRegistry;
	}

	private IPortletTypeRegistry portletTypeRegistry;
	
	@Autowired(required = true)
	public void setPortletTypeRegistry(IPortletTypeRegistry portletTypeRegistry) {
		this.portletTypeRegistry = portletTypeRegistry;
	}
	
	private IPortletCategoryRegistry portletCategoryRegistry;

	@Autowired(required = true)
	public void setPortletCategoryRegistry(IPortletCategoryRegistry portletCategoryRegistry) {
		this.portletCategoryRegistry = portletCategoryRegistry;
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
	
	public ExternalPortletRepresentation getExternalPortlet(IPortletDefinition def) throws GroupsException, Exception {
		ExternalPortletRepresentation rep = new ExternalPortletRepresentation();
		rep.setFname(def.getFName());
		rep.setDesc(def.getDescription());
		rep.setHasAbout(def.hasAbout());
		rep.setHasEdit(def.isEditable());
		rep.setHasHelp(def.hasHelp());
		rep.setName(def.getName());
		rep.setTimeout(def.getTimeout());
		rep.setTitle(def.getTitle());
		rep.setType(def.getType().getName());
		
		for (IPortletDefinitionParameter param : def.getParameters()) {
			rep.addParameter(new ExternalPortletParameterRepresentation(param
					.getName(), param.getValue()));
		}

		for (IPortletPreference pref : def.getPortletPreferences()
				.getPortletPreferences()) {
			rep.addPreference(new ExternalPortletPreferenceRepresentation(pref
					.getName(), pref.isReadOnly(), Arrays.asList(pref.getValues())));
		}
		
        IGroupMember gm = GroupService.getGroupMember(def.getPortletDefinitionId().getStringId(), IPortletDefinition.class);
        Iterator<IEntityGroup> categories = GroupService.getCompositeGroupService().findContainingGroups(gm);
        for (IEntityGroup category = categories.next(); categories.hasNext();) {
        	rep.addCategory(category.getName());
        }
        
        AuthorizationService authService = org.jasig.portal.services.AuthorizationService.instance();
        IPermissionManager pm = authService.newPermissionManager("UP_FRAMEWORK");
        IAuthorizationPrincipal[] principals = pm.getAuthorizedPrincipals("SUBSCRIBE", IPermission.PORTLET_PREFIX + def.getPortletDefinitionId().getStringId());
        
        for (IAuthorizationPrincipal principal : principals) {
        	IGroupMember member = authService.getGroupMember(principal);
        	if (member.isGroup()) {
        		rep.addGroup(EntityNameFinderService.instance().getNameFinder(member.getType()).getName(member.getKey()));
        	} else {
        		rep.addGroup(member.getKey());
        	}
        }

		
		return rep;
	}
	
	public IPortletDefinition savePortletDefinition(ExternalPortletRepresentation portletRep) {
		
		// get the portlet type
		IPortletType portletType = portletTypeRegistry.getPortletType(portletRep.getType());

		List<PortletCategory> categories = new ArrayList<PortletCategory>();
		for (String categoryName : portletRep.getCategories()) {
	        EntityIdentifier[] cats = GroupService.searchForGroups(categoryName, IGroupConstants.IS, IPortletDefinition.class);
	        
	        PortletCategory category = null;
	        if (cats != null && cats.length > 0) {
	        	category = portletCategoryRegistry.getPortletCategory(cats[0].getKey());
	        }
	        
	        else {
	        	category = portletCategoryRegistry.getPortletCategory(categoryName);
	        }
        	
        	if (category != null) {
        		categories.add(category);
        	}
		}

		List<IGroupMember> groups = new ArrayList<IGroupMember>();
		for (String groupName : portletRep.getGroups()) {
	        EntityIdentifier[] gs = GroupService.searchForGroups(groupName, IGroupConstants.IS, IPerson.class);
	        IGroupMember group;
	        if (gs != null && gs.length > 0) {
	            group = GroupService.findGroup(gs[0].getKey());
	        } else {
	            // An actual group key might be specified, so try looking up group directly
	            group = GroupService.findGroup(groupName);
	        }
	        
	        if (group != null) {
	        	groups.add(group);
	        }
			
		}
		
		IPortletDefinition def = portletDefinitionRegistry
				.getPortletDefinitionByFname(portletRep.getFname());
		if (def == null) {
			def = portletDefinitionRegistry.createPortletDefinition(
					portletType, portletRep.getFname(), portletRep.getName(),
					portletRep.getTitle(), portletRep.getApplicationId(), portletRep.getPortletName(), portletRep.isFramework());
		}
		
		def.setName(portletRep.getName());
		def.setTitle(portletRep.getTitle());
		def.setDescription(portletRep.getDesc());
		def.setEditable(portletRep.isHasEdit());
		def.setHasAbout(portletRep.isHasAbout());
		def.setHasHelp(portletRep.isHasHelp());
		def.setTimeout(portletRep.getTimeout());
		def.setType(portletType);
		
		Date now = new Date();
		IPerson systemUser = PersonFactory.createSystemPerson();
		def.setApprovalDate(now);
		def.setApproverId(systemUser.getID());
		def.setPublishDate(now);
		def.setPublisherId(systemUser.getID());		
		
		def.clearParameters();
		for (ExternalPortletParameterRepresentation param : portletRep.getParameters()) {
			def.addParameter(param.getName(), param.getValue());
		}
		
		final ArrayList<IPortletPreference> preferenceList = new ArrayList<IPortletPreference>();
		for (ExternalPortletPreferenceRepresentation pref : portletRep.getPreferences()) {
			preferenceList.add(new PortletPreferenceImpl(pref.getName(), pref
					.isReadOnly(), pref.getValues().toArray(
					new String[pref.getValues().size()])));
		}	
		def.setPortletPreferences(preferenceList);
		
		return savePortletDefinition(def, PersonFactory.createSystemPerson(), categories, groups);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.IChannelPublishingService#saveChannelDefinition(org.jasig.portal.channel.IChannelDefinition, org.jasig.portal.security.IPerson, org.jasig.portal.channel.ChannelLifecycleState, java.util.Date, java.util.Date, org.jasig.portal.ChannelCategory[], org.jasig.portal.groups.IGroupMember[])
	 */
	public IPortletDefinition savePortletDefinition(IPortletDefinition definition, IPerson publisher, List<PortletCategory> categories, List<IGroupMember> groupMembers) {
		boolean newChannel = (definition.getPortletDefinitionId() == null);

	    // save the channel
		definition = portletDefinitionRegistry.updatePortletDefinition(definition);
		definition = portletDefinitionRegistry.getPortletDefinitionByFname(definition.getFName());

	    // Delete existing category memberships for this channel
	    String defId = String.valueOf(definition.getPortletDefinitionId().getStringId());
	    IEntity channelDefEntity = GroupService.getEntity(defId, IPortletDefinition.class);
		@SuppressWarnings("unchecked")
	    Iterator<IEntityGroup> iter = channelDefEntity.getAllContainingGroups();
	    while (iter.hasNext()) {
	        IEntityGroup group = (IEntityGroup) iter.next();
	        group.removeMember(channelDefEntity);
	        group.update();
	    }

	    // For each category ID, add channel to category
	    for (PortletCategory category : categories) {
	        IEntity portletDefEntity = GroupService.getEntity(defId, IPortletDefinition.class);
	        IEntityGroup categoryGroup = GroupService.findGroup(category.getId());
	        categoryGroup.addMember(portletDefEntity);
	        categoryGroup.updateMembers();
	    }

	    // Set groups
	    AuthorizationService authService = AuthorizationService.instance();
	    String target = IPermission.PORTLET_PREFIX + defId;
	    IUpdatingPermissionManager upm = authService.newUpdatingPermissionManager(FRAMEWORK_OWNER);
	    IPermission[] permissions = new IPermission[groupMembers.size()];
	    int i = 0;
	    for (IGroupMember member : groupMembers) {
	      IAuthorizationPrincipal authPrincipal = authService.newPrincipal(member);
	      permissions[i] = upm.newPermission(authPrincipal);
	      permissions[i].setType(GRANT_PERMISSION_TYPE);
	      permissions[i].setActivity(SUBSCRIBER_ACTIVITY);
	      permissions[i].setTarget(target);
	      i++;
	    }

	    // If modifying the channel, remove the existing permissions before adding the new ones
	    if (!newChannel) {
	      IPermission[] oldPermissions = upm.getPermissions(SUBSCRIBER_ACTIVITY, target);
	      upm.removePermissions(oldPermissions);
	    }
	    upm.addPermissions(permissions);

	    if (log.isDebugEnabled()) {
	        log.debug( "Portlet " + defId + " has been " + 
	                (newChannel ? "published" : "modified") + ".");
	    }

	    // Record that a channel has been published or modified
	    if (newChannel) {
	    	eventPublisher.publishEvent(new PublishedPortletDefinitionPortalEvent(definition, publisher, definition));
	    } else {
	    	eventPublisher.publishEvent(new ModifiedPortletDefinitionPortalEvent(definition, publisher, definition));
	    }

		return definition;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.IChannelPublishingService#removeChannelDefinition(org.jasig.portal.channel.IChannelDefinition, org.jasig.portal.security.IPerson)
	 */
	public void removePortletDefinition(IPortletDefinition portletDefinition, IPerson person) {
		IPortletDefinition portletDef = portletDefinitionRegistry.getPortletDefinition(portletDefinition.getPortletDefinitionId());

	    // Delete existing category memberships for this channel
	    String portletDefinitionId = portletDefinition.getPortletDefinitionId().getStringId();
	    IEntity channelDefEntity = GroupService.getEntity(portletDefinitionId, IPortletDefinition.class);
		@SuppressWarnings("unchecked")
	    Iterator iter = channelDefEntity.getAllContainingGroups();
	    while (iter.hasNext()) {
	        IEntityGroup group = (IEntityGroup) iter.next();
	        group.removeMember(channelDefEntity);
	        group.update();
	    }

	    // remove permissions
	    AuthorizationService authService = AuthorizationService.instance();
	    String target = IPermission.PORTLET_PREFIX + portletDefinitionId;
	    IUpdatingPermissionManager upm = authService.newUpdatingPermissionManager(FRAMEWORK_OWNER);
	    IPermission[] oldPermissions = upm.getPermissions(SUBSCRIBER_ACTIVITY, target);
	    upm.removePermissions(oldPermissions);

	    // delete the channel
	    portletDefinitionRegistry.deletePortletDefinition(portletDef);

	    // Record that a channel has been deleted
	    eventPublisher.publishEvent(new RemovedPortletDefinitionPortalEvent(portletDef, person, portletDef));

	}

}
