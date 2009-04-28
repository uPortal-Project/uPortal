package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("param")
public class CPDParameter implements Serializable {

	@XStreamAsAttribute
	private String modify;

	private String name;

	private String label;

	private String example;

	private String description;

	private String defaultValue;
	
	private String units;

	private CPDParameterType type;

	public CPDParameter(String modify, String name, String label,
			String example, String description, String defaultValue, String units,
			CPDParameterType type) {
		this.modify = modify;
		this.name = name;
		this.label = label;
		this.example = example;
		this.description = description;
		this.defaultValue = defaultValue;
		this.units = units;
		this.type = type;
	}

	public String getModify() {
		return modify;
	}

	public void setModify(String modify) {
		this.modify = modify;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public CPDParameterType getType() {
		return type;
	}

	public void setType(CPDParameterType type) {
		this.type = type;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}
	
}
