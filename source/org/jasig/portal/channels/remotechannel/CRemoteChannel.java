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

package org.jasig.portal.channels.remotechannel;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.rpc.ServiceException;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.IPrivileged;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.provider.NotSoOpaqueCredentials;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/**
 * <p>A proxy channel for remote channels exposed by the uPortal
 * Web Services layer.  There is a related channel type called
 * "Remote Channel Proxy" that is included with uPortal, so to use
 * this channel, just select "Remote Channel Proxy" when publishing.</p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CRemoteChannel extends BaseChannel implements IPrivileged, ICacheable {

  protected RemoteChannel rc = null;
  protected String instanceId = null;
  protected static final String SSL_LOCATION = "CRemoteChannel.ssl";
  protected String baseUrl = null;
  protected String xslUriForKey = null;
  protected boolean receivedEvent = false;
  protected boolean focused = false;

  /**
   * Provides initialization opportunity for this channel.  The static
   * data is expected to contain the following properties:<br/>
   * <ul>
   *   <li><code>endpoint</code>, the service URL</li>
   *   <li><code>fname</code>, the remote channel's functional name</li>   
   * </ul>
   * If those parameters are present, this channel will attempt to
   * authenticate to the remote channel service and then instantiate
   * the remote channel.  If the remote channel is successfully
   * instantiated, then an instance ID is received from the remote
   * channel service and held by this channel to identify the remote
   * channel in future communications.
   * @param sd the channel static data
   * @throws org.jasig.portal.PortalException
   */  
  public void setStaticData(ChannelStaticData sd) throws PortalException {
    super.setStaticData(sd);
    String endpoint = staticData.getParameter("endpoint");
    String fname = staticData.getParameter("fname");
    RemoteChannelService rcs = new RemoteChannelServiceLocator();

    // Obtain a stub that talks to the remote channel service
    try {
      rc = rcs.getRemoteChannel(new URL(endpoint));
    } catch (MalformedURLException mue) {
      throw new ResourceMissingException(endpoint, "Remote channel service endpoint", mue.getMessage());
    } catch (ServiceException se) {
      throw new PortalException(se);
    }

    // Authenticate and instantiate an instance of the remote channel
    try {
      authenticate();
      instanceId = rc.instantiateChannel(fname);
    } catch (RemoteException re) {
      throw new PortalException(re);
    }
  }

  /**
   * Passes this channel a portal event.  All events are forwarded
   * to the remote channel.  When a "session done" event is passed,
   * this channel logs out of the remote channel service.  When an
   * "unsubscribe" event is passed, this channel frees the instance
   * of the remote channel living on the remote server.
   * @param ev the portal event
   */    
  public void receiveEvent(PortalEvent ev) {
    try {
      // Pass the portal event to the remote channel
      rc.receiveEvent(instanceId, ev);
      
      // For session done and unsubscribe events, logout and
      // and release remote channel respectively
      if (ev.getEventNumber() == PortalEvent.SESSION_DONE) {
        rc.logout();
      } else if (ev.getEventNumber() == PortalEvent.UNSUBSCRIBE) {
        rc.freeChannel(instanceId);
      }
    } catch (RemoteException re) {
      // Do nothing
    }
    receivedEvent = true;
  }

  /**
   * This is where the channel obtains markup from the remote channel
   * and dumps it into the content handler supplied by the portal framework. 
   * @param out the content handler
   * @throws org.jasig.portal.PortalException
   */  
  public void renderXML(ContentHandler out) throws PortalException {

    // Obtain the channel content
    Element channelE = null;
    try {
      channelE = rc.renderChannel(instanceId, runtimeData);
    } catch (RemoteException re) {
      throw new PortalException(re);
    }

    XSLT xslt = new XSLT(this);
    xslt.setXML(channelE);
    xslt.setXSL(SSL_LOCATION, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.transform();
  }

  // Helper methods

  /**
   * If credentials are cached by the security provider,
   * use them to authenticate
   * @throws RemoteException
   */
  protected void authenticate() throws RemoteException {
    String username = (String)staticData.getPerson().getAttribute(IPerson.USERNAME);
    String password = null;
    ISecurityContext ic = staticData.getPerson().getSecurityContext();
    IOpaqueCredentials oc = ic.getOpaqueCredentials();
    if (oc instanceof NotSoOpaqueCredentials) {
      NotSoOpaqueCredentials nsoc = (NotSoOpaqueCredentials)oc;
      password = nsoc.getCredentials();
    }

    // If still no password, loop through subcontexts to find cached credentials
    if (password == null) {
      java.util.Enumeration en = ic.getSubContexts();
      while (en.hasMoreElements()) {
        ISecurityContext sctx = (ISecurityContext)en.nextElement();
        IOpaqueCredentials soc = sctx.getOpaqueCredentials();
        if (soc instanceof NotSoOpaqueCredentials) {
          NotSoOpaqueCredentials nsoc = (NotSoOpaqueCredentials)soc;
          password = nsoc.getCredentials();
        }
      }
    }

    if (username != null && password != null)
      rc.authenticate(username, password);
  }
  
  
  // IPrivileged methods
  
  /**
   * This is where the portal control structures are passed to this channel by the framework.
   * We are only interested in getting the HttpServletRequest object which allows us to 
   * construct a base URL which we hold onto and eventually prepend to the baseActionURL
   * before we send it to the remote channel.
   * @param pcs the portal control structures
   * @throws org.jasig.portal.PortalException
   */
  public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
    if (baseUrl == null) {
      // If there is a better way to obtain the base URL, please improve this!      
      HttpServletRequest request = pcs.getHttpServletRequest();
      String protocol = request.getProtocol();
      String protocolFixed = protocol.substring(0, protocol.indexOf("/")).toLowerCase();
      String serverName = request.getServerName();
      int serverPort = request.getServerPort();
      String contextPath = request.getContextPath();
      baseUrl = protocolFixed + "://" + serverName + ":" + serverPort + contextPath + "/";
    }
  }
  

  // ICacheable methods

  /**
   * Generates a channel cache key.  The key scope is set to be system-wide
   * when the channel is anonymously accessed, otherwise it is set to be
   * instance-wide.  The caching implementation here is simple and may not
   * handle all cases.  Please improve these ICacheable methods as necessary.
   */
  public ChannelCacheKey generateKey() {
    ChannelCacheKey cck = new ChannelCacheKey();
    StringBuffer sbKey = new StringBuffer(1024);

    // Anonymously accessed pages can be cached system-wide
    if(staticData.getPerson().isGuest()) {
      cck.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
      sbKey.append("SYSTEM_");
    } else {
      cck.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
    }

    if (xslUriForKey == null) {
      try {
        String sslUri = ResourceLoader.getResourceAsURLString(this.getClass(), SSL_LOCATION);
        xslUriForKey = XSLT.getStylesheetURI(sslUri, runtimeData.getBrowserInfo());
      } catch (PortalException pe) {
        xslUriForKey = "Not attainable!";
      }
    }
    sbKey.append("xslUri: ").append(xslUriForKey);
    cck.setKey(sbKey.toString());
    return cck;
  }

  /**
   * Return <code>true</code> when we have not just received an event,
   * no runtime parameters are sent to the channel, and the focus hasn't switched.
   * Otherwise, return <code>false</code>.  In other words, cache the content
   * in all cases <i>except</i> for when a user clicks a channel button,
   * a link or form button within the channel, or the "focus" or "unfocus" button.
   */
  public boolean isCacheValid(Object validity) {
    // Determine if the channel focus has changed
    boolean previouslyFocused = focused;
    focused = runtimeData.isRenderingAsRoot();
    boolean focusHasSwitched = focused != previouslyFocused;
    
    // Dirty cache only when we receive an event, one or more request params, or a change in focus
    boolean cacheValid = !receivedEvent && runtimeData.size() == 0 && !focusHasSwitched;
    
    receivedEvent = false;
    return cacheValid;
  }
}
