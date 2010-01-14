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

package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.io.Serializable;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("preference")
public class CPDPreference implements Serializable {

	@XStreamAsAttribute
	private String modify;

	private String name;

	private String label;

	private String example;

	private String description;

	@XStreamImplicit(itemFieldName="defaultValue")
	private List<String> defaultValues;
	
	private String units;

	private CPDPreferenceType type;

	public CPDPreference(String modify, String name, String label,
			String example, String description, List<String> defaultValues, String units,
			CPDPreferenceType type) {
		this.modify = modify;
		this.name = name;
		this.label = label;
		this.example = example;
		this.description = description;
		this.defaultValues = defaultValues;
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

	public List<String> getDefaultValues() {
		return this.defaultValues;
	}

	public void setDefaultValues(List<String> defaultValue) {
		this.defaultValues = defaultValue;
	}

	public CPDPreferenceType getType() {
		return this.type;
	}

	public void setType(CPDPreferenceType type) {
		this.type = type;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}
	
}
