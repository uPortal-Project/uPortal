/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
