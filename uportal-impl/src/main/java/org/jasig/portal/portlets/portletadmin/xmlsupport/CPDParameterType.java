/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

public class CPDParameterType implements ICPDOptionType, Serializable {

	@XStreamAsAttribute
	private String base;

	@XStreamAsAttribute
	private String input;

	@XStreamAsAttribute
	private String display;

	private String length;

	private String maxlength;

	@XStreamConverter(value=CPDParameterRestrictionConverter.class)
	private CPDParameterTypeRestriction restriction;

	public CPDParameterType(String base, String input,
			String display, String length, String maxlength, CPDParameterTypeRestriction restriction) {
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

	public CPDParameterTypeRestriction getRestriction() {
		return restriction;
	}

	public void setRestriction(CPDParameterTypeRestriction restriction) {
		this.restriction = restriction;
	}

}
