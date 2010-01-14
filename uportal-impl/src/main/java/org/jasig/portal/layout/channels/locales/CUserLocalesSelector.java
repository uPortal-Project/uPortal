/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
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
