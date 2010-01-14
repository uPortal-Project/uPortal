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
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
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
