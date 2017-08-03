/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.io.xml.portlet;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.IUserIdentityStore;
import org.apereo.portal.groups.IEntity;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IEntityNameFinder;
import org.apereo.portal.groups.IGroupConstants;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.io.xml.AbstractJaxbDataHandler;
import org.apereo.portal.io.xml.IPortalData;
import org.apereo.portal.io.xml.IPortalDataType;
import org.apereo.portal.io.xml.PortalDataKey;
import org.apereo.portal.io.xml.portlettype.ExternalPermissionDefinition;
import org.apereo.portal.portlet.dao.IMarketplaceRatingDao;
import org.apereo.portal.portlet.dao.IPortletDefinitionDao;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.om.IPortletDescriptorKey;
import org.apereo.portal.portlet.om.IPortletLifecycleEntry;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.apereo.portal.portlet.om.PortletCategory;
import org.apereo.portal.portlet.registry.IPortletCategoryRegistry;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPermissionManager;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IUpdatingPermissionManager;
import org.apereo.portal.security.PermissionHelper;
import org.apereo.portal.security.SystemPerson;
import org.apereo.portal.services.AuthorizationServiceFacade;
import org.apereo.portal.services.EntityNameFinderService;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.utils.SafeFilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class PortletDefinitionImporterExporter
        extends AbstractJaxbDataHandler<ExternalPortletDefinition>
        implements IPortletPublishingService {

    @Autowired private ExternalPortletDefinitionUnmarshaller portletDefinitionUnmarshaller;

    private PortletPortalDataType portletPortalDataType;
    private IPortletDefinitionDao portletDefinitionDao;
    private IPortletCategoryRegistry portletCategoryRegistry;
    private IUserIdentityStore userIdentityStore;

    @Autowired private IMarketplaceRatingDao marketplaceRatingDao;

    private String systemUsername = SystemPerson.INSTANCE.getUserName();

    @Autowired
    public void setPortletPortalDataType(PortletPortalDataType portletPortalDataType) {
        this.portletPortalDataType = portletPortalDataType;
    }

    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionDao portletDefinitionRegistry) {
        this.portletDefinitionDao = portletDefinitionRegistry;
    }

    @Autowired
    public void setPortletCategoryRegistry(IPortletCategoryRegistry portletCategoryRegistry) {
        this.portletCategoryRegistry = portletCategoryRegistry;
    }

    @Autowired
    public void setUserIdentityStore(IUserIdentityStore identityStore) {
        this.userIdentityStore = identityStore;
    }

    @Override
    public Set<PortalDataKey> getImportDataKeys() {
        return Collections.singleton(PortletPortalDataType.IMPORT_50_DATA_KEY);
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

        final IPortletDefinition def = portletDefinitionUnmarshaller.unmarshall(portletRep);

        final List<PortletCategory> categories = new ArrayList<>();
        for (String categoryName : portletRep.getCategories()) {
            EntityIdentifier[] cats =
                    GroupService.searchForGroups(
                            categoryName, IGroupConstants.IS, IPortletDefinition.class);

            PortletCategory category;
            if (cats != null && cats.length > 0) {
                category = portletCategoryRegistry.getPortletCategory(cats[0].getKey());
            } else {
                category = portletCategoryRegistry.getPortletCategory(categoryName);
            }
            if (category == null) {
                throw new IllegalArgumentException(
                        "No category '"
                                + categoryName
                                + "' found when importing portlet: "
                                + portletRep.getFname());
            }
            categories.add(category);
        }

        final String fname = portletRep.getFname();
        final Map<ExternalPermissionDefinition, Set<IGroupMember>> permissions = new HashMap<>();
        final Set<IGroupMember> subscribeMembers = toGroupMembers(portletRep.getGroups(), fname);
        permissions.put(ExternalPermissionDefinition.SUBSCRIBE, subscribeMembers);

        if (portletRep.getPermissions() != null
                && portletRep.getPermissions().getPermissions() != null) {
            for (ExternalPermissionMemberList perm : portletRep.getPermissions().getPermissions()) {
                Set<IGroupMember> members = toGroupMembers(perm.getGroups(), fname);
                ExternalPermissionDefinition permDef =
                        toExternalPermissionDefinition(perm.getSystem(), perm.getActivity());

                if (permissions.containsKey(permDef)) {
                    permissions.get(permDef).addAll(members);
                } else {
                    permissions.put(permDef, members);
                }
            }
        }

        savePortletDefinition(def, categories, permissions);
    }

    // Returns the username for a valid userId, else the system username
    private String getUsernameForUserId(int id) {
        if (id > 0) {
            String username = userIdentityStore.getPortalUserName(id);
            if (username != null) {
                return username;
            }
            logger.warn(
                    "Invalid userID {} found when exporting a portlet; return system username instead",
                    id);
        }
        return systemUsername;
    }

    /** {@link String} id argument is treated as the portlet fname. */
    @Transactional
    @Override
    public ExternalPortletDefinition deleteData(String fname) {
        final IPortletDefinition def = this.portletDefinitionDao.getPortletDefinitionByFname(fname);
        if (null == def) {
            return null;
        }

        ExternalPortletDefinition result = convert(def);
        this.portletDefinitionDao.deletePortletDefinition(def);
        return result;
    }

    private final Object groupUpdateLock = new Object();

    @Override
    public IPortletDefinition savePortletDefinition(
            IPortletDefinition definition,
            IPerson publisher,
            List<PortletCategory> categories,
            List<IGroupMember> groupMembers) {
        Map<ExternalPermissionDefinition, Set<IGroupMember>> permissions = new HashMap<>();
        permissions.put(ExternalPermissionDefinition.SUBSCRIBE, new HashSet<>(groupMembers));
        return savePortletDefinition(definition, categories, permissions);
    }

    /**
     * Save a portlet definition.
     *
     * @param definition the portlet definition
     * @param categories the list of categories for the portlet
     * @param permissionMap a map of permission name -> list of groups who are granted that
     *     permission (Note: for now, only grant is supported and only for the FRAMEWORK_OWNER perm
     *     manager)
     */
    private IPortletDefinition savePortletDefinition(
            IPortletDefinition definition,
            List<PortletCategory> categories,
            Map<ExternalPermissionDefinition, Set<IGroupMember>> permissionMap) {
        boolean newChannel = (definition.getPortletDefinitionId() == null);

        // save the channel
        definition = portletDefinitionDao.savePortletDefinition(definition);
        definition = portletDefinitionDao.getPortletDefinitionByFname(definition.getFName());

        final String defId = definition.getPortletDefinitionId().getStringId();
        final IEntity portletDefEntity = GroupService.getEntity(defId, IPortletDefinition.class);

        //Sync on groups during update. This really should be a portal wide thread-safety check or
        //The groups service needs to deal with concurrent modification better.
        synchronized (this.groupUpdateLock) {
            // Delete existing category memberships for this channel
            if (!newChannel) {
                for (IEntityGroup group : portletDefEntity.getAncestorGroups()) {
                    group.removeChild(portletDefEntity);
                    group.update();
                }
            }

            // For each category ID, add channel to category
            for (PortletCategory category : categories) {
                final IEntityGroup categoryGroup = GroupService.findGroup(category.getId());
                categoryGroup.addChild(portletDefEntity);
                categoryGroup.updateMembers();
            }

            // Set groups
            final AuthorizationServiceFacade authService = AuthorizationServiceFacade.instance();
            final String target =
                    PermissionHelper.permissionTargetIdForPortletDefinition(definition);

            // Loop over the affected permission managers...
            Map<String, Collection<ExternalPermissionDefinition>> permissionsBySystem =
                    getPermissionsBySystem(permissionMap.keySet());
            for (String system : permissionsBySystem.keySet()) {
                Collection<ExternalPermissionDefinition> systemPerms =
                        permissionsBySystem.get(system);

                // get the permission manager for this system...
                final IUpdatingPermissionManager upm =
                        authService.newUpdatingPermissionManager(system);
                final List<IPermission> permissions = new ArrayList<>();

                // add activity grants for each permission..
                for (ExternalPermissionDefinition permissionDef : systemPerms) {

                    Set<IGroupMember> members = permissionMap.get(permissionDef);
                    for (final IGroupMember member : members) {

                        final IAuthorizationPrincipal authPrincipal =
                                authService.newPrincipal(member);
                        final IPermission permEntity = upm.newPermission(authPrincipal);
                        permEntity.setType(IPermission.PERMISSION_TYPE_GRANT);
                        permEntity.setActivity(permissionDef.getActivity());
                        permEntity.setTarget(target);
                        permissions.add(permEntity);
                    }
                }

                // If modifying the channel, remove the existing permissions before adding the new ones
                if (!newChannel) {
                    for (ExternalPermissionDefinition permissionName : permissionMap.keySet()) {
                        IPermission[] oldPermissions =
                                upm.getPermissions(permissionName.getActivity(), target);
                        upm.removePermissions(oldPermissions);
                    }
                }
                upm.addPermissions(permissions.toArray(new IPermission[permissions.size()]));
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Portlet "
                            + defId
                            + " has been "
                            + (newChannel ? "published" : "modified")
                            + ".");
        }

        return definition;
    }

    @Transactional
    @Override
    public void removePortletDefinition(IPortletDefinition portletDefinition, IPerson person) {
        IPortletDefinition portletDef =
                portletDefinitionDao.getPortletDefinition(
                        portletDefinition.getPortletDefinitionId());

        // Delete existing category memberships for this portlet
        String portletDefinitionId = portletDefinition.getPortletDefinitionId().getStringId();
        IEntity channelDefEntity =
                GroupService.getEntity(portletDefinitionId, IPortletDefinition.class);
        for (IEntityGroup group : channelDefEntity.getAncestorGroups()) {
            group.removeChild(channelDefEntity);
            group.update();
        }

        // Delete permissions records that refer to this portlet
        AuthorizationServiceFacade authService = AuthorizationServiceFacade.instance();
        String target = PermissionHelper.permissionTargetIdForPortletDefinition(portletDefinition);
        IUpdatingPermissionManager upm =
                authService.newUpdatingPermissionManager(IPermission.PORTAL_SUBSCRIBE);
        IPermission[] oldPermissions = upm.getPermissionsForTarget(target);
        upm.removePermissions(oldPermissions);

        // Delete any ratings (incl. reviews) associated with the portlet
        marketplaceRatingDao.clearRatingsForPortlet(portletDef);

        //Delete the portlet itself.
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

    private BigInteger convertToBigInteger(Integer i) {
        if (i == null) {
            return null;
        }

        return BigInteger.valueOf(i);
    }

    // Utility method to convert a date to a calendar.
    private static Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    protected ExternalPortletDefinition convert(IPortletDefinition def) {
        ExternalPortletDefinition rep = new ExternalPortletDefinition();

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

        final Lifecycle lifecycle = new Lifecycle();
        for (IPortletLifecycleEntry ple : def.getLifecycle()) {
            final LifecycleEntry entry = new LifecycleEntry();
            entry.setName(ple.getLifecycleState().toString());
            entry.setUser(getUsernameForUserId(ple.getUserId()));
            entry.setValue(getCalendar(ple.getDate()));
            lifecycle.getEntries().add(entry);
        }
        rep.setLifecycle(lifecycle);

        final org.apereo.portal.xml.PortletDescriptor portletDescriptor =
                new org.apereo.portal.xml.PortletDescriptor();
        final IPortletDescriptorKey portletDescriptorKey = def.getPortletDescriptorKey();
        if (portletDescriptorKey.isFrameworkPortlet()) {
            portletDescriptor.setIsFramework(true);
        } else {
            portletDescriptor.setWebAppName(portletDescriptorKey.getWebAppName());
        }
        portletDescriptor.setPortletName(portletDescriptorKey.getPortletName());
        rep.setPortletDescriptor(portletDescriptor);

        final List<ExternalPortletParameter> parameterList = rep.getParameters();
        for (IPortletDefinitionParameter param : def.getParameters()) {
            final ExternalPortletParameter externalPortletParameter =
                    new ExternalPortletParameter();
            externalPortletParameter.setName(param.getName());
            externalPortletParameter.setDescription(param.getDescription());
            externalPortletParameter.setValue(param.getValue());
            parameterList.add(externalPortletParameter);
        }
        parameterList.sort(ExternalPortletParameterNameComparator.INSTANCE);

        final List<ExternalPortletPreference> portletPreferenceList = rep.getPortletPreferences();
        for (IPortletPreference pref : def.getPortletPreferences()) {
            final ExternalPortletPreference externalPortletPreference =
                    new ExternalPortletPreference();
            externalPortletPreference.setName(pref.getName());
            externalPortletPreference.setReadOnly(pref.isReadOnly());

            final List<String> value = externalPortletPreference.getValues();
            value.addAll(Arrays.asList(pref.getValues()));
            //no sorting of preference values, order is specified by the portlet

            portletPreferenceList.add(externalPortletPreference);
        }
        portletPreferenceList.sort(ExternalPortletPreferenceNameComparator.INSTANCE);

        final List<String> categoryList = rep.getCategories();
        final IGroupMember gm =
                GroupService.getGroupMember(
                        def.getPortletDefinitionId().getStringId(), IPortletDefinition.class);
        @SuppressWarnings("unchecked")
        final Iterator<IEntityGroup> categories =
                GroupService.getCompositeGroupService().findParentGroups(gm);
        while (categories.hasNext()) {
            IEntityGroup category = categories.next();
            categoryList.add(category.getName());
        }
        Collections.sort(categoryList);

        // handle the SUBSCRIBER_ACTIVITY perm separately...
        final List<String> groupList = rep.getGroups();
        final List<String> userList = rep.getUsers();
        exportPermission(def, ExternalPermissionDefinition.SUBSCRIBE, groupList, userList);

        // handle other supported perms (currently just BROWSE)
        ExternalPermissions externalPermissions = new ExternalPermissions();
        for (ExternalPermissionDefinition perm : ExternalPermissionDefinition.values()) {
            if (!perm.getExportForPortletDef()) {
                continue;
            }

            ExternalPermissionMemberList members = new ExternalPermissionMemberList();
            members.setSystem(perm.getSystem());
            members.setActivity(perm.getActivity());
            List<String> groups = members.getGroups();

            boolean found = exportPermission(def, perm, groups, null);
            if (found) {
                externalPermissions.getPermissions().add(members);
            }
        }

        if (!externalPermissions.getPermissions().isEmpty()) {
            rep.setPermissions(externalPermissions);
        }

        return rep;
    }

    private boolean exportPermission(
            IPortletDefinition def,
            ExternalPermissionDefinition permDef,
            List<String> groupList,
            List<String> userList) {
        final AuthorizationServiceFacade authService = AuthorizationServiceFacade.instance();
        final IPermissionManager pm = authService.newPermissionManager(permDef.getSystem());
        final String portletTargetId = PermissionHelper.permissionTargetIdForPortletDefinition(def);
        final IAuthorizationPrincipal[] principals =
                pm.getAuthorizedPrincipals(permDef.getActivity(), portletTargetId);
        boolean permAdded = false;

        for (IAuthorizationPrincipal principal : principals) {
            IGroupMember member = authService.getGroupMember(principal);
            if (member.isGroup()) {
                final EntityNameFinderService entityNameFinderService =
                        EntityNameFinderService.instance();
                final IEntityNameFinder nameFinder =
                        entityNameFinderService.getNameFinder(member.getType());
                try {
                    groupList.add(nameFinder.getName(member.getKey()));
                    permAdded = true;
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Could not find group name for entity: " + member.getKey(), e);
                }
            } else {
                if (userList != null) {
                    userList.add(member.getKey());
                    permAdded = true;
                }
            }
        }

        Collections.sort(groupList);
        if (userList != null) {
            Collections.sort(userList);
        }

        return permAdded;
    }

    /**
     * Convert a list of group names to a list of groups.
     *
     * @param groupNames the list of group names
     * @return the list of groups.
     */
    private Set<IGroupMember> toGroupMembers(List<String> groupNames, String fname) {
        final Set<IGroupMember> groups = new HashSet<>();
        for (String groupName : groupNames) {
            EntityIdentifier[] gs =
                    GroupService.searchForGroups(groupName, IGroupConstants.IS, IPerson.class);
            IGroupMember group;
            if (gs != null && gs.length > 0) {
                group = GroupService.findGroup(gs[0].getKey());
            } else {
                // An actual group key might be specified, so try looking up group directly
                group = GroupService.findGroup(groupName);
            }

            if (group == null) {
                throw new IllegalArgumentException(
                        "No group '" + groupName + "' found when importing portlet: " + fname);
            }

            groups.add(group);
        }

        return groups;
    }

    /**
     * Check that a permission type from the XML file matches with a real permission.
     *
     * @param system The name of the permission manager
     * @param activity The name of the permission to search for.
     * @return the permission type string to use
     * @throws IllegalArgumentException if an unsupported permission type is specified
     */
    private ExternalPermissionDefinition toExternalPermissionDefinition(
            String system, String activity) {
        ExternalPermissionDefinition def = ExternalPermissionDefinition.find(system, activity);
        if (def != null) {
            return def;
        }

        String delim = "";
        StringBuilder buffer = new StringBuilder();
        for (ExternalPermissionDefinition perm : ExternalPermissionDefinition.values()) {
            buffer.append(delim);
            buffer.append(perm.toString());
            delim = ", ";
        }

        throw new IllegalArgumentException(
                "Permission type "
                        + system
                        + "."
                        + activity
                        + " is not supported.  "
                        + "The only supported permissions at this time are: "
                        + buffer.toString());
    }

    private Map<String, Collection<ExternalPermissionDefinition>> getPermissionsBySystem(
            Set<ExternalPermissionDefinition> perms) {
        Map<String, Collection<ExternalPermissionDefinition>> mappedPerms = new HashMap<>();
        for (ExternalPermissionDefinition perm : perms) {
            if (!mappedPerms.containsKey(perm.getSystem())) {
                mappedPerms.put(perm.getSystem(), new ArrayList<>());
            }

            mappedPerms.get(perm.getSystem()).add(perm);
        }

        return mappedPerms;
    }
}
