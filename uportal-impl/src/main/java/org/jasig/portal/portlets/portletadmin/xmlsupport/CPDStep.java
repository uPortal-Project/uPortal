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
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("step")
public class CPDStep implements Serializable {

	@XStreamAlias("ID")
	private String id;
	
	private String name;

	private String description;
   	
	@XStreamImplicit(itemFieldName="parameter")
   	private List<CPDParameter> parameters;
	
	@XStreamImplicit(itemFieldName="arbitrary-parameters")
	private List<CPDArbitraryParameter> arbitraryParameters;
	
	@XStreamImplicit(itemFieldName="preference")
   	private List<CPDPreference> preferences;
	
	@XStreamAlias("arbitrary-preferences")
    private boolean arbitraryPreferences = false;
	
	public CPDStep() { }
	
	public CPDStep(String id, String name, String description, List<CPDParameter> parameters, List<CPDArbitraryParameter> arbitraryParameters, List<CPDPreference> preferences, boolean arbitraryPreferences) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.parameters = parameters;
		this.arbitraryParameters = arbitraryParameters;
		this.preferences = preferences;
		this.arbitraryPreferences = arbitraryPreferences;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<CPDParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<CPDParameter> parameters) {
		this.parameters = parameters;
	}
	
	public List<CPDPreference> getPreferences() {
		return this.preferences;
	}

	public void setPreferences(List<CPDPreference> preferences) {
		this.preferences = preferences;
	}

	public List<CPDArbitraryParameter> getArbitraryParameters() {
		return arbitraryParameters;
	}

	public void setArbitraryParameters(
			List<CPDArbitraryParameter> arbitraryParameters) {
		this.arbitraryParameters = arbitraryParameters;
	}

    public boolean isArbitraryPreferences() {
        return this.arbitraryPreferences;
    }

    public void setArbitraryPreferences(boolean arbitraryPreferences) {
        this.arbitraryPreferences = arbitraryPreferences;
    }
	
}
