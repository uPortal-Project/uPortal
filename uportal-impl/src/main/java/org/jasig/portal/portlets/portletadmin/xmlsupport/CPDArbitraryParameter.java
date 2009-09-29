/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.io.Serializable;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class CPDArbitraryParameter implements Serializable {

	@XStreamImplicit(itemFieldName="paramName-prefix")
	private List<String> paramNamePrefixes;

	public CPDArbitraryParameter(
			List<String> paramNamePrefixes) {
		this.paramNamePrefixes = paramNamePrefixes;
	}

	public List<String> getParamNamePrefixes() {
		return paramNamePrefixes;
	}

	public void setParamNamePrefixes(List<String> paramNamePrefixes) {
		this.paramNamePrefixes = paramNamePrefixes;
	}

}
