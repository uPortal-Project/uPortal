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

import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.UtilitiesBean;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.security.*;
import org.xml.sax.DocumentHandler;
import java.util.Hashtable;
import java.net.URL;
import javax.servlet.http.HttpSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>Allows a user to logon to the portal.  Logon info is posted to
 * <code>authenticate.jsp</code>.  If user enters incorrect username and
 * password, he/she is instructed to log in again with a different
 * password (the username of the previous attempt is preserved.</p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CLogin implements IPrivilegedChannel
{
  private ChannelStaticData staticData;
  private ChannelRuntimeData runtimeData;
  private String channelName = "Log in...";
  private String media;
  private String attemptedUserName="";
  private static final String sslLocation = UtilitiesBean.fixURI("webpages/stylesheets/org/jasig/portal/channels/CLogin/CLogin.ssl");
  private boolean bAuthenticated = false;
  private boolean bAuthorizationAttemptFailed = false;
  private boolean bSecurityError = false;

  private ISecurityContext ic;

  public CLogin()
  {
  }

  public void setPortalControlStructures(PortalControlStructures pcs)
  {
    HttpSession session = pcs.getHttpSession();
    String authorizationAttempted = (String)session.getAttribute("up_authorizationAttempted");
    String authorizationError = (String)session.getAttribute("up_authorizationError");

    if (authorizationAttempted != null)
      bAuthorizationAttemptFailed = true;

    if (authorizationError != null)
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
    ic = staticData.getSecurityContext();

    if (ic!=null && ic.isAuthenticated())
      bAuthenticated = true;

  }

  public void setRuntimeData (ChannelRuntimeData rd)
  {
    this.runtimeData = rd;

    media = runtimeData.getMedia();
    attemptedUserName = runtimeData.getParameter("userName");
  }

  public void renderXML (DocumentHandler out) throws PortalException
  {
    String fullName = (String)staticData.getPerson().getFullName();
    Document doc = new org.apache.xerces.dom.DocumentImpl();

    // Create <login-status> element
    Element loginStatusElement = doc.createElement("login-status");

    if (bSecurityError)
    {
      // Create <error> element under <login-status>
      Element errorElement = doc.createElement("error");
      loginStatusElement.appendChild(errorElement);
    }
    else if (bAuthorizationAttemptFailed && !bAuthenticated)
    {
      // Create <failure> element under <login-status>
      Element failureElement = doc.createElement("failure");
      failureElement.setAttribute("attemptedUserName", attemptedUserName);
      loginStatusElement.appendChild(failureElement);
    }
    else
    {
      // Create <full-name> element under <header>
      Element fullNameElement = doc.createElement("full-name");
      fullNameElement.appendChild(doc.createTextNode(fullName));
      loginStatusElement.appendChild(fullNameElement);
    }

    doc.appendChild(loginStatusElement);

    Hashtable params = new Hashtable(2);
    params.put("baseActionURL", runtimeData.getBaseActionURL());
    if (fullName != null && fullName.equals("Guest"))
      params.put("guest", "true");

    try
    {
      XSLT.transform(doc, new URL(UtilitiesBean.fixURI(sslLocation)), out, params, "login", media);
    }
    catch (Exception e)
    {
      throw new GeneralRenderingException(e.getMessage());
    }
  }
}
