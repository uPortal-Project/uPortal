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

package org.jasig.portal.webservices;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.axis.MessageContext;
import org.apache.axis.handlers.SimpleSessionHandler;
import org.apache.axis.session.Session;
import org.apache.axis.transport.http.HTTPConstants;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelFactory;
import org.jasig.portal.ChannelParameter;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelRenderer;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IChannel;
import org.jasig.portal.InternalTimeoutException;
import org.jasig.portal.MediaManager;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.InitialSecurityContextFactory;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.security.provider.PersonImpl;
import org.jasig.portal.services.Authentication;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.utils.AbsoluteURLFilter;
import org.jasig.portal.utils.threading.BoundedThreadPool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.helpers.AttributesImpl;

/**
 * <p>A remote channel web service.  This is the implementation
 * of IRemoteChannel on which the WSDL for this web service is based.</p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class RemoteChannel implements IRemoteChannel {
  // Todo: Should get this thread pool from the ChannelManager!
  private static final BoundedThreadPool renderThreadPool = new BoundedThreadPool(20, 150, 5);

  protected static final String MARKUP_FRAGMENT_ROOT = "channel";
  protected static final String PERSON_KEY = "org.jasig.portal.security.IPerson";
  protected static final String CHANNEL_INSTANCE_ID_PREFIX = "chan_";
  protected static final String CHANNEL_DEFINITION_ID_PREFIX = "chandef_";
  
  protected static final Authentication authenticationService = new Authentication();
  protected static final Random randomNumberGenerator = new Random();

  protected static String baseUrl = null;

  /**
   * Authenticates user and establishes a session.
   * @param username the user name of the user
   * @param password the user's password
   * @throws java.lang.Exception if there was a problem trying to authenticate
   */
  public void authenticate(String username, String password) throws Exception {
    IPerson person = getPerson();

    // Set the user name and password
    HashMap principals = new HashMap(1);
    principals.put("root", username);
    HashMap credentials = new HashMap(1);
    credentials.put("root", password);

    // Attempt to authenticate using the incoming request
    authenticationService.authenticate(principals, credentials, person);

    if (!person.getSecurityContext().isAuthenticated()) {
      throw new PortalSecurityException("Unable to authenticate user '" + username + "'");
    } else {
      //System.out.println(person.getFullName() + " authenticated successfully");
    }
  }

  /**
   * Establishes a channel instance which the webservice client will communicate with.
   * @param fname an identifier for the channel unique within a particular portal implementation
   * @return instanceId an identifier for the newly-created channel instance
   * @throws java.lang.Exception if the channel cannot be located
   */
  public String instantiateChannel(String fname) throws Exception {
    MessageContext messageContext = MessageContext.getCurrentContext();
    Session session = messageContext.getSession();

    // Locate the channel by "fname" (functional name)
    ChannelDefinition channelDef = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl().getChannelDefinition(fname);
    if (channelDef == null)
      throw new ResourceMissingException("fname:" + fname, fname + " channel", "Unable to find a channel with functional name '" + fname + "'");

    // Make sure user is authorized to access this channel
    IPerson person = getPerson();
    EntityIdentifier ei = person.getEntityIdentifier();
    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
    int channelPublishId = channelDef.getId();
    boolean authorized = ap.canSubscribe(channelPublishId);
    if (!authorized)
      throw new AuthorizationException("User '" + person.getAttribute(person.USERNAME) + "' is not authorized to access channel with functional name '" + fname + "'");
      
    // Instantiate channel
    // Should this block be synchronized?
    String javaClass = channelDef.getJavaClass();
    String instanceId = Long.toHexString(randomNumberGenerator.nextLong()) + "_" + System.currentTimeMillis();    
    String uid = messageContext.getProperty(SimpleSessionHandler.SESSION_ID) + "/" + instanceId;
    IChannel channel = ChannelFactory.instantiateChannel(javaClass, uid);

    // Start to stuff the ChannelStaticData
    ChannelStaticData staticData = new ChannelStaticData();

    // Set the publish ID, person, and timeout
    staticData.setChannelPublishId(String.valueOf(channelPublishId));
    staticData.setPerson(person);
    staticData.setTimeout(channelDef.getTimeout());

    // Set channel context
    Hashtable environment = new Hashtable(1);
    environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jasig.portal.jndi.PortalInitialContextFactory");
    Context portalContext = new InitialContext(environment);
    Context channelContext = new tyrex.naming.MemoryContext(new Hashtable());
    Context servicesContext = (Context)portalContext.lookup("services");
    channelContext.bind("services", servicesContext);
    staticData.setJNDIContext(channelContext);

    // Set the channel parameters
    ChannelParameter[] channelParams = channelDef.getParameters();
    for (int i = 0; i < channelParams.length; i++) {
      staticData.setParameter(channelParams[i].getName(), channelParams[i].getValue());
    }

    // Done stuffing the ChannelStaticData, now pass it to the channel
    channel.setStaticData(staticData);

    // Store the channel instance and channel definition
    session.set(CHANNEL_INSTANCE_ID_PREFIX + instanceId, channel);
    session.set(CHANNEL_DEFINITION_ID_PREFIX + instanceId, channelDef);

    return instanceId;
  }

  /**
   * Asks the channel to render content and return it as an XML Element.
   * The content will be well-formed XML which the client must serialize.
   * @param instanceId an identifier for the channel instance returned by instantiateChannel()
   * @param runtimeData the channel runtime data including request parameters
            headers, cookies, etc.
   * @return xml an XML element representing the channel's output
   * @throws java.lang.Exception if the channel cannot respond with the expected rendering
   */
  public Element renderChannel(String instanceId, ChannelRuntimeData runtimeData) throws Exception {

    Element channelElement = null;
    IChannel channel = null;
    ChannelDefinition channelDef = null;
    
    MessageContext messageContext = MessageContext.getCurrentContext();
    Session session = messageContext.getSession();
    channel = (IChannel)session.get(CHANNEL_INSTANCE_ID_PREFIX + instanceId);
    channelDef = (ChannelDefinition)session.get(CHANNEL_DEFINITION_ID_PREFIX + instanceId);

    if (channel == null || channelDef == null)
      throw new ResourceMissingException("id:" + instanceId, instanceId + " channel", "Unable to find a channel with instance ID '" + instanceId + "'");
    
    // Start rendering
    ChannelRenderer cr = new ChannelRenderer(channel, runtimeData, renderThreadPool);
    cr.setTimeout(channelDef.getTimeout());
    cr.startRendering();

    // Set up channel attributes
    AttributesImpl chanAtts = new AttributesImpl();
    chanAtts.addAttribute("", "name", "name", "CDATA", channelDef.getName());
    chanAtts.addAttribute("", "title", "title", "CDATA", channelDef.getTitle());
    chanAtts.addAttribute("", "description", "description", "CDATA", channelDef.getDescription());
    chanAtts.addAttribute("", "hasAbout", "hasAbout", "CDATA", String.valueOf(channelDef.hasAbout()));
    chanAtts.addAttribute("", "hasHelp", "hasHelp", "CDATA", String.valueOf(channelDef.hasHelp()));
    chanAtts.addAttribute("", "editable", "editable", "CDATA", String.valueOf(channelDef.isEditable()));

    // Transfer SAX events into a DOM structure
    TransformerFactory tf = TransformerFactory.newInstance();
    SAXTransformerFactory stf = (SAXTransformerFactory)tf; // This could be a problem!
    TransformerHandler th = stf.newTransformerHandler();
    DOMResult domResult = new DOMResult();
    th.setResult(domResult);
    th.startDocument();
    // Set up the control attributes here!
    th.startElement("", MARKUP_FRAGMENT_ROOT, MARKUP_FRAGMENT_ROOT, chanAtts);

    try {
      // Insert a filter to re-write URLs
      MediaManager mm = new MediaManager();
      String media = mm.getMedia(runtimeData.getBrowserInfo());
      String mimeType = mm.getReturnMimeType(media);
      AbsoluteURLFilter urlFilter = AbsoluteURLFilter.newAbsoluteURLFilter(mimeType, getBaseUrl(), th);
      
      // Begin chain: channel renderer --> URL filter --> SAX2DOM transformer
      int status = cr.outputRendering(urlFilter);      
      if (status == ChannelRenderer.RENDERING_TIMED_OUT) {
        throw new InternalTimeoutException("The remote channel has timed out");
      }
    } catch (Exception e) {
      throw e;
    } catch (Throwable t) {
      throw new Exception(t.getMessage());
    }

    th.endElement("", MARKUP_FRAGMENT_ROOT, MARKUP_FRAGMENT_ROOT);
    th.endDocument();
    Document doc = (Document)domResult.getNode();
    channelElement = doc.getDocumentElement();

    return channelElement;
  }

  /**
   * Passes portal events to the channel.
   * @param instanceId an identifier for the channel instance returned by instantiateChannel()
   * @param event a portal event
   * @throws java.lang.Exception if the channel cannot receive its event
  */
  public void receiveEvent(String instanceId, PortalEvent event) throws Exception {
    MessageContext messageContext = MessageContext.getCurrentContext();
    Session session = messageContext.getSession();
    IChannel channel = (IChannel)session.get(CHANNEL_INSTANCE_ID_PREFIX + instanceId);  

    if (channel == null)
      throw new ResourceMissingException("id:" + instanceId, instanceId + " channel", "Unable to find a channel with instance ID '" + instanceId + "'");

    // Pass the channel its event
    channel.receiveEvent(event);
  }

  /**
   * Indicates to the portal that the web services client is finished
   * talking to the channel instance.
   * @param instanceId an identifier for the channel instance returned by instantiateChannel()
   * @throws java.lang.Exception if the channel cannot be freed
   */
  public void freeChannel(String instanceId) throws Exception {
    MessageContext messageContext = MessageContext.getCurrentContext();
    Session session = messageContext.getSession();
    session.remove(CHANNEL_DEFINITION_ID_PREFIX + instanceId);
    session.remove(CHANNEL_INSTANCE_ID_PREFIX + instanceId);
    //System.out.println("Freeing channel " + instanceId);
  }

  /**
   * Unauthenticates a user, killing the session.
   * @throws java.lang.Exception if there was a problem trying to logout
   */
  public void logout() throws Exception {
    MessageContext messageContext = MessageContext.getCurrentContext();
    messageContext.setSession(null);
    //System.out.println("Logging out");
  }


  // Helper methods

  /**
   * Looks in session for an IPerson.  If it doesn't find one,
   * a "guest" person object is returned
   * @return person the IPerson object
   * @throws org.jasig.portal.PortalException  
   */
  protected IPerson getPerson() throws PortalException {
    MessageContext messageContext = MessageContext.getCurrentContext();
    Session session = messageContext.getSession();
    IPerson person = (IPerson)session.get(PERSON_KEY);
    if (person == null) {
      person = getGuestPerson();
      session.set(PERSON_KEY, person);
    }
    return person;
  }

  /**
   * Returns a person object that is a "guest" user
   * @return person a person object that is a "guest" user
   * @throws org.jasig.portal.PortalException
   */
  protected IPerson getGuestPerson() throws PortalException {
    IPerson person = new PersonImpl();
    person.setSecurityContext(InitialSecurityContextFactory.getInitialContext("root"));
    person.setID(1); // Guest users have a UID of 1
    person.setAttribute(person.USERNAME,"guest");
    return person;
  }
  
  /**
   * Returns the server's base URL which can be prepended to relative resource paths.
   * The base URL is unique with respect to the server.
   * @return baseUrl the server's base URL which can be prepended to relative resource paths
   */
  protected static String getBaseUrl() {
    if (baseUrl == null) {
      // If there is a better way to obtain the base URL, please improve this!      
      MessageContext messageContext = MessageContext.getCurrentContext();
      HttpServletRequest request = (HttpServletRequest)messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
      String protocol = request.getProtocol();
      String protocolFixed = protocol.substring(0, protocol.indexOf("/")).toLowerCase();
      String serverName = request.getServerName();
      int serverPort = request.getServerPort();
      String contextPath = request.getContextPath();
      baseUrl = protocolFixed + "://" + serverName + ":" + serverPort + contextPath + "/";
    }
    return baseUrl;
  }  
}
