/* Copyright 2001, 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.ICacheable;
import org.jasig.portal.PortalException;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/**
 * This channel renders content identified by a URL within an inline browser
 * frame. For Browsers without support for IFRAMEs, the channel
 * just presents a link to open the URL in a separate window.  See
 * <a href="http://www.htmlhelp.com/reference/html40/special/iframe.html">
 * http://www.htmlhelp.com/reference/html40/special/iframe.html</a> for more
 * information on inline frames.
 *
 * @author Susan Bramhall
 * @version $Revision$
 */
public class CInlineFrame extends BaseChannel implements ICacheable {

  private static final String sslLocation = "CInlineFrame/CInlineFrame.ssl";

  /**
   * Build an XML document and transform for display using org.jasig.portal.util.XSLT
   * Creates IFrame or link depending on browser capability.
   * The XML will look something like this:
   *
   * <pre>
   * &lt;iframe&gt;
   *   &lt;url&gt;http://blah.blah.blah&lt;/url&gt;
   *    &lt;height&gt;600&lt;/height&gt;
   * &lt;/iframe&gt;
   * </pre>
   */
  public void renderXML (ContentHandler out) throws PortalException {

    // Obtain url and height, both static parameters
    String srcUrl = staticData.getParameter("url"); // the url for the IFrame content
    String frameHeight = staticData.getParameter("height"); // the height of the IFrame in pixels

    Document doc = null;
    try {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException pce) {
      log.error("Error getting Document", pce);
      throw new GeneralRenderingException(pce);
    }

    // Create XML doc
    Element iframeE = doc.createElement("iframe");
    Element urlE = doc.createElement("url");
    urlE.appendChild(doc.createTextNode(srcUrl));
    iframeE.appendChild(urlE);
    Element heightE = doc.createElement("height");
    heightE.appendChild(doc.createTextNode(frameHeight));
    iframeE.appendChild(heightE);
    doc.appendChild(iframeE);

    XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
    xslt.setXML(doc);
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.transform();
  }

  // IMultithreadedCachable methods...

  public ChannelCacheKey generateKey() {
    ChannelCacheKey key = new ChannelCacheKey();
    key.setKey(getKey());
    key.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
    key.setKeyValidity(null);
    return key;
  }

  public boolean isCacheValid(Object validity) {
    return true;
  }

  private String getKey() {
    StringBuffer sbKey = new StringBuffer(1024);
    sbKey.append("org.jasig.portal.channels.CInlineFrame").append(": ");
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