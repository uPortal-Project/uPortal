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

import org.jasig.portal.channel.IChannelParameter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("parameter")
public class ChannelParameterBean implements Serializable {
	
   	@XStreamAsAttribute
	private String name;
	
   	@XStreamAsAttribute
	private String override;
	
   	@XStreamAsAttribute
	private String value;
	
   	@XStreamAsAttribute
	private String description;

	public ChannelParameterBean(IChannelParameter param) {
		this.name = param.getName();
		this.override = param.getOverride() ? "yes" : "no";
		this.value = param.getValue();
		this.description = param.getDescription();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOverride() {
		return this.override;
	}

	public void setOverride(String override) {
		this.override = override;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
