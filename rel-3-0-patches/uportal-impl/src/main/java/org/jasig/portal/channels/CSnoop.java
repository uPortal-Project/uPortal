/* Copyright 2001, 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
  private ChannelRuntimeData runtimeData;

  private static final String sslLocation = "CSnoop/CSnoop.ssl";
  private static final String bundleLocation = "/org/jasig/portal/channels/CSnoop/CSnoop";

  /**
   * No-argument constructor for CSnoop.
   */
  public CSnoop () {
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

    addInfo(requestInfoE, "request-protocol", request.getProtocol());
    addInfo(requestInfoE, "request-method", request.getMethod());
    addInfo(requestInfoE, "server-name", request.getServerName());
    addInfo(requestInfoE, "server-port", String.valueOf(request.getServerPort()));
    addInfo(requestInfoE, "request-uri", request.getRequestURI());
    addInfo(requestInfoE, "context-path", request.getContextPath());
    addInfo(requestInfoE, "servlet-path", request.getServletPath());
    addInfo(requestInfoE, "query-string", request.getQueryString());
    addInfo(requestInfoE, "path-info", request.getPathInfo());
    addInfo(requestInfoE, "path-translated", request.getPathTranslated());
    addInfo(requestInfoE, "content-length", String.valueOf(request.getContentLength()));
    addInfo(requestInfoE, "content-type", request.getContentType());
    addInfo(requestInfoE, "remote-user", request.getRemoteUser());
    addInfo(requestInfoE, "remote-address", request.getRemoteAddr());
    addInfo(requestInfoE, "remote-host", request.getRemoteHost());
    addInfo(requestInfoE, "authorization-scheme", request.getAuthType());
    addInfo(requestInfoE, "locale", request.getLocale().toString());
    
    // <headers>
    Element headersE = doc.createElement("headers");
    java.util.Enumeration enum1 = request.getHeaderNames();
    while (enum1.hasMoreElements()) {
      String name = (String)enum1.nextElement();
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

  /**
   * Adds a text node with the given name and value. If the value is null then no text node
   * is added to the new node, but the new node is still added to parentElement
   * @param parentElement parent of the node to be added
   * @param name name of the node to add
   * @param value String value of the node to add
   */
  private void addInfo(Element parentElement, String name, String value) {
	  Document doc = parentElement.getOwnerDocument();
	  Element e = doc.createElement(name);
	  if (value != null){
		  e.appendChild(doc.createTextNode(value));
}
	  parentElement.appendChild(e);
  }
}

