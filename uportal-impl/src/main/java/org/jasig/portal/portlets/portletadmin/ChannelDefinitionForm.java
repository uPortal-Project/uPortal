package org.jasig.portal.portlets.portletadmin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LazyMap;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelParameter;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlets.Attribute;
import org.jasig.portal.portlets.AttributeFactory;
import org.jasig.portal.portlets.BooleanAttribute;
import org.jasig.portal.portlets.BooleanAttributeFactory;
import org.jasig.portal.portlets.StringListAttribute;
import org.jasig.portal.portlets.StringListAttributeFactory;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDControl;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDParameter;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDPreference;
import org.jasig.portal.portlets.portletadmin.xmlsupport.CPDStep;
import org.jasig.portal.portlets.portletadmin.xmlsupport.ChannelPublishingDefinition;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ChannelDefinitionForm implements Serializable {

	private int id = 0;
	private String fname = "";
	private String name = "";
	private String description = "";
	private String title = "";
	private String javaClass = "";
	private boolean isPortlet = false;
	private int timeout = 500;
	private int typeId;
	private boolean editable;
	private boolean hasHelp;
	private boolean hasAbout;
	private boolean secure;
	private List<String> groups = new ArrayList<String>();
	private List<String> categories = new ArrayList<String>();
	
	@SuppressWarnings("unchecked")
	private Map<String, Attribute> parameters = LazyMap.decorate(
			new HashMap<String, Attribute>(), new AttributeFactory());

	@SuppressWarnings("unchecked")
	private Map<String, BooleanAttribute> parameterOverrides = LazyMap.decorate(
			new HashMap<String, BooleanAttribute>(), new BooleanAttributeFactory());
	
	@SuppressWarnings("unchecked")
	private Map<String, StringListAttribute> portletPreferences = LazyMap.decorate(
			new HashMap<String, StringListAttribute>(), new StringListAttributeFactory());

	@SuppressWarnings("unchecked")
	private Map<String, BooleanAttribute> portletParameterOverrides = LazyMap.decorate(
			new HashMap<String, BooleanAttribute>(), new BooleanAttributeFactory());
	
	/**
	 * Default constructor
	 */
	public ChannelDefinitionForm() {
	}

	/**
	 * Construct a new ChannelDefinitionForm from a ChannelDefinition
	 * 
	 * @param def
	 */
	public ChannelDefinitionForm(IChannelDefinition def) {
		this.setId(def.getId());
		this.setFname(def.getFName());
		this.setName(def.getName());
		this.setDescription(def.getDescription());
		this.setTitle(def.getTitle());
		this.setJavaClass(def.getJavaClass());
		this.setPortlet(def.isPortlet());
		this.setTimeout(def.getTimeout());
		this.setTypeId(def.getTypeId());
		this.setEditable(def.isEditable());
		this.setHasHelp(def.hasHelp());
		this.setHasAbout(def.hasAbout());
		this.setSecure(def.isSecure());
		
		for (IChannelParameter param : def.getParameters()) {
			this.parameters.put(param.getName(),
					new Attribute(param.getValue()));
			this.parameterOverrides.put(param.getName(), 
					new BooleanAttribute(param.getOverride()));
		}
		
		if (def.isPortlet()) {
			for (IPortletPreference pref : def.getPortletPreferences()) {
				List<Attribute> attributes = new ArrayList<Attribute>();
				for (String value : pref.getValues()) {
					attributes.add(new Attribute(value));
				}
				this.portletPreferences.put(pref.getName(), new StringListAttribute(pref.getValues()));
				this.portletParameterOverrides.put(pref.getName(), new BooleanAttribute(!pref.isReadOnly()));
			}
		}
	}
	
	/**
	 * Generate a DOM element of the type ChannelRegistryManager expects. 
	 * 
	 * @return
	 */
	public Element toXml() {
	    Document doc = DocumentFactory.getNewDocument();
	    Element channelE = doc.createElement("channel");
	    if (this.id > 0) {
		    channelE.setAttribute("ID", String.valueOf(this.id));
	    }
	    channelE.setAttribute("timeout", String.valueOf(this.timeout));
        channelE.setAttribute("name", this.name);
        channelE.setAttribute("title", this.title);
	    channelE.setAttribute("fname", this.fname);
	    channelE.setAttribute("description", this.description);
	    channelE.setAttribute("class", this.javaClass);
	    channelE.setAttribute("typeID", String.valueOf(this.typeId));
	    channelE.setAttribute("editable", this.editable ? "true" : "false");
	    channelE.setAttribute("hasHelp", this.hasHelp ? "true" : "false");
	    channelE.setAttribute("hasAbout", this.hasAbout ? "true" : "false");
	    channelE.setAttribute("secure", this.isSecure() ? "true" : "false");
	    channelE.setAttribute("isPortlet", this.isPortlet ? "true" : "false");
	    
	    // Add any parameters
		for (String key : this.parameters.keySet()) {
			String value = this.parameters.get(key).getValue();
			if (value != null && value.trim().length() > 0) {
				Element parameterE = doc.createElement("parameter");
				parameterE.setAttribute("name", key);
				parameterE.setAttribute("value", this.parameters.get(key)
						.getValue());
				if (this.parameterOverrides.containsKey(key)
						&& this.parameterOverrides.get(key).getValue()) {
					parameterE.setAttribute("override", "yes");
				}
				channelE.appendChild(parameterE);
			}
		}

	    return channelE;

	}
	
	/**
	 * Sets the Java class name and parameter defaults based on the 
	 * ChannelPublishingDefinition.
	 * 
	 * @param cpd
	 */
	public void setChannelPublishingDefinition(ChannelPublishingDefinition cpd) {

		// set the Java class name
		this.javaClass = cpd.getClassName();

		// set channel control defaults
		for (CPDControl control : cpd.getControls()
				.getControls()) {
			if (control.getInclude().equals("true")) {
				if (control.getType().equals("help")) {
					this.hasHelp = true;
				} else if (control.getType().equals("about")) {
					this.hasAbout = true;
				} else if (control.getType().equals("edit")) {
					this.editable = true;
				}
			}
		}

		// set default values for all channel parameters
		for (CPDStep step : cpd.getParams().getSteps()) {
			if (step.getParameters() != null) {
				for (CPDParameter param : step.getParameters()) {
					// if this parameter doesn't currently have a value, check
					// for a default in the CPD
					if (!this.parameters.containsKey(param.getName())
							|| this.parameters.get(param.getName()).getValue().trim().equals("")) {
						
						// use the default value if one exists
						if (param.getDefaultValue() != null) {
							this.parameters.put(param.getName(), new Attribute(param
									.getDefaultValue()));
						}
							
						// otherwise look for a default in the type restriction	
						else if (param.getType() != null
								&& param.getType().getRestriction() != null
								&& param.getType().getRestriction().getDefaultValue() != null) {
							this.parameters.put(param.getName(), new Attribute(param
									.getType().getRestriction().getDefaultValue()));
						}
						
						// set parameter override value
						if (param.getModify().equals("subscribe")) {
							this.parameterOverrides.put(param.getName(), 
									new BooleanAttribute(true));
						} else {
							this.parameterOverrides.put(param.getName(), 
									new BooleanAttribute(false));
						}
					}
				}
			}
			if (step.getPreferences() != null) {
				for (CPDPreference pref : step.getPreferences()) {
					// if this parameter doesn't currently have a value, check
					// for a default in the CPD
					if (!this.portletPreferences.containsKey(pref.getName())
							|| this.portletPreferences.get(pref.getName()).getValue().size() == 0
							|| (this.portletPreferences.get(pref.getName()).getValue().size() == 1 && this.portletPreferences.get(pref.getName()).getValue().get(0).trim().equals(""))) {
						
						if (!this.portletPreferences.containsKey(pref.getName())) {
							this.portletPreferences.put(pref.getName(), new StringListAttribute());
						}
						
						// use the default value if one exists
						if (pref.getDefaultValues().size() > 0) {
							for (String value : pref.getDefaultValues()) {
								this.portletPreferences.get(pref.getName()).getValue().add(value);
							}
						}
							
						// otherwise look for a default in the type restriction	
						else if (pref.getType() != null
								&& pref.getType().getRestriction() != null
								&& pref.getType().getRestriction().getDefaultValues().size() > 0) {
							for (String value : pref.getType().getRestriction().getDefaultValues()) {
								this.portletPreferences.get(pref.getName()).getValue().add(value);
							}
						}
						
						// set parameter override value
						if (pref.getModify().equals("subscribe")) {
							this.parameterOverrides.put(pref.getName(), 
									new BooleanAttribute(true));
						} else {
							this.parameterOverrides.put(pref.getName(), 
									new BooleanAttribute(false));
						}
					}
				}
			}
		}

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String name) {
		fname = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getJavaClass() {
		return javaClass;
	}

	public void setJavaClass(String javaClass) {
		this.javaClass = javaClass;
	}

	public boolean isPortlet() {
		return isPortlet;
	}

	public void setPortlet(boolean isPortlet) {
		this.isPortlet = isPortlet;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean isHasHelp() {
		return hasHelp;
	}

	public void setHasHelp(boolean hasHelp) {
		this.hasHelp = hasHelp;
	}

	public boolean isHasAbout() {
		return hasAbout;
	}

	public void setHasAbout(boolean hasAbout) {
		this.hasAbout = hasAbout;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public Map<String, Attribute> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Attribute> parameters) {
		this.parameters = parameters;
	}
	
	public Map<String, BooleanAttribute> getParameterOverrides() {
		return parameterOverrides;
	}

	public void setParameterOverrides(Map<String, BooleanAttribute> parameterOverrides) {
		this.parameterOverrides = parameterOverrides;
	}

	public Map<String, StringListAttribute> getPortletPreferences() {
		return this.portletPreferences;
	}

	public void setPortletPreferences(Map<String, StringListAttribute> portletParameters) {
		this.portletPreferences = portletParameters;
	}

	public Map<String, BooleanAttribute> getPortletPreferencesOverrides() {
		return this.portletParameterOverrides;
	}

	public void setPortletPreferencesOverrides(
			Map<String, BooleanAttribute> portletParameterOverrides) {
		this.portletParameterOverrides = portletParameterOverrides;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}
	
	public void addGroup(String group) {
		this.groups.add(group);
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	
	public void addCategory(String category) {
		this.categories.add(category);
	}

}
