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

import  org.jasig.portal.*;
import  java.io.IOException;
import  java.util.Hashtable;
import  java.net.URL;
import  java.net.MalformedURLException;
import  org.xml.sax.DocumentHandler;
import  org.xml.sax.SAXException;
import  org.jasig.portal.utils.XSLT;
import  org.jasig.portal.services.LogService;


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
public class CInlineFrame extends BaseChannel {
  protected String srcUrl;      // the url for the IFrame content
  protected String frameHeight; // the height of the IFrame in pixels
  
  private static final String sslLocation = UtilitiesBean.fixURI("webpages/stylesheets/org/jasig/portal/channels/CInlineFrame/CInlineFrame.ssl");

  /**
   * Get channel parameters: url, height and name
   */
  public void setStaticData (ChannelStaticData sd) {
    this.srcUrl = sd.getParameter("url");
    this.frameHeight = sd.getParameter("height");
  }

  /**
   * Build an XML string and transform for display using org.jasig.portal.util.XSLT
   * Creates IFrame or link depending on browser capability.
   */
  public void renderXML (DocumentHandler out) throws PortalException {   
    StringBuffer sbXML = new StringBuffer("<?xml version=\"1.0\"?>");
    sbXML.append("<iframe>");
    sbXML.append("  <url>").append(srcUrl).append("</url>");
    sbXML.append("  <height>").append(frameHeight).append("</height>");
    sbXML.append("</iframe>");
    
    XSLT xslt = new XSLT();
    xslt.setXML(sbXML.toString());
    xslt.setSSL(sslLocation, getStylesheetTitle(), runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.transform();
  }

  /**
   * Uses the user agent string to determine which stylesheet title to use.
   * We wouldn't need this method if stylesheet sets could distinguish between browser versions
   * @return ssTitle the stylesheet title
   */
  private String getStylesheetTitle () {
    String ssTitle = "noIFrameSupport";
    String userAgent = runtimeData.getBrowserInfo().getUserAgent();
    if ((userAgent.indexOf("MSIE 3") >= 0) || (userAgent.indexOf("MSIE 4") >= 0) || (userAgent.indexOf("MSIE 5") >= 0) || 
        (userAgent.indexOf("Mozilla/5") >= 0)) {
      ssTitle = "IFrameSupport";
    }
    return  ssTitle;
  }
}



