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

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.rpc.holders.StringHolder;

import org.jasig.portal.wsrp.ChannelInstanceManager;
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
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class WSRP_v1_Markup_Binding_SOAPImpl implements WSRP_v1_Markup_PortType {

    protected static final Random randomNumberGenerator = new Random();

    // Maps session ID --> ChannelInstanceManager (cim) instance
    private static final Map cimCache = Collections.synchronizedMap(new HashMap());

    public void getMarkup(RegistrationContext registrationContext, 
                          PortletContext portletContext, 
                          RuntimeContext runtimeContext, 
                          UserContext userContext, 
                          MarkupParams markupParams, 
                          MarkupContextHolder markupContext, 
                          SessionContextHolder sessionContext, 
                          ExtensionArrayHolder extensions) throws RemoteException, InconsistentParametersFault, InvalidRegistrationFault, MissingParametersFault, OperationFailedFault, UnsupportedMimeTypeFault, UnsupportedModeFault, UnsupportedLocaleFault, InvalidUserCategoryFault, InvalidSessionFault, InvalidCookieFault, AccessDeniedFault, InvalidHandleFault, UnsupportedWindowStateFault {
        // Initialize return values
        markupContext.value = new MarkupContext();
        sessionContext.value = new SessionContext();
        extensions.value = new Extension[0];

        String portletHandle = portletContext.getPortletHandle();

        // Establish session ID. If this request contains a session ID,
        // then use it, otherwise generate a new one
        String sessionId = runtimeContext.getSessionID();
        if (sessionId == null) {
            sessionId = "wsrpsid:" + Long.toHexString(randomNumberGenerator.nextLong()) + "_" + System.currentTimeMillis();            
        }
        sessionContext.value.setSessionID(sessionId);
        
        try {
            // Obtain the channel markup
            ChannelInstanceManager cim = getChannelInstanceManager(sessionId, portletHandle, userContext);
            String markup = cim.getChannelMarkup(runtimeContext, markupParams);
            // We cannot implement caching because the WSRP and portlet
            // caching implementation only deals with expiration based caching.
            // uPortal channel caching is much more flexible and there is no
            // way to derive a simple expiration timeout via the ICacheable interface            
            // Consider doing something with the setUseCachedMarkup(Boolean.TRUE) method
            // in conjunction with the validateFlag.
            markupContext.value.setCacheControl(null);
            markupContext.value.setMarkupString(markup);
            
            // Producer will rewrite the URLs, not the Consumer
            markupContext.value.setRequiresUrlRewriting(Boolean.FALSE);
            
        } catch (RemoteException re) {
            throw re;
        } catch (Throwable t) {
            throw new RemoteException(t.getMessage());
        }        
    }

    public void performBlockingInteraction(RegistrationContext registrationContext, 
                                           PortletContext portletContext, 
                                           RuntimeContext runtimeContext, 
                                           UserContext userContext, 
                                           MarkupParams markupParams, 
                                           InteractionParams interactionParams, 
                                           UpdateResponseHolder updateResponse, 
                                           StringHolder redirectURL, 
                                           ExtensionArrayHolder extensions) throws RemoteException, InconsistentParametersFault, InvalidRegistrationFault, MissingParametersFault, OperationFailedFault, UnsupportedMimeTypeFault, UnsupportedModeFault, UnsupportedLocaleFault, InvalidUserCategoryFault, InvalidSessionFault, InvalidCookieFault, PortletStateChangeRequiredFault, AccessDeniedFault, InvalidHandleFault, UnsupportedWindowStateFault {
        // Initialize return values
        updateResponse.value = new UpdateResponse();
        redirectURL.value = new String();
        extensions.value = new Extension[0];
        
        try {
            // Pass runtime data to channel to prepare for next call to getMarkup()
            String sessionId = runtimeContext.getSessionID();
            String portletHandle = portletContext.getPortletHandle();
            ChannelInstanceManager cim = getChannelInstanceManager(sessionId, portletHandle, userContext);
            cim.setChannelRuntimeData(runtimeContext, markupParams, interactionParams);

            // The WSRP spec says that this method could optionally return the markup
            // right away, but the consumer should call getMarkup() if it doesn't.
        } catch (RemoteException re) {
            throw re;
        } catch (Throwable t) {
            throw new RemoteException(t.getMessage());
        }        
    }

    public Extension[] releaseSessions(RegistrationContext registrationContext, String[] sessionIDs) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault {
        // Remove all the cached ChannelInstanceManagers that correspond to these session IDs
        for (int i = 0; i < sessionIDs.length; i++) {
            cimCache.remove(sessionIDs[i]);
        }
        return null;
    }

    public Extension[] initCookie(RegistrationContext registrationContext) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.AccessDeniedFault {
        return null;
    }

    // Helper methods     
    
    /**
     * Retrieves a channel instance manager for this session.
     * @param sessionId, the session ID
     * @param portletHandle, the portlet handle (channel funcitonal name)
     * @param userContext, the user context
     * @return cim, a channel instance manager for this session
     * @throws java.lang.Exception
     */
    protected ChannelInstanceManager getChannelInstanceManager(String sessionId, String portletHandle, UserContext userContext) throws Exception {
        ChannelInstanceManager cim = (ChannelInstanceManager)cimCache.get(sessionId);
        if (cim == null) {       
            cim = new ChannelInstanceManager(portletHandle, sessionId, userContext);            
            // Put the channel instance manager in the cache
            cimCache.put(sessionId, cim);
        }        
        return cim;
    }    

}
