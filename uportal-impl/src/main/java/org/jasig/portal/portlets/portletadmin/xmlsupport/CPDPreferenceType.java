package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

public class CPDPreferenceType implements Serializable {

	@XStreamAsAttribute
	private String base;

	@XStreamAsAttribute
	private String input;

	@XStreamAsAttribute
	private String display;

	private String length;

	private String maxlength;

	@XStreamConverter(value=CPDPreferenceRestrictionConverter.class)
	private CPDPreferenceTypeRestriction restriction;
	
	public CPDPreferenceType(String base, String input,
			String display, String length, String maxlength, CPDPreferenceTypeRestriction restriction) {
		this.base = base;
		this.input = input;
		this.display = display;
		this.length = length;
		this.maxlength = maxlength;
		this.restriction = restriction;

	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getMaxlength() {
		return maxlength;
	}

	public void setMaxlength(String maxlength) {
		this.maxlength = maxlength;
	}

	public CPDPreferenceTypeRestriction getRestriction() {
		return this.restriction;
	}

	public void setRestriction(CPDPreferenceTypeRestriction restriction) {
		this.restriction = restriction;
	}

}
