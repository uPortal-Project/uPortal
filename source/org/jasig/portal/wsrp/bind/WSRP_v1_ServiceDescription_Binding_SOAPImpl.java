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

import javax.xml.rpc.holders.BooleanHolder;

import org.jasig.portal.wsrp.intf.WSRP_v1_ServiceDescription_PortType;
import org.jasig.portal.wsrp.types.CookieProtocol;
import org.jasig.portal.wsrp.types.Extension;
import org.jasig.portal.wsrp.types.InvalidRegistrationFault;
import org.jasig.portal.wsrp.types.ItemDescription;
import org.jasig.portal.wsrp.types.ModelDescription;
import org.jasig.portal.wsrp.types.OperationFailedFault;
import org.jasig.portal.wsrp.types.PortletDescription;
import org.jasig.portal.wsrp.types.RegistrationContext;
import org.jasig.portal.wsrp.types.ResourceList;
import org.jasig.portal.wsrp.types.holders.CookieProtocolHolder;
import org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder;
import org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder;
import org.jasig.portal.wsrp.types.holders.ModelDescriptionHolder;
import org.jasig.portal.wsrp.types.holders.PortletDescriptionArrayHolder;
import org.jasig.portal.wsrp.types.holders.ResourceListHolder;
import org.jasig.portal.wsrp.types.holders.StringArrayHolder;

/**
 *
 * WSRP_v1_ServiceDescription_Binding_SOAPImpl.java
 *
 * This file was originally auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 * 
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class WSRP_v1_ServiceDescription_Binding_SOAPImpl implements WSRP_v1_ServiceDescription_PortType {

  public void getServiceDescription(RegistrationContext registrationContext, String[] desiredLocales, BooleanHolder requiresRegistration, PortletDescriptionArrayHolder offeredPortlets, ItemDescriptionArrayHolder userCategoryDescriptions, ItemDescriptionArrayHolder customUserProfileItemDescriptions, ItemDescriptionArrayHolder customWindowStateDescriptions, ItemDescriptionArrayHolder customModeDescriptions, CookieProtocolHolder requiresInitCookie, ModelDescriptionHolder registrationPropertyDescription, StringArrayHolder locales, ResourceListHolder resourceList, ExtensionArrayHolder extensions) throws RemoteException, InvalidRegistrationFault, OperationFailedFault {

    // Initialize return values
    requiresRegistration.value = true;
    offeredPortlets.value = new PortletDescription[0];
    userCategoryDescriptions.value = new ItemDescription[0];
    customUserProfileItemDescriptions.value = new ItemDescription[0];
    customWindowStateDescriptions.value = new ItemDescription[0];
    customModeDescriptions.value = new ItemDescription[0];
    requiresInitCookie.value = CookieProtocol.none;
    registrationPropertyDescription.value = new ModelDescription();
    locales.value = new String[0];
    resourceList.value = new ResourceList();
    extensions.value = new Extension[0];
    
    
  }

}
