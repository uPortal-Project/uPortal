/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
import org.jasig.portal.i18n.LocaleManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class CApplet extends BaseMultithreadedChannel implements IMultithreadedCacheable {
  private static final String sslLocation = "CApplet/CApplet.ssl";
  private static final Log log = LogFactory.getLog(CApplet.class);

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
      log.error("Error obtaining a Document", pce);
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

    XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
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
    sbKey.append("locales:").append(LocaleManager.stringValueOf(runtimeData.getLocales()));

    return sbKey.toString();
  }
}
