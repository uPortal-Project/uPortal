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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.rpc.holders.BooleanHolder;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.LogService;
import org.jasig.portal.wsrp.Constants;
import org.jasig.portal.wsrp.intf.WSRP_v1_ServiceDescription_PortType;
import org.jasig.portal.wsrp.types.CookieProtocol;
import org.jasig.portal.wsrp.types.Extension;
import org.jasig.portal.wsrp.types.InvalidRegistrationFault;
import org.jasig.portal.wsrp.types.ItemDescription;
import org.jasig.portal.wsrp.types.LocalizedString;
import org.jasig.portal.wsrp.types.MarkupType;
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
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
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
        
        // Requires registration
        requiresRegistration.value = false;
        
        // Offered portlets
        try {
            IChannelRegistryStore crs = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl();
            ChannelDefinition[] chanDefs = crs.getChannelDefinitions();
            List portletDescriptionList = new ArrayList(chanDefs.length);
            for (int i = 0; i < chanDefs.length; i++) {
                ChannelDefinition chanDef = chanDefs[i];
                PortletDescription portletDescription = new PortletDescription();
                portletDescription.setPortletHandle(chanDef.getFName());
                MarkupType markupType = new MarkupType();
                markupType.setMimeType("*");
                List modeList = new ArrayList();
                if (chanDef.isEditable()) {
                    modeList.add(Constants.WSRP_EDIT);
                }
                if (chanDef.hasHelp()) {
                    modeList.add(Constants.WSRP_HELP);
                }
                markupType.setModes((String[])modeList.toArray(new String[0]));
                markupType.setWindowStates(new String[] { Constants.WSRP_NORMAL, Constants.WSRP_MINIMIZED, Constants.WSRP_MAXIMIZED, Constants.WSRP_SOLO});                
                portletDescription.setMarkupTypes(new MarkupType[] {markupType}); // We can't really know what a channel supports
                portletDescriptionList.add(portletDescription);
            }
            offeredPortlets.value = (PortletDescription[])portletDescriptionList.toArray(new PortletDescription[0]);
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        }
        
        // User category descriptions
        try {
            List userCategoryDescriptionList = new ArrayList();
            IEntityGroup everyone = GroupService.getDistinguishedGroup(GroupService.EVERYONE);
            Iterator iter = everyone.getAllMembers();
            while (iter.hasNext()) {
                IGroupMember gm = (IGroupMember)iter.next();
                if (gm.isGroup()) {
                    IEntityGroup eg = (IEntityGroup)gm;
                    ItemDescription itemDescription = new ItemDescription();
                    itemDescription.setItemName(eg.getName());
                    LocalizedString ls = new LocalizedString();
                    ls.setValue(eg.getDescription());
                    itemDescription.setDescription(ls);
                    userCategoryDescriptionList.add(itemDescription);
                }
            }
            userCategoryDescriptions.value = (ItemDescription[])userCategoryDescriptionList.toArray(new ItemDescription[0]);
        } catch (PortalException pe) {
            LogService.log(LogService.ERROR, pe);
        }
        
        // Custom user profile item descriptions
        // None yet
        
        // Custom window state descriptions
        // None yet
        
        // Custom mode descriptions
        // None yet
        
        // Requires initCookie
        requiresInitCookie.value = CookieProtocol.none;
        
        // Registration property description
        // None yet
        
        // Locales
        // Not sure what should go here yet
        
        // Resource list
        // Not sure what should go here yet
    }
}
