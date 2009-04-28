package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("channelDef")
public class ChannelPublishingDefinition implements Serializable {

	private String description;

   	@XStreamAlias("class")
	private String className;
   	
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
