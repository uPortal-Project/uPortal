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
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/** <p>A simple channel which renders an image along with an optional
 * caption and subcaption.</p>
 * <p>Channel parameters:</p>
 *   <table>
 *     <tr><th>Name</th><th>Description</th><th>Example</th><th>Required</th></tr>
 *     <tr><td>img-uri</td><td>The URI of the image to display</td><td>http://webcam.its.hawaii.edu/uhmwebcam/image01.jpg</td><td>yes</td></tr>
 *     <tr><td>img-width</td><td>The width of the image to display</td><td>320</td><td>no</td></tr>
 *     <tr><td>img-height</td><td>The height of the image to display</td><td>240</td><td>no</td></tr>
 *     <tr><td>img-border</td><td>The border of the image to display</td><td>0</td><td>no</td></tr>
 *     <tr><td>img-link</td><td>A URI to be used as an href for the image</td><td>http://www.hawaii.edu/visitor/#webcams</td><td>no</td></tr>
 *     <tr><td>caption</td><td>A caption of the image to display</td><td>Almost Live Shot of Hamilton Library Front Entrance</td><td>no</td></tr>
 *     <tr><td>subcaption</td><td>The subcaption of the image to display</td><td>Updated Once per Minute During Daylight Hours</td><td>no</td></tr>
 *   </table>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class CImage extends BaseMultithreadedChannel implements IMultithreadedCacheable
{
  private static final String sslLocation = "CImage/CImage.ssl";

  /**
   * Output channel content to the portal
   * @param out a sax content handler
   * @param uid a unique ID used to identify the state of the channel
   * @throws org.jasig.portal.PortalException
   */
  public void renderXML (ContentHandler out, String uid) throws PortalException
  {
    ChannelState channelState = (ChannelState)channelStateMap.get(uid);
    ChannelStaticData staticData = channelState.getStaticData();
    ChannelRuntimeData runtimeData = channelState.getRuntimeData();

    // Get the static data
    String sImageUri = staticData.getParameter ("img-uri");
    String sImageWidth = staticData.getParameter ("img-width");
    String sImageHeight = staticData.getParameter ("img-height");
    String sImageBorder = staticData.getParameter ("img-border");
    String sImageLink = staticData.getParameter ("img-link");
    String sCaption = staticData.getParameter ("caption");
    String sSubCaption = staticData.getParameter ("subcaption");

    Document doc = null;
    try {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException pce) {
      LogService.log(LogService.ERROR, pce);
      throw new GeneralRenderingException(pce.getMessage());
    }

    // Create XML doc
    Element contentE = doc.createElement("content");

    // Add image tag src, width, height, border, and link
    Element imageE = doc.createElement("image");
    imageE.setAttribute("src", sImageUri);
    if (exists(sImageWidth))
      imageE.setAttribute("width", sImageWidth);
    if (exists(sImageWidth))
      imageE.setAttribute("height", sImageHeight);
    if (exists(sImageWidth))
      imageE.setAttribute("border", sImageBorder);
    if (exists(sImageWidth))
      imageE.setAttribute("link", sImageLink);
    contentE.appendChild(imageE);

    // Add a caption if it is specified
    if (exists(sCaption)) {
      Element captionE = doc.createElement("caption");
      captionE.appendChild(doc.createTextNode(sCaption));
      contentE.appendChild(captionE);
    }

    // Add a subcaption if it is specified
    if (exists(sSubCaption)) {
      Element subcaptionE = doc.createElement("subcaption");
      subcaptionE.appendChild(doc.createTextNode(sSubCaption));
      contentE.appendChild(subcaptionE);
    }

    doc.appendChild(contentE);

    XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
    xslt.setXML(doc);
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
    xslt.transform();
  }

  private static boolean exists (String s)
  {
    return (s != null && s.length () > 0);
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
    sbKey.append("org.jasig.portal.channels.CImage").append(": ");
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
