package org.jasig.portal.portlets.portletadmin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryManager;
import org.jasig.portal.ChannelType;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.portlets.portletadmin.xmlsupport.ChannelPublishingDefinition;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.utils.ResourceLoader;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.thoughtworks.xstream.XStream;

public class PortletAdministrationService {

	private Log log = LogFactory.getLog(PortletAdministrationService.class);
	
	public ChannelDefinitionForm getChannelDefinitionForm(String chanId) {
		
		ChannelDefinition def = new ChannelDefinition(0);
		Element el = ChannelRegistryManager.getChannel(chanId);
		ChannelRegistryManager.setChannelXML(el, def);
		
		// create the new form
		ChannelDefinitionForm form = new ChannelDefinitionForm(def);
		form.setId(Integer.parseInt(chanId.replace("chan", "")));
		
		// if this is not a new channel, set the category and permissions
        if (form.getId() > 0) {
			try {
				IGroupMember entity = GroupService.getEntity(
						String.valueOf(form.getId()), Class
								.forName(GroupService.CHANNEL_CATEGORIES));
				Iterator groupIter = entity.getContainingGroups();
				while (groupIter.hasNext()) {
					IEntityGroup parent = (IEntityGroup) groupIter.next();
					form.addCategory(parent.getKey());
				}
			} catch (GroupsException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			try {
                IPermissionManager pm = AuthorizationService.instance().newPermissionManager("UP_FRAMEWORK");
                IAuthorizationPrincipal[] prins = pm.getAuthorizedPrincipals("SUBSCRIBE",
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
		IGroupMember[] groups = new IGroupMember[form.getGroups().size()];
		for (int i = 0; i < groups.length; i++) {
			groups[i] = GroupService.getGroupMember(form.getGroups().get(i),
					IEntityGroup.class);
		}

		// create the category array from the form's category list
		String[] categories = form.getCategories().toArray(new String[] {});

		// attempt to save the channel
		ChannelRegistryManager.publishChannel(form.toXml(), categories,
				groups, publisher);
	}
	
	/**
	 * Return a list of all currently registered channel types.
	 * 
	 * @return
	 */
	public List<ChannelType> getRegisteredChannelTypes() {
		List<ChannelType> chanTypes = new ArrayList<ChannelType>();
		
		// add the custom channel type
		ChannelType custom = new ChannelType(-1);
		custom.setName("Custom");
		chanTypes.add(custom);
		
		// get channel types from the ChannelRegistryManager
		Element el = ChannelRegistryManager.getChannelTypes().getDocumentElement();
		NodeList types = el.getElementsByTagName("channelType");
		
		// for each registered channel type, get it's details from the XML and
		// add it to our list
		for (int i = 0; i < types.getLength(); i++) {
			Element type = (Element) types.item(i);
			int typeId = Integer.valueOf(type.getAttribute("ID"));
			ChannelType chanType = new ChannelType(typeId);
			chanType.setName(type.getElementsByTagName("name").item(0).getTextContent());
			chanType.setDescription(type.getElementsByTagName("description").item(0).getTextContent());
			chanType.setJavaClass(type.getElementsByTagName("class").item(0).getTextContent());
			chanType.setCpdUri(type.getElementsByTagName("cpd-uri").item(0).getTextContent());
			chanTypes.add(chanType);
		}
		
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
		String cpd = null;
		Element el = ChannelRegistryManager.getChannelTypes().getDocumentElement();
		NodeList types = el.getElementsByTagName("channelType");
		List<ChannelType> chanTypes = new ArrayList<ChannelType>();
		if (channelTypeId < 0) {
			cpd = "org/jasig/portal/portlets/portletadmin/CustomChannel.cpd";
		} else {
			for (int i = 0; i < types.getLength(); i++) {
				Element type = (Element) types.item(i);
				int typeId = Integer.valueOf(type.getAttribute("ID"));
				if (typeId == channelTypeId) {
					cpd = type.getElementsByTagName("cpd-uri").item(0).getTextContent();
				}
			}
		}
		InputStream inputStream = null;
		try {
			inputStream = ResourceLoader.getResourceAsStream(PortletAdministrationService.class, cpd);
		} catch (ResourceMissingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		XStream stream = new XStream();
		stream.processAnnotations(ChannelPublishingDefinition.class);
		ChannelPublishingDefinition def = (ChannelPublishingDefinition) stream.fromXML(inputStream);
		return def;
	}
	
}
