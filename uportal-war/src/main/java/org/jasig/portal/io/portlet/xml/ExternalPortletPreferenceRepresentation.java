package org.jasig.portal.io.portlet.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class ExternalPortletPreferenceRepresentation {

	private String name;

	private boolean readOnly;
	
	private List<String> values;
	
	public ExternalPortletPreferenceRepresentation() { 
	}
	
	public ExternalPortletPreferenceRepresentation(String name, boolean readOnly, List<String> values) {
		this.name = name;
		this.readOnly = readOnly;
		this.values = values;
	}

	@XmlElement(name="name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name="readOnly")
	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@XmlElement(name="value")
	@XmlElementWrapper(name="values")
	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

}
