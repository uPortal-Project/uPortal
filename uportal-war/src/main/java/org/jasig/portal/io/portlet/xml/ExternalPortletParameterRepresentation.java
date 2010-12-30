package org.jasig.portal.io.portlet.xml;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class ExternalPortletParameterRepresentation {

	private String name;
	private String value;
	
	public ExternalPortletParameterRepresentation() {		
	}

	public ExternalPortletParameterRepresentation(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	@XmlElement(name="name")
	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	@XmlElement(name="value")
	public void setValue(String value) {
		this.value = value;
	}

}
