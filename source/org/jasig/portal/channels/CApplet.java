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
 */

package org.jasig.portal.channels;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.IMultithreadedCacheable;
import org.jasig.portal.PortalException;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/** <p>Displays an applet. To pass in applet parameters, construct
 * channel parameters whose keys start with the string "APPLET."</p>
 * <p>For example, the key/value pair
 * <code>APPLET.data=foo</code>
 * as a channel parameter is translated to an applet parameter as
 * <code>data=foo</code></p>
 * <p><i>This code was adapted from uPortal 1.0's
 * <code>org.jasig.portal.channels.CApplet</code></i></p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CApplet extends BaseMultithreadedChannel implements IMultithreadedCacheable {
  private static final String sslLocation = "CApplet/CApplet.ssl";

  /**
   * Output channel content to the portal
   * @param out a sax document handler
   * @param uid a unique ID used to identify the state of the channel
   */
  public void renderXML (ContentHandler out, String uid) throws PortalException {
    ChannelState channelState = (ChannelState)channelStateMap.get(uid);
    ChannelStaticData staticData = channelState.getStaticData();
    ChannelRuntimeData runtimeData = channelState.getRuntimeData();

    Document doc = null;
    try {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException pce) {
      LogService.log(LogService.ERROR, pce);
      throw new GeneralRenderingException(pce.getMessage());
    }

    // Create XML doc
    Element appletE = doc.createElement("applet");
    appletE.setAttribute("code", staticData.getParameter("code"));
    appletE.setAttribute("codebase", staticData.getParameter("codeBase"));
    appletE.setAttribute("width", staticData.getParameter("width"));
    appletE.setAttribute("height", staticData.getParameter("height"));
    appletE.setAttribute("align", "top");
    appletE.setAttribute("border", "0");
    appletE.setAttribute("archive", staticData.getParameter("archive"));

    // Take all parameters whose names start with "APPLET." and pass them
    // to the applet (after stripping "APPLET.")
    java.util.Enumeration allKeys = staticData.keys ();
    while (allKeys.hasMoreElements()) {
      String p = (String)allKeys.nextElement();
      if (p.startsWith ("APPLET.")) {
        Element paramE = doc.createElement("param");
        paramE.setAttribute("name", p.substring(7) /*skip "APPLET."*/);
        paramE.setAttribute("value", (String)staticData.getParameter(p));
        appletE.appendChild(paramE);
      }
    }

    doc.appendChild(appletE);

    XSLT xslt = new XSLT(this);
    xslt.setXML(doc);
    xslt.setXSL(sslLocation, "main", runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.transform();
  }

  // IMultithreadedCachable methods...

  public ChannelCacheKey generateKey(String uid) {
    ChannelCacheKey key = new ChannelCacheKey();
    key.setKey(getKey(uid));
    key.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
    key.setKeyValidity(null);
    return key;
  }

  public boolean isCacheValid(Object validity, String uid) {
    return true;
  }

  private String getKey(String uid) {
    ChannelState channelState = (ChannelState)channelStateMap.get(uid);
    ChannelStaticData staticData = channelState.getStaticData();
    ChannelRuntimeData runtimeData = channelState.getRuntimeData();

    StringBuffer sbKey = new StringBuffer(1024);
    sbKey.append("org.jasig.portal.channels.CApplet").append(": ");
    sbKey.append("xslUri:");
    try {
      String sslUrl = ResourceLoader.getResourceAsURLString(this.getClass(), sslLocation);
      sbKey.append(XSLT.getStylesheetURI(sslUrl, runtimeData.getBrowserInfo())).append(", ");
    } catch (PortalException pe) {
      sbKey.append("Not available, ");
    }
    sbKey.append("staticData:").append(staticData.toString());

    return sbKey.toString();
  }
}
