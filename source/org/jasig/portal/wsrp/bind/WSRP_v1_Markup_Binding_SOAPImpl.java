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

package org.jasig.portal.wsrp.bind;

import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.xml.rpc.holders.StringHolder;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.jasig.portal.BrowserInfo;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelFactory;
import org.jasig.portal.ChannelParameter;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelRendererFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IChannel;
import org.jasig.portal.IChannelRenderer;
import org.jasig.portal.IChannelRendererFactory;
import org.jasig.portal.MediaManager;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.InitialSecurityContextFactory;
import org.jasig.portal.security.provider.PersonImpl;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.AbsoluteURLFilter;
import org.jasig.portal.webservices.RemoteChannel;
import org.jasig.portal.wsrp.intf.WSRP_v1_Markup_PortType;
import org.jasig.portal.wsrp.types.AccessDeniedFault;
import org.jasig.portal.wsrp.types.Extension;
import org.jasig.portal.wsrp.types.InconsistentParametersFault;
import org.jasig.portal.wsrp.types.InteractionParams;
import org.jasig.portal.wsrp.types.InvalidCookieFault;
import org.jasig.portal.wsrp.types.InvalidHandleFault;
import org.jasig.portal.wsrp.types.InvalidRegistrationFault;
import org.jasig.portal.wsrp.types.InvalidSessionFault;
import org.jasig.portal.wsrp.types.InvalidUserCategoryFault;
import org.jasig.portal.wsrp.types.MarkupContext;
import org.jasig.portal.wsrp.types.MarkupParams;
import org.jasig.portal.wsrp.types.MissingParametersFault;
import org.jasig.portal.wsrp.types.OperationFailedFault;
import org.jasig.portal.wsrp.types.PortletContext;
import org.jasig.portal.wsrp.types.PortletStateChangeRequiredFault;
import org.jasig.portal.wsrp.types.RegistrationContext;
import org.jasig.portal.wsrp.types.RuntimeContext;
import org.jasig.portal.wsrp.types.SessionContext;
import org.jasig.portal.wsrp.types.UnsupportedLocaleFault;
import org.jasig.portal.wsrp.types.UnsupportedMimeTypeFault;
import org.jasig.portal.wsrp.types.UnsupportedModeFault;
import org.jasig.portal.wsrp.types.UnsupportedWindowStateFault;
import org.jasig.portal.wsrp.types.UpdateResponse;
import org.jasig.portal.wsrp.types.UserContext;
import org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder;
import org.jasig.portal.wsrp.types.holders.MarkupContextHolder;
import org.jasig.portal.wsrp.types.holders.SessionContextHolder;
import org.jasig.portal.wsrp.types.holders.UpdateResponseHolder;

/**
 *
 * WSRP_v1_Markup_Binding_SOAPImpl.java
 *
 * This file was originally auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 * 
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class WSRP_v1_Markup_Binding_SOAPImpl implements WSRP_v1_Markup_PortType {

    protected static String baseUrl = null;
    protected static final Random randomNumberGenerator = new Random();
    // Change portal.properties and then change the following line!
    private static final IChannelRendererFactory channelRendererFactory = ChannelRendererFactory.newInstance(RemoteChannel.class.getName());

    public void getMarkup(RegistrationContext registrationContext, PortletContext portletContext, RuntimeContext runtimeContext, UserContext userContext, MarkupParams markupParams, MarkupContextHolder markupContext, SessionContextHolder sessionContext, ExtensionArrayHolder extensions) throws RemoteException, InconsistentParametersFault, InvalidRegistrationFault, MissingParametersFault, OperationFailedFault, UnsupportedMimeTypeFault, UnsupportedModeFault, UnsupportedLocaleFault, InvalidUserCategoryFault, InvalidSessionFault, InvalidCookieFault, AccessDeniedFault, InvalidHandleFault, UnsupportedWindowStateFault {
        // Initialize return values
        markupContext.value = new MarkupContext();
        sessionContext.value = new SessionContext();
        extensions.value = new Extension[0];

        String regHandle = registrationContext.getRegistrationHandle();

        String portletHandle = portletContext.getPortletHandle();

        String userAuth = runtimeContext.getUserAuthentication();
        String sessionId = runtimeContext.getSessionID();

        String user = userContext.getProfile().getName().getGiven();
    
        // Locate the channel by portletHandle (uPortal calls this an "fname")
        ChannelDefinition channelDef = null;
        try {
            channelDef = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl().getChannelDefinition(portletHandle);
        } catch (Exception e) {
            // Do nothing
        }
        if (channelDef == null) {
            LogService.log(LogService.DEBUG, "Unable to find a channel with functional name '" + portletHandle + "'");      
            throw new InvalidHandleFault();
        }
    
        try {
            // Make sure user is authorized to access this channel
            IPerson person = getPerson();
            EntityIdentifier ei = person.getEntityIdentifier();
            IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
            int channelPublishId = channelDef.getId();
            boolean authorized = ap.canSubscribe(channelPublishId);
            if (!authorized) {
                LogService.log(LogService.DEBUG, "User '" + person.getAttribute(IPerson.USERNAME) + "' is not authorized to access channel with functional name '" + portletHandle + "'");
                throw new AccessDeniedFault();
            }
            
            // Instantiate channel
            // Should this block be synchronized?
            String javaClass = channelDef.getJavaClass();
            String instanceId = Long.toHexString(randomNumberGenerator.nextLong()) + "_" + System.currentTimeMillis();    
            String uid = sessionId + "/" + instanceId;
            IChannel channel = ChannelFactory.instantiateChannel(javaClass, uid);
            
            // Construct channel static data
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
            
            // Construct channel runtime data
            ChannelRuntimeData runtimeData = new ChannelRuntimeData();
            runtimeData.setBaseActionURL(runtimeContext.getTemplates().getRenderTemplate());
            String userAgent = markupParams.getClientData().getUserAgent();
            Map headers = new HashMap(1);
            headers.put("user-agent", userAgent);
            runtimeData.setBrowserInfo(new BrowserInfo(new Cookie[] {}, headers));
            runtimeData.setHttpRequestMethod("GET");
            runtimeData.setLocales(null); // get this from markup params
            // there is more to set!!!!
            
            // Start rendering
            IChannelRenderer cr = channelRendererFactory.newInstance(channel, runtimeData);
            cr.setTimeout(channelDef.getTimeout());
            cr.startRendering();

            StringWriter sw = new StringWriter();
            MediaManager mm = new MediaManager();
            BaseMarkupSerializer markupSerializer = mm.getSerializerByName("XHTML", sw);
            markupSerializer.asContentHandler();

            // Insert a filter to re-write URLs
            String media = mm.getMedia(runtimeData.getBrowserInfo());
            String mimeType = mm.getReturnMimeType(media);
            String baseUrl = getBaseUrl(); // for images, etc.
            AbsoluteURLFilter urlFilter = AbsoluteURLFilter.newAbsoluteURLFilter(mimeType, baseUrl, markupSerializer);
   
            // Begin chain: channel renderer --> URL filter --> SAX2DOM transformer
            int status = cr.outputRendering(urlFilter);      
            if (status == IChannelRenderer.RENDERING_TIMED_OUT) {
                LogService.log(LogService.DEBUG, portletHandle + " timed out");
                throw new OperationFailedFault();
            }
            
            // Provide the markup string
            markupContext.value.setMarkupString(sw.toString());
            
            // Producer will rewrite the URLs, not the Consumer
            markupContext.value.setRequiresUrlRewriting(Boolean.FALSE);
            
        } catch (Throwable t) {
            t.printStackTrace();
        }
        
        sessionContext.value.setSessionID("put a session ID here");
    }

    public void performBlockingInteraction(RegistrationContext registrationContext, PortletContext portletContext, RuntimeContext runtimeContext, UserContext userContext, MarkupParams markupParams, InteractionParams interactionParams, UpdateResponseHolder updateResponse, StringHolder redirectURL, ExtensionArrayHolder extensions) throws RemoteException, InconsistentParametersFault, InvalidRegistrationFault, MissingParametersFault, OperationFailedFault, UnsupportedMimeTypeFault, UnsupportedModeFault, UnsupportedLocaleFault, InvalidUserCategoryFault, InvalidSessionFault, InvalidCookieFault, PortletStateChangeRequiredFault, AccessDeniedFault, InvalidHandleFault, UnsupportedWindowStateFault {
        // Initialize return values
        updateResponse.value = new UpdateResponse();
        redirectURL.value = new String();
        extensions.value = new Extension[0];
        
        // TODO: Get the runtimeData out of InteractionParams
        // TODO: Figure out some way of storing the channel instance in the session
        
    }

    public Extension[] releaseSessions(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, java.lang.String[] sessionIDs) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault {
        return null;
    }

    public Extension[] initCookie(org.jasig.portal.wsrp.types.RegistrationContext registrationContext) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.AccessDeniedFault {
        return null;
    }

    // Helper methods

    /**
     * Looks in session for an IPerson.  If it doesn't find one,
     * a "guest" person object is returned
     * @return person the IPerson object
     * @throws org.jasig.portal.PortalException  
     */
    protected IPerson getPerson() throws PortalException {
        return getGuestPerson();
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
        person.setAttribute(IPerson.USERNAME, "guest");
        return person;
    }
    
    /**
     * Returns the server's base URL which can be 
     * prepended to relative resource paths.
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
