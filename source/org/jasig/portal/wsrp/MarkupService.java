/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
