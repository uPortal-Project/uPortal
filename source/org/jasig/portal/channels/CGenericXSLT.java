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

import org.xml.sax.DocumentHandler;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.MalformedURLException;
import org.jasig.portal.*;
import org.jasig.portal.utils.XSLT;

/**
 * <p>A channel which transforms XML for rendering in the portal.</p>
 *
 * <p>Static channel parameters to be supplied:
 *
 *  1) "xmlUri" - a URI representing the source XML document
 *  2) "sslUri" - a URI representing the corresponding .ssl (stylesheet list) file
 *  3) "xslTitle" - a title representing the stylesheet (optional)
 *                  <i>If no title parameter is specified, a default
 *                  stylesheet will be chosen according to the media</i>
 *  4) "xslUri" - a URI representing the stylesheet to use
 *                  <i>If <code>xslUri</code> is supplied, <code>sslUri</code>
 *                  and <code>xslTitle</code> will be ignored.
 * </p>
 * <p>The static parameters above can be overridden by including
 * parameters of the same name (<code>xmlUri</code>, <code>sslUri</code>,
 * <code>xslTitle</code> and/or <code>xslUri</code> in the HttpRequest string.</p>
 * <p>This channel can be used for all XML formats including RSS.
 * Any other parameters passed to this channel via HttpRequest will get
 * passed in turn to the XSLT stylesheet as stylesheet parameters. They can be
 * read in the stylesheet as follows:
 * <code>&lt;xsl:param name="yourParamName"&gt;aDefaultValue&lt;/xsl:param&gt;</code></p>
 * @author Steve Toth, stoth@interactivebusiness.com
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CGenericXSLT implements org.jasig.portal.IChannel
{
  protected String xmlUri;
  protected String sslUri;
  protected String xslTitle;
  protected String xslUri;
  protected ChannelRuntimeData runtimeData;
  protected String media;

  protected static String fs = File.separator;
  protected static String stylesheetDir = GenericPortalBean.getPortalBaseDir () + "webpages" + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "CGenericXSLT" + fs;
  protected static String sMediaProps = GenericPortalBean.getPortalBaseDir () + "properties" + fs + "media.properties";

  public CGenericXSLT ()
  {
  }

  // Get channel parameters.
  public void setStaticData (ChannelStaticData sd)
  {
    try
    {
      this.xmlUri = sd.getParameter ("xml");
      this.sslUri = sd.getParameter ("ssl");
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }

  public void setRuntimeData (ChannelRuntimeData rd)
  {
    runtimeData = rd;

    String xmlUri = runtimeData.getParameter("xmlUri");

    if (xmlUri != null)
       this.xmlUri = xmlUri;

    String sslUri = runtimeData.getParameter("sslUri");

    if (sslUri != null)
       this.sslUri = sslUri;

    String xslTitle = runtimeData.getParameter("xslTitle");

    if (xslTitle != null)
       this.xslTitle = xslTitle;

    String xslUri = runtimeData.getParameter("xslUri");

    if (xslUri != null)
       this.xslUri = xslUri;

    // The media will soon be passed to the channel I think.
    // This code can then be replaced with runtimeData.getMedia()
    MediaManager mm = new MediaManager();
    mm.setMediaProps(UtilitiesBean.getPortalBaseDir() + "properties" + fs + "media.properties");
    media = mm.getMedia(runtimeData.getHttpRequest());
  }

  public void receiveEvent (LayoutEvent ev)
  {
    // No events to process here
  }

  // Access channel runtime properties.
  public ChannelRuntimeProperties getRuntimeProperties ()
  {
    return new ChannelRuntimeProperties ();
  }

  // Set some channel properties.
  public ChannelSubscriptionProperties getSubscriptionProperties ()
  {
    ChannelSubscriptionProperties csb = new ChannelSubscriptionProperties ();
    csb.setDefaultDetachWidth ("550");
    csb.setDefaultDetachHeight ("450");
    return csb;
  }

  public void renderXML (DocumentHandler out) throws PortalException
  {
    String xml;

    try
    {
      xml = UtilitiesBean.getContentsAsString(xmlUri);
    }
    catch (Exception e)
    {
      throw new ResourceMissingException (xmlUri, "", e.getMessage());
    }

    runtimeData.put("baseActionURL", runtimeData.getBaseActionURL());

    try
    {
      if (xslUri != null)
        XSLT.transform(out, xml, xslUri, runtimeData);
      else
      {
        if (xslTitle != null)
          XSLT.transform(out, media, xml, sslUri, xslTitle, runtimeData);
        else
          XSLT.transform(out, media, xml, sslUri, runtimeData);
      }
    }
    catch (Exception e)
    {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      sw.flush();
      throw new GeneralRenderingException(sw.toString());
    }
  }
}
