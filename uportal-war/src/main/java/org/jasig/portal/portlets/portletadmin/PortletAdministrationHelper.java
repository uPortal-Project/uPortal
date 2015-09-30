/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlets.portletadmin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.driver.PortalDriverContainerServices;
import org.apache.pluto.container.driver.PortletRegistryService;
import org.apache.pluto.container.om.portlet.DisplayName;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.apache.pluto.container.om.portlet.Supports;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.api.portlet.DelegateState;
import org.jasig.portal.api.portlet.DelegationActionResponse;
import org.jasig.portal.api.portlet.PortletDelegationDispatcher;
import org.jasig.portal.api.portlet.PortletDelegationLocator;
import org.jasig.portal.channel.IPortletPublishingService;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.portlet.PortletUtils;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.delegation.jsp.RenderPortletTag;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.om.PortletLifecycleState;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletTypeRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.portletpublishing.xml.MultiValuedPreferenceInputType;
import org.jasig.portal.portletpublishing.xml.Parameter;
import org.jasig.portal.portletpublishing.xml.ParameterInputType;
import org.jasig.portal.portletpublishing.xml.PortletPublishingDefinition;
import org.jasig.portal.portletpublishing.xml.Preference;
import org.jasig.portal.portletpublishing.xml.PreferenceInputType;
import org.jasig.portal.portletpublishing.xml.SingleValuedPreferenceInputType;
import org.jasig.portal.portletpublishing.xml.Step;
import org.jasig.portal.portlets.Attribute;
import org.jasig.portal.portlets.BooleanAttribute;
import org.jasig.portal.portlets.StringListAttribute;
import org.jasig.portal.portlets.fragmentadmin.FragmentAdministrationHelper;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.portlets.portletadmin.xmlsupport.IChannelPublishingDefinitionDao;
import org.jasig.portal.security.AuthorizationPrincipalHelper;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PermissionHelper;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.UrlType;
import org.jasig.portal.utils.ComparableExtractingComparator;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.xml.PortletDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;
import org.springframework.webflow.context.ExternalContext;

/**
 * Helper methods for the portlet administration workflow.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @revision $Revision$
 */
@Service
public class PortletAdministrationHelper implements ServletContextAware {
	protected final Log logger = LogFactory.getLog(PortletAdministrationHelper.class);

    private static final String PORTLET_FNAME_FRAGMENT_ADMIN_PORTLET = "fragment-admin";

    /*
     * Autowired beans listed alphabetically by type
     */
    @Autowired
    private FragmentAdministrationHelper fragmentAdminHelper;
    @Autowired
    private IAuthorizationService authorizationService;
    @Autowired
    private IChannelPublishingDefinitionDao portletPublishingDefinitionDao;
    @Autowired
    private IGroupListHelper groupListHelper;
    @Autowired
    private IPortalUrlProvider urlProvider;
    @Autowired
    private IPortletCategoryRegistry portletCategoryRegistry;
    @Autowired
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    @Autowired
    private IPortletPublishingService portletPublishingService; 
    @Autowired    
    private IPortletTypeRegistry portletTypeRegistry;
    @Autowired
    private PortalDriverContainerServices portalDriverContainerServices;
    @Autowired
    private PortletDelegationLocator portletDelegationLocator;

    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
	    this.servletContext = servletContext;
    }

    /**
	 * Construct a new PortletDefinitionForm for the given IPortletDefinition id.
	 * If a PortletDefinition matching this ID already exists, the form will
	 * be pre-populated with the PortletDefinition's current configuration.  If
	 * the PortletDefinition does not yet exist, a new default form will be
	 * created.
	 *
     * @param person
     * @param portletId
	 * @return
	 */
    public PortletDefinitionForm getPortletDefinitionForm(IPerson person, String portletId) {

        IPortletDefinition def = portletDefinitionRegistry.getPortletDefinition(portletId);

		// create the new form
		final PortletDefinitionForm form;
		if (def != null) {
		    form = new PortletDefinitionForm(def);
		    form.setId(def.getPortletDefinitionId().getStringId());
		}
		else {
		    form = new PortletDefinitionForm();
		}

		// if this is a pre-existing portlet, set the category and permissions
        if (def != null) {

        	// create a JsonEntityBean for each current category and add it
        	// to our form bean's category list
        	Set<PortletCategory> categories = portletCategoryRegistry.getParentCategories(def);
        	for (PortletCategory cat : categories) {
        		form.addCategory(new JsonEntityBean(cat));
        	}

			try {
                IPermissionManager pm = AuthorizationService.instance().newPermissionManager(IPermission.PORTAL_SUBSCRIBE);
                IAuthorizationPrincipal[] prins =
                        pm.getAuthorizedPrincipals(IPermission.PORTLET_SUBSCRIBER_ACTIVITY,
                        PermissionHelper.permissionTargetIdForPortletDefinition(def));
                for (int mp = 0; mp < prins.length; mp++) {
                	JsonEntityBean bean;

                	// first assume this is a group
                	IEntityGroup group = GroupService.findGroup(prins[mp].getKey());
                	if (group != null) {
                    	bean = new JsonEntityBean(group, EntityEnum.GROUP);
                	}

                	// if a matching group can't be found, try to find a matching
                	// non-group entity
                	else {
                    	IGroupMember member = AuthorizationService.instance().getGroupMember(prins[mp]);
                    	bean = new JsonEntityBean(member, EntityEnum.PERSON);
                    	String name = groupListHelper.lookupEntityName(bean);
                    	bean.setName(name);
                	}

                    form.addGroup(bean);
                }
			} catch (GroupsException e) {
				e.printStackTrace();
			}
		}

        // otherwise, if this is a new portlet, pre-populate the categories
        // and groups with some reasonable defaults
        else {

			// pre-populate with top-level category
			IEntityGroup portletCategoriesGroup = GroupService.getDistinguishedGroup(GroupService.PORTLET_CATEGORIES);
			form.addCategory(new JsonEntityBean(portletCategoriesGroup, groupListHelper.getEntityType(portletCategoriesGroup)));

			// pre-populate with top-level group
			IEntityGroup everyoneGroup = GroupService.getDistinguishedGroup(GroupService.EVERYONE);
			form.addGroup(new JsonEntityBean(everyoneGroup, groupListHelper.getEntityType(everyoneGroup)));

		}

        /* TODO:  Service-Layer Security Reboot (great need of refactoring with a community-approved plan in place) */
        // User must have SOME FORM of lifecycle permission over AT LEAST ONE
        // category in which this portlet resides;  lifecycle permissions are
        // hierarchical, so we'll test with the weakest.
        if (!hasLifecyclePermission(person, PortletLifecycleState.CREATED, form.getCategories())) {
            logger.warn("User '" + person.getUserName() +
                    "' attempted to edit the following portlet without MANAGE permission:  " + def);
            throw new SecurityException("Not Authorized");
        }

        return form;
    }

    /**
     * Persist a new or edited PortletDefinition.
     * 
     * @param form
     * @param publisher
     */
    public PortletDefinitionForm savePortletRegistration(IPerson publisher, 
            PortletDefinitionForm form) throws Exception {

        /* TODO:  Service-Layer Security Reboot (great need of refactoring with a community-approved plan in place) */

        // User must have the selected lifecycle permission over AT LEAST ONE
        // category in which this portlet resides.  (This is the same check that
        // is made when the user enters the lifecycle-selection step in the wizard.)
        if (!hasLifecyclePermission(publisher, form.getLifecycleState(), form.getCategories())) {
            logger.warn("User '" + publisher.getUserName() + 
                    "' attempted to save the following portlet without the selected MANAGE permission:  " + form);
            throw new SecurityException("Not Authorized");
        }

        if (!form.isNew()) {
            // User must have the previous lifecycle permission
            // in AT LEAST ONE previous category as well
            IPortletDefinition def = this.portletDefinitionRegistry.getPortletDefinition(form.getId());
            Set<PortletCategory> categories = portletCategoryRegistry.getParentCategories(def);
            List<JsonEntityBean> categoryBeans = new ArrayList<JsonEntityBean>();
            for (PortletCategory cat : categories) {
                categoryBeans.add(new JsonEntityBean(cat));
            }
            if (!hasLifecyclePermission(publisher, def.getLifecycleState(), categoryBeans)) {
                logger.warn("User '" + publisher.getUserName() + 
                        "' attempted to save the following portlet without the previous MANAGE permission:  " + form);
                throw new SecurityException("Not Authorized");
            }
        }

        if (form.isNew() || portletDefinitionRegistry.getPortletDefinition(form.getId()).getType().getId() != form.getTypeId()) {
            // User must have access to the selected CPD if s/he selected it in this interaction
            final int selectedTypeId = form.getTypeId();
            final PortletPublishingDefinition cpd = portletPublishingDefinitionDao.getChannelPublishingDefinition(selectedTypeId);
            final Map<IPortletType, PortletPublishingDefinition> allowableCpds = this.getAllowableChannelPublishingDefinitions(publisher);
            if (!allowableCpds.containsValue(cpd)) {
                logger.warn("User '" + publisher.getUserName() + 
                        "' attempted to administer the following portlet without the selected " + 
                        IPermission.PORTLET_MANAGER_SELECT_PORTLET_TYPE + " permission:  " + form);
                throw new SecurityException("Not Authorized");
            }
        }

		// create the group array from the form's group list
		IGroupMember[] groupMembers = new IGroupMember[form.getGroups().size()];
		for (int i = 0; i < groupMembers.length; i++) {
			JsonEntityBean bean = form.getGroups().get(i);
			EntityEnum entityEnum = EntityEnum.getEntityEnum(bean.getEntityTypeAsString());
			if (entityEnum.isGroup()) {
				groupMembers[i] = GroupService.findGroup(bean.getId());
			} else {
            	groupMembers[i] = GroupService.getGroupMember(bean.getId(), entityEnum.getClazz());
				
			}
		}

        // create the category array from the form's category list
		PortletCategory[] categories = new PortletCategory[form.getCategories().size()];
		for (ListIterator<JsonEntityBean> iter = form.getCategories().listIterator(); iter.hasNext();) {
			String id = iter.next().getId();
			String iCatID = id.startsWith("cat") ? id.substring(3) : id;
			categories[iter.previousIndex()] = portletCategoryRegistry.getPortletCategory(iCatID);
		}

		IPortletDefinition portletDef = null;
		if (form.getId() == null) {
	        final String fname = form.getFname();
	        final String name = form.getName();
	        final String title = form.getTitle();
	        final String applicationId = form.getApplicationId();
	        final String portletName = form.getPortletName();
	        final boolean isFramework = form.isFramework(); 
	        
	        final IPortletType type = portletTypeRegistry.getPortletType(form.getTypeId()); 
	    	portletDef = portletDefinitionRegistry.createPortletDefinition(type, fname, name, title, applicationId, portletName, isFramework);
		} else {
			portletDef = portletDefinitionRegistry.getPortletDefinition(form.getId());
	    }
	    portletDef.setDescription(form.getDescription());
	    portletDef.setFName(form.getFname());
	    portletDef.setName(form.getName());
	    portletDef.setTimeout(form.getTimeout());
	    portletDef.setTitle(form.getTitle());
	    portletDef.getPortletDescriptorKey().setWebAppName(form.getApplicationId());
	    portletDef.getPortletDescriptorKey().setPortletName(form.getPortletName());
	    portletDef.getPortletDescriptorKey().setFrameworkPortlet(form.isFramework());

	    final IPortletType portletType = portletTypeRegistry.getPortletType(form.getTypeId());
	    if (portletType == null) {
	        throw new IllegalArgumentException("No IPortletType exists for ID " + form.getTypeId());
	    }
	    portletDef.setType(portletType);
	    
	    // Make parameters (NB:  these are different from preferences) in the
	    // portletDef reflect the state of the form, in case any have changed.
		for (String key : form.getParameters().keySet()) {
			String value = form.getParameters().get(key).getValue();
			if (!StringUtils.isBlank(value)) {
			    portletDef.addParameter(key, value);
			}
		}
	    portletDef.addParameter(IPortletDefinition.EDITABLE_PARAM, Boolean.toString(form.isEditable()));
	    portletDef.addParameter(IPortletDefinition.CONFIGURABLE_PARAM, Boolean.toString(form.isConfigurable()));
	    portletDef.addParameter(IPortletDefinition.HAS_HELP_PARAM, Boolean.toString(form.isHasHelp()));
	    portletDef.addParameter(IPortletDefinition.HAS_ABOUT_PARAM, Boolean.toString(form.isHasAbout()));

        // Now add portlet preferences
        List<IPortletPreference> preferenceList = new ArrayList<IPortletPreference>();
		for (String key : form.getPortletPreferences().keySet()) {
			List<String> prefValues = form.getPortletPreferences().get(key).getValue();
			if (prefValues != null && prefValues.size() > 0) {
				String[] values = prefValues.toArray(new String[prefValues.size()]);
				BooleanAttribute readOnly = form.getPortletPreferenceReadOnly().get(key);
				preferenceList.add(new PortletPreferenceImpl(key, readOnly.getValue(), values));
			}
		}
		portletDef.setPortletPreferences(preferenceList);

        // Lastly update the PortletDefinition's lifecycle state & lifecycle-related metadata
        updateLifecycleState(form, portletDef, publisher);

	    portletPublishingService.savePortletDefinition(portletDef, publisher, Arrays.asList(categories), Arrays.asList(groupMembers));

	    return this.getPortletDefinitionForm(publisher, portletDef.getPortletDefinitionId().getStringId());
	}

    /**
     * Delete the portlet with the given portlet ID.
     * 
     * @param person the person removing the portlet
     * @param form
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
            logger.warn("User '" + person.getUserName() +  "' attempted to remove portlet '" + 
                                form.getFname() + "' without the proper MANAGE permission");
            throw new SecurityException("Not Authorized");
        }

        IPortletDefinition def = portletDefinitionRegistry.getPortletDefinition(form.getId());
        portletDefinitionRegistry.deletePortletDefinition(def);

    }


    /**
     * Check if the link to the Fragment admin portlet should display in the status message.
     *
     * Checks that the portlet is new, that the portlet has been published and that
     * the user has necessary permissions to go to the fragment admin page.
     *
     * @param person the person publishing/editing the portlet
     * @param form the portlet being editted
     * @param portletId the id of the saved portlet
     * @return true If all three conditions are met
     */
    public boolean shouldDisplayLayoutLink(IPerson person, PortletDefinitionForm form, String portletId) {
        if (!form.isNew()) {
            return false;
        }

        // only include the "do layout" link for published portlets.
        if (form.getLifecycleState() != PortletLifecycleState.PUBLISHED) {
            return false;
        }

        // check that the user can edit at least 1 fragment.
        Map<String, String> layouts = fragmentAdminHelper.getAuthorizedDlmFragments(person.getUserName());
        if (layouts == null || layouts.isEmpty()) {
            return false;
        }

        // check that the user has subscribe priv.
        IAuthorizationPrincipal authPrincipal = authorizationService.newPrincipal(person.getUserName(), EntityEnum.PERSON.getClazz());
        if (!authPrincipal.canSubscribe(portletId)) {
            return false;
        }

        return true;
    }


    /**
     * Get the link to the fragment admin portlet.
     *
     * @param request the current http request.
     * @return the portlet link
     */
    public String getFragmentAdminURL(HttpServletRequest request) {
        IPortalUrlBuilder builder = urlProvider.getPortalUrlBuilderByPortletFName(request, PORTLET_FNAME_FRAGMENT_ADMIN_PORTLET, UrlType.RENDER);
        IPortletUrlBuilder portletUrlBuilder = builder.getTargetedPortletUrlBuilder();
        portletUrlBuilder.setPortletMode(PortletMode.VIEW);
        portletUrlBuilder.setWindowState(WindowState.MAXIMIZED);

        return builder.getUrlString();
    }

	/**
	 * Get a list of the key names of the currently-set arbitrary portlet
	 * preferences.
	 * 
	 * @param form
	 * @return
	 */
	public Set<String> getArbitraryPortletPreferenceNames(PortletDefinitionForm form) {
		// set default values for all portlet parameters
		PortletPublishingDefinition cpd = this.portletPublishingDefinitionDao.getChannelPublishingDefinition(form.getTypeId());
		Set<String> currentPrefs = new HashSet<String>();
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
	 * If the portlet is a portlet and if one of the supported portlet modes is {@link IPortletRenderer#CONFIG}
	 */
	public boolean supportsConfigMode(PortletDefinitionForm form) {
	    final Tuple<String, String> portletDescriptorKeys = this.getPortletDescriptorKeys(form);
	    if (portletDescriptorKeys == null) {
	        return false;
	    }
	    final String portletAppId = portletDescriptorKeys.first;
	    final String portletName = portletDescriptorKeys.second;
	    
        final PortletRegistryService portletRegistryService = this.portalDriverContainerServices.getPortletRegistryService();
        final PortletDefinition portletDescriptor;
        try {
            portletDescriptor = portletRegistryService.getPortlet(portletAppId, portletName);
        }
        catch (PortletContainerException e) {
            this.logger.warn("Failed to load portlet descriptor for appId='" + portletAppId + "', portletName='" + portletName + "'", e);
            return false;
        }
        
        if (portletDescriptor == null) {
            return false;
        }
        
        //Iterate over supported portlet modes, this ignores the content types for now
        final List<? extends Supports> supports = portletDescriptor.getSupports();
        for (final Supports support : supports) {
            final List<String> portletModes = support.getPortletModes();
            for (final String portletMode : portletModes) {
                if (IPortletRenderer.CONFIG.equals(PortletUtils.getPortletMode(portletMode))) {
                    return true;
                }
            }
        }
        
        return false;
	}
	
    private static final Pattern PARAM_PATTERN = Pattern.compile("^([^\\[]+)\\['([^\\']+)'\\]\\.value$");
    
    public void cleanOptions(PortletDefinitionForm form, PortletRequest request) {
        //Names of valid preferences and parameters
        final Set<String> preferenceNames = new HashSet<String>();
        final Set<String> parameterNames = new HashSet<String>();

        //Read all of the submitted channel parameter and portlet preference names from the request
        for (final Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
            final String name = e.nextElement();
            final Matcher nameMatcher = PARAM_PATTERN.matcher(name);
            if (nameMatcher.matches()) {
                final String paramType = nameMatcher.group(1);
                final String paramName = nameMatcher.group(2);
                
                if ("portletPreferences".equals(paramType)) {
                    preferenceNames.add(paramName);
                }
                else if ("parameters".equals(paramType)) {
                    parameterNames.add(paramName);
                }
            }
        }

        //Add all of the parameter and preference names that have default values in the CPD into the valid name sets
        final PortletPublishingDefinition cpd = this.portletPublishingDefinitionDao.getChannelPublishingDefinition(form.getTypeId());
        for (final Step step : cpd.getSteps()) {
            final List<Parameter> parameters = step.getParameters();
            if (parameters != null) {
                for (final Parameter parameter : parameters) {
                    final JAXBElement<? extends ParameterInputType> parameterInput = parameter.getParameterInput();
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
                    final JAXBElement<? extends PreferenceInputType> preferenceInput = preference.getPreferenceInput();
                    final PreferenceInputType preferenceInputType = preferenceInput.getValue();
                    if (preferenceInputType instanceof MultiValuedPreferenceInputType) {
                        final MultiValuedPreferenceInputType multiValuedPreferenceInputType = (MultiValuedPreferenceInputType)preferenceInputType;
                        final List<String> defaultValues = multiValuedPreferenceInputType.getDefaults();
                        if (defaultValues != null && !defaultValues.isEmpty()) {
                            preferenceNames.add(preference.getName());
                        }
                    }
                    else if (preferenceInputType instanceof SingleValuedPreferenceInputType) {
                        final SingleValuedPreferenceInputType SingleValuedPreferenceInputType = (SingleValuedPreferenceInputType)preferenceInputType;
                        if (SingleValuedPreferenceInputType.getDefault() != null) {
                            preferenceNames.add(preference.getName());
                        }
                    }
                }
            }
        }
        
        //Remove portlet preferences from the form object that were not part of this request or defined in the CPD
        // - do it only if portlet doesn't support configMode
        if (!this.supportsConfigMode(form)) {
            final Map<String, StringListAttribute> portletPreferences = form.getPortletPreferences();
            final Map<String, BooleanAttribute> portletPreferencesOverrides = form.getPortletPreferenceReadOnly();
            
            for (final Iterator<Entry<String, StringListAttribute>> portletPreferenceEntryItr = portletPreferences.entrySet().iterator(); portletPreferenceEntryItr.hasNext();) {
                final Map.Entry<String, StringListAttribute> portletPreferenceEntry = portletPreferenceEntryItr.next();
                final String key = portletPreferenceEntry.getKey();
                final StringListAttribute valueAttr = portletPreferenceEntry.getValue();
                if (!preferenceNames.contains(key) || valueAttr == null) {
                    portletPreferenceEntryItr.remove();
                    portletPreferencesOverrides.remove(key);
                } else {
                    final List<String> values = valueAttr.getValue();
                    for (final Iterator<String> iter = values.iterator(); iter.hasNext();) {
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

        for (final Iterator<Entry<String, Attribute>> parameterEntryItr = parameters.entrySet().iterator(); parameterEntryItr.hasNext();) {
            final Entry<String, Attribute> parameterEntry = parameterEntryItr.next();
            final String key = parameterEntry.getKey();
            final Attribute value = parameterEntry.getValue();
            
            if (!parameterNames.contains(key) || value == null || StringUtils.isBlank(value.getValue())) {
                parameterEntryItr.remove();
            }
        }
    }
	
	/**
	 * Retreive the list of portlet application contexts currently available in
	 * this portlet container.
	 * 
	 * @return list of portlet context
	 */
	public List<PortletApplicationDefinition> getPortletApplications() {
		final PortletRegistryService portletRegistryService = portalDriverContainerServices.getPortletRegistryService();
		final List<PortletApplicationDefinition> contexts = new ArrayList<PortletApplicationDefinition>();

		for (final Iterator<String> iter = portletRegistryService.getRegisteredPortletApplicationNames(); iter.hasNext();) {
			final String applicationName = iter.next();
			final PortletApplicationDefinition applicationDefninition;
            try {
                applicationDefninition = portletRegistryService.getPortletApplication(applicationName);
            }
            catch (PortletContainerException e) {
                throw new RuntimeException("Failed to load PortletApplicationDefinition for '" + applicationName + "'");
            }
			
			final List<? extends PortletDefinition> portlets = applicationDefninition.getPortlets();
			Collections.sort(portlets, new ComparableExtractingComparator<PortletDefinition, String>(String.CASE_INSENSITIVE_ORDER) {
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
		
		
		Collections.sort(contexts, new ComparableExtractingComparator<PortletApplicationDefinition, String>(String.CASE_INSENSITIVE_ORDER) {
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
	 * Get a portlet descriptor matching the current portlet definition form.
	 * If the current form does not represent a portlet, the application or 
	 * portlet name fields are blank, or the portlet description cannot be 
	 * retrieved, the method will return <code>null</code>.
	 * 
	 * @param form
	 * @return
	 */
	public PortletDefinition getPortletDescriptor(PortletDefinitionForm form) {
		final Tuple<String, String> portletDescriptorKeys = this.getPortletDescriptorKeys(form);
		if (portletDescriptorKeys == null) {
		    return null;
		}
        final String portletAppId = portletDescriptorKeys.first;
        final String portletName = portletDescriptorKeys.second;

		
		final PortletRegistryService portletRegistryService = portalDriverContainerServices.getPortletRegistryService();
		try {
			PortletDefinition portletDD = portletRegistryService.getPortlet(portletAppId, portletName);
			return portletDD;
		} catch (PortletContainerException e) {
			e.printStackTrace();
			return null;
		}
	}

    /**
     * Pre-populate a new {@link PortletDefinitionForm} with information from
     * the {@link PortletDefinition}.
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

        final PortletRegistryService portletRegistryService = portalDriverContainerServices.getPortletRegistryService();
        final PortletDefinition portletDef;
        try {
            portletDef = portletRegistryService.getPortlet(form.getApplicationId(), form.getPortletName());
        }
        catch (PortletContainerException e) {
            this.logger.warn("Failed to load portlet descriptor for appId='" + form.getApplicationId() + "', portletName='" + form.getPortletName() + "'", e);
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

	public Set<PortletLifecycleState> getAllowedLifecycleStates(IPerson person, List<JsonEntityBean> categories) {
		Set<PortletLifecycleState> states = new TreeSet<PortletLifecycleState>();
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
	
	public boolean hasLifecyclePermission(IPerson person, PortletLifecycleState state, List<JsonEntityBean> categories) {
		EntityIdentifier ei = person.getEntityIdentifier();
	    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
	    
        final String activity;
        switch (state) {
            case APPROVED: {
                activity = IPermission.PORTLET_MANAGER_APPROVED_ACTIVITY;
                break;
            }
            case CREATED: {
                activity = IPermission.PORTLET_MANAGER_CREATED_ACTIVITY;
                break;
            }
            case PUBLISHED: {
                activity = IPermission.PORTLET_MANAGER_ACTIVITY;
                break;
            }
            case EXPIRED: {
                activity = IPermission.PORTLET_MANAGER_EXPIRED_ACTIVITY;
                break;
            }
            case MAINTENANCE: {
                activity = IPermission.PORTLET_MANAGER_MAINTENANCE_ACTIVITY;
                break;
            }
            default: {
                throw new IllegalArgumentException("");
            }
        }
        if (ap.hasPermission(IPermission.PORTAL_PUBLISH, activity, IPermission.ALL_PORTLETS_TARGET)) {
            logger.debug("Found permission for category ALL_PORTLETS and lifecycle state " + state.toString());
            return true;
        }
	    
		for (JsonEntityBean category : categories) {
			if (ap.canManage(state, category.getId())) {
				logger.debug("Found permission for category " + category.getName() + " and lifecycle state " + state.toString());
				return true;
			}
		}
		logger.debug("No permission for lifecycle state " + state.toString());
		return false;
	}
	
	public IPortletWindowId getDelegateWindowId(ExternalContext externalContext, String fname) {
	    final PortletRequest nativeRequest = (PortletRequest)externalContext.getNativeRequest();
        final PortletSession portletSession = nativeRequest.getPortletSession();
        return (IPortletWindowId)portletSession.getAttribute(RenderPortletTag.DEFAULT_SESSION_KEY_PREFIX + fname);
	}
	
	public boolean configModeAction(ExternalContext externalContext, String fname) throws IOException {

	    final ActionRequest actionRequest = (ActionRequest)externalContext.getNativeRequest();
	    final ActionResponse actionResponse = (ActionResponse)externalContext.getNativeResponse();

	    final IPortletWindowId portletWindowId = this.getDelegateWindowId(externalContext, fname);
	    if (portletWindowId == null) {
	        throw new IllegalStateException("Cannot execute configModeAciton without a delegate window ID in the session for key: " + RenderPortletTag.DEFAULT_SESSION_KEY_PREFIX + fname);
	    }
	    
	    final PortletDelegationDispatcher requestDispatcher = this.portletDelegationLocator.getRequestDispatcher(actionRequest, portletWindowId);
	    
	    final DelegationActionResponse delegationResponse = requestDispatcher.doAction(actionRequest, actionResponse);
	    
	    final String redirectLocation = delegationResponse.getRedirectLocation();
	    final DelegateState delegateState = delegationResponse.getDelegateState();
        if (redirectLocation != null || 
	            (delegationResponse.getPortletMode() != null && !IPortletRenderer.CONFIG.equals(delegationResponse.getPortletMode())) ||
	            !IPortletRenderer.CONFIG.equals(delegateState.getPortletMode())) {
	        
	        //The portlet sent a redirect OR changed it's mode away from CONFIG, assume it is done
	        return true;
	    }

	    return false;
	}
	
	/**
	 * updates the editPortlet form with the portletType 
	 * of the first (and only) portletDefinition passed in through the Map of 
	 * portlet definitions.
	 * @param portletDefinitions
	 * @param form
	 * @return PortletPublishingDefinition of the first portlet definition in the 
	 * list, null if the list is empty or has more than one element. 
	 */
	public PortletPublishingDefinition updateFormForSinglePortletType( Map<IPortletType, PortletPublishingDefinition> portletDefinitions, PortletDefinitionForm form) {
		if (portletDefinitions.size() != 1) {
			return null;
		}
		IPortletType portletType = portletDefinitions.keySet().iterator().next();
		form.setTypeId(portletType.getId());
		PortletPublishingDefinition cpd = portletPublishingDefinitionDao.getChannelPublishingDefinition(portletType.getId());
		form.setChannelPublishingDefinition(cpd);
		
		return cpd;
	}
	
	public boolean offerPortletSelection(PortletDefinitionForm form) {
		final IPortletType portletType = this.portletTypeRegistry.getPortletType(form.getTypeId());
		final PortletPublishingDefinition portletPublishingDefinition = this.portletPublishingDefinitionDao.getChannelPublishingDefinition(portletType.getId());
		final PortletDescriptor portletDescriptor = portletPublishingDefinition.getPortletDescriptor();
		if (portletDescriptor == null) {
		    return true;
		}
		
		final Boolean isFramework = portletDescriptor.isIsFramework();
		if (isFramework != null && isFramework) {
		    form.setFramework(isFramework);
		}
		else {
		    final String webAppName = portletDescriptor.getWebAppName();
            form.setApplicationId(webAppName);
		}
		
		final String portletName = portletDescriptor.getPortletName();
        form.setPortletName(portletName);
		
		return false;
	}

    public Map<IPortletType, PortletPublishingDefinition> getAllowableChannelPublishingDefinitions(IPerson user) {

        Map<IPortletType, PortletPublishingDefinition> rslt;

        final Map<IPortletType, PortletPublishingDefinition> rawMap = portletPublishingDefinitionDao.getChannelPublishingDefinitions();
        final IAuthorizationPrincipal principal = AuthorizationPrincipalHelper.principalFromUser(user);
        if (principal.hasPermission(IPermission.PORTAL_PUBLISH, IPermission.PORTLET_MANAGER_SELECT_PORTLET_TYPE, IPermission.ALL_PORTLET_TYPES)) {
            // Send the whole collection back...
            rslt = rawMap;
        } else {
            // Filter the collection by permissions...
            rslt = new HashMap<IPortletType, PortletPublishingDefinition>();
            for (Map.Entry<IPortletType, PortletPublishingDefinition> y : rawMap.entrySet()) {
                if (principal.hasPermission(IPermission.PORTAL_PUBLISH, IPermission.PORTLET_MANAGER_SELECT_PORTLET_TYPE, y.getKey().getName())) {
                    rslt.put(y.getKey(), y.getValue());
                }
            }
        }

        return rslt;

    }

    protected Tuple<String, String> getPortletDescriptorKeys(PortletDefinitionForm form) {
        if (form.getPortletName() == null || (form.getApplicationId() == null && !form.isFramework())) {
            return null;
        }
        
        final String portletAppId;
        if (form.isFramework()) {
            portletAppId = this.servletContext.getContextPath();
        }
        else {
            portletAppId = form.getApplicationId();
        }
        
        final String portletName = form.getPortletName();
        
        return new Tuple<String, String>(portletAppId, portletName);
	}

    private void updateLifecycleState(PortletDefinitionForm form, IPortletDefinition portletDef, IPerson publisher) {

        /*
         * Manage the metadata for each possible lifecycle state in turn...
         */

        Date now = new Date();  // Will be entered as the timestamp for states that we trigger
        PortletLifecycleState selectedLifecycleState = form.getLifecycleState();

        /*
         * APPROVED
         */
        if (selectedLifecycleState.isEqualToOrAfter(PortletLifecycleState.APPROVED)) {
            // We are the 'approver' if it isn't previously approved...
            if (portletDef.getApprovalDate() == null) {
                portletDef.setApproverId(publisher.getID());
                portletDef.setApprovalDate(now);
            }
            if (selectedLifecycleState.equals(PortletLifecycleState.APPROVED)
                    && form.getPublishDate() != null
                    // Permissions check required (of course) to use the auto-publish feature
                    && hasLifecyclePermission(publisher, PortletLifecycleState.PUBLISHED, form.getCategories())) {
                // We are also the 'publisher' if we scheduled the portlet for (future) publication...
                portletDef.setPublishDate(form.getPublishDateTime());
                portletDef.setPublisherId(publisher.getID());
            }
        } else {
            // Clear previous approval fields, if present...
            portletDef.setApprovalDate(null);
            portletDef.setApproverId(-1);
        }

        /*
         * PUBLISHED
         */
        if (selectedLifecycleState.isEqualToOrAfter(PortletLifecycleState.PUBLISHED)) {
            // We are the 'publisher' if it isn't previously published or the publish time hasn't hit yet...
            if (portletDef.getPublishDate() == null || portletDef.getPublishDate().after(now)) {
                portletDef.setPublisherId(publisher.getID());
                portletDef.setPublishDate(now);
            }
            if (selectedLifecycleState.equals(PortletLifecycleState.PUBLISHED)
                    && form.getExpirationDate() != null
                    // Permissions check required (of course) to use the auto-expire feature
                    && hasLifecyclePermission(publisher, PortletLifecycleState.EXPIRED, form.getCategories())) {
                // We are also the 'expirer' if we scheduled the portlet for (future) expiration...
                portletDef.setExpirationDate(form.getExpirationDateTime());
                portletDef.setExpirerId(publisher.getID());
            }
        } else if (!selectedLifecycleState.equals(PortletLifecycleState.APPROVED)
                || form.getPublishDate() == null){
            // Clear previous publishing fields, if present...
            portletDef.setPublishDate(null);
            portletDef.setPublisherId(-1);
        }

        /*
         * EXPIRED
         */
        if (selectedLifecycleState.equals(PortletLifecycleState.EXPIRED)) {
            // We are only the 'expirer' if we specifically choose EXPIRED
            // (MAINTENANCE mode is not considered expired)
            portletDef.setExpirerId(publisher.getID());
            portletDef.setExpirationDate(now);
        } else if (!selectedLifecycleState.equals(PortletLifecycleState.PUBLISHED)
                || form.getExpirationDate() == null) {
            // Clear previous expiration fields, if present...
            portletDef.setExpirationDate(null);
            portletDef.setExpirerId(-1);
        }

        /*
         * MAINTENANCE
         */
        if (selectedLifecycleState.equals(PortletLifecycleState.MAINTENANCE)) {
            // We are placing the portlet into MAINTENANCE mode;
            // an admin will restore it (manually) when available
            portletDef.addParameter(PortletLifecycleState.MAINTENANCE_MODE_PARAMETER_NAME, "true");
        } else {
            // Otherwise we must remove the MAINTENANCE flag, if present
            portletDef.removeParameter(PortletLifecycleState.MAINTENANCE_MODE_PARAMETER_NAME);
        }
    }
}
