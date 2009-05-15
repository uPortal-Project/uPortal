package org.jasig.portal.portlets.portletadmin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelPublishingService;
import org.jasig.portal.channel.IChannelType;
import org.jasig.portal.channel.dao.jpa.ChannelTypeImpl;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.portlets.Attribute;
import org.jasig.portal.portlets.portletadmin.xmlsupport.ChannelPublishingDefinition;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.utils.ResourceLoader;

import com.thoughtworks.xstream.XStream;

public class PortletAdministrationHelper {

	private Log log = LogFactory.getLog(PortletAdministrationHelper.class);
	private IChannelRegistryStore channelRegistryStore;
	
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

	public ChannelDefinitionForm getChannelDefinitionForm(int chanId) {
		
		IChannelDefinition def = channelRegistryStore.getChannelDefinition(chanId);
		if (def == null) {
			def = channelRegistryStore.newChannelDefinition();
		}
		
		// create the new form
		ChannelDefinitionForm form = new ChannelDefinitionForm(def);
		form.setId(def.getId());
		
		// if this is not a new channel, set the category and permissions
        if (form.getId() > 0) {
        	ChannelCategory[] categories = channelRegistryStore.getParentCategories(def);
        	for (ChannelCategory cat : categories) {
        		form.addCategory(cat.getId());
        	}

			try {
                IPermissionManager pm = AuthorizationService.instance().newPermissionManager(IChannelPublishingService.FRAMEWORK_OWNER);
                IAuthorizationPrincipal[] prins = pm.getAuthorizedPrincipals(IChannelPublishingService.SUBSCRIBER_ACTIVITY,
                        "CHAN_ID." + String.valueOf(form.getId()));
                for (int mp = 0; mp < prins.length; mp++) {
                    form.addGroup(AuthorizationService.instance().getGroupMember(prins[mp]).getKey());
                }
			} catch (GroupsException e) {
				e.printStackTrace();
			}
		} else {
			// temporarily adding in a default group and category
			form.addGroup("local.0");
			form.addCategory("local.11");
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
			groupMembers[i] = GroupService.getGroupMember(form.getGroups().get(i),
					IEntityGroup.class);
		}

		// create the category array from the form's category list
		String[] categoryIDs = form.getCategories().toArray(new String[form.getCategories().size()]);

	    IChannelDefinition channelDef = channelRegistryStore.getChannelDefinition(form.getId());
	    if (channelDef == null) {
	    	channelDef = channelRegistryStore.newChannelDefinition();
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
	    channelDef.setTypeId(form.getTypeId());
	    
	    // add channel parameters
		for (String key : form.getParameters().keySet()) {
			String value = form.getParameters().get(key).getValue();
			if (!StringUtils.isBlank(value)) {
				boolean override = false;
				if (form.getParameterOverrides().containsKey(key)) {
					override = form.getParameterOverrides().get(key).getValue();
				}
				channelDef.addParameter(key, value, override);
			}
		}
		
		for (String key : form.getPortletPreferences().keySet()) {
			String[] values = new String[form.getPortletPreferences().get(key).size()];
			for (ListIterator<Attribute> iter = form.getPortletPreferences().get(key).listIterator(); iter.hasNext();) {
				String value = iter.next().getValue();
				values[iter.previousIndex()] = value;
			}
			boolean readOnly = true;
			if (form.getPortletPreferencesOverrides().containsKey(key)) {
				readOnly = !form.getPortletPreferencesOverrides().get(key).getValue();
			}
			// TODO: add portlet preferences
		}
	    
	    channelPublishingService.saveChannelDefinition(channelDef, publisher, categoryIDs, groupMembers);

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
	 * Return a list of all currently registered channel types.
	 * 
	 * @return
	 */
	public List<IChannelType> getRegisteredChannelTypes() {
		List<IChannelType> chanTypes = channelRegistryStore.getChannelTypes();
		
		// add the custom channel type
		ChannelTypeImpl custom = new ChannelTypeImpl(-1);
		custom.setName("Custom");
		chanTypes.add(custom);
		
		// return the list of channel types
		return chanTypes;
	}
	
	/**
	 * Return a ChannelPublishingDocument for a specified channel type id.
	 * 
	 * @param channelTypeId
	 * @return
	 */
	public ChannelPublishingDefinition getChannelType(int channelTypeId) {
		String cpdUri;
		if (channelTypeId >= 0) {
			IChannelType type = channelRegistryStore.getChannelType(channelTypeId);
			cpdUri = type.getCpdUri();
		} else {
			cpdUri = "org/jasig/portal/portlets/portletadmin/CustomChannel.cpd";
		}
		InputStream inputStream = null;
		try {
			inputStream = ResourceLoader.getResourceAsStream(PortletAdministrationHelper.class, cpdUri);
		} catch (ResourceMissingException e) {
			log.error("Failed to locate CPD for channel type " + channelTypeId, e);
		} catch (IOException e) {
			log.error("Failed to load CPD for channel type " + channelTypeId, e);
		}
		XStream stream = new XStream();
		stream.processAnnotations(ChannelPublishingDefinition.class);
		ChannelPublishingDefinition def = (ChannelPublishingDefinition) stream.fromXML(inputStream);
		return def;
	}
	
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
	
	public void prepopulatePortlet(String application, String portlet, ChannelDefinitionForm form) {
		final PortletRegistryService portletRegistryService = optionalContainerServices.getPortletRegistryService();
		try {
			PortletDD portletDD = portletRegistryService.getPortletDescriptor(application, portlet);
			form.setTitle(portletDD.getPortletName());
			form.setName(portletDD.getPortletName());
			form.setPortlet(true);
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
		} catch (PortletContainerException e) {
			e.printStackTrace();
		}
	}
		
}
