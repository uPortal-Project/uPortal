/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.i18n;

import java.util.Locale;

import org.apache.oro.text.perl.Perl5Util;
import org.jasig.portal.BrowserInfo;
import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.StylesheetSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;

/**
 * Selects XSLT stylesheets based on locale information.
 * @author Shoji Kajita <a href="mailto:">kajita@itc.nagoya-u.ac.jp</a>
 * @version $Revision$
 * @since uPortal 2.2
 */
public class LocaleAwareXSLT extends XSLT {

    private static final Log log = LogFactory.getLog(LocaleAwareXSLT.class);
    
    protected Locale[] locales;
    private static Perl5Util perl5Util = new Perl5Util();
    
    /**
     * Constructor that configures the calling class.
     * @param instance class name used to search for resources
     */
    public LocaleAwareXSLT(Object instance) {
        super(instance);
    }
    
    /**
     * Constructor that configures both the calling class and the locale list.
     * @param instance class name used to search for resources
     * @param locales a list of locales
     */
    public LocaleAwareXSLT(Object instance, Locale[] locales) {
        this(instance);
        this.locales = locales;
    }
    
    /**
     * Sets the locales.
     * @param locales a list of locales
     */
    public void setLocales(Locale[] locales) {
        this.locales = locales;
    }

    /**
     * Configures the xsl source by choosing the appropriate stylesheet from
     * the provided stylesheet list file, taking into account the list of locales.
     * @param sslUri the URL of the stylesheet list file
     * @param stylesheetTitle the title of a stylesheet within the stylesheet list file
     * @param browserInfo the browser info object
     * @throws org.jasig.portal.PortalException
     */
    public void setXSL(String sslUri, String stylesheetTitle, BrowserInfo browserInfo) throws PortalException {
        StylesheetSet set = getStylesheetSet(ResourceLoader.getResourceAsURLString(caller.getClass(), sslUri));
        set.setMediaProps(mediaProps);
        String xslUri = set.getStylesheetURI(stylesheetTitle, browserInfo);
        xslUri = getLocaleAwareXslUri(xslUri, locales, caller);
        setXSL(xslUri);
    }

    /**
     * Configures the xsl source by choosing the appropriate stylesheet from
     * the provided stylesheet list file, taking into account the list of locales.
     * @param sslUri the URL of the stylesheet list file
     * @param browserInfo the browser info object
     * @throws org.jasig.portal.PortalException
     */
    public void setXSL(String sslUri, BrowserInfo browserInfo) throws PortalException {
        setXSL(sslUri, (String)null, browserInfo);
    }

    /**
     * Finds localized version of stylesheet according to the supplied list of locales.
     * @param xslUri the URL of the stylesheet file
     * @param locales the list of locales
     * @param caller the calling class
     */
    public static String getLocaleAwareXslUri(String xslUri, Locale[] locales, Object caller) {
        String localeAwareXslUri = xslUri;
        int i;

        if (!LocaleManager.isLocaleAware() || locales == null) {
            try {
                xslUri = ResourceLoader.getResourceAsURLString(caller.getClass(), xslUri);
                log.debug("LocaleAwareXSLT.getLocaleAwareXslUri: XSL file found as " + xslUri);
            } catch (ResourceMissingException e) {
                log.debug("LocaleAwareXSLT.getLocaleAwareXslUri: XSL file NOT found as " + xslUri);
            }
        } else {
            for (i = 0; i < locales.length; i++) {
                // localeAwareXslUri = xslUri.replaceAll("\\.xsl", "_" + locales[i] + ".xsl");
                // replaceAll is introduced from JDK1.4
                localeAwareXslUri = perl5Util.substitute("s/\\.xsl/_" + locales[i] + ".xsl" + "/g", xslUri);
                log.debug("LocaleAwareXSLT.getLocaleAwareXslUri: locale aware xslUri=" + localeAwareXslUri);
                try {
                    xslUri = ResourceLoader.getResourceAsURLString(caller.getClass(), localeAwareXslUri);
                    log.debug("LocaleAwareXSLT.getLocaleAwareXslUri: XSL file found as " + xslUri);
                    break;
                } catch (ResourceMissingException e) {
                    log.debug("LocaleAwareXSLT.getLocaleAwareXslUri: XSL file NOT found as " + localeAwareXslUri);
                    log.debug("LocaleAwareXSLT.getLocaleAwareXslUri: Fallbacking...");
                }
            }
            if (i == locales.length) {
                try {
                    xslUri = ResourceLoader.getResourceAsURLString(caller.getClass(), xslUri);
                    log.debug("LocaleAwareXSLT.getLocaleAwareXslUri: XSL file found as " + xslUri);
                } catch (ResourceMissingException e) {
                    log.debug("LocaleAwareXSLT.getLocaleAwareXslUri: XSL file NOT found as " + xslUri);
                }
            }
        }
        return xslUri;
    }
}
