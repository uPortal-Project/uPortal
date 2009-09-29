/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.io.Serializable;

public class CPDParameterTypeRestrictionValue implements Serializable {

	private String display;
	
	private String value;

	public CPDParameterTypeRestrictionValue() { }
	
	public CPDParameterTypeRestrictionValue(String value, String display) {
		this.value = value;
		this.display = display;
	}

	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}
	
	
}
