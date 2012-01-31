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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.PortletLifecycleState;
import org.jasig.portal.portletpublishing.xml.MultiValuedPreferenceInputType;
import org.jasig.portal.portletpublishing.xml.Parameter;
import org.jasig.portal.portletpublishing.xml.ParameterInputType;
import org.jasig.portal.portletpublishing.xml.PortletPublishingDefinition;
import org.jasig.portal.portletpublishing.xml.Preference;
import org.jasig.portal.portletpublishing.xml.PreferenceInputType;
import org.jasig.portal.portletpublishing.xml.SingleValuedPreferenceInputType;
import org.jasig.portal.portletpublishing.xml.Step;
import org.jasig.portal.portlets.Attribute;
import org.jasig.portal.portlets.AttributeFactory;
import org.jasig.portal.portlets.BooleanAttribute;
import org.jasig.portal.portlets.BooleanAttributeFactory;
import org.jasig.portal.portlets.StringListAttribute;
import org.jasig.portal.portlets.StringListAttributeFactory;

public class PortletDefinitionForm implements Serializable {
	
	private static final long serialVersionUID = 892741367149099647L;
	protected transient final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Main portlet fields
	 */
	
	private String id = null;
	private String fname = "";
	private String name = "";
	private String description = "";
	private String title = "";
	private String applicationId = "";
	private String portletName = "";
	private boolean framework = false;
	private int timeout = 5000;
	private int typeId;
	
	/**
	 * Lifecycle information
	 */
	
	private PortletLifecycleState lifecycleState = PortletLifecycleState.CREATED;
	private Date publishDate;
	private int publishHour = 12;
	private int publishMinute = 0;
	private int publishAmPm = 0;
	private Date expirationDate;
	private int expirationHour = 12;
	private int expirationMinute = 0;
	private int expirationAmPm = 0;
	
	/**
	 * Portlet controls
	 */
	
	private boolean editable;
	private boolean hasHelp;
	private boolean hasAbout;
	
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
	private Map<String, StringListAttribute> portletPreferences = LazyMap.decorate(
			new HashMap<String, StringListAttribute>(), new StringListAttributeFactory());

    @SuppressWarnings("unchecked")
    private Map<String, BooleanAttribute> portletPreferenceReadOnly = LazyMap
            .decorate(new HashMap<String, BooleanAttribute>(), new BooleanAttributeFactory());

	/**
	 * Default constructor
	 */
	public PortletDefinitionForm() {
	}

	/**
	 * Construct a new PortletDefinitionForm from a PortletDefinition
	 * 
	 * @param def
	 */
	public PortletDefinitionForm(IPortletDefinition def) {
		this.setId(def.getPortletDefinitionId().getStringId());
		this.setFname(def.getFName());
		this.setName(def.getName());
		this.setDescription(def.getDescription());
		this.setTitle(def.getTitle());
		this.setTimeout(def.getTimeout());
		this.setTypeId(def.getType().getId());
		this.setApplicationId(def.getPortletDescriptorKey().getWebAppName());
		this.setPortletName(def.getPortletDescriptorKey().getPortletName());
		this.setFramework(def.getPortletDescriptorKey().isFrameworkPortlet());
		if (def.getParameter(IPortletDefinition.EDITABLE_PARAM) != null) {
		    this.setEditable(Boolean.parseBoolean(def.getParameter(IPortletDefinition.EDITABLE_PARAM).getValue()));
		}
        if (def.getParameter(IPortletDefinition.HAS_HELP_PARAM) != null) {
            this.setHasHelp(Boolean.parseBoolean(def.getParameter(IPortletDefinition.HAS_HELP_PARAM).getValue()));
        }
        if (def.getParameter(IPortletDefinition.HAS_ABOUT_PARAM) != null) {
    		this.setHasAbout(Boolean.parseBoolean(def.getParameter(IPortletDefinition.HAS_ABOUT_PARAM).getValue()));
    	}
		this.setLifecycleState(def.getLifecycleState());
		
		int order = this.getLifecycleState().getOrder();
		if (order < PortletLifecycleState.PUBLISHED.getOrder()) {
			this.setPublishDateTime(def.getPublishDate());
		}
		
		if (order < PortletLifecycleState.EXPIRED.getOrder()) {
			this.setExpirationDateTime(def.getExpirationDate());
		}
		
		for (IPortletDefinitionParameter param : def.getParameters()) {
			if (param.getName().startsWith("PORTLET.")) {
				this.portletPreferences.put(param.getName(),
						new StringListAttribute(new String[]{ param.getValue() }));
			} else {
				this.parameters.put(param.getName(),
						new Attribute(param.getValue()));
			}
		}
		
        for (IPortletPreference pref : def.getPortletPreferences()) {
			List<Attribute> attributes = new ArrayList<Attribute>();
			for (String value : pref.getValues()) {
				attributes.add(new Attribute(value));
			}
			this.portletPreferences.put(pref.getName(), new StringListAttribute(pref.getValues()));
            this.portletPreferenceReadOnly.put(pref.getName(), new BooleanAttribute(!pref.isReadOnly()));
		}
            
	}
	
	/**
	 * Sets the Java class name and parameter defaults based on the 
	 * PortletPublishingDefinition.
	 * 
	 * @param cpd
	 */
	public void setChannelPublishingDefinition(PortletPublishingDefinition cpd) {

		// set default values for all portlet parameters
		for (Step step : cpd.getSteps()) {
			if (step.getParameters() != null) {
				for (Parameter param : step.getParameters()) {
					// if this parameter doesn't currently have a value, check
					// for a default in the CPD
					Attribute attribute = this.parameters.get(param.getName());
					if (attribute == null
							|| attribute.getValue() == null
							|| attribute.getValue().trim().equals("")) {
						
						// use the default value if one exists
					    ParameterInputType input = param.getParameterInput().getValue();
						if (input != null) {
							this.parameters.put(param.getName(), new Attribute(input.getDefault()));
						}
					}
				}
			}
			if (step.getPreferences() != null) {
				for (Preference pref : step.getPreferences()) {
					// if this parameter doesn't currently have a value, check
					// for a default in the CPD
					if (!this.portletPreferences.containsKey(pref.getName())
							|| this.portletPreferences.get(pref.getName()).getValue().size() == 0
							|| (this.portletPreferences.get(pref.getName()).getValue().size() == 1 && this.portletPreferences.get(pref.getName()).getValue().get(0).trim().equals(""))) {
						
						if (!this.portletPreferences.containsKey(pref.getName())) {
							this.portletPreferences.put(pref.getName(), new StringListAttribute());
						}
						
						// use the default value if one exists
						PreferenceInputType input = pref.getPreferenceInput().getValue();
						if (input instanceof SingleValuedPreferenceInputType) {
						    SingleValuedPreferenceInputType singleValued = (SingleValuedPreferenceInputType) input;
						    if (singleValued.getDefault() != null) {
						        this.portletPreferences.get(pref.getName()).getValue().add(singleValued.getDefault());
						    }
						} else if (input instanceof MultiValuedPreferenceInputType) {
                            MultiValuedPreferenceInputType multiValued = (MultiValuedPreferenceInputType) input;
                            if (multiValued.getDefaults() != null) {
                                this.portletPreferences.get(pref.getName()).getValue().addAll(multiValued.getDefaults());
                            }
						}

					}
				}
			}
		}

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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

	public boolean isPortlet() {
		return true;
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

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getPortletName() {
		return portletName;
	}

	public void setPortletName(String portletName) {
		this.portletName = portletName;
	}

	public boolean isFramework() {
		return framework;
	}

	public void setFramework(boolean framework) {
		this.framework = framework;
	}

	public void setLifecycleState(PortletLifecycleState lifecycleState) {
		this.lifecycleState = lifecycleState;
	}

	public void setLifecycleState(String lifecycleState) {
		for (PortletLifecycleState state : PortletLifecycleState.values()) {
			if (state.toString().equals(lifecycleState)) {
				this.lifecycleState = state;
				break;
			}
		}
	}

	public PortletLifecycleState getLifecycleState() {
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

	public Map<String, Attribute> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Attribute> parameters) {
		this.parameters = parameters;
	}
	
	public Map<String, StringListAttribute> getPortletPreferences() {
		return this.portletPreferences;
	}

	public void setPortletPreferences(Map<String, StringListAttribute> portletParameters) {
		this.portletPreferences = portletParameters;
	}

    public Map<String, BooleanAttribute> getPortletPreferenceReadOnly() {
        return this.portletPreferenceReadOnly;
    }

    public void setPortletPreferenceReadOnly(
            Map<String, BooleanAttribute> portletPreferenceReadOnly) {
        this.portletPreferenceReadOnly = portletPreferenceReadOnly;
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
	 * Return the full date and time at which this portlet shoudl be automatically
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
	 * Return the full date and time at which this portlet shoudl be automatically
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
