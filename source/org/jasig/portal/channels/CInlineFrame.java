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
import org.jasig.portal.i18n.LocaleManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class CInlineFrame extends BaseMultithreadedChannel implements IMultithreadedCacheable {
    private static final Log log = LogFactory.getLog(CInlineFrame.class);
  private static final String sslLocation = "CInlineFrame/CInlineFrame.ssl";

  /**
   * Build an XML document and transform for display using org.jasig.portal.util.XSLT
   * Creates IFrame or link depending on browser capability.
   * The XML will look something like this:
   *
   * <iframe>
   *   <url>http://blah.blah.blah</url>
   *    <height>600</height>
   * </iframe>
   */
  public void renderXML (ContentHandler out, String uid) throws PortalException {
    ChannelState channelState = (ChannelState)channelStateMap.get(uid);
    ChannelStaticData staticData = channelState.getStaticData();
    ChannelRuntimeData runtimeData = channelState.getRuntimeData();

    // Obtain url and height, both static parameters
    String srcUrl = staticData.getParameter("url"); // the url for the IFrame content
    String frameHeight = staticData.getParameter("height"); // the height of the IFrame in pixels

    Document doc = null;
    try {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException pce) {
      log.error( pce);
      throw new GeneralRenderingException(pce.getMessage());
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
    xslt.setXSL(sslLocation, getStylesheetTitle(runtimeData.getBrowserInfo().getUserAgent()), runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.transform();
  }

  /**
   * Uses the user agent string to determine which stylesheet title to use.
   * We wouldn't need this method if stylesheet sets could distinguish between browser versions
   * @param userAgent the user agent string
   * @return ssTitle the stylesheet title
   */
  private String getStylesheetTitle (String userAgent) {
    String ssTitle = "noIFrameSupport";
    if ((userAgent.indexOf("MSIE 3") >= 0) || (userAgent.indexOf("MSIE 4") >= 0) ||
        (userAgent.indexOf("MSIE 5") >= 0) || (userAgent.indexOf("MSIE 6") >= 0) ||
        (userAgent.indexOf("Mozilla/5") >= 0 || (userAgent.indexOf("Opera/6") >= 0))) {
      ssTitle = "IFrameSupport";
    }
    return  ssTitle;
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
    sbKey.append("org.jasig.portal.channels.CInlineFrame").append(": ");
    sbKey.append("xslUri:");
    try {
      String sslUrl = ResourceLoader.getResourceAsURLString(this.getClass(), sslLocation);
      String ssTitle = getStylesheetTitle(runtimeData.getBrowserInfo().getUserAgent());
      sbKey.append(XSLT.getStylesheetURI(sslUrl, ssTitle, runtimeData.getBrowserInfo())).append(", ");
    } catch (PortalException pe) {
      sbKey.append("Not available, ");
    }
    sbKey.append("staticData:").append(staticData.toString());
    sbKey.append("locales:").append(LocaleManager.stringValueOf(runtimeData.getLocales()));
    return sbKey.toString();
  }
}



