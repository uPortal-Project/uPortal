/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.util.List;

public interface ICPDOptionTypeRestriction {

	public String getType();

	public void setType(String type);

	public String getMin();

	public void setMin(String min);

	public String getMax();

	public void setMax(String max);

	public List<CPDParameterTypeRestrictionValue> getValues();

	public void setValues(List<CPDParameterTypeRestrictionValue> values);
	
	public void addValue(CPDParameterTypeRestrictionValue value);

}
