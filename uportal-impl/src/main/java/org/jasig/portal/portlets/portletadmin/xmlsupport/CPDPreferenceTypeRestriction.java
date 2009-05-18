package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class CPDPreferenceTypeRestriction implements Serializable {

	private String type;
	
	private String min;
	private String max;
	
	private List<CPDParameterTypeRestrictionValue> values = new ArrayList<CPDParameterTypeRestrictionValue>();

	@XStreamImplicit(itemFieldName="defaultValue")
	private List<String> defaultValues;
	
	public CPDPreferenceTypeRestriction() { }
	
	public CPDPreferenceTypeRestriction(String type, String min, String max, List<CPDParameterTypeRestrictionValue> values, List<String> defaultValues) {
		this.type = type;
		this.min = min;
		this.max = max;
		this.values = values;
		this.defaultValues = defaultValues;
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

	public List<String> getDefaultValues() {
		return this.defaultValues;
	}

	public void setDefaultValues(List<String> defaultValues) {
		this.defaultValues = defaultValues;
	}

}
