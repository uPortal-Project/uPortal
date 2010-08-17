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

package org.jasig.portal.layout.dlm.remoting.registry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChannelBean implements Serializable {
	
	private String id;
	private int chanId;
	private String javaClass;
	private String description;
	private boolean editable;
	private String fname;
	private boolean hasAbout;
	private boolean hasHelp;
	private boolean isPortlet;
	private String locale;
	private String name;
	private boolean secure;
	private int timeout;
	private String state;
	private String title;
	private int typeId;
   	private List<ChannelParameterBean> parameters = new ArrayList<ChannelParameterBean>();
   	
   	public ChannelBean() { }

	public void addParameter(ChannelParameterBean parameter) {
		this.parameters.add(parameter);
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getChanId() {
		return this.chanId;
	}

	public void setChanId(int chanId) {
		this.chanId = chanId;
	}

	public String getJavaClass() {
		return this.javaClass;
	}

	public void setJavaClass(String javaClass) {
		this.javaClass = javaClass;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isEditable() {
		return this.editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public String getFname() {
		return this.fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public boolean isHasAbout() {
		return this.hasAbout;
	}

	public void setHasAbout(boolean hasAbout) {
		this.hasAbout = hasAbout;
	}

	public boolean isHasHelp() {
		return this.hasHelp;
	}

	public void setHasHelp(boolean hasHelp) {
		this.hasHelp = hasHelp;
	}

	public boolean isPortlet() {
		return this.isPortlet;
	}

	public void setPortlet(boolean isPortlet) {
		this.isPortlet = isPortlet;
	}

	public String getLocale() {
		return this.locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSecure() {
		return this.secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public int getTimeout() {
		return this.timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTypeId() {
		return this.typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	
	public List<ChannelParameterBean> getParameters() {
	    return this.parameters;
	}

}
