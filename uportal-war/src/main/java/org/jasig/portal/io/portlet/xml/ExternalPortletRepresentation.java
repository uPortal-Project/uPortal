package org.jasig.portal.io.portlet.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@XmlRootElement(name = "portlet-definition")
public class ExternalPortletRepresentation implements Serializable {

	private String title;
	private String name;
	private String fname;
	private String desc;
	private String type;
	private String applicationId;
	private String portletName;
	private boolean framework;
	private int timeout;
	private boolean hasEdit;
	private boolean hasHelp;
	private boolean hasAbout;
	private List<String> categories = new ArrayList<String>();
	private List<String> groups = new ArrayList<String>();
	private List<ExternalPortletParameterRepresentation> parameters = new ArrayList<ExternalPortletParameterRepresentation>();
	private List<ExternalPortletPreferenceRepresentation> preferences = new ArrayList<ExternalPortletPreferenceRepresentation>();
	
	@XmlElement(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@XmlElement(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name = "fname")
	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	@XmlElement(name = "desc")
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@XmlElement(name = "type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(name = "applicationId")
	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	@XmlElement(name = "portletName")
	public String getPortletName() {
		return portletName;
	}

	public void setPortletName(String portletName) {
		this.portletName = portletName;
	}

	@XmlElement(name = "isFramework")
	public boolean isFramework() {
		return framework;
	}

	public void setFramework(boolean framework) {
		this.framework = framework;
	}

	@XmlElement(name = "timeout")
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@XmlElement(name = "hasEdit")
	public boolean isHasEdit() {
		return hasEdit;
	}

	public void setHasEdit(boolean hasEdit) {
		this.hasEdit = hasEdit;
	}

	@XmlElement(name = "hasHelp")
	public boolean isHasHelp() {
		return hasHelp;
	}

	public void setHasHelp(boolean hasHelp) {
		this.hasHelp = hasHelp;
	}

	@XmlElement(name = "hasAbout")
	public boolean isHasAbout() {
		return hasAbout;
	}

	public void setHasAbout(boolean hasAbout) {
		this.hasAbout = hasAbout;
	}

	@XmlElement(name = "category")
	@XmlElementWrapper(name = "categories")
	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	
	public void addCategory(String category) {
		this.categories.add(category);
	}

	@XmlElement(name = "group")
	@XmlElementWrapper(name = "groups")
	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}
	
	public void addGroup(String group) {
		this.groups.add(group);
	}

	@XmlElement(name = "parameter")
	@XmlElementWrapper(name = "parameters")
	public List<ExternalPortletParameterRepresentation> getParameters() {
		return parameters;
	}

	public void setParameters(
			List<ExternalPortletParameterRepresentation> parameters) {
		this.parameters = parameters;
	}
	
	public void addParameter(ExternalPortletParameterRepresentation parameter) {
		this.parameters.add(parameter);
	}

	@XmlElement(name = "portletPreference")
	@XmlElementWrapper(name = "portletPreferences")
	public List<ExternalPortletPreferenceRepresentation> getPreferences() {
		return preferences;
	}

	public void setPreferences(
			List<ExternalPortletPreferenceRepresentation> preferences) {
		this.preferences = preferences;
	}
	
	public void addPreference(ExternalPortletPreferenceRepresentation pref) {
		this.preferences.add(pref);
	}
	
	@Override
	public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
	        .append("title", this.title).append("name", this.name)
	        .append("fname", this.fname).append("description", this.desc)
	        .append("type", this.type).append("timeout", this.timeout)
	        .append("timeout", this.timeout).append("hasEdit", this.hasEdit)
	        .append("hasHelp", this.hasHelp).append("hasAbout", this.hasAbout)
	        .toString();
	}

}
