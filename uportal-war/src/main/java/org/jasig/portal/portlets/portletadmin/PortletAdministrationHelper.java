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

package org.jasig.portal.portlets.portletadmin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
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
import org.jasig.portal.api.portlet.DelegationActionResponse;
import org.jasig.portal.api.portlet.PortletDelegationDispatcher;
import org.jasig.portal.api.portlet.PortletDelegationLocator;
import org.jasig.portal.channel.IPortletPublishingService;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.delegation.jsp.RenderPortletTag;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.om.PortletLifecycleState;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletTypeRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.portlets.Attribute;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDParameter;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDPreference;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDStep;
import org.jasig.portal.portlets.portletadmin.xmlsupport.ChannelPublishingDefinition;
import org.jasig.portal.portlets.portletadmin.xmlsupport.IChannelPublishingDefinitionDao;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.utils.ComparableExtractingComparator;
import org.jasig.portal.utils.Tuple;
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
	
	private IGroupListHelper groupListHelper;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletCategoryRegistry portletCategoryRegistry;
    private IPortletTypeRegistry portletTypeRegistry;
    private PortalDriverContainerServices portalDriverContainerServices;
    private IPortletPublishingService portletPublishingService; 
    private PortletDelegationLocator portletDelegationLocator;
    private IChannelPublishingDefinitionDao portletPublishingDefinitionDao;
    private ServletContext servletContext;
    
	@Override
    public void setServletContext(ServletContext servletContext) {
	    this.servletContext = servletContext;
    }
	@Autowired
    public void setPortletDelegationLocator(PortletDelegationLocator portletDelegationLocator) {
        this.portletDelegationLocator = portletDelegationLocator;
    }
	@Autowired
    public void setGroupListHelper(IGroupListHelper groupListHelper) {
		this.groupListHelper = groupListHelper;
	}
	
	/**
	 * Set the portlet registry store
	 * 
	 * @param portletRegistryStore
	 */
	@Autowired
	public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
		this.portletDefinitionRegistry = portletDefinitionRegistry;
	}
	
	@Autowired
	public void setPortletCategoryRegistry(IPortletCategoryRegistry portletCategoryRegistry) {
		this.portletCategoryRegistry = portletCategoryRegistry;
	}
	
	@Autowired
	public void setPortletTypeRegistry(IPortletTypeRegistry portletTypeRegistry) {
		this.portletTypeRegistry = portletTypeRegistry;
	}
	
	@Autowired
	public void setPortalDriverContainerServices(
			PortalDriverContainerServices portalDriverContainerServices) {
		this.portalDriverContainerServices = portalDriverContainerServices;
	}
	@Autowired
	public void setPortletPublishingService(
			IPortletPublishingService portletPublishingService) {
		this.portletPublishingService = portletPublishingService;
	}
	@Autowired
	public void setPortletChannelPublishingDefinitionDao(IChannelPublishingDefinitionDao portletPublishingDefinitionDao) {
        this.portletPublishingDefinitionDao = portletPublishingDefinitionDao;
    }

    /**
	 * Construct a new PortletDefinitionForm for the given IPortletDefinition id.
	 * If a PortletDefinition matching this ID already exists, the form will
	 * be pre-populated with the PortletDefinition's current configuration.  If
	 * the PortletDefinition does not yet exist, a new default form will be
	 * created.
	 * 
	 * @param chanId
	 * @return
	 */
	public PortletDefinitionForm getPortletDefinitionForm(String portletId) {
		
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
                IPermissionManager pm = AuthorizationService.instance().newPermissionManager(IPortletPublishingService.FRAMEWORK_OWNER);
                IAuthorizationPrincipal[] prins = pm.getAuthorizedPrincipals(IPortletPublishingService.SUBSCRIBER_ACTIVITY,
                        IPermission.PORTLET_PREFIX + String.valueOf(form.getId()));
                for (int mp = 0; mp < prins.length; mp++) {
                	JsonEntityBean bean;
                	
                	// first assume this is a group
                	IEntityGroup group = GroupService.findGroup(prins[mp].getKey());
                	if (group != null) {
                    	bean = new JsonEntityBean(group, EntityEnum.GROUP.toString());
                	} 
                	
                	// if a matching group can't be found, try to find a matching
                	// non-group entity
                	else {
                    	IGroupMember member = AuthorizationService.instance().getGroupMember(prins[mp]);
                    	bean = new JsonEntityBean(member, EntityEnum.PERSON.toString());
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

		return form;
	}
	
	/**
	 * Persist a new or edited PortletDefinition.
	 * 
	 * @param form
	 * @param publisher
	 */
	public PortletDefinitionForm savePortletRegistration(PortletDefinitionForm form,
			IPerson publisher) throws Exception {
		
		// create the group array from the form's group list
		IGroupMember[] groupMembers = new IGroupMember[form.getGroups().size()];
		for (int i = 0; i < groupMembers.length; i++) {
			JsonEntityBean bean = form.getGroups().get(i);
			EntityEnum entityEnum = EntityEnum.getEntityEnum(bean.getEntityType());
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
	    portletDef.setEditable(form.isEditable());
	    portletDef.setFName(form.getFname());
	    portletDef.setHasAbout(form.isHasAbout());
	    portletDef.setHasHelp(form.isHasHelp());
	    portletDef.setName(form.getName());
	    portletDef.setTimeout(form.getTimeout());
	    portletDef.setTitle(form.getTitle());
	    portletDef.getPortletDescriptorKey().setWebAppName(form.getApplicationId());
	    portletDef.getPortletDescriptorKey().setPortletName(form.getPortletName());
	    portletDef.getPortletDescriptorKey().setFrameworkPortlet(form.isFramework());
	    
	    Date now = new Date();

		int order = form.getLifecycleState().getOrder();
		
		if (form.getId() == null) {
			
			if (order >= PortletLifecycleState.APPROVED.getOrder()) {
				portletDef.setApproverId(publisher.getID());
				portletDef.setApprovalDate(now);
			}
			
			if (order >= PortletLifecycleState.PUBLISHED.getOrder()) {
			    portletDef.setPublisherId(publisher.getID());
			    if (portletDef.getPublishDate() == null) {
				    portletDef.setPublishDate(now);
			    }
			} else if (form.getPublishDate() != null) {
				portletDef.setPublishDate(form.getPublishDateTime());
				portletDef.setPublisherId(publisher.getID());
			}

			if (order >= PortletLifecycleState.EXPIRED.getOrder()) {
			    portletDef.setExpirerId(publisher.getID());
			    if (portletDef.getExpirationDate() == null) {
			    	portletDef.setExpirationDate(now);
			    }
			} else if (form.getExpirationDate() != null) {
				portletDef.setExpirationDate(form.getExpirationDateTime());
				portletDef.setExpirerId(publisher.getID());
			}
			
		} 
		
		// if we're updating a portlet
		else {

			if (order >= PortletLifecycleState.APPROVED.getOrder()) {
				if (portletDef.getApproverId() < 0) {
					portletDef.setApproverId(publisher.getID());
				}
				if (portletDef.getApprovalDate() == null) {
					portletDef.setApprovalDate(now);
				}
			} else {
				portletDef.setApprovalDate(null);
				portletDef.setApproverId(-1);
			}
			
			if (order >= PortletLifecycleState.PUBLISHED.getOrder()) {
				if (portletDef.getPublisherId() < 0) {
					portletDef.setPublisherId(publisher.getID());
				}
				if (portletDef.getPublishDate() == null) {
					portletDef.setPublishDate(now);
				}
			} else if (form.getPublishDate() != null) {
				portletDef.setPublishDate(form.getPublishDate());
				if (portletDef.getPublisherId() < 0) {
					portletDef.setPublisherId(publisher.getID());
				}
			} else {
				portletDef.setPublishDate(null);
				portletDef.setPublisherId(-1);
			}
			
			if (order >= PortletLifecycleState.EXPIRED.getOrder()) {
				if (portletDef.getExpirerId() < 0) {
					portletDef.setExpirerId(publisher.getID());
				}
				if (portletDef.getExpirationDate() == null) {
					portletDef.setExpirationDate(now);
				}
			} else if (form.getExpirationDate() != null) {
				portletDef.setExpirationDate(form.getExpirationDate());
				if (portletDef.getExpirerId() < 0) {
					portletDef.setExpirerId(publisher.getID());
				}
			} else {
				portletDef.setExpirationDate(null);
				portletDef.setExpirerId(-1);
			}
			
		}

	    
	    final IPortletType portletType = portletTypeRegistry.getPortletType(form.getTypeId());
	    if (portletType == null) {
	        throw new IllegalArgumentException("No IPortletType exists for ID " + form.getTypeId());
	    }
	    portletDef.setType(portletType);
	    
	    // add portlet parameters
		List<IPortletPreference> preferenceList = new ArrayList<IPortletPreference>();
		for (String key : form.getParameters().keySet()) {
			String value = form.getParameters().get(key).getValue();
			if (!StringUtils.isBlank(value)) {
				boolean override = false;
				if (key.startsWith("PORTLET.")) {
					preferenceList.add(new PortletPreferenceImpl(key, !override, new String[]{value}));
				} else {
					portletDef.addParameter(key, value);
				}
			}
		}
		
		for (String key : form.getPortletPreferences().keySet()) {
			List<String> prefValues = form.getPortletPreferences().get(key).getValue();
			if (prefValues != null && prefValues.size() > 0) {
				String[] values = prefValues.toArray(new String[prefValues.size()]);
				// fix read-only
				preferenceList.add(new PortletPreferenceImpl(key, false, values));
			}
		}
		final IPortletPreferences portletPreferences = portletDef.getPortletPreferences();
		portletPreferences.setPortletPreferences(preferenceList);
	    
	    portletPublishingService.savePortletDefinition(portletDef, publisher, Arrays.asList(categories), Arrays.asList(groupMembers));

	    return this.getPortletDefinitionForm(portletDef.getPortletDefinitionId().getStringId());
	}
	
	/**
	 * Delete the portlet with the given portlet ID.
	 * 
	 * @param portletID the portlet ID
	 * @param person the person removing the portlet
	 */
	public void removePortletRegistration(String portletId, IPerson person) {
		IPortletDefinition def = portletDefinitionRegistry.getPortletDefinition(portletId);
		portletDefinitionRegistry.deletePortletDefinition(def);
	}
	
	/**
	 * Get a list of the key names of the currently-set arbitrary portlet
	 * preferences.
	 * 
	 * @param form
	 * @param cpd
	 * @return
	 */
	public Set<String> getArbitraryPortletPreferenceNames(PortletDefinitionForm form) {
		// set default values for all portlet parameters
		ChannelPublishingDefinition cpd = this.portletPublishingDefinitionDao.getChannelPublishingDefinition(form.getTypeId());
		Set<String> currentPrefs = new HashSet<String>();
		currentPrefs.addAll(form.getPortletPreferences().keySet());
		for (CPDStep step : cpd.getParams().getSteps()) {
			if (step.getPreferences() != null) {
				for (CPDPreference pref : step.getPreferences()) {
					currentPrefs.remove(pref.getName());
				}
			}
			if (step.getParameters() != null) {
				for (CPDParameter param : step.getParameters()) {
					if (param.getName().startsWith("PORTLET.")) {
						currentPrefs.remove(param.getName().replace("PORTLET.", ""));
					}
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
                if (IPortletRenderer.CONFIG.equals(new PortletMode(portletMode))) {
                    return true;
                }
            }
        }
        
        return false;
	}
	
	public void cleanOptions(PortletDefinitionForm form, PortletRequest request) {
		Set<String> preferenceNames = new HashSet<String>();
		Set<String> parameterNames = new HashSet<String>();
		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
			String name = e.nextElement();
			if (name.startsWith("portletPreferences[")) {
				preferenceNames.add(name.split("\'")[1]);
			} else if (name.startsWith("parameters[")) {
				parameterNames.add(name.split("\'")[1]);
			}
		}
		
		Set<String> keys = new HashSet<String>();
		keys.addAll(form.getPortletPreferences().keySet());
		for (String key : keys) {
			if (!preferenceNames.contains(key)) {
				form.getPortletPreferences().remove(key);
			} else if (form.getPortletPreferences().get(key) == null) {
				form.getPortletPreferences().remove(key);
			} else {
				List<String> values = form.getPortletPreferences().get(key).getValue();
				for (ListIterator<String> iter = values.listIterator(); iter.hasNext();) {
					String value = iter.next();
					if (StringUtils.isEmpty(value)) {
						iter.remove();
					}
				}
				if (values.size() == 0) {
					form.getPortletPreferences().remove(key);
				}
			}
		}
		
		keys = new HashSet<String>();
		keys.addAll(form.getParameters().keySet());
		for (String key : keys) {
			if (!parameterNames.contains(key)) {
				form.getParameters().remove(key);
			} else if (form.getParameters().get(key) == null || StringUtils.isBlank(form.getParameters().get(key).getValue())) {
				form.getParameters().remove(key);
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
	 * Pre-populate a PortletDefinitionForm with portlet-specific information
	 * using the supplied portlet descriptor.
	 * 
	 * @param application
	 * @param portlet
	 * @param form
	 */
	public void prepopulatePortlet(String application, String portlet, PortletDefinitionForm form) {
		final PortletRegistryService portletRegistryService = portalDriverContainerServices.getPortletRegistryService();
		final PortletDefinition portletDD;
		try {
		    portletDD = portletRegistryService.getPortlet(application, portlet);
        }
		catch (PortletContainerException e) {
		    this.logger.warn("Failed to load portlet descriptor for appId='" + application + "', portletName='" + portlet + "'", e);
            return;
        }
		    
	    form.setTitle(portletDD.getPortletName());
		form.setName(portletDD.getPortletName());
		form.setApplicationId(application);
		form.setPortletName(portletDD.getPortletName());
		for (Supports supports : portletDD.getSupports()) {
			for (String mode : supports.getPortletModes()) {
				if ("edit".equals(mode)) {
					form.setEditable(true);
				} else if ("help".equals(mode)) {
					form.setHasHelp(true);
				}
			}
		}
	}
	
	public PortletLifecycleState[] getLifecycleStates() {
		return PortletLifecycleState.values();
	}

	public Set<PortletLifecycleState> getAllowedLifecycleStates(IPerson person, List<JsonEntityBean> categories) {
		Set<PortletLifecycleState> states = new TreeSet<PortletLifecycleState>();
		if (hasLifecyclePermission(person, PortletLifecycleState.EXPIRED, categories)) {
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
            default: {
                throw new IllegalArgumentException("");
            }
        }
        if (ap.hasPermission("UP_FRAMEWORK", activity, IPermission.ALL_PORTLETS_TARGET)) {
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
	
	
	public boolean configModeAction(ExternalContext externalContext, String fname) throws IOException {
	    final ActionRequest actionRequest = (ActionRequest)externalContext.getNativeRequest();
	    final ActionResponse actionResponse = (ActionResponse)externalContext.getNativeResponse();
	    
	    final PortletSession portletSession = actionRequest.getPortletSession();
	    final IPortletWindowId portletWindowId = (IPortletWindowId)portletSession.getAttribute(RenderPortletTag.DEFAULT_SESSION_KEY_PREFIX + fname);
	    if (portletWindowId == null) {
	        throw new IllegalStateException("Cannot execute configModeAciton without a delegate window ID in the session for key: " + RenderPortletTag.DEFAULT_SESSION_KEY_PREFIX + fname);
	    }
	    
	    final PortletDelegationDispatcher requestDispatcher = this.portletDelegationLocator.getRequestDispatcher(actionRequest, portletWindowId);
	    
	    final DelegationActionResponse delegationResponse = requestDispatcher.doAction(actionRequest, actionResponse);
//	    final PortletUrl renderUrl = delegationResponse.getRenderUrl();
//	    final DelegateState delegateState = delegationResponse.getDelegateState();
//        if (renderUrl == null || 
//	            (renderUrl.getPortletMode() != null && !IPortletRenderer.CONFIG.equals(renderUrl.getPortletMode())) ||
//	            !IPortletRenderer.CONFIG.equals(delegateState.getPortletMode())) {
//	        
//	        //The portlet sent a redirect OR changed it's mode away from CONFIG, assume it is done
//	        return true;
//	    }
	    
	    return false;
	}
	
	public boolean offerPortletSelection(PortletDefinitionForm form) {
		Map<String, Attribute> parameters = form.getParameters();
		if (parameters.get("portletName") != null
				&& !StringUtils.isBlank(parameters.get("portletName").getValue())){
			return false;
		}
		
		return true;
	}
	
	protected Tuple<String, String> getPortletDescriptorKeys(PortletDefinitionForm form) {
        if (form.getPortletName() == null || form.getApplicationId() == null) {
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
}
