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
package org.apereo.portal.portlets.portletadmin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.WindowState;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.driver.PortalDriverContainerServices;
import org.apache.pluto.container.driver.PortletRegistryService;
import org.apache.pluto.container.om.portlet.DisplayName;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.apache.pluto.container.om.portlet.Supports;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.api.portlet.DelegateState;
import org.apereo.portal.api.portlet.DelegationActionResponse;
import org.apereo.portal.api.portlet.PortletDelegationDispatcher;
import org.apereo.portal.api.portlet.PortletDelegationLocator;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.io.xml.portlet.IPortletPublishingService;
import org.apereo.portal.layout.dlm.remoting.IGroupListHelper;
import org.apereo.portal.layout.dlm.remoting.JsonEntityBean;
import org.apereo.portal.portlet.PortletUtils;
import org.apereo.portal.portlet.dao.jpa.PortletDefinitionImpl;
import org.apereo.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.apereo.portal.portlet.delegation.jsp.RenderPortletTag;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.apereo.portal.portlet.om.IPortletType;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.om.PortletCategory;
import org.apereo.portal.portlet.om.PortletLifecycleState;
import org.apereo.portal.portlet.registry.IPortletCategoryRegistry;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portlet.registry.IPortletTypeRegistry;
import org.apereo.portal.portlet.rendering.IPortletRenderer;
import org.apereo.portal.portletpublishing.xml.MultiValuedPreferenceInputType;
import org.apereo.portal.portletpublishing.xml.Parameter;
import org.apereo.portal.portletpublishing.xml.ParameterInputType;
import org.apereo.portal.portletpublishing.xml.PortletPublishingDefinition;
import org.apereo.portal.portletpublishing.xml.Preference;
import org.apereo.portal.portletpublishing.xml.PreferenceInputType;
import org.apereo.portal.portletpublishing.xml.SingleValuedPreferenceInputType;
import org.apereo.portal.portletpublishing.xml.Step;
import org.apereo.portal.portlets.Attribute;
import org.apereo.portal.portlets.BooleanAttribute;
import org.apereo.portal.portlets.StringListAttribute;
import org.apereo.portal.portlets.fragmentadmin.FragmentAdministrationHelper;
import org.apereo.portal.portlets.groupselector.EntityEnum;
import org.apereo.portal.portlets.portletadmin.xmlsupport.IChannelPublishingDefinitionDao;
import org.apereo.portal.security.AuthorizationPrincipalHelper;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPermissionManager;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IUpdatingPermissionManager;
import org.apereo.portal.security.PermissionHelper;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.url.IPortalUrlBuilder;
import org.apereo.portal.url.IPortalUrlProvider;
import org.apereo.portal.url.IPortletUrlBuilder;
import org.apereo.portal.url.UrlType;
import org.apereo.portal.utils.ComparableExtractingComparator;
import org.apereo.portal.utils.Tuple;
import org.apereo.portal.xml.PortletDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;
import org.springframework.webflow.context.ExternalContext;

/** Helper methods for the portlet administration workflow. */
@Service
public final class PortletAdministrationHelper implements ServletContextAware {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String PORTLET_FNAME_FRAGMENT_ADMIN_PORTLET = "fragment-admin";

    /**
     * Enumeration of the portlet-related permissions that may be managed in the Portlet Manager.
     *
     * @since 5.0
     */
    /* package-private */ enum PortletPermissionsOnForm {
        SUBSCRIBE(IPermission.PORTAL_SUBSCRIBE, IPermission.PORTLET_SUBSCRIBER_ACTIVITY),
        BROWSE(IPermission.PORTAL_SUBSCRIBE, IPermission.PORTLET_BROWSE_ACTIVITY),
        CONFIGURE(IPermission.PORTAL_PUBLISH, IPermission.PORTLET_MODE_CONFIG);

        private final String owner;
        private final String activity;

        PortletPermissionsOnForm(String owner, String activity) {
            this.owner = owner;
            this.activity = activity;
        }

        public String getOwner() {
            return owner;
        }

        public String getActivity() {
            return activity;
        }
    }

    /*
     * Autowired beans listed alphabetically by type
     */
    @Autowired private FragmentAdministrationHelper fragmentAdminHelper;
    @Autowired private IAuthorizationService authorizationService;
    @Autowired private IChannelPublishingDefinitionDao portletPublishingDefinitionDao;
    @Autowired private IGroupListHelper groupListHelper;
    @Autowired private IPortalUrlProvider urlProvider;
    @Autowired private IPortletCategoryRegistry portletCategoryRegistry;
    @Autowired private IPortletDefinitionRegistry portletDefinitionRegistry;
    @Autowired private IPortletPublishingService portletPublishingService;
    @Autowired private IPortletTypeRegistry portletTypeRegistry;
    @Autowired private PortalDriverContainerServices portalDriverContainerServices;
    @Autowired private PortletDelegationLocator portletDelegationLocator;

    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Construct a new PortletDefinitionForm for the given IPortletDefinition id. If a
     * PortletDefinition matching this ID already exists, the form will be pre-populated with the
     * PortletDefinition's current configuration. If the PortletDefinition does not yet exist, a new
     * default form will be created.
     *
     * @param person user that is required to have related lifecycle permission
     * @param portletId identifier for the portlet definition
     * @return {@PortletDefinitionForm} with set values based on portlet definition or default
     *     category and principal if no definition is found
     */
    public PortletDefinitionForm createPortletDefinitionForm(IPerson person, String portletId) {
        logger.error("in createPortletDefinitionForm");
        IPortletDefinition def = portletDefinitionRegistry.getPortletDefinition(portletId);

        // create the new form
        final PortletDefinitionForm form;
        if (def != null) {
            // if this is a pre-existing portlet, set the category and permissions
            form = new PortletDefinitionForm(def);
            form.setId(def.getPortletDefinitionId().getStringId());

            // create a JsonEntityBean for each current category and add it
            // to our form bean's category list
            Set<PortletCategory> categories = portletCategoryRegistry.getParentCategories(def);
            for (PortletCategory cat : categories) {
                form.addCategory(new JsonEntityBean(cat));
            }

            addPrincipalPermissionsToForm(def, form);
        } else {
            form = createNewPortletDefinitionForm();
        }

        /* TODO:  Service-Layer Security Reboot (great need of refactoring with a community-approved plan in place) */
        // User must have SOME FORM of lifecycle permission over AT LEAST ONE
        // category in which this portlet resides;  lifecycle permissions are
        // hierarchical, so we'll test with the weakest.
        if (!hasLifecyclePermission(person, PortletLifecycleState.CREATED, form.getCategories())) {
            logger.warn(
                    "User '"
                            + person.getUserName()
                            + "' attempted to edit the following portlet without MANAGE permission:  "
                            + def);
            throw new SecurityException("Not Authorized");
        }

        return form;
    }

    /*
     * Add to the form SUBSCRIBE, BROWSE, and CONFIGURE activity permissions, along with their principals,
     * assigned to the portlet.
     */
    private void addPrincipalPermissionsToForm(IPortletDefinition def, PortletDefinitionForm form) {
        final String portletTargetId = PermissionHelper.permissionTargetIdForPortletDefinition(def);

        final Set<JsonEntityBean> principalBeans = new HashSet<>();
        Map<String, IPermissionManager> permManagers = new HashMap<>();
        for (PortletPermissionsOnForm perm : PortletPermissionsOnForm.values()) {
            if (!permManagers.containsKey(perm.getOwner())) {
                permManagers.put(
                        perm.getOwner(),
                        authorizationService.newPermissionManager(perm.getOwner()));
            }
            final IPermissionManager pm = permManagers.get(perm.getOwner());
            /* Obtain the principals that have permission for the activity on this portlet */
            final IAuthorizationPrincipal[] principals =
                    pm.getAuthorizedPrincipals(perm.getActivity(), portletTargetId);

            for (IAuthorizationPrincipal principal : principals) {
                JsonEntityBean principalBean;

                // first assume this is a group
                final IEntityGroup group = GroupService.findGroup(principal.getKey());
                if (group != null) {
                    // principal is a group
                    principalBean = new JsonEntityBean(group, EntityEnum.GROUP);
                } else {
                    // not a group, so it must be a person
                    final IGroupMember member = authorizationService.getGroupMember(principal);
                    principalBean = new JsonEntityBean(member, EntityEnum.PERSON);
                    // set the name
                    final String name = groupListHelper.lookupEntityName(principalBean);
                    principalBean.setName(name);
                }

                principalBeans.add(principalBean);

                form.addPermission(principalBean.getTypeAndIdHash() + "_" + perm.getActivity());
            }
        }
        form.setPrincipals(principalBeans, false);
    }

    /*
     * Create a {@code PortletDefinitionForm} and pre-populate it with default categories and principal permissions.
     */
    private PortletDefinitionForm createNewPortletDefinitionForm() {
        final PortletDefinitionForm form = new PortletDefinitionForm();

        // pre-populate with top-level category
        final IEntityGroup portletCategoriesGroup =
                GroupService.getDistinguishedGroup(IPortletDefinition.DISTINGUISHED_GROUP);
        form.addCategory(
                new JsonEntityBean(
                        portletCategoriesGroup,
                        groupListHelper.getEntityType(portletCategoriesGroup)));

        // pre-populate with top-level group
        final IEntityGroup everyoneGroup =
                GroupService.getDistinguishedGroup(IPerson.DISTINGUISHED_GROUP);
        final JsonEntityBean everyoneBean =
                new JsonEntityBean(everyoneGroup, groupListHelper.getEntityType(everyoneGroup));
        form.setPrincipals(Collections.singleton(everyoneBean), true);

        return form;
    }

    /**
     * Persist a new or edited PortletDefinition from a form, replacing existing values.
     *
     * @param publisher {@code IPerson} that requires permission to save this definition
     * @param form form data to persist
     * @return new {@code PortletDefinitionForm} for this portlet ID
     */
    public PortletDefinitionForm savePortletRegistration(
            IPerson publisher, PortletDefinitionForm form) {
        logger.trace("In savePortletRegistration() - for: {}", form.getPortletName());
        logger.error("form stopDate [" + form.getStopDate() + "]");
        logger.error("form stopImmediately [" + form.getStopImmediately() + "]");
        /* TODO:  Service-Layer Security Reboot (great need of refactoring with a community-approved plan in place) */

        // User must have the selected lifecycle permission over AT LEAST ONE
        // category in which this portlet resides.  (This is the same check that
        // is made when the user enters the lifecycle-selection step in the wizard.)
        if (!hasLifecyclePermission(publisher, form.getLifecycleState(), form.getCategories())) {
            logger.warn(
                    "User '"
                            + publisher.getUserName()
                            + "' attempted to save the following portlet without the selected MANAGE permission:  "
                            + form);
            throw new SecurityException("Not Authorized");
        }

        if (!form.isNew()) {
            // User must have the previous lifecycle permission
            // in AT LEAST ONE previous category as well
            IPortletDefinition def =
                    this.portletDefinitionRegistry.getPortletDefinition(form.getId());
            Set<PortletCategory> categories = portletCategoryRegistry.getParentCategories(def);
            SortedSet<JsonEntityBean> categoryBeans = new TreeSet<>();
            for (PortletCategory cat : categories) {
                categoryBeans.add(new JsonEntityBean(cat));
            }
            if (!hasLifecyclePermission(publisher, def.getLifecycleState(), categoryBeans)) {
                logger.warn(
                        "User '"
                                + publisher.getUserName()
                                + "' attempted to save the following portlet without the previous MANAGE permission:  "
                                + form);
                throw new SecurityException("Not Authorized");
            }
        }

        if (form.isNew()
                || portletDefinitionRegistry.getPortletDefinition(form.getId()).getType().getId()
                        != form.getTypeId()) {
            // User must have access to the selected CPD if s/he selected it in this interaction
            final int selectedTypeId = form.getTypeId();
            final PortletPublishingDefinition cpd =
                    portletPublishingDefinitionDao.getChannelPublishingDefinition(selectedTypeId);
            final Map<IPortletType, PortletPublishingDefinition> allowableCpds =
                    this.getAllowableChannelPublishingDefinitions(publisher);
            if (!allowableCpds.containsValue(cpd)) {
                logger.warn(
                        "User '"
                                + publisher.getUserName()
                                + "' attempted to administer the following portlet without the selected "
                                + IPermission.PORTLET_MANAGER_SELECT_PORTLET_TYPE
                                + " permission:  "
                                + form);
                throw new SecurityException("Not Authorized");
            }
        }

        // create the principal array from the form's principal list -- only principals with
        // permissions
        final Set<IGroupMember> subscribePrincipalSet = new HashSet<>(form.getPrincipals().size());
        final Set<IGroupMember> browsePrincipalSet = new HashSet<>(form.getPrincipals().size());
        final Set<IGroupMember> configurePrincipalSet = new HashSet<>(form.getPrincipals().size());
        for (JsonEntityBean bean : form.getPrincipals()) {
            final String subscribePerm =
                    bean.getTypeAndIdHash() + "_" + IPermission.PORTLET_SUBSCRIBER_ACTIVITY;
            final String browsePerm =
                    bean.getTypeAndIdHash() + "_" + IPermission.PORTLET_BROWSE_ACTIVITY;
            final String configurePerm =
                    bean.getTypeAndIdHash() + "_" + IPermission.PORTLET_MODE_CONFIG;
            final EntityEnum entityEnum = bean.getEntityType();
            final IGroupMember principal =
                    entityEnum.isGroup()
                            ? (GroupService.findGroup(bean.getId()))
                            : (GroupService.getGroupMember(bean.getId(), entityEnum.getClazz()));
            if (form.getPermissions().contains(subscribePerm)) {
                logger.info(
                        "In savePortletRegistration() - Found a subscribePerm for principal: {}",
                        principal);
                subscribePrincipalSet.add(principal);
            }
            if (form.getPermissions().contains(browsePerm)) {
                logger.info(
                        "In savePortletRegistration() - Found a browsePerm for principal: {}",
                        principal);
                browsePrincipalSet.add(principal);
            }
            if (form.getPermissions().contains(configurePerm)) {
                logger.info(
                        "In savePortletRegistration() - Found a configurePerm for principal: {}",
                        principal);
                configurePrincipalSet.add(principal);
            }
        }

        // create the category list from the form's category bean list
        List<PortletCategory> categories = new ArrayList<>();
        for (JsonEntityBean category : form.getCategories()) {
            String id = category.getId();
            String iCatID = id.startsWith("cat") ? id.substring(3) : id;
            categories.add(portletCategoryRegistry.getPortletCategory(iCatID));
        }

        final IPortletType portletType = portletTypeRegistry.getPortletType(form.getTypeId());
        if (portletType == null) {
            throw new IllegalArgumentException("No IPortletType exists for ID " + form.getTypeId());
        }

        IPortletDefinition portletDef;
        if (form.getId() == null) {
            portletDef =
                    new PortletDefinitionImpl(
                            portletType,
                            form.getFname(),
                            form.getName(),
                            form.getTitle(),
                            form.getApplicationId(),
                            form.getPortletName(),
                            form.isFramework());
        } else {
            portletDef = portletDefinitionRegistry.getPortletDefinition(form.getId());
            portletDef.setType(portletType);
            portletDef.setFName(form.getFname());
            portletDef.setName(form.getName());
            portletDef.setTitle(form.getTitle());
            portletDef.getPortletDescriptorKey().setWebAppName(form.getApplicationId());
            portletDef.getPortletDescriptorKey().setPortletName(form.getPortletName());
            portletDef.getPortletDescriptorKey().setFrameworkPortlet(form.isFramework());
        }
        portletDef.setDescription(form.getDescription());
        portletDef.setTimeout(form.getTimeout());

        // Make parameters (NB:  these are different from preferences) in the
        // portletDef reflect the state of the form, in case any have changed.
        for (String key : form.getParameters().keySet()) {
            String value = form.getParameters().get(key).getValue();
            if (!StringUtils.isBlank(value)) {
                portletDef.addParameter(key, value);
            }
        }
        portletDef.addParameter(
                IPortletDefinition.EDITABLE_PARAM, Boolean.toString(form.isEditable()));
        portletDef.addParameter(
                IPortletDefinition.CONFIGURABLE_PARAM, Boolean.toString(form.isConfigurable()));
        portletDef.addParameter(
                IPortletDefinition.HAS_HELP_PARAM, Boolean.toString(form.isHasHelp()));
        portletDef.addParameter(
                IPortletDefinition.HAS_ABOUT_PARAM, Boolean.toString(form.isHasAbout()));

        // Now add portlet preferences
        List<IPortletPreference> preferenceList = new ArrayList<>();
        for (String key : form.getPortletPreferences().keySet()) {
            List<String> prefValues = form.getPortletPreferences().get(key).getValue();
            if (prefValues != null && prefValues.size() > 0) {
                String[] values = prefValues.toArray(new String[0]);
                BooleanAttribute readOnly = form.getPortletPreferenceReadOnly().get(key);
                preferenceList.add(new PortletPreferenceImpl(key, readOnly.getValue(), values));
            }
        }
        portletDef.setPortletPreferences(preferenceList);

        // Lastly update the PortletDefinition's lifecycle state & lifecycle-related metadata
        updateLifecycleState(form, portletDef, publisher);

        // The final parameter of IGroupMembers is used to set the initial SUBSCRIBE permission set
        portletPublishingService.savePortletDefinition(
                portletDef, publisher, categories, new ArrayList<>(subscribePrincipalSet));
        // updatePermissions(portletDef, subscribePrincipalSet,
        // IPermission.PORTLET_SUBSCRIBER_ACTIVITY);
        updatePermissions(
                portletDef,
                browsePrincipalSet,
                IPermission.PORTAL_SUBSCRIBE,
                IPermission.PORTLET_BROWSE_ACTIVITY);
        updatePermissions(
                portletDef,
                configurePrincipalSet,
                IPermission.PORTAL_PUBLISH,
                IPermission.PORTLET_MODE_CONFIG);
        return this.createPortletDefinitionForm(
                publisher, portletDef.getPortletDefinitionId().getStringId());
    }

    /*
     * Update permissions for a given owner, activity, and portlet definition combination. Adds new principals' permissions passed in and removes
     * principals' permissions if not in the list for the given activity.
     */
    private void updatePermissions(
            IPortletDefinition def,
            Set<IGroupMember> newPrincipals,
            String owner,
            String activity) {
        final String portletTargetId = PermissionHelper.permissionTargetIdForPortletDefinition(def);

        final IUpdatingPermissionManager pm =
                authorizationService.newUpdatingPermissionManager(owner);

        /* Create the new permissions array */
        final List<IPermission> newPermissions = new ArrayList<>();
        for (final IGroupMember newPrincipal : newPrincipals) {
            final IAuthorizationPrincipal authorizationPrincipal =
                    authorizationService.newPrincipal(newPrincipal);
            final IPermission permission = pm.newPermission(authorizationPrincipal);
            permission.setType(IPermission.PERMISSION_TYPE_GRANT);
            permission.setActivity(activity);
            permission.setTarget(portletTargetId);
            newPermissions.add(permission);
            logger.trace("In updatePermissions() - adding a new permission of: {}", permission);
        }

        /* Remove former permissions for this portlet / activity */
        final IPermission[] oldPermissions = pm.getPermissions(activity, portletTargetId);
        pm.removePermissions(oldPermissions);

        /* Add the new permissions */
        pm.addPermissions(newPermissions.toArray(new IPermission[newPermissions.size()]));
    }

    /**
     * Delete the portlet with the given portlet ID.
     *
     * @param person the person removing the portlet
     */
    public void removePortletRegistration(IPerson person, PortletDefinitionForm form) {

        /* TODO:  Service-Layer Security Reboot (great need of refactoring with a community-approved plan in place) */
        // Arguably a check here is redundant since -- in the current
        // portlet-manager webflow -- you can't get to this point in the
        // conversation with out first obtaining a PortletDefinitionForm;  but
        // it makes sense to check permissions here as well since the route(s)
        // to reach this method could evolve in the future.

        // Let's enforce the policy that you may only delete a portlet thet's
        // currently in a lifecycle state you have permission to MANAGE.
        // (They're hierarchical.)
        if (!hasLifecyclePermission(person, form.getLifecycleState(), form.getCategories())) {
            logger.warn(
                    "User '"
                            + person.getUserName()
                            + "' attempted to remove portlet '"
                            + form.getFname()
                            + "' without the proper MANAGE permission");
            throw new SecurityException("Not Authorized");
        }

        IPortletDefinition def = portletDefinitionRegistry.getPortletDefinition(form.getId());
        /*
         * It's very important to remove portlets via the portletPublishingService
         * because that API cleans up details like category memberships and permissions.
         */
        portletPublishingService.removePortletDefinition(def, person);
    }

    /**
     * Check if the link to the Fragment admin portlet should display in the status message.
     *
     * <p>Checks that the portlet is new, that the portlet has been published and that the user has
     * necessary permissions to go to the fragment admin page.
     *
     * @param person the person publishing/editing the portlet
     * @param form the portlet being editted
     * @param portletId the id of the saved portlet
     * @return true If all three conditions are met
     */
    public boolean shouldDisplayLayoutLink(
            IPerson person, PortletDefinitionForm form, String portletId) {
        if (!form.isNew()) {
            return false;
        }

        // only include the "do layout" link for published portlets.
        if (form.getLifecycleState() != PortletLifecycleState.PUBLISHED) {
            return false;
        }

        // check that the user can edit at least 1 fragment.
        Map<String, String> layouts =
                fragmentAdminHelper.getAuthorizedDlmFragments(person.getUserName());
        if (layouts == null || layouts.isEmpty()) {
            return false;
        }

        // check that the user has subscribe priv.
        IAuthorizationPrincipal authPrincipal =
                authorizationService.newPrincipal(
                        person.getUserName(), EntityEnum.PERSON.getClazz());

        return authPrincipal.canSubscribe(portletId);
    }

    /**
     * Get the link to the fragment admin portlet.
     *
     * @param request the current http request.
     * @return the portlet link
     */
    public String getFragmentAdminURL(HttpServletRequest request) {
        IPortalUrlBuilder builder =
                urlProvider.getPortalUrlBuilderByPortletFName(
                        request, PORTLET_FNAME_FRAGMENT_ADMIN_PORTLET, UrlType.RENDER);
        IPortletUrlBuilder portletUrlBuilder = builder.getTargetedPortletUrlBuilder();
        portletUrlBuilder.setPortletMode(PortletMode.VIEW);
        portletUrlBuilder.setWindowState(WindowState.MAXIMIZED);

        return builder.getUrlString();
    }

    /**
     * Get a list of the key names of the currently-set arbitrary portlet preferences.
     *
     * @param form
     * @return
     */
    public Set<String> getArbitraryPortletPreferenceNames(PortletDefinitionForm form) {
        // set default values for all portlet parameters
        PortletPublishingDefinition cpd =
                this.portletPublishingDefinitionDao.getChannelPublishingDefinition(
                        form.getTypeId());
        Set<String> currentPrefs = new HashSet<>();
        currentPrefs.addAll(form.getPortletPreferences().keySet());
        for (Step step : cpd.getSteps()) {
            if (step.getPreferences() != null) {
                for (Preference pref : step.getPreferences()) {
                    currentPrefs.remove(pref.getName());
                }
            }
        }
        return currentPrefs;
    }

    /**
     * If the portlet is a portlet and if one of the supported portlet modes is {@link
     * IPortletRenderer#CONFIG}
     */
    public boolean supportsConfigMode(PortletDefinitionForm form) {
        final Tuple<String, String> portletDescriptorKeys = this.getPortletDescriptorKeys(form);
        if (portletDescriptorKeys == null) {
            logger.trace("Unable to determine supportsConfig - portletDescriptorKeys is null.");
            return false;
        }
        final String portletAppId = portletDescriptorKeys.first;
        final String portletName = portletDescriptorKeys.second;

        final PortletRegistryService portletRegistryService =
                this.portalDriverContainerServices.getPortletRegistryService();
        final PortletDefinition portletDescriptor;
        try {
            portletDescriptor = portletRegistryService.getPortlet(portletAppId, portletName);
        } catch (PortletContainerException e) {
            this.logger.warn(
                    "Unable to determine supportsConfig - Failed to load portlet descriptor for appId='"
                            + portletAppId
                            + "', portletName='"
                            + portletName
                            + "'",
                    e);
            return false;
        }

        if (portletDescriptor == null) {
            logger.trace("Unable to determine supportsConfig - portletDescriptor is null.");
            return false;
        }
        logger.trace(
                "Checking supportsConfig for portlet ID=[{}] and Name=[{}].",
                portletAppId,
                portletName);

        // Iterate over supported portlet modes, this ignores the content types for now
        final List<? extends Supports> supports = portletDescriptor.getSupports();
        for (final Supports support : supports) {
            logger.trace("Checking Supports instance:  {}", support.getMimeType());
            final List<String> portletModes = support.getPortletModes();
            for (final String portletMode : portletModes) {
                logger.trace("Checking portletMode {}.", portletMode);
                if (IPortletRenderer.CONFIG.equals(PortletUtils.getPortletMode(portletMode))) {
                    logger.trace("Returning supportsConfig is true.");
                    return true;
                }
            }
        }
        logger.trace("Returning supportsConfig is false.");
        return false;
    }

    private static final Pattern PARAM_PATTERN =
            Pattern.compile("^([^\\[]+)\\['([^\\']+)'\\]\\.value$");

    public void cleanOptions(PortletDefinitionForm form, PortletRequest request) {
        // Add permission parameters to permissions collection
        form.clearPermissions();
        for (PortletPermissionsOnForm perm : PortletPermissionsOnForm.values()) {
            addPermissionsFromRequestToForm(form, request, perm.getActivity());
        }

        // Names of valid preferences and parameters
        final Set<String> preferenceNames = new HashSet<>();
        final Set<String> parameterNames = new HashSet<>();

        // Read all of the submitted channel parameter and portlet preference names from the request
        for (final Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
            final String name = e.nextElement();
            final Matcher nameMatcher = PARAM_PATTERN.matcher(name);
            if (nameMatcher.matches()) {
                final String paramType = nameMatcher.group(1);
                final String paramName = nameMatcher.group(2);

                if ("portletPreferences".equals(paramType)) {
                    preferenceNames.add(paramName);
                } else if ("parameters".equals(paramType)) {
                    parameterNames.add(paramName);
                }
            }
        }

        // Add all of the parameter and preference names that have default values in the CPD into
        // the valid name sets
        final PortletPublishingDefinition cpd =
                this.portletPublishingDefinitionDao.getChannelPublishingDefinition(
                        form.getTypeId());
        for (final Step step : cpd.getSteps()) {
            final List<Parameter> parameters = step.getParameters();
            if (parameters != null) {
                for (final Parameter parameter : parameters) {
                    final JAXBElement<? extends ParameterInputType> parameterInput =
                            parameter.getParameterInput();
                    if (parameterInput != null) {
                        final ParameterInputType parameterInputType = parameterInput.getValue();
                        if (parameterInputType != null && parameterInputType.getDefault() != null) {
                            parameterNames.add(parameter.getName());
                        }
                    }
                }
            }

            final List<Preference> preferences = step.getPreferences();
            if (preferences != null) {
                for (final Preference preference : preferences) {
                    final JAXBElement<? extends PreferenceInputType> preferenceInput =
                            preference.getPreferenceInput();
                    final PreferenceInputType preferenceInputType = preferenceInput.getValue();
                    if (preferenceInputType instanceof MultiValuedPreferenceInputType) {
                        final MultiValuedPreferenceInputType multiValuedPreferenceInputType =
                                (MultiValuedPreferenceInputType) preferenceInputType;
                        final List<String> defaultValues =
                                multiValuedPreferenceInputType.getDefaults();
                        if (defaultValues != null && !defaultValues.isEmpty()) {
                            preferenceNames.add(preference.getName());
                        }
                    } else if (preferenceInputType instanceof SingleValuedPreferenceInputType) {
                        final SingleValuedPreferenceInputType SingleValuedPreferenceInputType =
                                (SingleValuedPreferenceInputType) preferenceInputType;
                        if (SingleValuedPreferenceInputType.getDefault() != null) {
                            preferenceNames.add(preference.getName());
                        }
                    }
                }
            }
        }

        // Remove portlet preferences from the form object that were not part of this request or
        // defined in the CPD
        // - do it only if portlet doesn't support configMode
        if (!this.supportsConfigMode(form)) {
            final Map<String, StringListAttribute> portletPreferences =
                    form.getPortletPreferences();
            final Map<String, BooleanAttribute> portletPreferencesOverrides =
                    form.getPortletPreferenceReadOnly();

            for (final Iterator<Entry<String, StringListAttribute>> portletPreferenceEntryItr =
                            portletPreferences.entrySet().iterator();
                    portletPreferenceEntryItr.hasNext(); ) {
                final Map.Entry<String, StringListAttribute> portletPreferenceEntry =
                        portletPreferenceEntryItr.next();
                final String key = portletPreferenceEntry.getKey();
                final StringListAttribute valueAttr = portletPreferenceEntry.getValue();
                if (!preferenceNames.contains(key) || valueAttr == null) {
                    portletPreferenceEntryItr.remove();
                    portletPreferencesOverrides.remove(key);
                } else {
                    final List<String> values = valueAttr.getValue();
                    for (final Iterator<String> iter = values.iterator(); iter.hasNext(); ) {
                        String value = iter.next();
                        if (value == null) {
                            iter.remove();
                        }
                    }
                    if (values.size() == 0) {
                        portletPreferenceEntryItr.remove();
                        portletPreferencesOverrides.remove(key);
                    }
                }
            }
        }

        final Map<String, Attribute> parameters = form.getParameters();

        for (final Iterator<Entry<String, Attribute>> parameterEntryItr =
                        parameters.entrySet().iterator();
                parameterEntryItr.hasNext(); ) {
            final Entry<String, Attribute> parameterEntry = parameterEntryItr.next();
            final String key = parameterEntry.getKey();
            final Attribute value = parameterEntry.getValue();

            if (!parameterNames.contains(key)
                    || value == null
                    || StringUtils.isBlank(value.getValue())) {
                parameterEntryItr.remove();
            }
        }
    }

    private void addPermissionsFromRequestToForm(
            PortletDefinitionForm form, PortletRequest request, String activity) {
        final String ending = "_" + activity;
        for (final String name : request.getParameterMap().keySet()) {
            if (name.endsWith(ending)) {
                logger.info(
                        "In addPermissionsFromRequestToForm() - Adding permission request: {}",
                        name);
                form.addPermission(name);
            } else {
                logger.info(
                        "In addPermissionsFromRequestToForm() - For permissions, ignoring request parameter: {}",
                        name);
            }
        }
    }

    /**
     * Retreive the list of portlet application contexts currently available in this portlet
     * container.
     *
     * @return list of portlet context
     */
    public List<PortletApplicationDefinition> getPortletApplications() {
        final PortletRegistryService portletRegistryService =
                portalDriverContainerServices.getPortletRegistryService();
        final List<PortletApplicationDefinition> contexts = new ArrayList<>();

        for (final Iterator<String> iter =
                        portletRegistryService.getRegisteredPortletApplicationNames();
                iter.hasNext(); ) {
            final String applicationName = iter.next();
            final PortletApplicationDefinition applicationDefninition;
            try {
                applicationDefninition =
                        portletRegistryService.getPortletApplication(applicationName);
            } catch (PortletContainerException e) {
                throw new RuntimeException(
                        "Failed to load PortletApplicationDefinition for '"
                                + applicationName
                                + "'");
            }

            final List<? extends PortletDefinition> portlets = applicationDefninition.getPortlets();
            portlets.sort(
                    new ComparableExtractingComparator<PortletDefinition, String>(
                            String.CASE_INSENSITIVE_ORDER) {
                        @Override
                        protected String getComparable(PortletDefinition o) {
                            final List<? extends DisplayName> displayNames = o.getDisplayNames();
                            if (displayNames != null && displayNames.size() > 0) {
                                return displayNames.get(0).getDisplayName();
                            }

                            return o.getPortletName();
                        }
                    });

            contexts.add(applicationDefninition);
        }

        contexts.sort(
                new ComparableExtractingComparator<PortletApplicationDefinition, String>(
                        String.CASE_INSENSITIVE_ORDER) {
                    @Override
                    protected String getComparable(PortletApplicationDefinition o) {
                        final String portletContextName = o.getName();
                        if (portletContextName != null) {
                            return portletContextName;
                        }

                        final String applicationName = o.getContextPath();
                        if ("/".equals(applicationName)) {
                            return "ROOT";
                        }

                        if (applicationName.startsWith("/")) {
                            return applicationName.substring(1);
                        }

                        return applicationName;
                    }
                });
        return contexts;
    }

    /**
     * Get a portlet descriptor matching the current portlet definition form. If the current form
     * does not represent a portlet, the application or portlet name fields are blank, or the
     * portlet description cannot be retrieved, the method will return <code>null</code>.
     */
    public PortletDefinition getPortletDescriptor(PortletDefinitionForm form) {
        final Tuple<String, String> portletDescriptorKeys = this.getPortletDescriptorKeys(form);
        if (portletDescriptorKeys == null) {
            return null;
        }
        final String portletAppId = portletDescriptorKeys.first;
        final String portletName = portletDescriptorKeys.second;

        final PortletRegistryService portletRegistryService =
                portalDriverContainerServices.getPortletRegistryService();
        try {
            return portletRegistryService.getPortlet(portletAppId, portletName);
        } catch (PortletContainerException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Pre-populate a new {@link PortletDefinitionForm} with information from the {@link
     * PortletDefinition}.
     *
     * @param form
     */
    public void loadDefaultsFromPortletDefinitionIfNew(PortletDefinitionForm form) {

        if (!form.isNew()) {
            // Get out;  we only prepopulate new portlets
            return;
        }

        // appName/portletName must be set at this point
        Validate.notBlank(form.getApplicationId(), "ApplicationId not set");
        Validate.notBlank(form.getPortletName(), "PortletName not set");

        final PortletRegistryService portletRegistryService =
                portalDriverContainerServices.getPortletRegistryService();
        final PortletDefinition portletDef;
        try {
            portletDef =
                    portletRegistryService.getPortlet(
                            form.getApplicationId(), form.getPortletName());
        } catch (PortletContainerException e) {
            this.logger.warn(
                    "Failed to load portlet descriptor for appId='"
                            + form.getApplicationId()
                            + "', portletName='"
                            + form.getPortletName()
                            + "'",
                    e);
            return;
        }

        form.setTitle(portletDef.getPortletName());
        form.setName(portletDef.getPortletName());
        for (Supports supports : portletDef.getSupports()) {
            for (String mode : supports.getPortletModes()) {
                if ("edit".equalsIgnoreCase(mode)) {
                    form.setEditable(true);
                } else if ("help".equalsIgnoreCase(mode)) {
                    form.setHasHelp(true);
                } else if ("config".equalsIgnoreCase(mode)) {
                    form.setConfigurable(true);
                }
            }
        }
    }

    public PortletLifecycleState[] getLifecycleStates() {
        return PortletLifecycleState.values();
    }

    public Set<PortletLifecycleState> getAllowedLifecycleStates(
            IPerson person, SortedSet<JsonEntityBean> categories) {
        Set<PortletLifecycleState> states = new TreeSet<>();
        if (hasLifecyclePermission(person, PortletLifecycleState.MAINTENANCE, categories)) {
            states.add(PortletLifecycleState.CREATED);
            states.add(PortletLifecycleState.APPROVED);
            states.add(PortletLifecycleState.EXPIRED);
            states.add(PortletLifecycleState.PUBLISHED);
            states.add(PortletLifecycleState.MAINTENANCE);
        } else if (hasLifecyclePermission(person, PortletLifecycleState.EXPIRED, categories)) {
            states.add(PortletLifecycleState.CREATED);
            states.add(PortletLifecycleState.APPROVED);
            states.add(PortletLifecycleState.EXPIRED);
            states.add(PortletLifecycleState.PUBLISHED);
        } else if (hasLifecyclePermission(person, PortletLifecycleState.PUBLISHED, categories)) {
            states.add(PortletLifecycleState.CREATED);
            states.add(PortletLifecycleState.APPROVED);
            states.add(PortletLifecycleState.PUBLISHED);
        } else if (hasLifecyclePermission(person, PortletLifecycleState.APPROVED, categories)) {
            states.add(PortletLifecycleState.CREATED);
            states.add(PortletLifecycleState.APPROVED);
        } else if (hasLifecyclePermission(person, PortletLifecycleState.CREATED, categories)) {
            states.add(PortletLifecycleState.CREATED);
        }
        return states;
    }

    public boolean hasLifecyclePermission(
            IPerson person, PortletLifecycleState state, SortedSet<JsonEntityBean> categories) {
        EntityIdentifier ei = person.getEntityIdentifier();
        IAuthorizationPrincipal ap = authorizationService.newPrincipal(ei.getKey(), ei.getType());

        final String activity;
        switch (state) {
            case APPROVED:
                {
                    activity = IPermission.PORTLET_MANAGER_APPROVED_ACTIVITY;
                    break;
                }
            case CREATED:
                {
                    activity = IPermission.PORTLET_MANAGER_CREATED_ACTIVITY;
                    break;
                }
            case PUBLISHED:
                {
                    activity = IPermission.PORTLET_MANAGER_ACTIVITY;
                    break;
                }
            case EXPIRED:
                {
                    activity = IPermission.PORTLET_MANAGER_EXPIRED_ACTIVITY;
                    break;
                }
            case MAINTENANCE:
                {
                    activity = IPermission.PORTLET_MANAGER_MAINTENANCE_ACTIVITY;
                    break;
                }
            default:
                {
                    throw new IllegalArgumentException("");
                }
        }
        if (ap.hasPermission(
                IPermission.PORTAL_PUBLISH, activity, IPermission.ALL_PORTLETS_TARGET)) {
            logger.debug(
                    "Found permission for category ALL_PORTLETS and lifecycle state "
                            + state.toString());
            return true;
        }

        for (JsonEntityBean category : categories) {
            if (ap.canManage(state, category.getId())) {
                logger.debug(
                        "Found permission for category "
                                + category.getName()
                                + " and lifecycle state "
                                + state.toString());
                return true;
            }
        }
        logger.debug("No permission for lifecycle state " + state.toString());
        return false;
    }

    public IPortletWindowId getDelegateWindowId(ExternalContext externalContext, String fname) {
        final PortletRequest nativeRequest = (PortletRequest) externalContext.getNativeRequest();
        final PortletSession portletSession = nativeRequest.getPortletSession();
        return (IPortletWindowId)
                portletSession.getAttribute(RenderPortletTag.DEFAULT_SESSION_KEY_PREFIX + fname);
    }

    public boolean configModeAction(ExternalContext externalContext, String fname)
            throws IOException {

        final ActionRequest actionRequest = (ActionRequest) externalContext.getNativeRequest();
        final ActionResponse actionResponse = (ActionResponse) externalContext.getNativeResponse();

        final IPortletWindowId portletWindowId = this.getDelegateWindowId(externalContext, fname);
        if (portletWindowId == null) {
            throw new IllegalStateException(
                    "Cannot execute configModeAciton without a delegate window ID in the session for key: "
                            + RenderPortletTag.DEFAULT_SESSION_KEY_PREFIX
                            + fname);
        }

        final PortletDelegationDispatcher requestDispatcher =
                this.portletDelegationLocator.getRequestDispatcher(actionRequest, portletWindowId);

        final DelegationActionResponse delegationResponse =
                requestDispatcher.doAction(actionRequest, actionResponse);

        final String redirectLocation = delegationResponse.getRedirectLocation();
        final DelegateState delegateState = delegationResponse.getDelegateState();
        if (redirectLocation != null
                || (delegationResponse.getPortletMode() != null
                        && !IPortletRenderer.CONFIG.equals(delegationResponse.getPortletMode()))
                || !IPortletRenderer.CONFIG.equals(delegateState.getPortletMode())) {

            // The portlet sent a redirect OR changed it's mode away from CONFIG, assume it is done
            return true;
        }

        return false;
    }

    /**
     * updates the editPortlet form with the portletType of the first (and only) portletDefinition
     * passed in through the Map of portlet definitions.
     *
     * @return PortletPublishingDefinition of the first portlet definition in the list, null if the
     *     list is empty or has more than one element.
     */
    public PortletPublishingDefinition updateFormForSinglePortletType(
            Map<IPortletType, PortletPublishingDefinition> portletDefinitions,
            PortletDefinitionForm form) {
        if (portletDefinitions.size() != 1) {
            return null;
        }
        IPortletType portletType = portletDefinitions.keySet().iterator().next();
        form.setTypeId(portletType.getId());
        PortletPublishingDefinition cpd =
                portletPublishingDefinitionDao.getChannelPublishingDefinition(portletType.getId());
        form.setChannelPublishingDefinition(cpd);

        return cpd;
    }

    public boolean offerPortletSelection(PortletDefinitionForm form) {
        final IPortletType portletType = this.portletTypeRegistry.getPortletType(form.getTypeId());
        final PortletPublishingDefinition portletPublishingDefinition =
                this.portletPublishingDefinitionDao.getChannelPublishingDefinition(
                        portletType.getId());
        final PortletDescriptor portletDescriptor =
                portletPublishingDefinition.getPortletDescriptor();
        if (portletDescriptor == null) {
            return true;
        }

        final Boolean isFramework = portletDescriptor.isIsFramework();
        if (isFramework != null && isFramework) {
            form.setFramework(true);
        } else {
            final String webAppName = portletDescriptor.getWebAppName();
            form.setApplicationId(webAppName);
        }

        final String portletName = portletDescriptor.getPortletName();
        form.setPortletName(portletName);

        return false;
    }

    public Map<IPortletType, PortletPublishingDefinition> getAllowableChannelPublishingDefinitions(
            IPerson user) {

        Map<IPortletType, PortletPublishingDefinition> rslt;

        final Map<IPortletType, PortletPublishingDefinition> rawMap =
                portletPublishingDefinitionDao.getChannelPublishingDefinitions();
        final IAuthorizationPrincipal principal =
                AuthorizationPrincipalHelper.principalFromUser(user);
        if (principal.hasPermission(
                IPermission.PORTAL_PUBLISH,
                IPermission.PORTLET_MANAGER_SELECT_PORTLET_TYPE,
                IPermission.ALL_PORTLET_TYPES)) {
            // Send the whole collection back...
            rslt = rawMap;
        } else {
            // Filter the collection by permissions...
            rslt = new HashMap<>();
            for (Map.Entry<IPortletType, PortletPublishingDefinition> y : rawMap.entrySet()) {
                if (principal.hasPermission(
                        IPermission.PORTAL_PUBLISH,
                        IPermission.PORTLET_MANAGER_SELECT_PORTLET_TYPE,
                        y.getKey().getName())) {
                    rslt.put(y.getKey(), y.getValue());
                }
            }
        }

        return rslt;
    }

    protected Tuple<String, String> getPortletDescriptorKeys(PortletDefinitionForm form) {
        if (form.getPortletName() == null
                || (form.getApplicationId() == null && !form.isFramework())) {
            return null;
        }

        final String portletAppId;
        if (form.isFramework()) {
            portletAppId = this.servletContext.getContextPath();
        } else {
            portletAppId = form.getApplicationId();
        }

        final String portletName = form.getPortletName();

        return new Tuple<>(portletAppId, portletName);
    }

    private void updateLifecycleState(
            PortletDefinitionForm form, IPortletDefinition portletDef, IPerson publisher) {

        final PortletLifecycleState selectedLifecycleState = form.getLifecycleState();

        /*
         * Step One:  update the current lifecycle state, even if it isn't changing.
         *
         * NOTE 1:  forcing an update (even to the same state) will have the effect of
         * clearing the publish/expire date if set previously (but not set in this submit).
         *
         * NOTE 2:  permissions for this step have already been checked in savePortletRegistration.
         */
        portletDef.updateLifecycleState(selectedLifecycleState, publisher);

        /*
         * Step Two:  depending on which state was selected, we may have extra work to do.
         */
        switch (selectedLifecycleState) {
            case APPROVED:
                // Check for an auto-publish date...
                if (form.getPublishDate() != null
                        && hasLifecyclePermission(
                                publisher, PortletLifecycleState.PUBLISHED, form.getCategories())) {
                    // We are also the 'publisher' if we scheduled the portlet for (future)
                    // publication...
                    portletDef.updateLifecycleState(
                            PortletLifecycleState.PUBLISHED, publisher, form.getPublishDateTime());
                }
                break;
            case PUBLISHED:
                // Check for an expiration date...
                if (form.getExpirationDate() != null
                        && hasLifecyclePermission(
                                publisher, PortletLifecycleState.EXPIRED, form.getCategories())) {
                    // We are also the 'expirer' if we scheduled the portlet for (future)
                    // expiration...
                    portletDef.updateLifecycleState(
                            PortletLifecycleState.EXPIRED, publisher, form.getExpirationDateTime());
                }
                break;
            case MAINTENANCE:
                // Persist the custom message (if provided) as a publishing parameter...
                final String customMessage = form.getCustomMaintenanceMessage();
                if (StringUtils.isNotBlank(customMessage)) {
                    // Update the message
                    portletDef.addParameter(
                            PortletLifecycleState.CUSTOM_MAINTENANCE_MESSAGE_PARAMETER_NAME,
                            customMessage);
                } else {
                    // Clear any previous message
                    portletDef.removeParameter(
                            PortletLifecycleState.CUSTOM_MAINTENANCE_MESSAGE_PARAMETER_NAME);
                }
                final Boolean stopImmediately = form.getStopImmediately();
                portletDef.addParameter("stopImmediately", stopImmediately ? "true" : "false");
                if (stopImmediately) {
                    portletDef.removeParameter(
                            PortletLifecycleState.MAINTENANCE_STOP_DATE);
                    portletDef.removeParameter(
                            PortletLifecycleState.MAINTENANCE_STOP_TIME);
                } else {
                    final String stopDate = form.getStopDate();
                    final String stopTime = form.getStopTime();
                    portletDef.addParameter(
                            PortletLifecycleState.MAINTENANCE_STOP_DATE,
                            stopDate);
                    portletDef.addParameter(
                            PortletLifecycleState.MAINTENANCE_STOP_TIME,
                            stopTime);
                }
                final Boolean restartManually = form.getRestartManually();
                portletDef.addParameter("restartManually", restartManually ? "true" : "false");
                logger.error("restartManually from form (" + restartManually + "]");
                logger.error("restartManually  portlet def param [" + portletDef.getParameter("restartManually") + "]");
                if (restartManually) {
                    portletDef.removeParameter(
                            PortletLifecycleState.MAINTENANCE_RESTART_DATE);
                    portletDef.removeParameter(
                            PortletLifecycleState.MAINTENANCE_RESTART_TIME);
                } else {
                    final String restartDate = form.getRestartDate();
                    final String restartTime = form.getRestartTime();
                    portletDef.addParameter(
                            PortletLifecycleState.MAINTENANCE_RESTART_DATE,
                            restartDate);
                    portletDef.addParameter(
                            PortletLifecycleState.MAINTENANCE_RESTART_TIME,
                            restartTime);
                }
                break;
            default:
                // No extra work
                break;
        }
    }
}
