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

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.utils.XMLEscaper;
import org.jasig.portal.utils.XSLT;
import org.xml.sax.ContentHandler;

/**
 * <p>A channel which displays HTTP request and HTML header info.
 * This channel implements IPrivilegedChannel rather than
 * IChannel because it needs access to the HttpServletRequest object.</p>
 * <p>This channel was partially developed at Columbia University
 * as an exercise.</p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CSnoop implements IPrivilegedChannel
{
  private PortalControlStructures pcs;
  private ChannelStaticData staticData;
  private ChannelRuntimeData runtimeData;

  private static final String sslLocation = "CSnoop/CSnoop.ssl";

  /**
   * No-argument constructor for CSnoop.
   */
  public CSnoop ()
  {
    this.staticData = new ChannelStaticData ();
    this.runtimeData = new ChannelRuntimeData ();
  }

  /**
   * Sends portal control structures to the portal, i.e. HttpServletRequest,
   * HttpServletResponse, UserPreferencesManager, etc.
   * @param pcs the portal control structures
   */
  public void setPortalControlStructures(PortalControlStructures pcs)
  {
    this.pcs = pcs;
  }

  /**
   * Returns channel runtime properties.
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
  }

  /**
   * Receives channel runtime data from the portal and processes actions
   * passed to it.  The names of these parameters are entirely up to the channel.
   * @param rd handle to channel runtime data
   */
  public void setRuntimeData (ChannelRuntimeData rd)
  {
    this.runtimeData = rd;
  }

  /**
   * Output channel content to the portal
   * @param out a sax document handler
   */
  public void renderXML (ContentHandler out) throws PortalException
  {
    HttpServletRequest request = pcs.getHttpServletRequest();
    StringBuffer sb = new StringBuffer();
    sb.append("<?xml version='1.0'?>");

    sb.append("<request-info>");
    sb.append("  <request-protocol>").append(XMLEscaper.escape(request.getProtocol())).append("</request-protocol>");
    sb.append("  <request-method>").append(XMLEscaper.escape(request.getMethod())).append("</request-method>");
    sb.append("  <server-name>").append(XMLEscaper.escape(request.getServerName())).append("</server-name>");
    sb.append("  <server-port>").append(XMLEscaper.escape(String.valueOf(request.getServerPort()))).append("</server-port>");
    sb.append("  <request-uri>").append(XMLEscaper.escape(request.getRequestURI())).append("</request-uri>");
    sb.append("  <context-path>").append(XMLEscaper.escape(request.getContextPath())).append("</context-path>");
    sb.append("  <servlet-path>").append(XMLEscaper.escape(request.getServletPath())).append("</servlet-path>");
    sb.append("  <query-string>").append(XMLEscaper.escape(request.getQueryString())).append("</query-string>");
    sb.append("  <path-info>").append(XMLEscaper.escape(request.getPathInfo())).append("</path-info> ");
    sb.append("  <path-translated>").append(XMLEscaper.escape(request.getPathTranslated())).append("</path-translated>");
    sb.append("  <content-length>").append(XMLEscaper.escape(String.valueOf(request.getContentLength()))).append("</content-length>");
    sb.append("  <content-type>").append(XMLEscaper.escape(request.getContentType())).append("</content-type>");
    sb.append("  <remote-user>").append(XMLEscaper.escape(request.getRemoteUser())).append("</remote-user>");
    sb.append("  <remote-address>").append(XMLEscaper.escape(request.getRemoteAddr())).append("</remote-address>");
    sb.append("  <remote-host>").append(XMLEscaper.escape(request.getRemoteHost())).append("</remote-host>");
    sb.append("  <authorization-scheme>").append(XMLEscaper.escape(request.getAuthType())).append("</authorization-scheme>");
    sb.append("  <locale>").append(XMLEscaper.escape(request.getLocale().toString())).append("</locale>");

    sb.append("  <headers>");

    java.util.Enumeration enum = request.getHeaderNames();

    while (enum.hasMoreElements())
    {
      String name = (String)enum.nextElement();
      String value = request.getHeader(name);
      sb.append("<header name=\"");
      sb.append(XMLEscaper.escape(name));
      sb.append("\">");
      sb.append(XMLEscaper.escape(value));
      sb.append("</header>");
    }

    sb.append("  </headers>");
    sb.append("</request-info>");

    XSLT xslt = new XSLT(this);
    xslt.setXML(sb.toString());
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.transform();
  }
}
