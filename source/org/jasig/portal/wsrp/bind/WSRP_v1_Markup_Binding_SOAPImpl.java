/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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

import javax.xml.rpc.holders.StringHolder;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
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

  public void getMarkup(RegistrationContext registrationContext, PortletContext portletContext, RuntimeContext runtimeContext, UserContext userContext, MarkupParams markupParams, MarkupContextHolder markupContext, SessionContextHolder sessionContext, ExtensionArrayHolder extensions) throws java.rmi.RemoteException, InvalidUserCategoryFault, InvalidSessionFault, InconsistentParametersFault, InvalidRegistrationFault, OperationFailedFault, MissingParametersFault, InvalidCookieFault, UnsupportedMimeTypeFault, UnsupportedLocaleFault, UnsupportedModeFault, AccessDeniedFault, InvalidHandleFault, UnsupportedWindowStateFault {

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
      ChannelRegistryStoreFactory.getChannelRegistryStoreImpl().getChannelDefinition(portletHandle);
    } catch (Exception e) {
      // Do nothing
    }
    if (channelDef == null) {
       //System.out.println("Unable to find a channel with functional name '" + portletHandle + "'");      
       throw new InvalidHandleFault();
    }
    

    markupContext.value.setMarkupString("<b>Hello there.</b>");

    sessionContext.value.setSessionID("put a session ID here");
  }

  public void performBlockingInteraction(RegistrationContext registrationContext, PortletContext portletContext, RuntimeContext runtimeContext, UserContext userContext, MarkupParams markupParams, InteractionParams interactionParams, UpdateResponseHolder updateResponse, StringHolder redirectURL, ExtensionArrayHolder extensions) throws RemoteException, InvalidUserCategoryFault, InvalidSessionFault, InconsistentParametersFault, InvalidRegistrationFault, OperationFailedFault, MissingParametersFault, InvalidCookieFault, UnsupportedMimeTypeFault, UnsupportedLocaleFault, UnsupportedModeFault, PortletStateChangeRequiredFault, AccessDeniedFault, InvalidHandleFault, UnsupportedWindowStateFault {
    updateResponse.value = new UpdateResponse();
    redirectURL.value = new String();
    extensions.value = new Extension[0];
  }

  public Extension[] releaseSessions(RegistrationContext registrationContext, String[] sessionIDs) throws java.rmi.RemoteException, InvalidRegistrationFault, OperationFailedFault, MissingParametersFault, AccessDeniedFault {
    return null;
  }

  public Extension[] initCookie(RegistrationContext registrationContext) throws RemoteException, InvalidRegistrationFault, OperationFailedFault, AccessDeniedFault {
    return null;
  }

}
