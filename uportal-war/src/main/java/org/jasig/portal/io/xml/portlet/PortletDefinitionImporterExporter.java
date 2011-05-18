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

package org.jasig.portal.io.xml.portlet;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.channel.IPortletPublishingService;
import org.jasig.portal.events.support.ModifiedPortletDefinitionPortalEvent;
import org.jasig.portal.events.support.PublishedPortletDefinitionPortalEvent;
import org.jasig.portal.events.support.RemovedPortletDefinitionPortalEvent;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityNameFinder;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.io.xml.AbstractJaxbIDataImporterExporter;
import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.om.IPortletDescriptorKey;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletDefinitionImporterExporter extends AbstractJaxbIDataImporterExporter<ExternalPortletDefinition> implements IPortletPublishingService, ApplicationEventPublisherAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IPortletTypeRegistry portletTypeRegistry;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletCategoryRegistry portletCategoryRegistry;
    private ApplicationEventPublisher applicationEventPublisher;
    
    

    @Autowired
    public void setPortletTypeRegistry(IPortletTypeRegistry portletTypeRegistry) {
        this.portletTypeRegistry = portletTypeRegistry;
    }

    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
    public void setPortletCategoryRegistry(IPortletCategoryRegistry portletCategoryRegistry) {
        this.portletCategoryRegistry = portletCategoryRegistry;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    @Override
    public PortalDataKey getImportDataKey() {
        return PortletDefinitionPortalDataType.IMPORT_DATA_KEY;
    }

    @Override
    public IPortalDataType getPortalDataType() {
        return PortletDefinitionPortalDataType.INSTANCE;
    }

    @Override
    public Set<IPortalData> getPortalData() {
        final List<IPortletDefinition> portletDefinitions = this.portletDefinitionRegistry.getAllPortletDefinitions();
        final Set<IPortalData> portalData = new LinkedHashSet<IPortalData>(portletDefinitions);
        return Collections.unmodifiableSet(portalData);
    }

    @Transactional
    @Override
    public void importData(ExternalPortletDefinition portletRep) {
        // get the portlet type
        IPortletType portletType = portletTypeRegistry.getPortletType(portletRep.getType());
        
        final List<PortletCategory> categories = new ArrayList<PortletCategory>();
        for (String categoryName : portletRep.getCategory()) {
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

        final List<IGroupMember> groups = new ArrayList<IGroupMember>();
        for (String groupName : portletRep.getGroup()) {
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
        final org.jasig.portal.xml.PortletDescriptor portletDescriptor = portletRep.getPortletDescriptor();
        final Boolean isFramework = portletDescriptor.isIsFramework();
        if (def == null) {
            def = portletDefinitionRegistry.createPortletDefinition(
                    portletType, 
                    portletRep.getFname(), 
                    portletRep.getName(),
                    portletRep.getTitle(), 
                    portletDescriptor.getWebAppName(), 
                    portletDescriptor.getPortletName(), 
                    isFramework != null ? isFramework : false);
        } else {
            def.getPortletDescriptorKey().setPortletName(portletDescriptor.getPortletName());
            if (isFramework != null && isFramework) {
                def.getPortletDescriptorKey().setFrameworkPortlet(isFramework);
            } else {
                def.getPortletDescriptorKey().setWebAppName(portletDescriptor.getWebAppName());
            }
        }
        
        def.setName(portletRep.getName());
        def.setTitle(portletRep.getTitle());
        def.setDescription(portletRep.getDesc());
        final BigInteger timeout = portletRep.getTimeout();
        if (timeout != null) {
            def.setTimeout(timeout.intValue());
        }
        def.setType(portletType);
        
        Date now = new Date();
        IPerson systemUser = PersonFactory.createSystemPerson();
        def.setApprovalDate(now);
        def.setApproverId(systemUser.getID());
        def.setPublishDate(now);
        def.setPublisherId(systemUser.getID());     
        
        def.clearParameters();
        for (ExternalPortletParameter param : portletRep.getParameter()) {
            def.addParameter(param.getName(), param.getValue());
        }
        
        final ArrayList<IPortletPreference> preferenceList = new ArrayList<IPortletPreference>();
        for (ExternalPortletPreference pref : portletRep.getPortletPreference()) {
            final List<String> valueList = pref.getValue();
            final String[] values = valueList.toArray(new String[valueList.size()]);
            
            final Boolean readOnly = pref.isReadOnly();
            preferenceList.add(
                    new PortletPreferenceImpl(
                            pref.getName(), 
                            readOnly != null ? readOnly : false, 
                            values));
        }
        def.setPortletPreferences(preferenceList);
        
        savePortletDefinition(def, PersonFactory.createSystemPerson(), categories, groups);
    }

    /**
     * {@link String} id argument is treated as the portlet fname.
     * 
     * (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataImporterExporter#deleteData(java.lang.String)
     */
    @Transactional
    @Override
	public ExternalPortletDefinition deleteData(String fname) {
    	final IPortletDefinition def = this.portletDefinitionRegistry.getPortletDefinitionByFname(fname);
    	if(null == def) {
    		return null;
    	} else {
    		ExternalPortletDefinition result = convert(def);
    		this.portletDefinitionRegistry.deletePortletDefinition(def);
    		return result;
    	}
	}

	/*
     * (non-Javadoc)
     * @see org.jasig.portal.channel.IChannelPublishingService#saveChannelDefinition(org.jasig.portal.channel.IChannelDefinition, org.jasig.portal.security.IPerson, org.jasig.portal.channel.ChannelLifecycleState, java.util.Date, java.util.Date, org.jasig.portal.ChannelCategory[], org.jasig.portal.groups.IGroupMember[])
     */
    @Transactional
    @Override
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
            IEntityGroup group = iter.next();
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

        if (logger.isDebugEnabled()) {
            logger.debug( "Portlet " + defId + " has been " + 
                    (newChannel ? "published" : "modified") + ".");
        }

        // Record that a channel has been published or modified
        //TODO move these events into the portlet definition registry
        if (newChannel) {
            applicationEventPublisher.publishEvent(new PublishedPortletDefinitionPortalEvent(definition, publisher, definition));
        } else {
            applicationEventPublisher.publishEvent(new ModifiedPortletDefinitionPortalEvent(definition, publisher, definition));
        }

        return definition;
    }

    @Transactional
    @Override
    public void removePortletDefinition(IPortletDefinition portletDefinition, IPerson person) {
        IPortletDefinition portletDef = portletDefinitionRegistry.getPortletDefinition(portletDefinition.getPortletDefinitionId());

        // Delete existing category memberships for this channel
        String portletDefinitionId = portletDefinition.getPortletDefinitionId().getStringId();
        IEntity channelDefEntity = GroupService.getEntity(portletDefinitionId, IPortletDefinition.class);
        @SuppressWarnings("unchecked")
        Iterator<IEntityGroup> iter = channelDefEntity.getAllContainingGroups();
        while (iter.hasNext()) {
            IEntityGroup group = iter.next();
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
        //TODO move these events into the portlet definition registry
        applicationEventPublisher.publishEvent(new RemovedPortletDefinitionPortalEvent(portletDef, person, portletDef));
    }

    @Override
    public ExternalPortletDefinition exportData(String fname) {
        final IPortletDefinition def = this.portletDefinitionRegistry.getPortletDefinitionByFname(fname);
        if (def == null) {
            return null;
        }
        
        return convert(def);
    }
    
    protected ExternalPortletDefinition convert(IPortletDefinition def) {
    	 ExternalPortletDefinition rep = new ExternalPortletDefinition();
         
         rep.setVersion("4.0");
         
         rep.setFname(def.getFName());
         rep.setDesc(def.getDescription());
         rep.setName(def.getName());
         rep.setTimeout(BigInteger.valueOf(def.getTimeout()));
         rep.setTitle(def.getTitle());
         rep.setType(def.getType().getName());
         
         
         final org.jasig.portal.xml.PortletDescriptor portletDescriptor = new org.jasig.portal.xml.PortletDescriptor();
         final IPortletDescriptorKey portletDescriptorKey = def.getPortletDescriptorKey();
         if (portletDescriptorKey.isFrameworkPortlet()) {
             portletDescriptor.setIsFramework(true);
         }
         else {
             portletDescriptor.setWebAppName(portletDescriptorKey.getWebAppName());
         }
         portletDescriptor.setPortletName(portletDescriptorKey.getPortletName());
         rep.setPortletDescriptor(portletDescriptor);
         
         
         final List<ExternalPortletParameter> parameterList = rep.getParameter();
         for (IPortletDefinitionParameter param : def.getParameters()) {
             final ExternalPortletParameter externalPortletParameter = new ExternalPortletParameter();
             externalPortletParameter.setName(param.getName());
             externalPortletParameter.setDescription(param.getDescription());
             externalPortletParameter.setValue(param.getValue());
             parameterList.add(externalPortletParameter);
         }

         
         final List<ExternalPortletPreference> portletPreferenceList = rep.getPortletPreference();
         final IPortletPreferences portletPreferences = def.getPortletPreferences();
         for (IPortletPreference pref : portletPreferences.getPortletPreferences()) {
             final ExternalPortletPreference externalPortletPreference = new ExternalPortletPreference();
             externalPortletPreference.setName(pref.getName());
             externalPortletPreference.setReadOnly(pref.isReadOnly());
             
             final List<String> value = externalPortletPreference.getValue();
             value.addAll(Arrays.asList(pref.getValues()));
             
             portletPreferenceList.add(externalPortletPreference);
         }
         
         
         final List<String> categoryList = rep.getCategory();
         final IGroupMember gm = GroupService.getGroupMember(def.getPortletDefinitionId().getStringId(), IPortletDefinition.class);
         final Iterator<IEntityGroup> categories = GroupService.getCompositeGroupService().findContainingGroups(gm);
         while (categories.hasNext()) {
        	IEntityGroup category = categories.next();
        	categoryList.add(category.getName());
         }
        
         
         
         final List<String> groupList = rep.getGroup();
         final List<String> userList = rep.getUser();
         
         final AuthorizationService authService = org.jasig.portal.services.AuthorizationService.instance();
         final IPermissionManager pm = authService.newPermissionManager("UP_FRAMEWORK");
         final IAuthorizationPrincipal[] principals = pm.getAuthorizedPrincipals("SUBSCRIBE", IPermission.PORTLET_PREFIX + def.getPortletDefinitionId().getStringId());
         
         for (IAuthorizationPrincipal principal : principals) {
             IGroupMember member = authService.getGroupMember(principal);
             if (member.isGroup()) {
                 final EntityNameFinderService entityNameFinderService = EntityNameFinderService.instance();
                 final IEntityNameFinder nameFinder = entityNameFinderService.getNameFinder(member.getType());
                 try {
                     groupList.add(nameFinder.getName(member.getKey()));
                 }
                 catch (Exception e) {
                     throw new RuntimeException("Could not find group name for entity: " + member.getKey(), e);
                 }
             } else {
                 userList.add(member.getKey());
             }
         }
         
         return rep;
    }
}
