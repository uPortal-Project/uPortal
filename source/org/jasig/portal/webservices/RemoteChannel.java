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

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.BrowserInfo;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.ChannelRenderer;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.IChannel;
import org.jasig.portal.IPrivileged;
import org.jasig.portal.IMultithreadedChannel;
import org.jasig.portal.IMultithreadedCacheable;
import org.jasig.portal.MultithreadedChannelAdapter;
import org.jasig.portal.MultithreadedCacheableChannelAdapter;
import org.jasig.portal.MultithreadedPrivilegedChannelAdapter;
import org.jasig.portal.MultithreadedPrivilegedCacheableChannelAdapter;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.InternalTimeoutException;
import org.jasig.portal.services.Authentication;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;
import org.jasig.portal.security.InitialSecurityContextFactory;
import org.jasig.portal.security.PortalSecurityException;
import org.apache.axis.MessageContext;
import org.apache.axis.session.Session;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.Cookie;

public class RemoteChannel implements IRemoteChannel {

  protected static final String MARKUP_FRAGMENT_ROOT = "channel";
  protected static final String PERSON_KEY = "org.jasig.portal.security.IPerson";
  protected static final String CHANNEL_INSTANCE_ID_PREFIX = "chan_";
  protected static final String CHANNEL_DEFINITION_ID_PREFIX = "chandef_";

  protected static final Authentication authenticationService = new Authentication();
  protected static final Map multithreadedChannelTable = new HashMap();
  protected static final Random randomNumberGenerator = new Random();

  /**
   * Authenticates user and establishes a session.
   * @param username the user name of the user
   * @param password the user's password
   * @throws Exception if there was a problem trying to authenticate
   */
  public void authenticate(String username, String password) throws Exception {
    IPerson person = getPerson();

    // Set the user name and password
    HashMap principals = new HashMap(1);
    principals.put("username", username);
    HashMap credentials = new HashMap(1);
    credentials.put("password", password);

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
   * @throws Exception if the channel cannot be located
   */
  public String instantiateChannel(String fname) throws Exception {
    MessageContext messageContext = MessageContext.getCurrentContext();
    Session session = messageContext.getSession();

    IChannel channel = null;
    String instanceId = Long.toHexString(randomNumberGenerator.nextLong()) + "_" + System.currentTimeMillis();

    ChannelDefinition channelDef = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl().getChannelDefinition(fname);
    Object channelObj = Class.forName(channelDef.getJavaClass()).newInstance();

    // Figure out what kind of channel we have an use adapters if necessary
    // to make any channel look like an IChannel
    if (channelObj instanceof IMultithreadedChannel) {
      String uid = messageContext.getProperty(SimpleSessionHandler.SESSION_ID) + "/" + instanceId;
      if (channelObj instanceof IMultithreadedCacheable) {
        if (channelObj instanceof IPrivileged) {
          // Multithreaded=true, cacheable=true, privileged=true
          channel = new MultithreadedPrivilegedCacheableChannelAdapter((IMultithreadedChannel)channelObj, uid);
        } else {
          // Multithreaded=true, cacheable=true, privileged=false
          channel = new MultithreadedCacheableChannelAdapter((IMultithreadedChannel)channelObj, uid);
        }
      } else if (channelObj instanceof IPrivileged) {
          // Multithreaded=true, cacheable=false, privileged=true
          channel = new MultithreadedPrivilegedChannelAdapter((IMultithreadedChannel)channelObj, uid);
      } else {
        // Multithreaded=true, cacheable=false, privileged=false
        channel = new MultithreadedChannelAdapter((IMultithreadedChannel)channelObj, uid);
      }
    } else {
      // Channel must be a normal IChannel
      channel = (IChannel)channelObj;
    }

    ChannelStaticData staticData = new ChannelStaticData();

    // Set the publish ID, person, and timeout
    staticData.setChannelPublishId(String.valueOf(channelDef.getId()));
    staticData.setPerson(getPerson());
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
    ChannelDefinition.ChannelParameter[] channelParams = channelDef.getParameters();
    for (int i = 0; i < channelParams.length; i++) {
      staticData.setParameter(channelParams[i].getName(), channelParams[i].getValue());
    }

    // Done stuffing the ChannelStaticData, now pass it to the channel
    channel.setStaticData(staticData);

    // Store the channel instance and channel definition
    session.set(CHANNEL_INSTANCE_ID_PREFIX + instanceId, channel);
    session.set(CHANNEL_DEFINITION_ID_PREFIX + instanceId, channelDef);

    // Use this for multithreaded channels
    //multithreadedChannelTable.put(instanceId, channel);

    return instanceId;
  }

  /**
   * Asks the channel to render content and return it as a String.
   * The content will be well-formed XML which the client must serialize.
   * @param instanceId an identifier for the channel instance returned by instantiateChannel()
   * @param headers a Map of headers (name/value pairs).
            One of the headers must be a "user-agent".
   * @param cookies an array of javax.servlet.http.Cookie objects.
            Can be null if there are no cookies to send.
   * @param parameters a Map of request parameter name/value pairs.
            Can be null if there are no request parameters.
   * @param baseActionURL a String representing the base action URL to which
            channels will append '?' and a set of name/value pairs delimited by '&'.
   * @return xml an XML element representing the channel's output
   * @throws Throwable if the channel cannot respond with the expected rendering
   */
  public Element renderChannel(String instanceId, Map headers, Cookie[] cookies,
                               Map parameters, String baseActionURL) throws Throwable {

    Element channelElement = null;
    IChannel channel = null;
    ChannelDefinition channelDef = null;

    MessageContext messageContext = MessageContext.getCurrentContext();
    Session session = messageContext.getSession();
    channel = (IChannel)session.get(CHANNEL_INSTANCE_ID_PREFIX + instanceId);
    channelDef = (ChannelDefinition)session.get(CHANNEL_DEFINITION_ID_PREFIX + instanceId);

    if (channel == null || channelDef == null)
      throw new PortalException("No channel found for instance ID '" + instanceId);

    // Set up channel runtime data and give it to channel
    ChannelRuntimeData runtimeData = new ChannelRuntimeData();
    runtimeData.setBrowserInfo(new BrowserInfo(cookies, headers));
    runtimeData.setParametersSingleValued(parameters);
    runtimeData.setBaseActionURL(baseActionURL);
    runtimeData.setRenderingAsRoot(true);
    runtimeData.setUPFile(new UPFileSpec(null, UPFileSpec.RENDER_METHOD, "webServiceRoot", "singlet", null));

    // Start rendering
    ChannelRenderer cr = new ChannelRenderer(channel, runtimeData);
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
      int status = cr.outputRendering(th);
      if (status == ChannelRenderer.RENDERING_TIMED_OUT) {
        throw new InternalTimeoutException("The remote channel has timed out");
      }
    } catch (Throwable t) {
      throw new Exception(t.getMessage()); // Consider throwing the Throwable!
    }

    th.endElement("", MARKUP_FRAGMENT_ROOT, MARKUP_FRAGMENT_ROOT);
    th.endDocument();
    Document doc = (Document)domResult.getNode();
    channelElement = doc.getDocumentElement();

    //System.out.println("Rendering '" + channelDef.getName() + "' channel");
    //System.out.println(org.jasig.portal.utils.XML.serializeNode(channelElement));

    return channelElement;
  }

  /**
   * Indicates to the portal that the web services client is finished
   * talking to the channel instance.
   * @param instanceId an identifier for the channel instance returned by instantiateChannel()
   * @throws Exception if the channel cannot be freed
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
   * @throws Exception if there was a problem trying to logout
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
   */
  protected IPerson getGuestPerson() throws PortalException {
    IPerson person = new PersonImpl();
    person.setSecurityContext(InitialSecurityContextFactory.getInitialContext("root"));
    person.setID(1); // Guest users have a UID of 1
    return person;
  }
}