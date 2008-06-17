/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
 */
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
