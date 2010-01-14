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

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("channelDef")
public class ChannelPublishingDefinition implements Serializable {
    private static final long serialVersionUID = 5480102681300403689L;

    private String description;

   	@XStreamAlias("class")
	private String className;
   	
   	@XStreamAlias("deprecated")
   	private boolean deprecated;
   	
   	@XStreamAlias("params")
   	private CPDParameterList params;
   	
   	@XStreamAlias("controls")
   	private CPDControlList controls;
   	
   	public ChannelPublishingDefinition() { }
   	
   	public ChannelPublishingDefinition(String description, String className, CPDParameterList params, CPDControlList controls) {
   		this.description = description;
   		this.className = className;
   		this.params = params;
   		this.controls = controls;
   	}
   	
	public boolean isDeprecated() {
        return this.deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public CPDParameterList getParams() {
		return params;
	}

	public void setParams(CPDParameterList params) {
		this.params = params;
	}

	public CPDControlList getControls() {
		return controls;
	}

	public void setControls(CPDControlList controls) {
		this.controls = controls;
	}
   	
	
}
