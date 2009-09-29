/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CPDParameterTypeRestriction implements ICPDOptionTypeRestriction, Serializable {

	private String type;
	
	private String min;
	private String max;
	
	private List<CPDParameterTypeRestrictionValue> values = new ArrayList<CPDParameterTypeRestrictionValue>();

	private String defaultValue;
	
	public CPDParameterTypeRestriction() { }
	
	public CPDParameterTypeRestriction(String type, String min, String max, List<CPDParameterTypeRestrictionValue> values, String defaultValue) {
		this.type = type;
		this.min = min;
		this.max = max;
		this.values = values;
		this.defaultValue = defaultValue;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public List<CPDParameterTypeRestrictionValue> getValues() {
		return values;
	}

	public void setValues(List<CPDParameterTypeRestrictionValue> values) {
		this.values = values;
	}
	
	public void addValue(CPDParameterTypeRestrictionValue value) {
		this.values.add(value);
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

}
