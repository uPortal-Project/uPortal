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

import org.jasig.portal.*;
import org.jasig.portal.utils.XSLT;
import org.apache.xalan.xslt.*;
import org.xml.sax.DocumentHandler;
import java.io.File;
import java.util.Hashtable;
import java.net.URL;

/** <p>A simple channel which renders an image along with an optional
 * caption and subcaption.</p>
 * <p>Channel parameters:</p>
 *   <table>
 *     <tr><th>Name</th><th>Description</th><th>Example</th><th>Required</th></tr>
 *     <tr><td>name</td><td>The name of the channel instance</td><td>Hamilton Library Web Cam</td><td>yes</td></tr>
 *     <tr><td>img-uri</td><td>The URI of the image to display</td><td>http://webcam.its.hawaii.edu/uhmwebcam/image01.jpg</td><td>yes</td></tr>
 *     <tr><td>img-width</td><td>The width of the image to display</td><td>320</td><td>no</td></tr>
 *     <tr><td>img-height</td><td>The height of the image to display</td><td>240</td><td>no</td></tr>
 *     <tr><td>img-border</td><td>The border of the image to display</td><td>0</td><td>no</td></tr>
 *     <tr><td>img-link</td><td>A URI to be used as an href for the image</td><td>http://www.hawaii.edu/visitor/#webcams</td><td>no</td></tr>
 *     <tr><td>caption</td><td>A caption of the image to display</td><td>Almost Live Shot of Hamilton Library Front Entrance</td><td>no</td></tr>
 *     <tr><td>subcaption</td><td>The subcaption of the image to display</td><td>Updated Once per Minute During Daylight Hours</td><td>no</td></tr>
 *   </table>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CImage implements IChannel
{
  private ChannelStaticData staticData = null;
  private ChannelRuntimeData runtimeData = null;
  private String media;

  private static final String fs = File.separator;
  private static final String portalBaseDir = GenericPortalBean.getPortalBaseDir ();
  private String stylesheetDir = portalBaseDir + fs + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "channels" + fs + "CImage";
  private static final String sslLocation = UtilitiesBean.getPortalBaseDir() + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "channels" + fs + "CImage" + fs + "CImage.ssl";

  private String sImageWidth = null;
  private String sImageHeight = null;
  private String sImageBorder = null;
  private String sImageLink = null;
  private String sCaption = null;
  private String sSubCaption = null;
    private MediaManager mm;

  /**
   * Constructs a CImage.
   */
  public CImage ()
  {
      this.mm=new MediaManager();
  }

  /**
   * Returns channel runtime properties
   * @return handle to runtime properties
   */
  public ChannelRuntimeProperties getRuntimeProperties ()
  {
    // Channel will always render, so the default values are ok
    return new ChannelRuntimeProperties ();
  }

  /**
   * Processes layout-level events coming from the portal
   * @param ev a portal layout event
   */
  public void receiveEvent (PortalEvent ev)
  {
    // no events for this channel
  }

  /**
   * Receive static channel data from the portal
   * @param sd static channel data
   */
  public void setStaticData (ChannelStaticData sd)
  {
    this.staticData = sd;
    sImageWidth = sd.getParameter ("img-width");
    sImageHeight = sd.getParameter ("img-height");
    sImageBorder = sd.getParameter ("img-border");
    sImageLink = sd.getParameter ("img-link");
    sCaption = sd.getParameter ("caption");
    sSubCaption = sd.getParameter ("subcaption");
  }


  /**
   * Receives channel runtime data from the portal and processes actions
   * passed to it.  The names of these parameters are entirely up to the channel.
   * @param rd handle to channel runtime data
   */
  public void setRuntimeData (ChannelRuntimeData rd)
  {
    this.runtimeData = rd;
    media = mm.getMedia(runtimeData.getBrowserInfo());
  }

  /**
   * Output channel content to the portal
   * @param out a sax document handler
   */
  public void renderXML (DocumentHandler out)
  {
    try
    {
      StringBuffer sb = new StringBuffer(1024);
      sb.append("<?xml version='1.0'?>\n");
      sb.append("<content>\n");
      sb.append("  <image src=\"" + staticData.getParameter ("img-uri") + "\" ");

      if (exists (sImageWidth))
        sb.append("         width=\"" + sImageWidth + "\" ");

      if (exists (sImageHeight))
        sb.append("         height=\"" + sImageHeight + "\" ");

      if (exists (sImageBorder))
        sb.append("         border=\"" + sImageBorder + "\"");

      if (exists (sImageLink))
        sb.append("         link=\"" + sImageLink + "\"");

      sb.append("  />\n");

      if (exists (sCaption))
        sb.append("  <caption>" + sCaption + "</caption>\n");

      if (exists (sSubCaption))
        sb.append("  <subcaption>" + sSubCaption + "</subcaption>\n");

      sb.append("</content>\n");

      Hashtable params = new Hashtable(1);
      params.put("baseActionURL", runtimeData.getBaseActionURL());

      try
      {
        XSLT.transform(sb.toString(), new URL(UtilitiesBean.fixURI(sslLocation)), out, params, media);
      }
      catch (Exception e)
      {
        throw new GeneralRenderingException(e.getMessage());
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }

  private static boolean exists (String s)
  {
    return (s != null && s.length () > 0);
  }
}
