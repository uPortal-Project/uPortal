/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

import javax.servlet.http.HttpSession;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/**
 * <p>Allows a user to login to the portal.  Login info is posted to
 * <code>LoginServlet</code>.  If user enters incorrect username and
 * password, he/she is instructed to login again with a different
 * password (the username of the previous attempt is preserved).</p>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class CLogin implements IPrivilegedChannel, ICacheable
{
  private ChannelStaticData staticData;
  private ChannelRuntimeData runtimeData;
  private String channelName = "Log in...";
  private String attemptedUserName="";
  private static final String sslLocation = "CLogin/CLogin.ssl";
  private boolean bAuthenticated = false;
  private boolean bauthenticationAttemptFailed = false;
  private boolean bSecurityError = false;
  private String xslUriForKey = null;

  private static final String systemCacheId="org.jasig.portal.CLogin:";

  private ISecurityContext ic;

  public CLogin()
  {
  }

  public void setPortalControlStructures(PortalControlStructures pcs)
  {
    HttpSession session = pcs.getHttpSession();
    String authenticationAttempted = (String)session.getAttribute("up_authenticationAttempted");
    String authenticationError = (String)session.getAttribute("up_authenticationError");
    attemptedUserName = (String)session.getAttribute("up_attemptedUserName");

    if (authenticationAttempted != null)
      bauthenticationAttemptFailed = true;

    if (authenticationError != null)
      bSecurityError = true;
  }

  public ChannelRuntimeProperties getRuntimeProperties()
  {
    return new ChannelRuntimeProperties();
  }

  public void receiveEvent(PortalEvent ev)
  {
  }

  public void setStaticData (ChannelStaticData sd)
  {
    this.staticData = sd;
    ic = staticData.getPerson().getSecurityContext();

    if (ic!=null && ic.isAuthenticated())
      bAuthenticated = true;
  }

  public void setRuntimeData (ChannelRuntimeData rd)
  {
    this.runtimeData = rd;
  }

  public void renderXML (ContentHandler out) throws PortalException
  {
    String fullName = (String)staticData.getPerson().getFullName();
    Document doc = DocumentFactory.getNewDocument();

    // Create <login-status> element
    Element loginStatusElement = doc.createElement("login-status");

    if (bSecurityError)
    {
      // Create <error> element under <login-status>
      Element errorElement = doc.createElement("error");
      loginStatusElement.appendChild(errorElement);
    }
    else if (bauthenticationAttemptFailed && !bAuthenticated)
    {
      // Create <failure> element under <login-status>
      Element failureElement = doc.createElement("failure");
      failureElement.setAttribute("attemptedUserName", attemptedUserName);
      loginStatusElement.appendChild(failureElement);
    }
    else if (fullName != null)
    {
      // Create <full-name> element under <header>
      Element fullNameElement = doc.createElement("full-name");
      fullNameElement.appendChild(doc.createTextNode(fullName));
      loginStatusElement.appendChild(fullNameElement);
    }

    doc.appendChild(loginStatusElement);

    XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
    xslt.setXML(doc);
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
    if (!staticData.getPerson().getSecurityContext().isAuthenticated()) {
      xslt.setStylesheetParameter("unauthenticated", "true");
    }
    xslt.transform();
  }

  public ChannelCacheKey generateKey() {
    ChannelCacheKey k = new ChannelCacheKey();
    StringBuffer sbKey = new StringBuffer(1024);
    // guest pages are cached system-wide
    if(staticData.getPerson().isGuest()) {
      k.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
      sbKey.append(systemCacheId);
    } else {
      k.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
    }
    sbKey.append("userId:").append(staticData.getPerson().getID()).append(", ");
    sbKey.append("authenticated:").append(staticData.getPerson().getSecurityContext().isAuthenticated()).append(", ");

    if(xslUriForKey == null) {
      try {
        String sslUri = ResourceLoader.getResourceAsURLString(this.getClass(), sslLocation);
        xslUriForKey=XSLT.getStylesheetURI(sslUri, runtimeData.getBrowserInfo());
      } catch (PortalException pe) {
        xslUriForKey = "Not attainable!";
      }
    }
    sbKey.append("xslUri:").append(xslUriForKey).append(", ");
    sbKey.append("bAuthenticated:").append(bAuthenticated).append(", ");
    sbKey.append("bauthenticationAttemptFailed:").append(bauthenticationAttemptFailed).append(", ");
    sbKey.append("attemptedUserName:").append(attemptedUserName).append(", ");
    sbKey.append("bSecurityError:").append(bSecurityError);
    sbKey.append("locales:").append(LocaleManager.stringValueOf(runtimeData.getLocales()));
    k.setKey(sbKey.toString());
    k.setKeyValidity(new Long(System.currentTimeMillis()));
    return k;
  }

  public boolean isCacheValid(Object validity) {
    return true;
  }
}
