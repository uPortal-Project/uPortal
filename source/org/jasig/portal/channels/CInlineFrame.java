/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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
import java.io.*;
import java.util.*;
import org.jasig.portal.*;
import org.xml.sax.DocumentHandler;
import org.jasig.portal.utils.XSLT;

/**
 * This channel renders an InlineFrame with the content provided by a URL.
 * For Browsers without support for Inline Frames the channel just presents
 * a link to open in a separate window.
 *
 * @author Susan Bramhall
 * @version $Revision$
 */
public class CInlineFrame extends BaseChannel
{
  /**
 *  The URL for the IFrame content
 */
  protected String srcUrl = null;
  protected String frameHeight = null;
  protected String channelName = null;

  private static final String fs = File.separator;
  private static final String portalBaseDir = UtilitiesBean.getPortalBaseDir ();
  private static final String stylesheetDir = portalBaseDir + fs + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "channels" + fs + "CInlineFrame";
  private static final String sslLocation = stylesheetDir + fs + "CInlineFrame.ssl";

  private StylesheetSet set = null;
  private String media;

  /**
 * Constructs CInlineFrame
 *  Locate and stylesheet set CInlineFrame.ssl
 */
  public CInlineFrame()
  {
    this.staticData = new ChannelStaticData ();
    this.runtimeData = new ChannelRuntimeData ();
    this.set = new StylesheetSet (stylesheetDir + fs + "CInlineFrame.ssl");
//    this.set.setMediaProps (portalBaseDir + fs + "properties" + fs + "media.properties");
    }

    /**
     * discover browser via mediaManager and save for render time
     */
public void setRuntimeData (ChannelRuntimeData rd)
  {
    this.runtimeData = rd;

    // The media will soon be passed to the channel I think.
    // This code can then be replaced with runtimeData.getMedia()
    MediaManager mm = new MediaManager();
    mm.setMediaProps(portalBaseDir + "properties" + fs + "media.properties");
    media = runtimeData.getMedia();

  }

  /**
   *    Get channel parameters: URL, Height and Name
   */
  public void setStaticData (ChannelStaticData sd)
  {
    try
    {
      this.srcUrl = sd.getParameter ("URL");
      this.frameHeight = sd.getParameter ("Height");
      this.channelName = sd.getParameter ("Name");
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }

/**
 * Build an XML string and transform for display using org.jasig.portal.util.XSLT
 * Create IFrame or link depending on Browser.
 */
      public void renderXML (DocumentHandler out)
  {
    try
    {

    if (set != null) {
      String  sXML = "<IFrame><url>"+srcUrl+"</url><height>"+frameHeight+"</height></IFrame>";
      Hashtable ssParams = new Hashtable();
      ssParams.put("baseActionURL", runtimeData.getBaseActionURL());
      Logger.log(Logger.DEBUG, "sXML is "+sXML);
      XSLT.transform(out, media, sXML, sslLocation, "main", ssParams);

         }
    }
    catch (Exception e)
    {
    Logger.log (Logger.ERROR, e);
    }
  }
}