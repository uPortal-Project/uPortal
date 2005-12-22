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

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/**
 * <p>A channel which displays HTTP request and HTML header info.
 * This channel implements IPrivilegedChannel rather than
 * IChannel because it needs access to the HttpServletRequest object.</p>
 * <p>This channel was partially developed at Columbia University
 * as an exercise.</p>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class CSnoop implements IPrivilegedChannel {
  private PortalControlStructures pcs;
  private ChannelStaticData staticData;
  private ChannelRuntimeData runtimeData;

  private static final String sslLocation = "CSnoop/CSnoop.ssl";
  private static final String bundleLocation = "/org/jasig/portal/channels/CSnoop/CSnoop";

  /**
   * No-argument constructor for CSnoop.
   */
  public CSnoop () {
    this.staticData = new ChannelStaticData ();
    this.runtimeData = new ChannelRuntimeData ();
  }

  /**
   * Sends portal control structures to the portal, i.e. HttpServletRequest,
   * HttpServletResponse, UserPreferencesManager, etc.
   * @param pcs the portal control structures
   */
  public void setPortalControlStructures(PortalControlStructures pcs) {
    this.pcs = pcs;
  }

  /**
   * Returns channel runtime properties.
   * @return handle to runtime properties
   */
  public ChannelRuntimeProperties getRuntimeProperties () {
    // Channel will always render, so the default values are ok
    return new ChannelRuntimeProperties ();
  }

  /**
   * Processes layout-level events coming from the portal
   * @param ev a portal layout event
   */
  public void receiveEvent (PortalEvent ev) {
    // no events for this channel
  }

  /**
   * Receive static channel data from the portal
   * @param sd static channel data
   */
  public void setStaticData (ChannelStaticData sd) {
    this.staticData = sd;
  }

  /**
   * Receives channel runtime data from the portal and processes actions
   * passed to it.  The names of these parameters are entirely up to the channel.
   * @param rd handle to channel runtime data
   */
  public void setRuntimeData (ChannelRuntimeData rd) {
    this.runtimeData = rd;
  }

  /**
   * Output channel content to the portal
   * @param out a sax document handler
   */
  public void renderXML (ContentHandler out) throws PortalException {
    HttpServletRequest request = pcs.getHttpServletRequest();
    Document doc = DocumentFactory.getNewDocument();

    // <snooper>
    Element snooperE = doc.createElement("snooper");
    
    // <request-info>
    Element requestInfoE = doc.createElement("request-info");

    // <request-protocol>
    Element requestProtocolE = doc.createElement("request-protocol");
    requestProtocolE.appendChild(doc.createTextNode(request.getProtocol()));
    requestInfoE.appendChild(requestProtocolE);
    
    // <request-method>
    Element requestMethodE = doc.createElement("request-method");
    requestMethodE.appendChild(doc.createTextNode(request.getMethod()));
    requestInfoE.appendChild(requestMethodE);

    // <server-name>
    Element serverNameE = doc.createElement("server-name");
    serverNameE.appendChild(doc.createTextNode(request.getServerName()));
    requestInfoE.appendChild(serverNameE);

    // <server-port>
    Element serverPortE = doc.createElement("server-port");
    serverPortE.appendChild(doc.createTextNode(String.valueOf(request.getServerPort())));
    requestInfoE.appendChild(serverPortE);

    // <request-uri>
    Element requestUriE = doc.createElement("request-uri");
    requestUriE.appendChild(doc.createTextNode(request.getRequestURI()));
    requestInfoE.appendChild(requestUriE);

    // <context-path>
    Element contextPathE = doc.createElement("context-path");
    contextPathE.appendChild(doc.createTextNode(request.getContextPath()));
    requestInfoE.appendChild(contextPathE);

    // <servlet-path>
    Element servletPathE = doc.createElement("servlet-path");
    servletPathE.appendChild(doc.createTextNode(request.getServletPath()));
    requestInfoE.appendChild(servletPathE);
    
    // <query-string>
    Element queryStringE = doc.createElement("query-string");
    queryStringE.appendChild(doc.createTextNode(request.getQueryString()));
    requestInfoE.appendChild(queryStringE);

    // <path-info>
    Element pathInfoE = doc.createElement("path-info");
    pathInfoE.appendChild(doc.createTextNode(request.getPathInfo()));
    requestInfoE.appendChild(pathInfoE);

    // <path-translated>
    Element pathTranslatedE = doc.createElement("path-translated");
    pathTranslatedE.appendChild(doc.createTextNode(request.getPathTranslated()));
    requestInfoE.appendChild(pathTranslatedE);

    // <content-length>
    Element contentLengthE = doc.createElement("content-length");
    contentLengthE.appendChild(doc.createTextNode(String.valueOf(request.getContentLength())));
    requestInfoE.appendChild(contentLengthE);

    // <content-type>
    Element contentTypeE = doc.createElement("content-type");
    contentTypeE.appendChild(doc.createTextNode(request.getContentType()));
    requestInfoE.appendChild(contentTypeE);

    // <remote-user>
    Element remoteUserE = doc.createElement("remote-user");
    remoteUserE.appendChild(doc.createTextNode(request.getRemoteUser()));
    requestInfoE.appendChild(remoteUserE);

    // <remote-address>
    Element remoteAddressE = doc.createElement("remote-address");
    remoteAddressE.appendChild(doc.createTextNode(request.getRemoteAddr()));
    requestInfoE.appendChild(remoteAddressE);

    // <remote-host>
    Element remoteHostE = doc.createElement("remote-host");
    remoteHostE.appendChild(doc.createTextNode(request.getRemoteHost()));
    requestInfoE.appendChild(remoteHostE);

    // <authorization-scheme>
    Element authorizationSchemeE = doc.createElement("authorization-scheme");
    authorizationSchemeE.appendChild(doc.createTextNode(request.getAuthType()));
    requestInfoE.appendChild(authorizationSchemeE);

    // <locale>
    Element localeE = doc.createElement("locale");
    localeE.appendChild(doc.createTextNode(request.getLocale().toString()));
    requestInfoE.appendChild(localeE);
    
    // <headers>
    Element headersE = doc.createElement("headers");
    java.util.Enumeration enumeration = request.getHeaderNames();
    while (enumeration.hasMoreElements()) {
      String name = (String)enumeration.nextElement();
      String value = request.getHeader(name);
      Element headerE = doc.createElement("header");
      headerE.setAttribute("name", name);
      headerE.appendChild(doc.createTextNode(value));
      headersE.appendChild(headerE);
    }    
    requestInfoE.appendChild(headersE);
    snooperE.appendChild(requestInfoE);

    // <channel-runtime-data>
    Element channelRuntimeDataE = doc.createElement("channel-runtime-data");
    
    // <locales>
    Locale[] locales = runtimeData.getLocales();
    if (locales == null) { // Take this out if locales are guaranteed
        locales = new Locale[] { Locale.getDefault() };
    }
    Element localesE = LocaleManager.xmlValueOf(locales).getDocumentElement();
    channelRuntimeDataE.appendChild(doc.importNode(localesE, true));
    
    snooperE.appendChild(channelRuntimeDataE);
    
    doc.appendChild(snooperE);

    ResourceBundle l18n = ResourceBundle.getBundle(bundleLocation,runtimeData.getLocales()[0]);
    // Now perform the transformation
    XSLT xslt = XSLT.getTransformer(this);
    xslt.setResourceBundle(l18n);
    xslt.setXML(doc);
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.transform();
  }
}

