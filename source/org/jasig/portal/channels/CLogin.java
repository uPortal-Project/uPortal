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

import org.jasig.portal.*;
import org.jasig.portal.utils.XSLT;
import org.xml.sax.DocumentHandler;
import java.io.File;

///Transform specific...
// Won't be needed if we move the Xalan code outside the channel
import org.apache.xalan.xslt.*;
import java.io.StringReader;

/** <p>Allows a user to logon to the portal.  Logon info is posted to
 *  <code>authenticate.jsp</code></p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CLogin implements IChannel
{
  private ChannelStaticData staticData;
  private ChannelRuntimeData runtimeData;
  private String channelName = "Log in...";
  private String media;
  private static final String fs = File.separator;
  private static final String sslLocation = UtilitiesBean.getPortalBaseDir() + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "channels" + fs + "CLogin" + fs + "CLogin.ssl";

  public CLogin()
  {
  }

  public ChannelSubscriptionProperties getSubscriptionProperties()
  {
    ChannelSubscriptionProperties csb = new ChannelSubscriptionProperties();
    csb.setName(this.channelName);
    return csb;
  }

  public ChannelRuntimeProperties getRuntimeProperties()
  {
    return new ChannelRuntimeProperties();
  }

  public void receiveEvent(LayoutEvent ev)
  {
  }

  public void setStaticData (ChannelStaticData sd)
  {
    this.staticData = sd;
  }

  public void setRuntimeData (ChannelRuntimeData rd)
  {
    this.runtimeData = rd;

    // The media will soon be passed to the channel I think.
    // This code can then be replaced with runtimeData.getMedia()
    MediaManager mm = new MediaManager();
    mm.setMediaProps(UtilitiesBean.getPortalBaseDir() + "properties" + fs + "media.properties");
    media = mm.getMedia(runtimeData.getHttpRequest());
  }

  public void renderXML (DocumentHandler out) throws Exception
  {
    StringBuffer sb = new StringBuffer ("<?xml version='1.0'?>\n");
    sb.append ("<xml/>\n");

    XSLT.transform(out, media, sb.toString(), sslLocation, "login");
  }
}
