/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.utils.web.flow;

import org.springframework.binding.convert.converters.StringToDate;
import org.springframework.binding.convert.service.DefaultConversionService;

/**
 * <p>This class contains the custom converters for Spring Web Flow type
 * conversions.</p>
 * 
 * @author Drew Mazurek
 */
public class PortalConversionService extends DefaultConversionService {

	// TODO: this should be externalized
	private static final String DATE_AND_TIME_FORMAT = "MM/dd/yyyy hh:mm aa";
	
	public PortalConversionService() {
		super();
	}
	
	/* (non-Javadoc)
	 * Modify this method to add more converters.
	 */
	protected void addDefaultConverters() {
		super.addDefaultConverters();
		
		/* Add the "shortDate" conversion. */
		StringToDate dateAndTimeToDate = new StringToDate();
		dateAndTimeToDate.setPattern(DATE_AND_TIME_FORMAT);
		addConverter("dateAndTime", dateAndTimeToDate);
	}
}
