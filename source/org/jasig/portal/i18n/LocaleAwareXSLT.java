/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or withoutu
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.i18n;

import java.util.Locale;

import org.apache.oro.text.perl.Perl5Util;
import org.jasig.portal.BrowserInfo;
import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.StylesheetSet;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;

/**
 * Selects XSLT stylesheets based on locale information.
 * @author Shoji Kajita <a href="mailto:">kajita@itc.nagoya-u.ac.jp</a>
 * @version $Revision$
 */
public class LocaleAwareXSLT extends XSLT {

    private Object caller;
    private Locale[] locales;
    private static final String mediaProps = "/properties/media.properties";
    private static Perl5Util perl5Util = new Perl5Util();

    public LocaleAwareXSLT(Object instance, Locale[] locales) {
        super(instance);
        this.caller = instance;
        this.locales = locales;

        // debug
        if (locales != null) {
            for (int i = 0; i < locales.length; i++) {
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
        setXSL(sslUri, (String) null, browserInfo);
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
        } else {
            for (i = 0; i < locales.length; i++) {
                // localeAwareXslUri = xslUri.replaceAll("\\.xsl", "_" + locales[i] + ".xsl");
                // replaceAll is introduced from JDK1.4
                localeAwareXslUri = perl5Util.substitute("s/\\.xsl/_" + locales[i] + ".xsl" + "/g", xslUri);
                LogService.log(LogService.DEBUG, "LocaleAwareXSLT.getLocaleAwareXslUri: locale aware xslUri=" + localeAwareXslUri);
                try {
                    xslUri = ResourceLoader.getResourceAsURLString(caller.getClass(), localeAwareXslUri);
                    LogService.log(LogService.DEBUG, "LocaleAwareXSLT.getLocaleAwareXslUri: XSL file found as " + xslUri);
                    break;
                } catch (ResourceMissingException e) {
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
