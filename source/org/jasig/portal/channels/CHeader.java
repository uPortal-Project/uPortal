/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.channels;

import  java.net.URL;
import  java.util.Hashtable;
import  javax.naming.InitialContext;
import  javax.naming.Context;
import  javax.naming.NamingException;
import org.jasig.portal.MediaManager;
import  org.jasig.portal.UtilitiesBean;
import  org.jasig.portal.Logger;
import  org.jasig.portal.utils.XSLT;
import  org.jasig.portal.PortalException;
import  org.jasig.portal.GeneralRenderingException;
import  org.xml.sax.DocumentHandler;
import  org.w3c.dom.Document;
import  org.w3c.dom.Element;


/**
 * This channel provides content for a page header.  It is indended
 * to be included in a layout folder of type "header".  Most stylesheets
 * will render the content of such header channels consistently on every
 * page.
 * @author Peter Kharchenko
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision 1.1$
 */
public class CHeader extends BaseChannel {
  private static final String sslUri = UtilitiesBean.fixURI("webpages/stylesheets/org/jasig/portal/channels/CHeader/CHeader.ssl");
    private MediaManager mm=new MediaManager();

  /**
   * put your documentation comment here
   * @param out
   * @exception PortalException
   */
  public void renderXML (DocumentHandler out) throws PortalException {
    String fullName = (String)staticData.getPerson().getFullName();
    Document doc = new org.apache.xerces.dom.DocumentImpl();
    // Create <header> element
    Element headerEl = doc.createElement("header");
    // Create <full-name> element under <header>
    Element fullNameEl = doc.createElement("full-name");
    fullNameEl.appendChild(doc.createTextNode(fullName));
    headerEl.appendChild(fullNameEl);
    // Create <timestamp-long> element under <header>
    Element timeStampLongEl = doc.createElement("timestamp-long");
    timeStampLongEl.appendChild(doc.createTextNode(UtilitiesBean.getDate("EEEE, MMM d, yyyy 'at' hh:mm a")));
    headerEl.appendChild(timeStampLongEl);
    // Create <timestamp-short> element under <header>
    Element timeStampShortEl = doc.createElement("timestamp-short");
    timeStampShortEl.appendChild(doc.createTextNode(UtilitiesBean.getDate("M.d.y h:mm a")));
    headerEl.appendChild(timeStampShortEl);
    if (fullName != null && !fullName.equals("Guest")) {
      Context globalIDContext = null;
      try {
        // Get the context that holds the global IDs for this user
        globalIDContext = (Context)staticData.getPortalContext().lookup("/users/" + staticData.getPerson().getID() + "/channel-ids");
        // Create <timestamp-short> element under <header>
        Element publishChanidEl = doc.createElement("publish-chanid");
        publishChanidEl.appendChild(doc.createTextNode((String)globalIDContext.lookup("/portal/publish/general")));
        headerEl.appendChild(publishChanidEl);
        // Create <timestamp-short> element under <header>
        Element subscribeChanidEl = doc.createElement("subscribe-chanid");
        subscribeChanidEl.appendChild(doc.createTextNode((String)globalIDContext.lookup("/portal/subscribe/general")));
        headerEl.appendChild(subscribeChanidEl);
        // Create <timestamp-short> element under <header>
        Element preferencesChanidEl = doc.createElement("preferences-chanid");
        preferencesChanidEl.appendChild(doc.createTextNode((String)globalIDContext.lookup("/portal/userpreferences/general")));
        headerEl.appendChild(preferencesChanidEl);
      } catch (NamingException e) {
        Logger.log(Logger.ERROR, e);
      }
    }
    doc.appendChild(headerEl);
    // Set up stylesheet parameters: "baseActionURL" and "guest"
    Hashtable ssParams = new Hashtable(2);
    ssParams.put("baseActionURL", runtimeData.getBaseActionURL());
    if (fullName != null && fullName.equals("Guest")) {
      ssParams.put("guest", "true");
    }
    try {
      XSLT.transform(doc, new URL(sslUri), out, ssParams, mm.getMedia(runtimeData.getBrowserInfo()));
    } catch (Exception e) {
      throw  new GeneralRenderingException(e.getMessage());
    }
  }
}



