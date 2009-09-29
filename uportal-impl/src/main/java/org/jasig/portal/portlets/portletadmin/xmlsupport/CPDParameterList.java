/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.io.Serializable;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("params")
public class CPDParameterList implements Serializable {

	@XStreamImplicit(itemFieldName="step")
   	private List<CPDStep> steps;
	
	public CPDParameterList() { }
	
	public CPDParameterList(List<CPDStep> steps) {
		this.steps = steps;
	}

	public List<CPDStep> getSteps() {
		return steps;
	}

	public void setSteps(List<CPDStep> steps) {
		this.steps = steps;
	}

}
