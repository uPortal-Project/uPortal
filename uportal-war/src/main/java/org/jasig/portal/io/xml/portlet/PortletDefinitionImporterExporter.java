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
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityNameFinder;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.io.xml.AbstractJaxbDataHandler;
import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.dao.jpa.PortletDefinitionParameterImpl;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.om.IPortletDescriptorKey;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
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
import org.jasig.portal.utils.SafeFilenameUtils;
import org.jasig.portal.xml.PortletDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletDefinitionImporterExporter 
        extends AbstractJaxbDataHandler<ExternalPortletDefinition> 
        implements IPortletPublishingService {
	
    private PortletPortalDataType portletPortalDataType;
    private IPortletTypeRegistry portletTypeRegistry;
    private IPortletDefinitionDao portletDefinitionDao;
    private IPortletCategoryRegistry portletCategoryRegistry;
    private boolean errorOnChannel = true;

    @Value("${org.jasig.portal.io.errorOnChannel}")
	public void setErrorOnChannel(boolean errorOnChannel) {
		this.errorOnChannel = errorOnChannel;
	}

	@Autowired
    public void setPortletPortalDataType(PortletPortalDataType portletPortalDataType) {
        this.portletPortalDataType = portletPortalDataType;
    }

    @Autowired
    public void setPortletTypeRegistry(IPortletTypeRegistry portletTypeRegistry) {
        this.portletTypeRegistry = portletTypeRegistry;
    }

    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionDao portletDefinitionRegistry) {
        this.portletDefinitionDao = portletDefinitionRegistry;
    }

    @Autowired
    public void setPortletCategoryRegistry(IPortletCategoryRegistry portletCategoryRegistry) {
        this.portletCategoryRegistry = portletCategoryRegistry;
    }
    
    @Override
    public Set<PortalDataKey> getImportDataKeys() {
        return Collections.singleton(PortletPortalDataType.IMPORT_40_DATA_KEY);
    }

    @Override
    public IPortalDataType getPortalDataType() {
        return this.portletPortalDataType;
    }

    @Override
    public Iterable<? extends IPortalData> getPortalData() {
        return this.portletDefinitionDao.getPortletDefinitions();
    }

    @Transactional
    @Override
    public void importData(ExternalPortletDefinition portletRep) {
    	final PortletDescriptor portletDescriptor = portletRep.getPortletDescriptor();
    	final Boolean isFramework = portletDescriptor.isIsFramework();

    	if (isFramework != null && isFramework && "UPGRADED_CHANNEL_IS_NOT_A_PORTLET".equals(portletDescriptor.getPortletName())) {
    		if (errorOnChannel) {
    			throw new IllegalArgumentException(portletRep.getFname() + " is not a portlet. It was likely an IChannel from a previous version of uPortal and cannot be imported.");
    		}

    		logger.warn(portletRep.getFname() + " is not a portlet. It was likely an IChannel from a previous version of uPortal and will not be imported.");
    		return;
    	}
    		
        // get the portlet type
        final IPortletType portletType = portletTypeRegistry.getPortletType(portletRep.getType());
        if (portletType == null) {
        	throw new IllegalArgumentException("No portlet type registered for: " + portletRep.getType());
        }
        
        final List<PortletCategory> categories = new ArrayList<PortletCategory>();
        for (String categoryName : portletRep.getCategories()) {
            EntityIdentifier[] cats = GroupService.searchForGroups(categoryName, IGroupConstants.IS, IPortletDefinition.class);
            
            PortletCategory category = null;
            if (cats != null && cats.length > 0) {
                category = portletCategoryRegistry.getPortletCategory(cats[0].getKey());
            }
            else {
                category = portletCategoryRegistry.getPortletCategory(categoryName);
            }
            
            if (category == null) {
                throw new IllegalArgumentException("No category '" + categoryName + "' found when importing porltet: " + portletRep.getFname());
            }
            
            categories.add(category);
        }

        final List<IGroupMember> groups = new ArrayList<IGroupMember>();
        for (String groupName : portletRep.getGroups()) {
            EntityIdentifier[] gs = GroupService.searchForGroups(groupName, IGroupConstants.IS, IPerson.class);
            IGroupMember group;
            if (gs != null && gs.length > 0) {
                group = GroupService.findGroup(gs[0].getKey());
            } else {
                // An actual group key might be specified, so try looking up group directly
                group = GroupService.findGroup(groupName);
            }
            
            if (group == null) {
                throw new IllegalArgumentException("No group '" + groupName + "' found when importing porltet: " + portletRep.getFname());
            }
            
            groups.add(group);
        }
        
        
        final String fname = portletRep.getFname();
        IPortletDefinition def = portletDefinitionDao.getPortletDefinitionByFname(fname);
        if (def == null) {
            def = portletDefinitionDao.createPortletDefinition(
                    portletType, 
                    fname, 
                    portletRep.getName(),
                    portletRep.getTitle(), 
                    portletDescriptor.getWebAppName(), 
                    portletDescriptor.getPortletName(), 
                    isFramework != null ? isFramework : false);
        }
        else {
            final IPortletDescriptorKey portletDescriptorKey = def.getPortletDescriptorKey();
            portletDescriptorKey.setPortletName(portletDescriptor.getPortletName());
            if (isFramework != null && isFramework) {
                portletDescriptorKey.setFrameworkPortlet(isFramework);
            }
            else {
                portletDescriptorKey.setWebAppName(portletDescriptor.getWebAppName());
            }
        }
        
        def.setName(portletRep.getName());
        def.setTitle(portletRep.getTitle());
        def.setDescription(portletRep.getDesc());
        final BigInteger timeout = portletRep.getTimeout();
        if (timeout != null) {
            def.setTimeout(timeout.intValue());
        }
        final BigInteger actionTimeout = portletRep.getActionTimeout();
        if (actionTimeout != null) {
            def.setActionTimeout(actionTimeout.intValue());
        }
        final BigInteger eventTimeout = portletRep.getEventTimeout();
        if (eventTimeout != null) {
            def.setEventTimeout(eventTimeout.intValue());
        }
        final BigInteger renderTimeout = portletRep.getRenderTimeout();
        if (renderTimeout != null) {
            def.setRenderTimeout(renderTimeout.intValue());
        }
        final BigInteger resourceTimeout = portletRep.getResourceTimeout();
        if (resourceTimeout != null) {
            def.setResourceTimeout(resourceTimeout.intValue());
        }
        def.setType(portletType);
        
        Date now = new Date();
        IPerson systemUser = PersonFactory.createSystemPerson();
        def.setApprovalDate(now);
        def.setApproverId(systemUser.getID());
        def.setPublishDate(now);
        def.setPublisherId(systemUser.getID());     
        
        
        final Set<IPortletDefinitionParameter> parameters = new LinkedHashSet<IPortletDefinitionParameter>();
        for (ExternalPortletParameter param : portletRep.getParameters()) {
            parameters.add(new PortletDefinitionParameterImpl(param.getName(), param.getValue()));
        }
        def.setParameters(parameters);
        
        final ArrayList<IPortletPreference> preferenceList = new ArrayList<IPortletPreference>();
        for (ExternalPortletPreference pref : portletRep.getPortletPreferences()) {
            final List<String> valueList = pref.getValues();
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
     * @see org.jasig.portal.io.xml.IDataImporter#deleteData(java.lang.String)
     */
    @Transactional
    @Override
	public ExternalPortletDefinition deleteData(String fname) {
    	final IPortletDefinition def = this.portletDefinitionDao.getPortletDefinitionByFname(fname);
    	if(null == def) {
    		return null;
    	}
    	
		ExternalPortletDefinition result = convert(def);
		this.portletDefinitionDao.deletePortletDefinition(def);
		return result;
	}

    private final Object groupUpdateLock = new Object();
    
	/*
     * (non-Javadoc)
     * @see org.jasig.portal.channel.IChannelPublishingService#saveChannelDefinition(org.jasig.portal.portlet.om.IPortletDefinition, org.jasig.portal.security.IPerson, org.jasig.portal.channel.ChannelLifecycleState, java.util.Date, java.util.Date, org.jasig.portal.ChannelCategory[], org.jasig.portal.groups.IGroupMember[])
     */
    @Override
    public IPortletDefinition savePortletDefinition(IPortletDefinition definition, IPerson publisher, List<PortletCategory> categories, List<IGroupMember> groupMembers) {
        boolean newChannel = (definition.getPortletDefinitionId() == null);

        // save the channel
        definition = portletDefinitionDao.updatePortletDefinition(definition);
        definition = portletDefinitionDao.getPortletDefinitionByFname(definition.getFName());

        final String defId = definition.getPortletDefinitionId().getStringId();
        final IEntity portletDefEntity = GroupService.getEntity(defId, IPortletDefinition.class);

        //Sync on groups during update. This really should be a portal wide thread-safety check or
        //The groups service needs to deal with concurrent modification better.
        synchronized(this.groupUpdateLock) {
            // Delete existing category memberships for this channel
            if (!newChannel) {
                @SuppressWarnings("unchecked")
                final Iterator<IEntityGroup> iter = portletDefEntity.getAllContainingGroups();
                while (iter.hasNext()) {
                    final IEntityGroup group = iter.next();
                    group.removeMember(portletDefEntity);
                    group.update();
                }
            }
    
            // For each category ID, add channel to category
            for (PortletCategory category : categories) {
                final IEntityGroup categoryGroup = GroupService.findGroup(category.getId());
                categoryGroup.addMember(portletDefEntity);
                categoryGroup.updateMembers();
            }
    
            // Set groups
            final AuthorizationService authService = AuthorizationService.instance();
            final String target = IPermission.PORTLET_PREFIX + defId;
    
            final IUpdatingPermissionManager upm = authService.newUpdatingPermissionManager(FRAMEWORK_OWNER);
            final List<IPermission> permissions = new ArrayList<IPermission>(groupMembers.size());
            for (final IGroupMember member : groupMembers) {
                final IAuthorizationPrincipal authPrincipal = authService.newPrincipal(member);
                final IPermission permission = upm.newPermission(authPrincipal);
                permission.setType(GRANT_PERMISSION_TYPE);
                permission.setActivity(SUBSCRIBER_ACTIVITY);
                permission.setTarget(target);
                permissions.add(permission);
            }
    
            // If modifying the channel, remove the existing permissions before adding the new ones
            if (!newChannel) {
                IPermission[] oldPermissions = upm.getPermissions(SUBSCRIBER_ACTIVITY, target);
                upm.removePermissions(oldPermissions);
            }
            upm.addPermissions(permissions.toArray(new IPermission[permissions.size()]));
        }

        if (logger.isDebugEnabled()) {
            logger.debug( "Portlet " + defId + " has been " + 
                    (newChannel ? "published" : "modified") + ".");
        }

        return definition;
    }

    @Transactional
    @Override
    public void removePortletDefinition(IPortletDefinition portletDefinition, IPerson person) {
        IPortletDefinition portletDef = portletDefinitionDao.getPortletDefinition(portletDefinition.getPortletDefinitionId());

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
        portletDefinitionDao.deletePortletDefinition(portletDef);
    }

    @Override
    public ExternalPortletDefinition exportData(String fname) {
        final IPortletDefinition def = this.portletDefinitionDao.getPortletDefinitionByFname(fname);
        if (def == null) {
            return null;
        }
        
        return convert(def);
    }
    
    
    
    @Override
    public String getFileName(ExternalPortletDefinition data) {
        return SafeFilenameUtils.makeSafeFilename(data.getFname());
    }
    
    protected BigInteger convertToBigInteger(Integer i) {
        if (i == null) {
            return null;
        }
        
        return BigInteger.valueOf(i);
    }

    protected ExternalPortletDefinition convert(IPortletDefinition def) {
        ExternalPortletDefinition rep = new ExternalPortletDefinition();
         
        rep.setVersion("4.0");
         
        rep.setFname(def.getFName());
        rep.setDesc(def.getDescription());
        rep.setName(def.getName());
        rep.setTimeout(BigInteger.valueOf(def.getTimeout()));
        rep.setActionTimeout(convertToBigInteger(def.getActionTimeout()));
        rep.setEventTimeout(convertToBigInteger(def.getEventTimeout()));
        rep.setRenderTimeout(convertToBigInteger(def.getRenderTimeout()));
        rep.setResourceTimeout(convertToBigInteger(def.getResourceTimeout()));
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
         
         
        final List<ExternalPortletParameter> parameterList = rep.getParameters();
        for (IPortletDefinitionParameter param : def.getParameters()) {
            final ExternalPortletParameter externalPortletParameter = new ExternalPortletParameter();
            externalPortletParameter.setName(param.getName());
            externalPortletParameter.setDescription(param.getDescription());
            externalPortletParameter.setValue(param.getValue());
            parameterList.add(externalPortletParameter);
        }

         
        final List<ExternalPortletPreference> portletPreferenceList = rep.getPortletPreferences();
        for (IPortletPreference pref : def.getPortletPreferences()) {
            final ExternalPortletPreference externalPortletPreference = new ExternalPortletPreference();
            externalPortletPreference.setName(pref.getName());
            externalPortletPreference.setReadOnly(pref.isReadOnly());
             
            final List<String> value = externalPortletPreference.getValues();
            value.addAll(Arrays.asList(pref.getValues()));
             
            portletPreferenceList.add(externalPortletPreference);
        }
         
         
        final List<String> categoryList = rep.getCategories();
        final IGroupMember gm = GroupService.getGroupMember(def.getPortletDefinitionId().getStringId(), IPortletDefinition.class);
        @SuppressWarnings("unchecked")
        final Iterator<IEntityGroup> categories = GroupService.getCompositeGroupService().findContainingGroups(gm);
        while (categories.hasNext()) {
            IEntityGroup category = categories.next();
            categoryList.add(category.getName());
        }
        
         
         
        final List<String> groupList = rep.getGroups();
        final List<String> userList = rep.getUsers();
        
        final AuthorizationService authService = org.jasig.portal.services.AuthorizationService.instance();
        final IPermissionManager pm = authService.newPermissionManager("UP_PORTLET_SUBSCRIBE");
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
