package org.jasig.portal.layout.dlm.remoting;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jasig.portal.ChannelCategory;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelParameter;
import org.jasig.portal.security.AdminEvaluator;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>A Spring controller that returns a JSON or XML view of channels.  For
 * non-admins, this will only display the channels the user is allowed to
 * manage or subscribe to.  Admins have a choice of viewing manageable,
 * subscribable, or all channels by the "type" request parameter.</p>
 * <p>Request parameters:</p>
 * <ul>
 *   <li>xml: if "true", return an XML view of the channels rather than a
 *   JSON view</li>
 *   <li>type: "subscribe", "manage", or "all".  Displays subscribable,
 *   manageable, or all channels (admin only).  Default is subscribable.
 * </ul>
 *
 * @author Drew Mazurek
 */
public class ChannelListController extends AbstractController {

	private IChannelRegistryStore channelRegistryStore;
	private IPersonManager personManager;
	private static final String TYPE_SUBSCRIBE = "subscribe";
	private static final String TYPE_MANAGE = "manage";
	private static final String TYPE_ALL = "all";

	public ModelAndView handleRequestInternal(HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
		ChannelCategory rootCategory = channelRegistryStore.getTopLevelChannelCategory();
		String type = request.getParameter("type");
		
		if(type == null || (!type.equals(TYPE_MANAGE) && !type.equals(TYPE_ALL))) {
			type = TYPE_SUBSCRIBE;
		}
		
		IPerson user = personManager.getPerson(request);
		if(type.equals(TYPE_ALL) && !AdminEvaluator.isAdmin(user)) {
			/* Don't let non-admins list all channels. */
			type = TYPE_SUBSCRIBE;
		}

		if("true".equals(request.getParameter("xml"))) {
			return buildXmlModelAndView(rootCategory, user, type);
		} else {
			return buildJsonModelAndView(rootCategory, user, type);
		}
	}
	
	private ModelAndView buildXmlModelAndView(ChannelCategory rootCategory, IPerson user, String type) {
		
		Element root = DocumentHelper.createElement("registry");
		
		final Element registryDocument = buildRegistryDocument(root, rootCategory, user, type);
        Document document = DocumentHelper.createDocument(registryDocument);
		
		return new ModelAndView("xmlView","xml",document);
	}
	
	private Element buildRegistryDocument(Element element, ChannelCategory category, IPerson user, String type) {
		
		IChannelDefinition[] channels;
		
		if(type.equals(TYPE_ALL)) {
			channels = channelRegistryStore.getChildChannels(category);
		} else if(type.equals(TYPE_MANAGE)) {
			channels = channelRegistryStore.getManageableChildChannels(category, user);
		} else {
			channels = channelRegistryStore.getChildChannels(category, user);
		}
		
		for(IChannelDefinition channelDef : channels) {
			
			Element channel = DocumentHelper.createElement("channel");
			channel.addAttribute("ID","chan" + Integer.toString(channelDef.getId()));
			if(channelDef.getEntityIdentifier() != null)
				channel.addAttribute("chanID",channelDef.getEntityIdentifier().getKey());
			if(channelDef.getJavaClass() != null)
				channel.addAttribute("class",channelDef.getJavaClass());
			channel.addAttribute("description",channelDef.getDescription());
			if(channelDef.getDescription() != null)
				channel.addAttribute("editable",Boolean.toString(channelDef.isEditable()));
			if(channelDef.getFName() != null)
				channel.addAttribute("fname",channelDef.getFName());
			channel.addAttribute("hasAbout",Boolean.toString(channelDef.hasAbout()));
			channel.addAttribute("hasHelp",Boolean.toString(channelDef.hasHelp()));
			channel.addAttribute("isPortlet",Boolean.toString(channelDef.isPortlet()));
			if(channelDef.getLocale() != null)
				channel.addAttribute("locale",channelDef.getLocale());
			channel.addAttribute("name",channelDef.getName());
			channel.addAttribute("secure",Boolean.toString(channelDef.isSecure()));
			channel.addAttribute("timeout",Integer.toString(channelDef.getTimeout()));
			if(channelDef.getTitle() != null)
				channel.addAttribute("title",channelDef.getTitle());
			channel.addAttribute("typeID",Integer.toString(channelDef.getType().getId()));
		
			for(IChannelParameter param : channelDef.getParameters()) {
				Element childParam = DocumentHelper.createElement("parameter");
				childParam.addAttribute("name",param.getName());
				childParam.addAttribute("override",param.getOverride() ? "yes" : "no");
				childParam.addAttribute("value",param.getValue());
				if(param.getDescription() != null && !param.getDescription().trim().equals(""))
					childParam.addAttribute("description",param.getDescription());
				channel.add(childParam);
			}
			
			element.add(channel);
		}

		/* Now add children categories. */
		for(ChannelCategory childCategory : channelRegistryStore.getChildCategories(category)) {

			Element newCategory = DocumentHelper.createElement("category");
			newCategory.addAttribute("ID","cat" + childCategory.getId());
			newCategory.addAttribute("description",childCategory.getDescription());
			newCategory.addAttribute("name",childCategory.getName());
			/* Populate new category's children. */
			buildRegistryDocument(newCategory, childCategory, user, type);
			element.add(newCategory);
		}

		return element;
	}
	
	private ModelAndView buildJsonModelAndView(ChannelCategory rootCategory, IPerson user, String type) {
		
		JsonEntityBean root = new JsonEntityBean(rootCategory);
		
		root = buildJsonTree(root, rootCategory, user, type);
		
		return new ModelAndView("jsonView", "registry", root);
	}
	
	private JsonEntityBean buildJsonTree(JsonEntityBean bean,
			ChannelCategory category, IPerson user, String type) {

		IChannelDefinition[] channels;
		
		if(type.equals(TYPE_ALL)) {
			channels = channelRegistryStore.getChildChannels(category);
		} else if(type.equals(TYPE_MANAGE)) {
			channels = channelRegistryStore.getManageableChildChannels(category, user);
		} else {
			channels = channelRegistryStore.getChildChannels(category, user);
		}
		
		for(IChannelDefinition channelDef : channels) {
		
			bean.addChild(channelDef);
		}
		
		for(ChannelCategory childCategory : channelRegistryStore.getChildCategories(category)) {

			JsonEntityBean childBean = new JsonEntityBean(childCategory);
			buildJsonTree(childBean, childCategory, user, type);
			bean.addChild(childBean);
		}
			
		return bean;
	}
	

	public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
		this.channelRegistryStore = channelRegistryStore;
	}

	/**
	 * <p>For injection of the person manager.  Used for authorization.</p>
	 * @param personManager IPersonManager instance
	 */
	public void setPersonManager(IPersonManager personManager) {
		this.personManager = personManager;
	}
}
