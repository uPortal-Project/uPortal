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
package org.jasig.portal.wsrp;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

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
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.IChannelRenderer;
import org.jasig.portal.IChannelRendererFactory;
import org.jasig.portal.MediaManager;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.services.AuthorizationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.AbsoluteURLFilter;
import org.jasig.portal.wsrp.types.AccessDeniedFault;
import org.jasig.portal.wsrp.types.Fault;
import org.jasig.portal.wsrp.types.InteractionParams;
import org.jasig.portal.wsrp.types.InvalidHandleFault;
import org.jasig.portal.wsrp.types.MarkupParams;
import org.jasig.portal.wsrp.types.NamedString;
import org.jasig.portal.wsrp.types.OperationFailedFault;
import org.jasig.portal.wsrp.types.RuntimeContext;
import org.jasig.portal.wsrp.types.UserContext;

/**
 * Manages the interactions between the WSRP consumer
 * and one channel instance.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class ChannelInstanceManager {
    
    private static final Log log = LogFactory.getLog(ChannelInstanceManager.class);
    
    private IChannel channel;
    private ChannelDefinition channelDef;
    private ChannelRuntimeData runtimeData;
    private IPerson person;

    private static final IChannelRegistryStore channelRegistryStore = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl();
    protected static final Random randomNumberGenerator = new Random();
    private static final IChannelRendererFactory channelRendererFactory = ChannelRendererFactory.newInstance(ChannelInstanceManager.class.getName());
    protected static String baseUrl;

    /**
     * Constructs a ChannelInstanceManager.
     * @param portletHandle the portlet handle
     * @param sessionId the session ID
     * @param userContext the user context
     */
    public ChannelInstanceManager(String portletHandle, String sessionId, UserContext userContext) throws Exception {
        person = getPerson(userContext);
        channelDef = getChannelDefinition(portletHandle);
        channel = getChannel(sessionId, channelDef);
    }
    
    /**
     * Accessor to IChannel.
     * @return the channel instance
     */
    public IChannel getChannel() {
        return this.channel;
    }

    /**
     * Retrieves channel runtime data.
     * @param runtimeContext the channel runtime context
     * @param markupParams the channel markup parameters
     * @param interactionParams the channel interaction parameters
     */
    public void setChannelRuntimeData(RuntimeContext runtimeContext, MarkupParams markupParams, InteractionParams interactionParams) throws PortalException {
        // Construct channel runtime data
        runtimeData = new ChannelRuntimeData();
        runtimeData.setBaseActionURL(runtimeContext.getTemplates().getRenderTemplate());
        String userAgent = markupParams.getClientData().getUserAgent();
        Map headers = new HashMap(1);
        headers.put("user-agent", userAgent);
        runtimeData.setBrowserInfo(new BrowserInfo(new Cookie[] {}, headers));
        runtimeData.setHttpRequestMethod("GET");
        runtimeData.setLocales(null); // get this from markup params
        // there is more to set!!!!
        if (interactionParams != null) {
            NamedString[] formParams = interactionParams.getFormParameters();
            for (int i = 0; i < formParams.length; i++) {
                NamedString formParam = formParams[i];
                runtimeData.setParameter(formParam.getName(), formParam.getValue());
            }
        }
    }
    
    /**
     * Asks channel to render and retrieves channel markup.
     * @param runtimeContext the channel runtime context
     * @param markupParams the channel markup parameters
     */
    public String getChannelMarkup(RuntimeContext runtimeContext, MarkupParams markupParams) throws Throwable {
        // Set up channel runtime data if user hasn't yet interacted with the channel
        if (runtimeData == null) {
            setChannelRuntimeData(runtimeContext, markupParams, (InteractionParams)null);
        }
        
        // Set mode
        String mode = markupParams.getMode();
        if (mode.equals(Constants.WSRP_HELP)) {
            channel.receiveEvent(new PortalEvent(PortalEvent.HELP_BUTTON_EVENT));
        } else if (mode.equals(Constants.WSRP_EDIT)) {
            channel.receiveEvent(new PortalEvent(PortalEvent.EDIT_BUTTON_EVENT));
        } else if (mode.equals(Constants.UP_ABOUT)) {
            channel.receiveEvent(new PortalEvent(PortalEvent.ABOUT_BUTTON_EVENT));
        }
           
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
            log.debug(channelDef.getFName() + " timed out");
            throw new OperationFailedFault();
        }
           
        // Get rid of channel runtime data
        runtimeData = null;
            
        // Provide the markup string
        return sw.toString();        
    }
        
    /**
     * Retrieves a channel definition.  The underlying implementation
     * if IChannelRegistryStore does the caching.
     * @return channelDef a channel definition
     * @throws java.lang.Exception
     */
    protected ChannelDefinition getChannelDefinition(String portletHandle) throws Exception {
        // Locate the channel by portletHandle (uPortal calls this an "fname")
        ChannelDefinition channelDef = null;
        try {
            channelDef = channelRegistryStore.getChannelDefinition(portletHandle);
        } catch (Exception e) {
            // Do nothing
        }
        if (channelDef == null) {
            log.debug("Unable to find a channel with functional name '" + portletHandle + "'");      
            throw new InvalidHandleFault();
        }
        return channelDef;        
    }
    
    /**
     * Retrieves a channel instance for this session.
     * @return channel a channel instance for this session
     * @throws java.lang.Exception
     */
    protected IChannel getChannel(String sessionId, ChannelDefinition channelDef) throws Exception {
        // Make sure user is authorized to access this channel
        EntityIdentifier ei = person.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        int channelPublishId = channelDef.getId();
        boolean authorized = ap.canSubscribe(channelPublishId);
        if (!authorized) {
            String message = "User [" + person.getAttribute(IPerson.USERNAME) + "] is not authorized to access channel with functional name [" + channelDef.getFName() + "]";
            log.debug(message);
            Fault accessDeniedFault = new AccessDeniedFault();
            accessDeniedFault.setFaultString(message);
            throw accessDeniedFault;
        }
        
        // Instantiate channel
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
                    
        return channel;
    }
    
    /**
     * Looks in session for an IPerson.  If it doesn't find one,
     * a "guest" person object is returned
     * @param userContext the WSRP user context
     * @return person an IPerson object derived from the WSRP user context
     * @throws org.jasig.portal.PortalException  
     */
    protected IPerson getPerson(UserContext userContext) throws PortalException {
      try {	
          return PersonFactory.createGuestPerson();
      } catch ( Exception e ) {
      	  throw new PortalException(e);
      }
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
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();
            baseUrl = scheme + "://" + serverName + ":" + serverPort + contextPath + "/";
        }
        return baseUrl;
    }           
}
