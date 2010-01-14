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

import org.jasig.portal.Constants;
import org.jasig.portal.PortalException;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;

/**
 * Changes the locale preferences for the current session.
 * Any changes made via this channel will not be persisted
 * between sessions.
 * @author Shoji Kajita <a href="mailto:">kajita@itc.nagoya-u.ac.jp</a>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class CSessionLocalesSelector extends BaseChannel {
    
    protected final String sslUri = "sessionLocales.ssl";
    
    public void renderXML(ContentHandler out) throws PortalException {
            Locale[] locales = runtimeData.getLocales();
            Document doc = LocaleManager.xmlValueOf(locales, locales[0]);
            XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
            xslt.setXML(doc);
            xslt.setXSL(sslUri, runtimeData.getBrowserInfo());
            xslt.setTarget(out);
            xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
            xslt.setStylesheetParameter("localesParam", Constants.LOCALES_PARAM);
            xslt.transform();
    }

}
