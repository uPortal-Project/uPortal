/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
    try {
        String authenticationAttempted = (String)session.getAttribute("up_authenticationAttempted");
        String authenticationError = (String)session.getAttribute("up_authenticationError");
        attemptedUserName = (String)session.getAttribute("up_attemptedUserName");
    
        if (authenticationAttempted != null)
          bauthenticationAttemptFailed = true;
    
        if (authenticationError != null)
            bSecurityError = true;
    }
    catch (IllegalStateException ise) {
        //ignore
    }
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
    xslt.setStylesheetParameter("unauthenticated", String.valueOf(!staticData.getPerson().getSecurityContext().isAuthenticated()));
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
    sbKey.append("bSecurityError:").append(bSecurityError).append(", ");
    sbKey.append("locales:").append(LocaleManager.stringValueOf(runtimeData.getLocales()));
    k.setKey(sbKey.toString());
    k.setKeyValidity(new Long(System.currentTimeMillis()));
    return k;
  }

  public boolean isCacheValid(Object validity) {
    return true;
  }
  
  public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append(" authenticated:");
        sb.append(this.bAuthenticated);
        sb.append(" authenticationAttemptFailed:"); 
        sb.append(this.bauthenticationAttemptFailed);
        sb.append(" securityError:");
        sb.append(this.bSecurityError);
        sb.append(" attemptedUserName=[");
        sb.append(this.attemptedUserName);
        sb.append("]");
        return sb.toString();
    }
}
