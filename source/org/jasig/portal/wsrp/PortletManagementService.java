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

import org.jasig.portal.wsrp.intf.WSRP_v1_PortletManagement_PortType;
import org.jasig.portal.wsrp.types.DestroyPortletsResponse;
import org.jasig.portal.wsrp.types.PortletContext;
import org.jasig.portal.wsrp.types.PortletDescriptionResponse;
import org.jasig.portal.wsrp.types.PortletPropertyDescriptionResponse;
import org.jasig.portal.wsrp.types.PropertyList;
import org.jasig.portal.wsrp.types.RegistrationContext;
import org.jasig.portal.wsrp.types.UserContext;
import org.jasig.portal.wsrp.types.holders.DestroyFailedArrayHolder;
import org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder;
import org.jasig.portal.wsrp.types.holders.ModelDescriptionHolder;
import org.jasig.portal.wsrp.types.holders.PortletDescriptionHolder;
import org.jasig.portal.wsrp.types.holders.PropertyArrayHolder;
import org.jasig.portal.wsrp.types.holders.ResetPropertyArrayHolder;
import org.jasig.portal.wsrp.types.holders.ResourceListHolder;
import org.jasig.portal.wsrp.wsdl.WSRPServiceLocator;

/**
 * This class is a helper class that makes it easier
 * for a WSRP consumer to call the methods of the
 * PortletManagementService API for a particular service endpoint.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class PortletManagementService {
    WSRPServiceLocator locator = new WSRPServiceLocator();
    WSRP_v1_PortletManagement_PortType pt = null;
    private static final String serviceName = "WSRPPortletManagementService";
    private static final Map services = new WeakHashMap();

    private PortletManagementService(String baseEndpoint) throws Exception {
        if (!baseEndpoint.endsWith("/")) {
            baseEndpoint += "/";
        }
        String serviceEndpoint = baseEndpoint + serviceName;
        pt = locator.getWSRPPortletManagementService(new URL(serviceEndpoint));
    }
    
    public static PortletManagementService getService(String baseEndpoint) throws Exception {
        PortletManagementService service = (PortletManagementService)services.get(baseEndpoint);
        if (service == null) {
            service = new PortletManagementService(baseEndpoint);
            services.put(baseEndpoint, service);
        }
        return service;
    }

    public PortletDescriptionResponse getPortletDescription(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, String[] desiredLocales) throws Exception {
        // Initialize holders
        PortletDescriptionHolder portletDescription = new PortletDescriptionHolder();
        ResourceListHolder resourceList = new ResourceListHolder();
        ExtensionArrayHolder extensions = new ExtensionArrayHolder();
        
        // Call the real service method which fills holders with values
        pt.getPortletDescription(registrationContext, portletContext, userContext, desiredLocales, portletDescription, resourceList, extensions);

        // Set the return values
        PortletDescriptionResponse portletDescriptionResponse = new PortletDescriptionResponse();
        portletDescriptionResponse.setPortletDescription(portletDescription.value);
        portletDescriptionResponse.setResourceList(resourceList.value);
        portletDescriptionResponse.setExtensions(extensions.value);
        
        return portletDescriptionResponse;
    }
    
    public PortletContext clonePortlet(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext) throws Exception {
        // Initialize holders
        StringHolder portletHandle = new StringHolder();
        ByteArrayHolder portletState = new ByteArrayHolder();
        ExtensionArrayHolder extensions = new ExtensionArrayHolder();
                
        // Call the real service method which fills holders with values
        pt.clonePortlet(registrationContext, portletContext, userContext, portletHandle, portletState, extensions);
        
        // Set the return values
        PortletContext clonePortletResponse = new PortletContext();
        clonePortletResponse.setPortletHandle(portletHandle.value);
        clonePortletResponse.setPortletState(portletState.value);
        clonePortletResponse.setExtensions(extensions.value);
        
        return clonePortletResponse;
    }
    
    public DestroyPortletsResponse destroyPortlets(RegistrationContext registrationContext, String[] portletHandles) throws Exception {
        // Initialize holders
        DestroyFailedArrayHolder destroyFailed = new DestroyFailedArrayHolder();
        ExtensionArrayHolder extensions = new ExtensionArrayHolder();
        
        // Call the real service method which fills holders with values
        pt.destroyPortlets(registrationContext, portletHandles, destroyFailed, extensions);
        
        // Set the return value
        DestroyPortletsResponse destroyPortletsResponse = new DestroyPortletsResponse();
        destroyPortletsResponse.setDestroyFailed(destroyFailed.value);
        destroyPortletsResponse.setExtensions(extensions.value);
        
        return destroyPortletsResponse;
    }
    
    public PortletContext setPortletProperties(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, PropertyList propertyList) throws Exception {
        // Initialize holders
        StringHolder portletHandle = new StringHolder();
        ByteArrayHolder portletState = new ByteArrayHolder();
        ExtensionArrayHolder extensions = new ExtensionArrayHolder();
        
        // Call the real service method which fills holders with values
        pt.setPortletProperties(registrationContext, portletContext, userContext, propertyList, portletHandle, portletState, extensions);
        
        // Set the return value
        PortletContext setPortletPropertiesResponse = new PortletContext();
        setPortletPropertiesResponse.setPortletHandle(portletHandle.value);
        setPortletPropertiesResponse.setPortletState(portletState.value);
        setPortletPropertiesResponse.setExtensions(extensions.value);
        
        return setPortletPropertiesResponse; 
    }
    
    public PropertyList getPortletProperties(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, String[] names) throws Exception {
        // Initialize holders
        PropertyArrayHolder properties = new PropertyArrayHolder();
        ResetPropertyArrayHolder resetProperties = new ResetPropertyArrayHolder();
        ExtensionArrayHolder extensions = new ExtensionArrayHolder();

        // Call the real service method which fills holders with values
        pt.getPortletProperties(registrationContext, portletContext, userContext, names, properties, resetProperties, extensions);
        
        // Set the return value
        PropertyList propertyList = new PropertyList();
        propertyList.setProperties(properties.value);
        propertyList.setResetProperties(resetProperties.value);
        propertyList.setExtensions(extensions.value);
        
        return propertyList;
    }
          
    public PortletPropertyDescriptionResponse getPortletPropertyDescription(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, String[] desiredLocales) throws Exception {
        // Initialize holders
        ModelDescriptionHolder modelDescription = new ModelDescriptionHolder();
        ResourceListHolder resourceList = new ResourceListHolder();
        ExtensionArrayHolder extensions = new ExtensionArrayHolder();

        // Call the real service method which fills holders with values
        pt.getPortletPropertyDescription(registrationContext, portletContext, userContext, desiredLocales, modelDescription, resourceList, extensions);
        
        // Set the return value
        PortletPropertyDescriptionResponse portletPropertyDescriptionResponse = new PortletPropertyDescriptionResponse();
        portletPropertyDescriptionResponse.setModelDescription(modelDescription.value);
        portletPropertyDescriptionResponse.setResourceList(resourceList.value);
        portletPropertyDescriptionResponse.setExtensions(extensions.value);
        
        return portletPropertyDescriptionResponse;
    }
}
