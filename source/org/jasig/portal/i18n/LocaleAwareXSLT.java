package org.jasig.portal.i18n;

import java.util.Locale;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.BrowserInfo;
import org.jasig.portal.PortalException;
import org.jasig.portal.StylesheetSet;
import org.jasig.portal.services.LogService;
import org.jasig.portal.ResourceMissingException;

public class LocaleAwareXSLT extends XSLT {

    private Object caller;
    private Locale locale;
    private static final String mediaProps = "/properties/media.properties";

    public LocaleAwareXSLT(Object instance, Locale locale) {
	super(instance);
	this.caller=instance;
	this.locale=locale;
	LogService.log(LogService.DEBUG, "LocaleAwareXSLT: locale=" + locale);
    }

    public void setXSL(String sslUri, String stylesheetTitle, BrowserInfo browserInfo) throws PortalException {

	StylesheetSet set = getStylesheetSet(ResourceLoader.getResourceAsURLString(caller.getClass(), sslUri));
	LogService.log(LogService.DEBUG, "LocaleAwareXSLT.setXSL: sslUri=" + sslUri);
	LogService.log(LogService.DEBUG, "LocaleAwareXSLT.setXSL: fullpath sslUri=" + ResourceLoader.getResourceAsURLString(caller.getClass(), sslUri));
	set.setMediaProps(mediaProps);
	String xslUri = set.getStylesheetURI(stylesheetTitle, browserInfo);

	try {
	    xslUri = ResourceLoader.getResourceAsURLString(caller.getClass(), xslUri + "." + locale);
	    LogService.log(LogService.DEBUG, "LocaleAwareXSLT.setXSL: XSL file found as " + xslUri + "." + locale);
	}  catch (ResourceMissingException e) {
	    LogService.log(LogService.DEBUG, "LocaleAwareXSLT.setXSL: XSL file NOT found as " + xslUri + "." + locale);
	    LogService.log(LogService.DEBUG, "LocaleAwareXSLT.setXSL: Fallbacked as " + xslUri);
	}
	setXSL(xslUri);
    }
	
    public void setXSL(String sslUri, BrowserInfo browserInfo) throws PortalException {
	setXSL(sslUri, (String)null, browserInfo);
    }


}
