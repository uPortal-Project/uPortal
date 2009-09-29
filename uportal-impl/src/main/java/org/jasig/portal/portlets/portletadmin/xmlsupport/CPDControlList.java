/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.io.Serializable;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class CPDControlList implements Serializable {
	
	@XStreamImplicit(itemFieldName="control")
	private List<CPDControl> controls;
	
	public CPDControlList() {
		super();
		this.controls = controls;
	}

	public List<CPDControl> getControls() {
		return controls;
	}

	public void setControls(List<CPDControl> controls) {
		this.controls = controls;
	}
	
	

}
