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
