/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.channels.wsrp;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.rpc.holders.BooleanHolder;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedChannel;
import org.jasig.portal.IMultithreadedPrivileged;
import org.jasig.portal.MediaManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.wsrp.intf.WSRP_v1_Markup_PortType;
import org.jasig.portal.wsrp.intf.WSRP_v1_ServiceDescription_PortType;
import org.jasig.portal.wsrp.types.ClientData;
import org.jasig.portal.wsrp.types.MarkupParams;
import org.jasig.portal.wsrp.types.MarkupResponse;
import org.jasig.portal.wsrp.types.PersonName;
import org.jasig.portal.wsrp.types.PortletContext;
import org.jasig.portal.wsrp.types.RegistrationContext;
import org.jasig.portal.wsrp.types.RuntimeContext;
import org.jasig.portal.wsrp.types.ServiceDescription;
import org.jasig.portal.wsrp.types.UserContext;
import org.jasig.portal.wsrp.types.UserProfile;
import org.jasig.portal.wsrp.types.holders.CookieProtocolHolder;
import org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder;
import org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder;
import org.jasig.portal.wsrp.types.holders.MarkupContextHolder;
import org.jasig.portal.wsrp.types.holders.ModelDescriptionHolder;
import org.jasig.portal.wsrp.types.holders.PortletDescriptionArrayHolder;
import org.jasig.portal.wsrp.types.holders.ResourceListHolder;
import org.jasig.portal.wsrp.types.holders.SessionContextHolder;
import org.jasig.portal.wsrp.types.holders.StringArrayHolder;
import org.jasig.portal.wsrp.wsdl.WSRPService;
import org.jasig.portal.wsrp.wsdl.WSRPServiceLocator;
import org.xml.sax.ContentHandler;

/**
 * A uPortal WSRP consumer channel.  
 * There is a related channel type called
 * "WSRP Consumer" that is included with uPortal, so to use
 * this channel, just select "WSRP Consumer" when publishing.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CConsumer implements IMultithreadedChannel, IMultithreadedPrivileged {

  protected static Map channelStateMap;
  protected static WSRPService wsrpService;
  protected static RegistrationContext registrationContext;
  protected static Map serviceDescriptionMap;

  /**
   * The channel's state including the static data, runtime data,
   * portal event, portal control structures and a ChannelData object. 
   */
  protected class ChannelState {
    private ChannelStaticData staticData = null;
    private ChannelRuntimeData runtimeData = null;
    private PortalEvent portalEvent = null;
    private PortalControlStructures pcs = null;
    private ChannelData channelData = new ChannelData();

    public ChannelStaticData getStaticData() { return this.staticData; }
    public ChannelRuntimeData getRuntimeData() { return this.runtimeData; }
    public PortalControlStructures getPortalControlStructures() { return this.pcs; }
    public PortalEvent getPortalEvent() { return this.portalEvent; }
    public ChannelData getChannelData() { return this.channelData; }

    public void setStaticData(ChannelStaticData sd) { this.staticData = sd; }
    public void setRuntimeData(ChannelRuntimeData rd) { this.runtimeData = rd; }
    public void setPortalControlStructures(PortalControlStructures pcs) { this.pcs = pcs; }
    public void setPortalEvent(PortalEvent ev) { this.portalEvent = ev; }
    public void setChannelData(ChannelData cd) { this.channelData = cd; }
  }

  /**
   * An object that keeps track of session data.
   */
  protected class ChannelData {
    private String sessionId = null;
    //private WSRP_v1_ServiceDescription_PortType wsrpServiceDescriptionPort = null;
    private WSRP_v1_Markup_PortType wsrpMarkupPort = null;
    //private ServiceDescription serviceDescription = null;

    public String getSessionId() { return this.sessionId; }
    //public WSRP_v1_ServiceDescription_PortType getWsrpServiceDescriptionPort() { return this.wsrpServiceDescriptionPort; }
    public WSRP_v1_Markup_PortType getWsrpMarkupPort() { return this.wsrpMarkupPort; }
    //public ServiceDescription getServiceDescription() { return this.serviceDescription;  }

    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    //public void setWsrpServiceDescriptionPort(WSRP_v1_ServiceDescription_PortType wsrpServiceDescriptionPort) { this.wsrpServiceDescriptionPort = wsrpServiceDescriptionPort; }
    public void setWsrpMarkupPort(WSRP_v1_Markup_PortType wsrpMarkupPort) { this.wsrpMarkupPort = wsrpMarkupPort; }
    //public void setServiceDescription(ServiceDescription serviceDescription) { this.serviceDescription = serviceDescription; }
  }


  static {
    channelStateMap = Collections.synchronizedMap(new HashMap());
    
    wsrpService = new WSRPServiceLocator();
    
    // Setup registration context
    // The registration context would be normally be produced when the consumer
    // registers with the producer - right now we are skipping this optional step
    registrationContext = new RegistrationContext();
            
    serviceDescriptionMap = Collections.synchronizedMap(new HashMap());
  }

  /**
   * Sets channel runtime properties.
   * @param uid, a unique ID used to identify the state of the channel
   * @return channel runtime properties
   */
  public ChannelRuntimeProperties getRuntimeProperties(String uid) {
    return new ChannelRuntimeProperties();
  }

  /**
   * React to portal events.
   * Removes channel state from the channel state map when the session expires.
   * @param ev, a portal event
   * @param uid, a unique ID used to identify the state of the channel
   */
  public void receiveEvent(PortalEvent ev, String uid) {
    ChannelState channelState = (ChannelState)channelStateMap.get(uid);
    if (channelState != null) {
      channelState.setPortalEvent(ev);
      if (ev.getEventNumber() == PortalEvent.SESSION_DONE) {
        channelStateMap.remove(uid); // Clean up
      }
    }
  }

  /**
   * Sets the channel static data.
   * @param sd, the channel static data
   * @param uid, a unique ID used to identify the state of the channel
   * @throws org.jasig.portal.PortalException
   */
  public void setStaticData(ChannelStaticData sd, String uid) throws PortalException {
    System.out.println("CConsumer.setStaticData() called");
    ChannelState channelState = new ChannelState();
    channelState.setStaticData(sd);
    channelStateMap.put(uid, channelState);
    
    ChannelData cd = channelState.getChannelData();
    
    // Establish service endpoints
    String baseEndpoint = "http://localhost:8080/uPortal/services/";
    String baseServiceEndpoint = baseEndpoint + "WSRPBaseService";
    String serviceDescriptionServiceEndpoint = baseEndpoint + "WSRPServiceDescriptionService";
    
    // Get service description
    ServiceDescription serviceDescription = getServiceDescription(serviceDescriptionServiceEndpoint);
    
    // Get markup port
    WSRP_v1_Markup_PortType wsrpMarkupPort = null;   
    try {
      wsrpMarkupPort = wsrpService.getWSRPBaseService(new URL(baseServiceEndpoint));
      cd.setWsrpMarkupPort(wsrpMarkupPort);
    } catch (Exception e) {
      throw new PortalException(e);
    }
    

  }

  /**
   * Sets the channel runtime data.
   * @param rd, the channel runtime data
   * @param uid, a unique ID used to identify the state of the channel
   * @throws org.jasig.portal.PortalException
   */
  public void setRuntimeData(ChannelRuntimeData rd, String uid) throws PortalException {
    ChannelState channelState = (ChannelState)channelStateMap.get(uid);
    channelState.setRuntimeData(rd);
  }

  /**
   * Sets the portal control structures.
   * @param rd, the channel runtime data
   * @param uid, a unique ID used to identify the state of the channel
   * @throws org.jasig.portal.PortalException
   */
  public void setPortalControlStructures(PortalControlStructures pcs, String uid) throws PortalException {
    ChannelState channelState = (ChannelState)channelStateMap.get(uid);
    channelState.setPortalControlStructures(pcs);
  }
  
  /**
   * Output channel content to the portal
   * @param out a sax document handler
   * @param uid a unique ID used to identify the state of the channel
   */
  public void renderXML(ContentHandler out, String uid) throws PortalException {
    ChannelState channelState = (ChannelState)channelStateMap.get(uid);
    ChannelStaticData staticData = channelState.getStaticData();
    ChannelRuntimeData runtimeData = channelState.getRuntimeData();
    PortalControlStructures pcs = channelState.getPortalControlStructures();
    ChannelData cd = channelState.getChannelData();

    System.out.println("Rendering inside WSRP Consumer");
      
    String fname = "salon.com";
    

    try {
      
      // Registration context
      //RegistrationContext registrationContext = cd.getRegistrationContext();
      
      // Portlet context
      PortletContext portletContext = new PortletContext();
      portletContext.setPortletHandle(fname);
      
      // Runtime context
      RuntimeContext runtimeContext = new RuntimeContext();
      runtimeContext.setUserAuthentication("wsrp:none");
      runtimeContext.setSessionID(cd.getSessionId());
      
      // User context
      UserContext userContext = new UserContext();
      UserProfile userProfile = new UserProfile();
      PersonName personName = new PersonName();
	  personName.setGiven((String)staticData.getPerson().getAttribute("givenName"));
	  personName.setFamily((String)staticData.getPerson().getAttribute("sn"));
      userProfile.setName(personName);
      userContext.setProfile(userProfile);
      
      // Markup params
      MarkupParams markupParams = new MarkupParams();
      ClientData clientData = new ClientData();
      clientData.setUserAgent(runtimeData.getBrowserInfo().getUserAgent());
      markupParams.setClientData(clientData);
      markupParams.setSecureClientCommunication(false); // is consumer currently communicating securly with end user?
      markupParams.setLocales(getLocalesAsStringArray(runtimeData.getLocales()));
      MediaManager mediaManager = new MediaManager();
      markupParams.setMimeTypes(new String[] { mediaManager.getReturnMimeType(mediaManager.getMedia(runtimeData.getBrowserInfo())) });
      markupParams.setMode("wsrp:view"); // can be a different mode
      markupParams.setWindowState("wsrp:normal");
      //markupParams.setNavigationalState(""); // ???
      markupParams.setMarkupCharacterSets(new String[] {"UTF-8"});
      //markupParams.setValidateTag(""); // ???
      //markupParams.setValidNewModes(null);
      //markupParams.setValidNewWindowStates(null);
      
      MarkupContextHolder markupContext = new MarkupContextHolder();     
      SessionContextHolder sessionContext = new SessionContextHolder();
      ExtensionArrayHolder extensions = new ExtensionArrayHolder();
      
      System.out.println("Calling getMarkup()");
      
      cd.getWsrpMarkupPort().getMarkup(registrationContext, portletContext, runtimeContext, userContext, markupParams, markupContext, sessionContext, extensions);
      MarkupResponse markupResponse = new MarkupResponse();
      markupResponse.setMarkupContext(markupContext.value);
      markupResponse.setSessionContext(sessionContext.value);
      markupResponse.setExtensions(extensions.value);
      
      
      System.out.println("Called getMarkup()");
      
      String markupString = markupResponse.getMarkupContext().getMarkupString();
      String sessionId = markupResponse.getSessionContext().getSessionID();
      cd.setSessionId(sessionId);
      
      System.out.println("markupString=" + markupString);
      System.out.println("sessionId=" + sessionId);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
  
  private static String[] getLocalesAsStringArray(Locale[] locales) {
    String[] localesStringArray = new String[locales.length];
    for (int i = 0; i < locales.length; i++) {
      localesStringArray[i] = locales[i].toString();
    }
    return localesStringArray;
  }

  private ServiceDescription getServiceDescription(String serviceDescriptionServiceEndpoint) throws PortalException {

    ServiceDescription serviceDescription = (ServiceDescription)serviceDescriptionMap.get(serviceDescriptionServiceEndpoint);
    if (serviceDescription == null) {
    
      WSRP_v1_ServiceDescription_PortType wsrpServiceDescriptionPort = null;
      try {
        wsrpServiceDescriptionPort = wsrpService.getWSRPServiceDescriptionService(new URL(serviceDescriptionServiceEndpoint));
      } catch (Exception e) {
        throw new PortalException(e);
      }
      
      try {
        //RegistrationContext registrationContext = cd.getRegistrationContext();      
        String[] desiredLocales = new String[] { "en_US" }; // how is this different than the locales sent in getMarkup()?
        BooleanHolder requiresRegistration = new BooleanHolder();
        PortletDescriptionArrayHolder offeredPortlets = new PortletDescriptionArrayHolder();
        ItemDescriptionArrayHolder userCategoryDescriptions = new ItemDescriptionArrayHolder(); 
        ItemDescriptionArrayHolder customUserProfileItemDescriptions = new ItemDescriptionArrayHolder();
        ItemDescriptionArrayHolder customWindowStateDescriptions = new ItemDescriptionArrayHolder();
        ItemDescriptionArrayHolder customModeDescriptions = new ItemDescriptionArrayHolder();
        CookieProtocolHolder requiresInitCookie = new CookieProtocolHolder();
        ModelDescriptionHolder registrationPropertyDescription = new ModelDescriptionHolder();
        StringArrayHolder locales = new StringArrayHolder();
        ResourceListHolder resourceList = new ResourceListHolder();
        ExtensionArrayHolder extensions = new ExtensionArrayHolder();
        wsrpServiceDescriptionPort.getServiceDescription(registrationContext, desiredLocales, requiresRegistration, offeredPortlets, userCategoryDescriptions, customUserProfileItemDescriptions, customWindowStateDescriptions, customModeDescriptions, requiresInitCookie, registrationPropertyDescription, locales, resourceList, extensions);
        
        // Store the objects that are returned
        serviceDescription = new ServiceDescription();
        serviceDescription.setRequiresRegistration(requiresRegistration.value);
        serviceDescription.setOfferedPortlets(offeredPortlets.value);
        serviceDescription.setUserCategoryDescriptions(userCategoryDescriptions.value);
        serviceDescription.setCustomUserProfileItemDescriptions(customUserProfileItemDescriptions.value);
        serviceDescription.setCustomWindowStateDescriptions(customWindowStateDescriptions.value);
        serviceDescription.setCustomModeDescriptions(customModeDescriptions.value);
        serviceDescription.setRequiresInitCookie(requiresInitCookie.value);
        serviceDescription.setRegistrationPropertyDescription(registrationPropertyDescription.value);
        serviceDescription.setLocales(locales.value);
        serviceDescription.setResourceList(resourceList.value);
        serviceDescription.setExtensions(extensions.value);
  
        serviceDescriptionMap.put(serviceDescriptionServiceEndpoint, serviceDescription);
        
      } catch (Exception e) {
        throw new PortalException(e);
      }
    }
    
    return serviceDescription;
  
  }


}
