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

import javax.xml.rpc.holders.ByteArrayHolder;
import javax.xml.rpc.holders.StringHolder;

import org.jasig.portal.wsrp.intf.WSRP_v1_Registration_PortType;
import org.jasig.portal.wsrp.types.Property;
import org.jasig.portal.wsrp.types.RegistrationContext;
import org.jasig.portal.wsrp.types.RegistrationData;
import org.jasig.portal.wsrp.types.RegistrationState;
import org.jasig.portal.wsrp.types.ReturnAny;
import org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder;
import org.jasig.portal.wsrp.wsdl.WSRPServiceLocator;

/**
 * This class is a helper class that makes it easier
 * for a WSRP consumer to call the methods of the
 * RegistrationService API for a particular service endpoint.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class RegistrationService {
    WSRPServiceLocator locator = new WSRPServiceLocator();
    WSRP_v1_Registration_PortType pt = null;
    private static final String serviceName = "WSRPRegistrationService";
    private static final Map services = new WeakHashMap();

    private RegistrationService(String baseEndpoint) throws Exception {
        if (!baseEndpoint.endsWith("/")) {
            baseEndpoint += "/";
        }
        String serviceEndpoint = baseEndpoint + serviceName;
        pt = locator.getWSRPRegistrationService(new URL(serviceEndpoint));
    }
    
    public static RegistrationService getService(String baseEndpoint) throws Exception {
        RegistrationService service = (RegistrationService)services.get(baseEndpoint);
        if (service == null) {
            service = new RegistrationService(baseEndpoint);
            services.put(baseEndpoint, service);
        }
        return service;
    }

    public RegistrationContext register(String consumerName, String consumerAgent, boolean methodGetSupported, String[] consumerModes, String[] consumerWindowStates, String[] consumerUserScopes, String[] customUserProfileData, Property[] registrationProperties) throws Exception {
        // Initialize holders
        ExtensionArrayHolder extensions = new ExtensionArrayHolder();
        StringHolder registrationHandle = new StringHolder();
        ByteArrayHolder registrationState = new ByteArrayHolder();
        
        // Call the real service method which fills holders with values
        pt.register(consumerName, consumerAgent, methodGetSupported, consumerModes, consumerWindowStates, consumerUserScopes, customUserProfileData, registrationProperties, extensions, registrationHandle, registrationState);

        // Set the return value
        RegistrationContext registrationContext = new RegistrationContext();
        registrationContext.setExtensions(extensions.value);
        registrationContext.setRegistrationHandle(registrationHandle.value);
        registrationContext.setRegistrationState(registrationState.value);
        
        return registrationContext;
    }
    
    public ReturnAny deregister(String registrationHandle, byte[] registrationState) throws Exception {
        // Initialize holders
        ExtensionArrayHolder extensions = new ExtensionArrayHolder();

        // Call the real service method which fills holders with values
        pt.deregister(registrationHandle, registrationState, extensions);
        
        // Set the return value
        ReturnAny returnAny = new ReturnAny();
        returnAny.setExtensions(extensions.value);
        
        return returnAny;
    }
    
    public RegistrationState modifyRegistration(RegistrationContext registrationContext, RegistrationData registrationData) throws Exception {
        // Initialize holders
        ByteArrayHolder registrationState = new ByteArrayHolder();
        ExtensionArrayHolder extensions = new ExtensionArrayHolder();

        // Call the real service method which fills holders with values
        pt.modifyRegistration(registrationContext, registrationData, registrationState, extensions);
        
        // Set the return value
        RegistrationState modifyRegistrationResponse = new RegistrationState();
        modifyRegistrationResponse.setRegistrationState(registrationState.value);
        modifyRegistrationResponse.setExtensions(extensions.value);
        
        return modifyRegistrationResponse;
    }
}
