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

import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.rpc.holders.StringHolder;

import org.jasig.portal.wsrp.intf.WSRP_v1_Markup_PortType;
import org.jasig.portal.wsrp.types.BlockingInteractionResponse;
import org.jasig.portal.wsrp.types.Extension;
import org.jasig.portal.wsrp.types.InteractionParams;
import org.jasig.portal.wsrp.types.MarkupParams;
import org.jasig.portal.wsrp.types.MarkupResponse;
import org.jasig.portal.wsrp.types.PortletContext;
import org.jasig.portal.wsrp.types.RegistrationContext;
import org.jasig.portal.wsrp.types.RuntimeContext;
import org.jasig.portal.wsrp.types.UserContext;
import org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder;
import org.jasig.portal.wsrp.types.holders.MarkupContextHolder;
import org.jasig.portal.wsrp.types.holders.SessionContextHolder;
import org.jasig.portal.wsrp.types.holders.UpdateResponseHolder;
import org.jasig.portal.wsrp.wsdl.WSRPServiceLocator;

/**
 * This class is a helper class that makes it easier
 * for a WSRP consumer to call the methods of the
 * MarkupService API for a particular service endpoint.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class MarkupService {
    WSRPServiceLocator locator = new WSRPServiceLocator();
    WSRP_v1_Markup_PortType pt = null;
    private static final String serviceName = "WSRPBaseService";
    private static final Map services = new WeakHashMap();

    private MarkupService(String baseEndpoint) throws Exception {
        if (!baseEndpoint.endsWith("/")) {
            baseEndpoint += "/";
        }
        String serviceEndpoint = baseEndpoint + serviceName;
        pt = locator.getWSRPBaseService(new URL(serviceEndpoint));
    }
    
    public static MarkupService getService(String baseEndpoint) throws Exception {
        MarkupService service = (MarkupService)services.get(baseEndpoint);
        if (service == null) {
            service = new MarkupService(baseEndpoint);
            services.put(baseEndpoint, service);
        }
        return service;
    }

    public MarkupResponse getMarkup(RegistrationContext registrationContext, PortletContext portletContext, RuntimeContext runtimeContext, UserContext userContext, MarkupParams markupParams) throws Exception {        
        // Initialize holders
        MarkupContextHolder markupContext = new MarkupContextHolder();
        SessionContextHolder sessionContext = new SessionContextHolder();
        ExtensionArrayHolder extensions = new ExtensionArrayHolder();
        
        // Call the real service method which fills holders with values
        pt.getMarkup(registrationContext, portletContext, runtimeContext, userContext, markupParams, markupContext, sessionContext, extensions);

        // Set the return values
        MarkupResponse markupResponse = new MarkupResponse();
        markupResponse.setMarkupContext(markupContext.value);
        markupResponse.setSessionContext(sessionContext.value);
        markupResponse.setExtensions(extensions.value);
        
        return markupResponse;
    }
    
    public void performBlockingInteraction(RegistrationContext registrationContext, PortletContext portletContext, RuntimeContext runtimeContext, UserContext userContext, MarkupParams markupParams, InteractionParams interactionParams) throws Exception {  
        // Initialize holders
        UpdateResponseHolder updateResponse = new UpdateResponseHolder();
        StringHolder redirectURL = new StringHolder();
        ExtensionArrayHolder extensions = new ExtensionArrayHolder();
    
        // Call the real service method which fills holders will values
        pt.performBlockingInteraction(registrationContext, portletContext, runtimeContext, userContext, markupParams, interactionParams, updateResponse, redirectURL, extensions);

        // Set the return values
        BlockingInteractionResponse blockingInteractionResponse = new BlockingInteractionResponse();
        blockingInteractionResponse.setUpdateResponse(updateResponse.value);
        blockingInteractionResponse.setRedirectURL(redirectURL.value);
        blockingInteractionResponse.setExtensions(extensions.value);        
    }    
    
    public Extension[] releaseSessions(RegistrationContext registrationContext, String[] sessionIDs) throws Exception {
        return pt.releaseSessions(registrationContext, sessionIDs);
    }
    
    public Extension[] initCookie(RegistrationContext registrationContext) throws Exception {
        return pt.initCookie(registrationContext);
    }

}
