package org.jasig.portal.i18n;

import java.util.Locale;
import java.lang.String;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.BrowserInfo;
import org.jasig.portal.PortalException;
import org.jasig.portal.StylesheetSet;
import org.jasig.portal.services.LogService;
import org.jasig.portal.ResourceMissingException;
import org.apache.oro.text.perl.Perl5Util;

public class LocaleAwareXSLT extends XSLT {

    private Object caller;
    private Locale[] locales;
    private static final String mediaProps = "/properties/media.properties";
    private static Perl5Util perl5Util = new Perl5Util();

    public LocaleAwareXSLT(Object instance, Locale[] locales) {
	super(instance);
	this.caller=instance;
	this.locales=locales;

        // debug
        if  (locales != null) {
            for (int i=0; i<locales.length; i++) {
                LogService.log(LogService.DEBUG, "LocaleAwareXSLT: locales #" + i + " = " + locales[i]);
            }
        }
    }

    public void setXSL(String sslUri, String stylesheetTitle, BrowserInfo browserInfo) throws PortalException {

	StylesheetSet set = getStylesheetSet(ResourceLoader.getResourceAsURLString(caller.getClass(), sslUri));
	LogService.log(LogService.DEBUG, "LocaleAwareXSLT.setXSL: sslUri=" + sslUri);
	LogService.log(LogService.DEBUG, "LocaleAwareXSLT.setXSL: fullpath sslUri=" + ResourceLoader.getResourceAsURLString(caller.getClass(), sslUri));
	set.setMediaProps(mediaProps);
	String xslUri = set.getStylesheetURI(stylesheetTitle, browserInfo);
	xslUri = getLocaleAwareXslUri(xslUri, locales, caller);
	setXSL(xslUri);
    }
	
    public void setXSL(String sslUri, BrowserInfo browserInfo) throws PortalException {
	setXSL(sslUri, (String)null, browserInfo);
    }

    public static String getLocaleAwareXslUri(String xslUri, Locale[] locales, Object caller) {

	String localeAwareXslUri = xslUri;
	int i;

        if (locales == null) {
	    try {
		xslUri = ResourceLoader.getResourceAsURLString(caller.getClass(), xslUri);	    
		LogService.log(LogService.DEBUG, "LocaleAwareXSLT.getLocaleAwareXslUri: XSL file found as " + xslUri);
	    } catch (ResourceMissingException e) {
		LogService.log(LogService.DEBUG, "LocaleAwareXSLT.getLocaleAwareXslUri: XSL file NOT found as " + xslUri);
	    }
        }  else {

            for (i=0; i<locales.length; i++) {
                // localeAwareXslUri = xslUri.replaceAll("\\.xsl", "_" + locales[i] + ".xsl");
                // replaceAll is introduced from JDK1.4
                localeAwareXslUri = perl5Util.substitute("s/\\.xsl/_" + locales[i] + ".xsl" + "/g", xslUri);
                LogService.log(LogService.DEBUG, "LocaleAwareXSLT.getLocaleAwareXslUri: locale aware xslUri=" + localeAwareXslUri);
                try {
                    xslUri = ResourceLoader.getResourceAsURLString(caller.getClass(), localeAwareXslUri);
                    LogService.log(LogService.DEBUG, "LocaleAwareXSLT.getLocaleAwareXslUri: XSL file found as " + xslUri);
                    break;
                }  catch (ResourceMissingException e) {
                    LogService.log(LogService.DEBUG, "LocaleAwareXSLT.getLocaleAwareXslUri: XSL file NOT found as " + localeAwareXslUri);
                    LogService.log(LogService.DEBUG, "LocaleAwareXSLT.getLocaleAwareXslUri: Fallbacking...");
                }
            }
            if (i == locales.length) {
                try {
                    xslUri = ResourceLoader.getResourceAsURLString(caller.getClass(), xslUri);	    
                    LogService.log(LogService.DEBUG, "LocaleAwareXSLT.getLocaleAwareXslUri: XSL file found as " + xslUri);
                } catch (ResourceMissingException e) {
                    LogService.log(LogService.DEBUG, "LocaleAwareXSLT.getLocaleAwareXslUri: XSL file NOT found as " + xslUri);
                }
            }
        }
	return xslUri;
    }
}
