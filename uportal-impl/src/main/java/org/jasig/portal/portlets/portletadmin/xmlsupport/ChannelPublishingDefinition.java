/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
