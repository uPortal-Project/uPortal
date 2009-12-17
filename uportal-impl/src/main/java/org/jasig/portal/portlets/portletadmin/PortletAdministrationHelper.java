/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets.portletadmin;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;

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
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.channel.ChannelLifecycleState;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelPublishingService;
import org.jasig.portal.channel.IChannelType;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
import org.jasig.portal.portlets.Attribute;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDParameter;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDParameterList;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDPreference;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDStep;
import org.jasig.portal.portlets.portletadmin.xmlsupport.ChannelPublishingDefinition;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.utils.ResourceLoader;

import com.thoughtworks.xstream.XStream;

/**
 * Helper methods for the portlet administration workflow.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @revision $Revision$
 */
public class PortletAdministrationHelper {
	
	private static final String CUSTOM_CPD_PATH = "org/jasig/portal/portlets/portletadmin/CustomChannel.cpd";
	private static final String SHARED_PARAMETERS_PATH = "org/jasig/portal/channels/SharedParameters.cpd";

	private Log log = LogFactory.getLog(PortletAdministrationHelper.class);
	
	private IGroupListHelper groupListHelper;

	public void setGroupListHelper(IGroupListHelper groupListHelper) {
		this.groupListHelper = groupListHelper;
	}
	
	private IChannelRegistryStore channelRegistryStore;
	
	/**
	 * Set the channel registry store
	 * 
	 * @param channelRegistryStore
	 */
	public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
		this.channelRegistryStore = channelRegistryStore;
	}

	private OptionalContainerServices optionalContainerServices;

	public void setOptionalContainerServices(
			OptionalContainerServices optionalContainerServices) {
		this.optionalContainerServices = optionalContainerServices;
	}
	
	private IChannelPublishingService channelPublishingService;	

	public void setChannelPublishingService(
			IChannelPublishingService channelPublishingService) {
		this.channelPublishingService = channelPublishingService;
	}
	
    private Map<Serializable, ChannelPublishingDefinition> cpdCache;

    /**
     * Cache to use for parsed CPDs.
     * 
     * @param cpdCache
     */
	public void setCpdCache(Map<Serializable, ChannelPublishingDefinition> cpdCache) {
		this.cpdCache = cpdCache;
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
	public void savePortletRegistration(ChannelDefinitionForm form,
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
	 * Return a ChannelPublishingDocument for a specified channel type id.
	 * 
	 * @param channelTypeId
	 * @return
	 */
	public ChannelPublishingDefinition getChannelType(int channelTypeId) {
		
		// attempt to retrieve the CPD from the cache
		if (this.cpdCache.containsKey(channelTypeId)) {
			return this.cpdCache.get(channelTypeId);
		}
		
		// if the CPD is not already in the cache, determine the CPD URI
		String cpdUri;
		if (channelTypeId >= 0) {
			IChannelType type = channelRegistryStore.getChannelType(channelTypeId);
			cpdUri = type.getCpdUri();
		} else {
			cpdUri = CUSTOM_CPD_PATH;
		}
		
		// read in the CPD
		InputStream inputStream = null;
		try {
			inputStream = ResourceLoader.getResourceAsStream(PortletAdministrationHelper.class, cpdUri);
		} catch (ResourceMissingException e) {
			log.error("Failed to locate CPD for channel type " + channelTypeId, e);
		} catch (IOException e) {
			log.error("Failed to load CPD for channel type " + channelTypeId, e);
		}
		
		// parse the CPD
		XStream stream = new XStream();
		stream.processAnnotations(ChannelPublishingDefinition.class);
		ChannelPublishingDefinition def = (ChannelPublishingDefinition) stream.fromXML(inputStream);
		
		// read in the shared CPD
		try {
			inputStream = ResourceLoader.getResourceAsStream(PortletAdministrationHelper.class, SHARED_PARAMETERS_PATH);
		} catch (ResourceMissingException e) {
			log.error("Failed to locate shared parameters CPD for channel type " + channelTypeId, e);
		} catch (IOException e) {
			log.error("Failed to load shared parameters CPD for channel type " + channelTypeId, e);
		}
		
		// parse the shared CPD and add its steps to the end of the type-specific
		// CPD
		stream = new XStream();
		stream.processAnnotations(CPDParameterList.class);
		CPDParameterList paramList = (CPDParameterList) stream.fromXML(inputStream);
		int stepId = def.getParams().getSteps().size();
		for (CPDStep step : paramList.getSteps()) {
			stepId = stepId++;
			step.setId(String.valueOf(stepId));
		}
		
		def.getParams().getSteps().addAll(paramList.getSteps());
		
		// add the CPD to the cache and return it
		this.cpdCache.put(channelTypeId, def);
		return def;
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
		ChannelPublishingDefinition cpd = getChannelType(form.getTypeId());
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
	    
	    final Map<String, Attribute> parameters = form.getParameters();
	    
	    final Attribute portletAppIdAttribute = parameters.get(IPortletAdaptor.CHANNEL_PARAM__PORTLET_APPLICATION_ID);
	    final Attribute portletNameAttribute = parameters.get(IPortletAdaptor.CHANNEL_PARAM__PORTLET_NAME);
	    if (portletAppIdAttribute == null || portletNameAttribute == null) {
	        return false;
	    }
	    
	    final String portletAppId = portletAppIdAttribute.getValue();
	    final String portletName = portletNameAttribute.getValue();
        if (portletAppId == null || portletName == null) {
            return false;
        }
	    
        final PortletRegistryService portletRegistryService = this.optionalContainerServices.getPortletRegistryService();
        final PortletDD portletDescriptor;
        try {
            portletDescriptor = portletRegistryService.getPortletDescriptor(portletAppId, portletName);
        }
        catch (PortletContainerException e) {
            this.log.warn("Failed to load portlet descriptor for appId='" + portletAppId + "', portletName='" + portletName + "'", e);
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
		if (!form.isPortlet() || !form.getParameters().containsKey("portletApplicationId") || !form.getParameters().containsKey("portletName")) {
			return null;
		}
		
		final String application = form.getParameters().get(IPortletAdaptor.CHANNEL_PARAM__PORTLET_APPLICATION_ID).getValue();
		final String portlet = form.getParameters().get(IPortletAdaptor.CHANNEL_PARAM__PORTLET_NAME).getValue();
		if (StringUtils.isBlank(application) || StringUtils.isBlank(portlet)) {
			return null;
		}
		
		final PortletRegistryService portletRegistryService = optionalContainerServices.getPortletRegistryService();
		try {
			PortletDD portletDD = portletRegistryService.getPortletDescriptor(application, portlet);
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
		    this.log.warn("Failed to load portlet descriptor for appId='" + application + "', portletName='" + portlet + "'", e);
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
		
}
