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

import javax.xml.rpc.holders.BooleanHolder;

import org.jasig.portal.wsrp.intf.WSRP_v1_ServiceDescription_PortType;
import org.jasig.portal.wsrp.types.RegistrationContext;
import org.jasig.portal.wsrp.types.ServiceDescription;
import org.jasig.portal.wsrp.types.holders.CookieProtocolHolder;
import org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder;
import org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder;
import org.jasig.portal.wsrp.types.holders.ModelDescriptionHolder;
import org.jasig.portal.wsrp.types.holders.PortletDescriptionArrayHolder;
import org.jasig.portal.wsrp.types.holders.ResourceListHolder;
import org.jasig.portal.wsrp.types.holders.StringArrayHolder;
import org.jasig.portal.wsrp.wsdl.WSRPServiceLocator;

/**
 * This class is a helper class that makes it easier
 * for a WSRP consumer to call the methods of the
 * ServiceDescriptionService API for a particular service endpoint.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class ServiceDescriptionService {
    WSRPServiceLocator locator = new WSRPServiceLocator();
    WSRP_v1_ServiceDescription_PortType pt = null;
    private static final String serviceName = "WSRPServiceDescriptionService";
    private static final Map services = new WeakHashMap();

    private ServiceDescriptionService(String baseEndpoint) throws Exception {
        if (!baseEndpoint.endsWith("/")) {
            baseEndpoint += "/";
        }
        String serviceEndpoint = baseEndpoint + serviceName;
        pt = locator.getWSRPServiceDescriptionService(new URL(serviceEndpoint));
    }
    
    public static ServiceDescriptionService getService(String baseEndpoint) throws Exception {
        ServiceDescriptionService service = (ServiceDescriptionService)services.get(baseEndpoint);
        if (service == null) {
            service = new ServiceDescriptionService(baseEndpoint);
            services.put(baseEndpoint, service);
        }
        return service;
    }

    public ServiceDescription getServiceDescription(RegistrationContext registrationContext, String[] desiredLocales) throws Exception {

        // Initialize holders
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
        
        // Call the real service method which fills holders with values
        pt.getServiceDescription(registrationContext, desiredLocales, requiresRegistration, offeredPortlets, userCategoryDescriptions, customUserProfileItemDescriptions, customWindowStateDescriptions, customModeDescriptions, requiresInitCookie, registrationPropertyDescription, locales, resourceList, extensions);

        // Set the return values
        ServiceDescription serviceDescription = new ServiceDescription();
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
        
        return serviceDescription;
    }

}
