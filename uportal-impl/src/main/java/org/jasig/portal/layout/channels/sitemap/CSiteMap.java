/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout.channels.sitemap;

import org.jasig.portal.IPrivileged;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;

/**
 * Site map that shows you, at a glance, what channels are in your layout.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class CSiteMap extends BaseChannel implements IPrivileged {

    Document userLayoutDoc = null;
    private static final String sslUri = "sitemap.ssl";
        
    public void setPortalControlStructures(PortalControlStructures pcs)
        throws PortalException {
        IUserLayout userLayout = pcs.getUserPreferencesManager().getUserLayoutManager().getUserLayout();
        userLayoutDoc = DocumentFactory.getNewDocument();
        userLayout.writeTo(userLayoutDoc);
    }

    public void renderXML(ContentHandler out) throws PortalException {
        XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
        xslt.setXML(userLayoutDoc);
        xslt.setXSL(sslUri, runtimeData.getBrowserInfo());
        xslt.setTarget(out);
        xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
        xslt.transform();
    }

}
