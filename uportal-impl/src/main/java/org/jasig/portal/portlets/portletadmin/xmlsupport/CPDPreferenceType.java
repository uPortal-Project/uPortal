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

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

public class CPDPreferenceType implements ICPDOptionType, Serializable {

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
