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
package org.jasig.portal.layout.channels.locales;

import java.util.Locale;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.IPrivileged;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/**
 * Lets a user select his/her locale preference. The UI, for now, is
 * limited.  The user can only select one locale choice.  The underlying
 * APIs and data structures actually allow an "ordering" of locales, so
 * this channel can be enhanced to allow the user to sort a list of locales.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class CUserLocalesSelector extends BaseChannel implements IPrivileged {

    private IUserPreferencesManager upm = null;
    private LocaleManager lm = null;
    private Locale userLocale = null;
    private static final String sslUri = "userLocales.ssl";

    public void setPortalControlStructures(PortalControlStructures pcs)
        throws PortalException {
        upm = pcs.getUserPreferencesManager();
        lm = upm.getUserPreferences().getProfile().getLocaleManager();
        Locale[] userLocales = lm.getUserLocales();
        if (userLocales != null && userLocales.length > 0) {
            userLocale = userLocales[0];
        }
    }

    public void setRuntimeData(ChannelRuntimeData runtimeData) throws PortalException {
        this.runtimeData = runtimeData;
        String localeString = runtimeData.getParameter("locale");
        if (localeString != null) {
            userLocale = LocaleManager.parseLocale(localeString);
            try {
                lm.persistUserLocales(new Locale[] { userLocale });
                upm.getUserLayoutManager().loadUserLayout();
            } catch (Exception e) {
                throw new PortalException(e);
            }
        }
    }

    public void renderXML(ContentHandler out) throws PortalException {
        Document doc = DocumentFactory.getNewDocument();
        Locale[] locales = lm.getLocales();

        // <locales>
        Element localesE = doc.createElement("locales");
        Locale[] portalLocales = lm.getPortalLocales();
        for (int i = 0; i < portalLocales.length; i++) {
          Element locE = doc.createElement("locale");
          locE.setAttribute("displayName", portalLocales[i].getDisplayName(locales[0]));
          locE.setAttribute("code", portalLocales[i].toString());

          // Mark which locale is the user's preference
          if (userLocale != null && userLocale.equals(portalLocales[i])) {
              locE.setAttribute("selected", "true");
          }

          // <language iso2="..." iso3="..." displayName="..."/>
          Element languageE = doc.createElement("language");
          languageE.setAttribute("iso2", portalLocales[i].getLanguage());
          languageE.setAttribute("iso3", portalLocales[i].getISO3Language());
          languageE.setAttribute("displayName", portalLocales[i].getDisplayLanguage(locales[0]));
          locE.appendChild(languageE);

          // <country iso2="..." iso3="..." displayName="..."/>
          Element countryE = doc.createElement("country");
          countryE.setAttribute("iso2", portalLocales[i].getCountry());
          countryE.setAttribute("iso3", portalLocales[i].getISO3Country());
          countryE.setAttribute("displayName", portalLocales[i].getDisplayCountry(locales[0]));
          locE.appendChild(countryE);

          // <variant code="..." displayName="..."/>
          Element variantE = doc.createElement("variant");
          variantE.setAttribute("code", portalLocales[i].getVariant());
          variantE.setAttribute("displayName", portalLocales[i].getDisplayVariant(locales[0]));
          locE.appendChild(variantE);

          localesE.appendChild(locE);
        }

        doc.appendChild(localesE);
        XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
        xslt.setXML(doc);
        xslt.setXSL(sslUri, runtimeData.getBrowserInfo());
        xslt.setTarget(out);
        xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
        xslt.transform();
    }

}
