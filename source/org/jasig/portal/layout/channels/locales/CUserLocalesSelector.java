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
import org.jasig.portal.utils.XSLT;
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
                runtimeData.setLocales(lm.getLocales());
            } catch (Exception e) {
                throw new PortalException(e);
            }
        }
    }

    public void renderXML(ContentHandler out) throws PortalException {
        Locale[] locales = runtimeData.getLocales();
        XSLT xslt = XSLT.getTransformer(this, locales);
        xslt.setXML(LocaleManager.xmlValueOf(locales, userLocale));
        xslt.setXSL(sslUri, runtimeData.getBrowserInfo());
        xslt.setTarget(out);
        xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
        xslt.transform();
    }

}
