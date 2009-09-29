/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class CPDControl implements Serializable {
	
	@XStreamAsAttribute
	private String type;
	
	@XStreamAsAttribute
	private String include;
	
	@XStreamAsAttribute
	private String override;
	
	public CPDControl(String type, String include,
			String override) {
		super();
		this.type = type;
		this.include = include;
		this.override = override;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getInclude() {
		return include;
	}

	public void setInclude(String include) {
		this.include = include;
	}

	public String getOverride() {
		return override;
	}

	public void setOverride(String override) {
		this.override = override;
	}
	
	

}
