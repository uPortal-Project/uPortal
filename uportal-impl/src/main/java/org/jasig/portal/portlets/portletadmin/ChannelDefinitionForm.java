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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.channel.ChannelLifecycleState;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelParameter;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
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

public class ChannelDefinitionForm implements Serializable {
	
	private static final long serialVersionUID = 892741367149099647L;
	private static Log log = LogFactory.getLog(ChannelDefinitionForm.class);
	
	/**
	 * Main channel fields
	 */
	
	private int id = -1;
	private String fname = "";
	private String name = "";
	private String description = "";
	private String title = "";
	private String javaClass = "";
	private int timeout = 5000;
	private int typeId;
	
	/**
	 * Lifecycle information
	 */
	
	private ChannelLifecycleState lifecycleState = ChannelLifecycleState.CREATED;
	private Date publishDate;
	private int publishHour = 12;
	private int publishMinute = 0;
	private int publishAmPm = 0;
	private Date expirationDate;
	private int expirationHour = 12;
	private int expirationMinute = 0;
	private int expirationAmPm = 0;
	
	/**
	 * Channel controls
	 */
	
	private boolean editable;
	private boolean hasHelp;
	private boolean hasAbout;
	private boolean secure;
	
	/**
	 * Groups and categories
	 */
	
	private List<JsonEntityBean> groups = new ArrayList<JsonEntityBean>();
	private List<JsonEntityBean> categories = new ArrayList<JsonEntityBean>();

	
	/**
	 * Parameters and preferences
	 */
	
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
	public ChannelDefinitionForm(IChannelDefinition def, IPortletDefinition portletDef) {
		this.setId(def.getId());
		this.setFname(def.getFName());
		this.setName(def.getName());
		this.setDescription(def.getDescription());
		this.setTitle(def.getTitle());
		this.setJavaClass(def.getJavaClass());
		this.setTimeout(def.getTimeout());
		this.setTypeId(def.getType().getId());
		this.setEditable(def.isEditable());
		this.setHasHelp(def.hasHelp());
		this.setHasAbout(def.hasAbout());
		this.setSecure(def.isSecure());		
		this.setLifecycleState(def.getLifecycleState());
		
		int order = this.getLifecycleState().getOrder();
		if (order < ChannelLifecycleState.PUBLISHED.getOrder()) {
			this.setPublishDateTime(def.getPublishDate());
		}
		
		if (order < ChannelLifecycleState.EXPIRED.getOrder()) {
			this.setExpirationDateTime(def.getExpirationDate());
		}
		
		for (IChannelParameter param : def.getParameters()) {
			if (def.isPortlet() && param.getName().startsWith("PORTLET.")) {
				this.portletPreferences.put(param.getName(),
						new StringListAttribute(new String[]{ param.getValue() }));
				this.portletParameterOverrides.put(param.getName(), 
						new BooleanAttribute(param.getOverride()));
			} else {
				this.parameters.put(param.getName(),
						new Attribute(param.getValue()));
				this.parameterOverrides.put(param.getName(), 
						new BooleanAttribute(param.getOverride()));
			}
		}
		
		if (def.isPortlet()) {
            final IPortletPreferences prefs = portletDef.getPortletPreferences();
            for (IPortletPreference pref : prefs.getPortletPreferences()) {
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
						if (pref.getDefaultValues() != null && pref.getDefaultValues().size() > 0) {
							for (String value : pref.getDefaultValues()) {
								this.portletPreferences.get(pref.getName()).getValue().add(value);
							}
						}
							
						// otherwise look for a default in the type restriction	
						else if (pref.getType() != null
								&& pref.getType().getRestriction() != null
								&& pref.getType().getRestriction().getDefaultValues() != null
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
		if (!StringUtils.isBlank(this.javaClass)) {
			try {
				final Class<?> channelClazz = Class.forName(this.javaClass);
				return IPortletAdaptor.class.isAssignableFrom(channelClazz);
			} catch (ClassNotFoundException e) {
				log.info("Failed to find class " + this.javaClass + " for portlet");
			}
		}
		return false;
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

	public void setLifecycleState(ChannelLifecycleState lifecycleState) {
		this.lifecycleState = lifecycleState;
	}

	public void setLifecycleState(String lifecycleState) {
		for (ChannelLifecycleState state : ChannelLifecycleState.values()) {
			if (state.toString().equals(lifecycleState)) {
				this.lifecycleState = state;
				break;
			}
		}
	}

	public ChannelLifecycleState getLifecycleState() {
		return lifecycleState;
	}

	public Date getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}
	
	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
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

	public List<JsonEntityBean> getGroups() {
		return groups;
	}

	public void setGroups(List<JsonEntityBean> groups) {
		this.groups = groups;
	}
	
	public void addGroup(JsonEntityBean group) {
		this.groups.add(group);
	}

	public List<JsonEntityBean> getCategories() {
		return categories;
	}

	public void setCategories(List<JsonEntityBean> categories) {
		this.categories = categories;
	}
	
	public void addCategory(JsonEntityBean category) {
		this.categories.add(category);
	}

	public int getPublishHour() {
		return this.publishHour;
	}

	public void setPublishHour(int publishHour) {
		this.publishHour = publishHour;
	}

	public int getPublishMinute() {
		return this.publishMinute;
	}

	public void setPublishMinute(int publishMinute) {
		this.publishMinute = publishMinute;
	}

	public int getPublishAmPm() {
		return this.publishAmPm;
	}

	public void setPublishAmPm(int publishAmPm) {
		this.publishAmPm = publishAmPm;
	}

	public int getExpirationHour() {
		return this.expirationHour;
	}

	public void setExpirationHour(int expirationHour) {
		this.expirationHour = expirationHour;
	}

	public int getExpirationMinute() {
		return this.expirationMinute;
	}

	public void setExpirationMinute(int expirationMinute) {
		this.expirationMinute = expirationMinute;
	}

	public int getExpirationAmPm() {
		return this.expirationAmPm;
	}

	public void setExpirationAmPm(int expirationAmPm) {
		this.expirationAmPm = expirationAmPm;
	}
	
	/**
	 * Return the full date and time at which this channel shoudl be automatically
	 * published.  This value is built from the individual date/time fields.
	 * 
	 * @return
	 */
	public Date getPublishDateTime() {
		if (this.getPublishDate() == null) {
			return null;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(this.getPublishDate());
		cal.set(Calendar.HOUR, this.getPublishHour());
		cal.set(Calendar.MINUTE, this.getPublishMinute());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.AM_PM, this.getPublishAmPm());
		return cal.getTime();
	}

	/**
	 * Return the full date and time at which this channel shoudl be automatically
	 * expired.  This value is built from the individual date/time fields.
	 * 
	 * @return
	 */
	public Date getExpirationDateTime() {
		if (this.getExpirationDate() == null) {
			return null;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(this.getExpirationDate());
		cal.set(Calendar.HOUR, this.getExpirationHour());
		cal.set(Calendar.MINUTE, this.getExpirationMinute());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.AM_PM, this.getExpirationAmPm());
		return cal.getTime();
	}

	public void setPublishDateTime(Date publish) {
		if (publish != null) {
			this.setPublishDate(publish);
			Calendar cal = Calendar.getInstance();
			cal.setTime(publish);
			if (cal.get(Calendar.HOUR) == 0) {
				this.setPublishHour(12);
			} else {
				this.setPublishHour(cal.get(Calendar.HOUR));
			}
			this.setPublishMinute(cal.get(Calendar.MINUTE));
			this.setPublishAmPm(cal.get(Calendar.AM_PM));
		}
	}
	
	public void setExpirationDateTime(Date expire) {
		if (expire != null) {
			this.setExpirationDate(expire);
			Calendar cal = Calendar.getInstance();
			cal.setTime(expire);
			if (cal.get(Calendar.HOUR) == 0) {
				this.setExpirationHour(12);
			} else {
				this.setExpirationHour(cal.get(Calendar.HOUR));
			}
			this.setExpirationMinute(cal.get(Calendar.MINUTE));
			this.setExpirationAmPm(cal.get(Calendar.AM_PM));
		}
	}
	
}
