/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
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

package org.jasig.portal.channels.localechooser;

import org.jasig.portal.Constants;
import org.jasig.portal.PortalException;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/**
 * Changes the locale preferences for the current session.
 * Any changes made via this channel will not be persisted
 * between sessions.
 * @author Shoji Kajita <a href="mailto:">kajita@itc.nagoya-u.ac.jp</a>
 * @version $Revision$
 */
public class CLocaleChooser extends BaseChannel
{
    private final String channelName = "Locale Chooser";
    protected final String sslLocation = "CLocaleChooser.ssl";
    // I18n propertiy
    protected static final boolean localeAware = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.i18n.LocaleManager.locale_aware");

    public void renderXML(ContentHandler out) throws PortalException {
        Document doc = DocumentFactory.getNewDocument();
        String locale = runtimeData.getParameter(Constants.LOCALES_PARAM);

        if (localeAware == false)
            return;

        // Create <locale-status> element
        Element localeStatusElement = doc.createElement("locale-status");

        if (locale != null) {
            // Create <locale> element
            Element localeElement = doc.createElement("locale");
            localeElement.appendChild(doc.createTextNode(locale));
            localeStatusElement.appendChild(localeElement);
        }

        doc.appendChild(localeStatusElement);

        LogService.log(LogService.DEBUG, "LocaleChooser - render XML");
        LogService.log(LogService.DEBUG, "LocaleChooser -  locale: " + locale);
        LogService.log(LogService.DEBUG, "LocaleChooser -  sslLocation: " + sslLocation);
        LogService.log(LogService.DEBUG, "LocaleChooser -  XSL= " + locale + "/" + sslLocation);

        XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
        xslt.setXML(doc);
        xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
        xslt.setTarget(out);
        xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
        xslt.setStylesheetParameter("localesParam", Constants.LOCALES_PARAM);
        xslt.transform();
    }

}
