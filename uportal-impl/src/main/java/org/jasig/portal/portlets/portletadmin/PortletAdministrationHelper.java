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
import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.portlet.SupportsDD;
import org.apache.pluto.internal.impl.PortletContextImpl;
import org.apache.pluto.spi.optional.PortletRegistryService;
import org.jasig.portal.ChannelCategory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.api.portlet.DelegateState;
import org.jasig.portal.api.portlet.DelegationActionResponse;
import org.jasig.portal.api.portlet.PortletDelegationDispatcher;
import org.jasig.portal.api.portlet.PortletDelegationLocator;
import org.jasig.portal.channel.ChannelLifecycleState;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelPublishingService;
import org.jasig.portal.channel.IChannelType;
import org.jasig.portal.channels.portlet.CSpringPortletAdaptor;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
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
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.url.PortletUrl;
import org.jasig.portal.portlets.Attribute;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDParameter;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDPreference;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDStep;
import org.jasig.portal.portlets.portletadmin.xmlsupport.ChannelPublishingDefinition;
import org.jasig.portal.portlets.portletadmin.xmlsupport.IChannelPublishingDefinitionDao;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
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
    private IChannelRegistryStore channelRegistryStore;
    private OptionalContainerServices optionalContainerServices;
    private IChannelPublishingService channelPublishingService; 
    private PortletDelegationLocator portletDelegationLocator;
    private IChannelPublishingDefinitionDao channelPublishingDefinitionDao;
    private ServletContext servletContext;
    
	@Override
    public void setServletContext(ServletContext servletContext) {
	    this.servletContext = servletContext;
    }
	@Autowired(required=true)
    public void setPortletDelegationLocator(PortletDelegationLocator portletDelegationLocator) {
        this.portletDelegationLocator = portletDelegationLocator;
    }
	@Autowired(required=true)
    public void setGroupListHelper(IGroupListHelper groupListHelper) {
		this.groupListHelper = groupListHelper;
	}
	
	/**
	 * Set the channel registry store
	 * 
	 * @param channelRegistryStore
	 */
	@Autowired(required=true)
	public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
		this.channelRegistryStore = channelRegistryStore;
	}
	@Autowired(required=true)
	public void setOptionalContainerServices(
			OptionalContainerServices optionalContainerServices) {
		this.optionalContainerServices = optionalContainerServices;
	}
	@Autowired(required=true)
	public void setChannelPublishingService(
			IChannelPublishingService channelPublishingService) {
		this.channelPublishingService = channelPublishingService;
	}
	@Autowired(required=true)
	public void setChannelPublishingDefinitionDao(IChannelPublishingDefinitionDao channelPublishingDefinitionDao) {
        this.channelPublishingDefinitionDao = channelPublishingDefinitionDao;
    }

    /**
	 * Construct a new ChannelDefinitionForm for the given IChannelDefinition id.
	 * If a ChannelDefinition matching this ID already exists, the form will
	 * be pre-populated with the ChannelDefinition's current configuration.  If
	 * the ChannelDefinition does not yet exist, a new default form will be
	 * created.
	 * 
	 * @param chanId
	 * @return
	 */
	public ChannelDefinitionForm getChannelDefinitionForm(int chanId) {
		
		IChannelDefinition def = channelRegistryStore.getChannelDefinition(chanId);
		
		// create the new form
		final ChannelDefinitionForm form;
		if (def != null) {
		    form = new ChannelDefinitionForm(def);
		    form.setId(def.getId());
		}
		else {
		    form = new ChannelDefinitionForm();
		}
		
		// if this is a pre-existing channel, set the category and permissions
        if (def != null) {
        	
        	// create a JsonEntityBean for each current category and add it 
        	// to our form bean's category list
        	ChannelCategory[] categories = channelRegistryStore.getParentCategories(def);
        	for (ChannelCategory cat : categories) {
        		form.addCategory(new JsonEntityBean(cat));
        	}

			try {
                IPermissionManager pm = AuthorizationService.instance().newPermissionManager(IChannelPublishingService.FRAMEWORK_OWNER);
                IAuthorizationPrincipal[] prins = pm.getAuthorizedPrincipals(IChannelPublishingService.SUBSCRIBER_ACTIVITY,
                        "CHAN_ID." + String.valueOf(form.getId()));
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
        
        // otherwise, if this is a new channel, pre-populate the categories
        // and groups with some reasonable defaults
        else {
        	
			// pre-populate with top-level category
			IEntityGroup channelCategoriesGroup = GroupService.getDistinguishedGroup(GroupService.CHANNEL_CATEGORIES);
			form.addCategory(new JsonEntityBean(channelCategoriesGroup, groupListHelper.getEntityType(channelCategoriesGroup)));

			// pre-populate with top-level group
			IEntityGroup everyoneGroup = GroupService.getDistinguishedGroup(GroupService.EVERYONE);
			form.addGroup(new JsonEntityBean(everyoneGroup, groupListHelper.getEntityType(everyoneGroup)));
		}

		return form;
	}
	
	/**
	 * Persist a new or edited ChannelDefinition.
	 * 
	 * @param form
	 * @param publisher
	 */
	public ChannelDefinitionForm savePortletRegistration(ChannelDefinitionForm form,
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
		ChannelCategory[] categories = new ChannelCategory[form.getCategories().size()];
		for (ListIterator<JsonEntityBean> iter = form.getCategories().listIterator(); iter.hasNext();) {
			String id = iter.next().getId();
			String iCatID = id.startsWith("cat") ? id.substring(3) : id;
			categories[iter.previousIndex()] = channelRegistryStore
					.getChannelCategory(iCatID);
		}

	    IChannelDefinition channelDef = channelRegistryStore.getChannelDefinition(form.getId());
	    if (channelDef == null) {
	        final String fname = form.getFname();
	        final String clazz = form.getJavaClass();
	        final String name = form.getName();
	        final String title = form.getTitle();
	        final int typeId = form.getTypeId();
	        
	    	channelDef = channelRegistryStore.newChannelDefinition(typeId, fname, clazz, name, title);
	    }
	    channelDef.setDescription(form.getDescription());
	    channelDef.setEditable(form.isEditable());
	    channelDef.setFName(form.getFname());
	    channelDef.setHasAbout(form.isHasAbout());
	    channelDef.setHasHelp(form.isHasHelp());
	    channelDef.setIsSecure(form.isSecure());
	    channelDef.setJavaClass(form.getJavaClass());
	    channelDef.setName(form.getName());
	    channelDef.setTimeout(form.getTimeout());
	    channelDef.setTitle(form.getTitle());
	    
	    Date now = new Date();

		int order = form.getLifecycleState().getOrder();
		
		if (form.getId() < 0) {
			
			if (order >= ChannelLifecycleState.APPROVED.getOrder()) {
				channelDef.setApproverId(publisher.getID());
				channelDef.setApprovalDate(now);
			}
			
			if (order >= ChannelLifecycleState.PUBLISHED.getOrder()) {
			    channelDef.setPublisherId(publisher.getID());
			    if (channelDef.getPublishDate() == null) {
				    channelDef.setPublishDate(now);
			    }
			} else if (form.getPublishDate() != null) {
				channelDef.setPublishDate(form.getPublishDateTime());
				channelDef.setPublisherId(publisher.getID());
			}

			if (order >= ChannelLifecycleState.EXPIRED.getOrder()) {
			    channelDef.setExpirerId(publisher.getID());
			    if (channelDef.getExpirationDate() == null) {
			    	channelDef.setExpirationDate(now);
			    }
			} else if (form.getExpirationDate() != null) {
				channelDef.setExpirationDate(form.getExpirationDateTime());
				channelDef.setExpirerId(publisher.getID());
			}
			
		} 
		
		// if we're updating a channel
		else {

			if (order >= ChannelLifecycleState.APPROVED.getOrder()) {
				if (channelDef.getApproverId() < 0) {
					channelDef.setApproverId(publisher.getID());
				}
				if (channelDef.getApprovalDate() == null) {
					channelDef.setApprovalDate(now);
				}
			} else {
				channelDef.setApprovalDate(null);
				channelDef.setApproverId(-1);
			}
			
			if (order >= ChannelLifecycleState.PUBLISHED.getOrder()) {
				if (channelDef.getPublisherId() < 0) {
					channelDef.setPublisherId(publisher.getID());
				}
				if (channelDef.getPublishDate() == null) {
					channelDef.setPublishDate(now);
				}
			} else if (form.getPublishDate() != null) {
				channelDef.setPublishDate(form.getPublishDate());
				if (channelDef.getPublisherId() < 0) {
					channelDef.setPublisherId(publisher.getID());
				}
			} else {
				channelDef.setPublishDate(null);
				channelDef.setPublisherId(-1);
			}
			
			if (order >= ChannelLifecycleState.EXPIRED.getOrder()) {
				if (channelDef.getExpirerId() < 0) {
					channelDef.setExpirerId(publisher.getID());
				}
				if (channelDef.getExpirationDate() == null) {
					channelDef.setExpirationDate(now);
				}
			} else if (form.getExpirationDate() != null) {
				channelDef.setExpirationDate(form.getExpirationDate());
				if (channelDef.getExpirerId() < 0) {
					channelDef.setExpirerId(publisher.getID());
				}
			} else {
				channelDef.setExpirationDate(null);
				channelDef.setExpirerId(-1);
			}
			
		}

	    
	    final IChannelType channelType = channelRegistryStore.getChannelType(form.getTypeId());
	    if (channelType == null) {
	        throw new IllegalArgumentException("No IChannelType exists for ID " + form.getTypeId());
	    }
	    channelDef.setType(channelType);
	    
	    // add channel parameters
		List<IPortletPreference> preferenceList = new ArrayList<IPortletPreference>();
		for (String key : form.getParameters().keySet()) {
			String value = form.getParameters().get(key).getValue();
			if (!StringUtils.isBlank(value)) {
				boolean override = false;
				if (form.getParameterOverrides().containsKey(key)) {
					override = form.getParameterOverrides().get(key).getValue();
				}
				if (key.startsWith("PORTLET.")) {
					preferenceList.add(new PortletPreferenceImpl(key, !override, new String[]{value}));
				} else {
					channelDef.addParameter(key, value, override);
				}
			}
		}
		
		for (String key : form.getPortletPreferences().keySet()) {
			List<String> prefValues = form.getPortletPreferences().get(key).getValue();
			if (prefValues != null && prefValues.size() > 0) {
				String[] values = prefValues.toArray(new String[prefValues.size()]);
				boolean readOnly = true;
				if (form.getPortletPreferencesOverrides().containsKey(key)) {
					readOnly = !form.getPortletPreferencesOverrides().get(key).getValue();
				}
				preferenceList.add(new PortletPreferenceImpl(key, readOnly, values));
			}
		}
		final IPortletDefinition portletDefinition = channelDef.getPortletDefinition();
		final IPortletPreferences portletPreferences = portletDefinition.getPortletPreferences();
		portletPreferences.setPortletPreferences(preferenceList);
	    
	    channelPublishingService.saveChannelDefinition(channelDef, publisher, categories, groupMembers);

	    return this.getChannelDefinitionForm(channelDef.getId());
	}
	
	/**
	 * Delete the portlet with the given channel ID.
	 * 
	 * @param channelID the channel ID
	 * @param person the person removing the channel
	 */
	public void removePortletRegistration(int channelId, IPerson person) {
		IChannelDefinition channelDef = channelRegistryStore.getChannelDefinition(channelId);
		channelPublishingService.removeChannelDefinition(channelDef, person);
	}
	
	/**
	 * Get a list of the key names of the currently-set arbitrary portlet
	 * preferences.
	 * 
	 * @param form
	 * @param cpd
	 * @return
	 */
	public Set<String> getArbitraryPortletPreferenceNames(ChannelDefinitionForm form) {
		// set default values for all channel parameters
		ChannelPublishingDefinition cpd = this.channelPublishingDefinitionDao.getChannelPublishingDefinition(form.getTypeId());
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
	 * If the channel is a portlet and if one of the supported portlet modes is {@link IPortletAdaptor#CONFIG}
	 */
	public boolean supportsConfigMode(ChannelDefinitionForm form) {
	    if (!form.isPortlet()) {
	        return false;
	    }
	    
	    final Tuple<String, String> portletDescriptorKeys = this.getPortletDescriptorKeys(form);
	    if (portletDescriptorKeys == null) {
	        return false;
	    }
	    final String portletAppId = portletDescriptorKeys.first;
	    final String portletName = portletDescriptorKeys.second;
	    
        final PortletRegistryService portletRegistryService = this.optionalContainerServices.getPortletRegistryService();
        final PortletDD portletDescriptor;
        try {
            portletDescriptor = portletRegistryService.getPortletDescriptor(portletAppId, portletName);
        }
        catch (PortletContainerException e) {
            this.logger.warn("Failed to load portlet descriptor for appId='" + portletAppId + "', portletName='" + portletName + "'", e);
            return false;
        }
        
        if (portletDescriptor == null) {
            return false;
        }
        
        //Iterate over supported portlet modes, this ignores the content types for now
        final List<SupportsDD> supports = portletDescriptor.getSupports();
        for (final SupportsDD support : supports) {
            final List<String> portletModes = support.getPortletModes();
            for (final String portletMode : portletModes) {
                if (IPortletAdaptor.CONFIG.equals(new PortletMode(portletMode))) {
                    return true;
                }
            }
        }
        
        return false;
	}
	
	public void cleanOptions(ChannelDefinitionForm form, PortletRequest request) {
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
				form.getPortletPreferencesOverrides().remove(key);
			} else if (form.getPortletPreferences().get(key) == null) {
				form.getPortletPreferences().remove(key);
				form.getPortletPreferencesOverrides().remove(key);
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
					form.getPortletPreferencesOverrides().remove(key);
				}
			}
		}
		
		keys = new HashSet<String>();
		keys.addAll(form.getParameters().keySet());
		for (String key : keys) {
			if (!parameterNames.contains(key)) {
				form.getParameters().remove(key);
				form.getParameterOverrides().remove(key);
			} else if (form.getParameters().get(key) == null || StringUtils.isBlank(form.getParameters().get(key).getValue())) {
				form.getParameters().remove(key);
				form.getParameterOverrides().remove(key);
			}
		}
	}
	
	/**
	 * Retreive the list of portlet application contexts currently available in
	 * this portlet container.
	 * 
	 * @return list of portlet context
	 */
	@SuppressWarnings("unchecked")
	public List<PortletContextImpl> getPortletApplications() {
		final PortletRegistryService portletRegistryService = optionalContainerServices.getPortletRegistryService();
		List<PortletContextImpl> contexts = new ArrayList<PortletContextImpl>();
		for (Iterator iter = portletRegistryService.getRegisteredPortletApplications(); iter.hasNext();) {
			PortletContextImpl context = (PortletContextImpl) iter.next();
			contexts.add(context);
		}
		return contexts;
	}
	
	/**
	 * Get a portlet descriptor matching the current channel definition form.
	 * If the current form does not represent a portlet, the application or 
	 * portlet name fields are blank, or the portlet description cannot be 
	 * retrieved, the method will return <code>null</code>.
	 * 
	 * @param form
	 * @return
	 */
	public PortletDD getPortletDescriptor(ChannelDefinitionForm form) {
		final Tuple<String, String> portletDescriptorKeys = this.getPortletDescriptorKeys(form);
		if (portletDescriptorKeys == null) {
		    return null;
		}
        final String portletAppId = portletDescriptorKeys.first;
        final String portletName = portletDescriptorKeys.second;

		
		final PortletRegistryService portletRegistryService = optionalContainerServices.getPortletRegistryService();
		try {
			PortletDD portletDD = portletRegistryService.getPortletDescriptor(portletAppId, portletName);
			return portletDD;
		} catch (PortletContainerException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Pre-populate a ChannelDefinitionForm with portlet-specific information
	 * using the supplied portlet descriptor.
	 * 
	 * @param application
	 * @param portlet
	 * @param form
	 */
	public void prepopulatePortlet(String application, String portlet, ChannelDefinitionForm form) {
		final PortletRegistryService portletRegistryService = optionalContainerServices.getPortletRegistryService();
		final PortletDD portletDD;
		try {
		    portletDD = portletRegistryService.getPortletDescriptor(application, portlet);
        }
		catch (PortletContainerException e) {
		    this.logger.warn("Failed to load portlet descriptor for appId='" + application + "', portletName='" + portlet + "'", e);
            return;
        }
		    
	    form.setTitle(portletDD.getPortletName());
		form.setName(portletDD.getPortletName());
		form.getParameters().put(IPortletAdaptor.CHANNEL_PARAM__PORTLET_APPLICATION_ID, new Attribute(application));
		form.getParameters().put(IPortletAdaptor.CHANNEL_PARAM__PORTLET_NAME, new Attribute(portletDD.getPortletName()));
		for (Object obj : portletDD.getSupports()) {
			SupportsDD supports = (SupportsDD) obj;
			for (Object mode : supports.getPortletModes()) {
				if ("edit".equals(mode)) {
					form.setEditable(true);
				} else if ("help".equals(mode)) {
					form.setHasHelp(true);
				}
			}
		}
	}
	
	public ChannelLifecycleState[] getLifecycleStates() {
		return ChannelLifecycleState.values();
	}

	public Set<ChannelLifecycleState> getAllowedLifecycleStates(IPerson person, List<JsonEntityBean> categories) {
		Set<ChannelLifecycleState> states = new TreeSet<ChannelLifecycleState>();
		if (hasLifecyclePermission(person, ChannelLifecycleState.EXPIRED, categories)) {
			states.add(ChannelLifecycleState.CREATED);
			states.add(ChannelLifecycleState.APPROVED);
			states.add(ChannelLifecycleState.EXPIRED);
			states.add(ChannelLifecycleState.PUBLISHED);
		} else if (hasLifecyclePermission(person, ChannelLifecycleState.PUBLISHED, categories)) {
			states.add(ChannelLifecycleState.CREATED);
			states.add(ChannelLifecycleState.APPROVED);
			states.add(ChannelLifecycleState.PUBLISHED);
		} else if (hasLifecyclePermission(person, ChannelLifecycleState.APPROVED, categories)) {
			states.add(ChannelLifecycleState.CREATED);
			states.add(ChannelLifecycleState.APPROVED);
		} else if (hasLifecyclePermission(person, ChannelLifecycleState.CREATED, categories)) {
			states.add(ChannelLifecycleState.CREATED);
		}
		return states;
	}
	
	public boolean hasLifecyclePermission(IPerson person, ChannelLifecycleState state, List<JsonEntityBean> categories) {
		EntityIdentifier ei = person.getEntityIdentifier();
	    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
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
	    final PortletUrl renderUrl = delegationResponse.getRenderUrl();
	    final DelegateState delegateState = delegationResponse.getDelegateState();
        if (renderUrl == null || 
	            (renderUrl.getPortletMode() != null && !IPortletAdaptor.CONFIG.equals(renderUrl.getPortletMode())) ||
	            !IPortletAdaptor.CONFIG.equals(delegateState.getPortletMode())) {
	        
	        //The portlet sent a redirect OR changed it's mode away from CONFIG, assume it is done
	        return true;
	    }
	    
	    return false;
	}
	
	public boolean offerPortletSelection(ChannelDefinitionForm form) {
		if (!CSpringPortletAdaptor.class.getName().equals(form.getJavaClass())) {
			return false;
		}
		
		Map<String, Attribute> parameters = form.getParameters();
		if (parameters.get("portletName") != null
				&& !StringUtils.isBlank(parameters.get("portletName").getValue())){
			return false;
		}
		
		return true;
	}
	
	protected Tuple<String, String> getPortletDescriptorKeys(ChannelDefinitionForm form) {
	    final Map<String, Attribute> parameters = form.getParameters();
	    
	    final Attribute frameworkPortletAttribute = parameters.get(IPortletAdaptor.CHANNEL_PARAM__IS_FRAMEWORK_PORTLET);
        final Attribute portletAppIdAttribute = parameters.get(IPortletAdaptor.CHANNEL_PARAM__PORTLET_APPLICATION_ID);
        final Attribute portletNameAttribute = parameters.get(IPortletAdaptor.CHANNEL_PARAM__PORTLET_NAME);
        if ((portletAppIdAttribute == null && frameworkPortletAttribute == null) || portletNameAttribute == null) {
            return null;
        }
        
        final String portletAppId;
        if (frameworkPortletAttribute != null && Boolean.valueOf(frameworkPortletAttribute.getValue())) {
            portletAppId = this.servletContext.getContextPath();
        }
        else {
            portletAppId = portletAppIdAttribute.getValue();
        }
        
        final String portletName = portletNameAttribute.getValue();
        
        return new Tuple<String, String>(portletAppId, portletName);
	}
}
