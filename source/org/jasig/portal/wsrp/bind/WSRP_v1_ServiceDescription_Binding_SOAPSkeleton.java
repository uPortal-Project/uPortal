/**
 * WSRP_v1_ServiceDescription_Binding_SOAPSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.bind;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class WSRP_v1_ServiceDescription_Binding_SOAPSkeleton implements org.jasig.portal.wsrp.intf.WSRP_v1_ServiceDescription_PortType, org.apache.axis.wsdl.Skeleton {
    private org.jasig.portal.wsrp.intf.WSRP_v1_ServiceDescription_PortType impl;
    private static java.util.Map _myOperations = new java.util.Hashtable();
    private static java.util.Collection _myOperationsList = new java.util.ArrayList();

    /**
    * Returns List of OperationDesc objects with this name
    */
    public static java.util.List getOperationDescByName(java.lang.String methodName) {
        return (java.util.List)_myOperations.get(methodName);
    }

    /**
    * Returns Collection of OperationDescs
    */
    public static java.util.Collection getOperationDescs() {
        return _myOperationsList;
    }

    static {
        org.apache.axis.description.OperationDesc _oper;
        org.apache.axis.description.FaultDesc _fault;
        org.apache.axis.description.ParameterDesc [] _params;
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "desiredLocales"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "requiresRegistration"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "offeredPortlets"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletDescription"), org.jasig.portal.wsrp.types.PortletDescription[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userCategoryDescriptions"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ItemDescription"), org.jasig.portal.wsrp.types.ItemDescription[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customUserProfileItemDescriptions"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ItemDescription"), org.jasig.portal.wsrp.types.ItemDescription[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customWindowStateDescriptions"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ItemDescription"), org.jasig.portal.wsrp.types.ItemDescription[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customModeDescriptions"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ItemDescription"), org.jasig.portal.wsrp.types.ItemDescription[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "requiresInitCookie"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "CookieProtocol"), org.jasig.portal.wsrp.types.CookieProtocol.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationPropertyDescription"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ModelDescription"), org.jasig.portal.wsrp.types.ModelDescription.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "locales"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "resourceList"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ResourceList"), org.jasig.portal.wsrp.types.ResourceList.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getServiceDescription", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "getServiceDescription"));
        _oper.setSoapAction("urn:oasis:names:tc:wsrp:v1:getServiceDescription");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getServiceDescription") == null) {
            _myOperations.put("getServiceDescription", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getServiceDescription")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidRegistration");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidRegistration"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidRegistrationFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidRegistrationFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("OperationFailed");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailed"));
        _fault.setClassName("org.jasig.portal.wsrp.types.OperationFailedFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailedFault"));
        _oper.addFault(_fault);
    }

    public WSRP_v1_ServiceDescription_Binding_SOAPSkeleton() {
        this.impl = new org.jasig.portal.wsrp.bind.WSRP_v1_ServiceDescription_Binding_SOAPImpl();
    }

    public WSRP_v1_ServiceDescription_Binding_SOAPSkeleton(org.jasig.portal.wsrp.intf.WSRP_v1_ServiceDescription_PortType impl) {
        this.impl = impl;
    }
    public void getServiceDescription(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, java.lang.String[] desiredLocales, javax.xml.rpc.holders.BooleanHolder requiresRegistration, org.jasig.portal.wsrp.types.holders.PortletDescriptionArrayHolder offeredPortlets, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder userCategoryDescriptions, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder customUserProfileItemDescriptions, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder customWindowStateDescriptions, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder customModeDescriptions, org.jasig.portal.wsrp.types.holders.CookieProtocolHolder requiresInitCookie, org.jasig.portal.wsrp.types.holders.ModelDescriptionHolder registrationPropertyDescription, org.jasig.portal.wsrp.types.holders.StringArrayHolder locales, org.jasig.portal.wsrp.types.holders.ResourceListHolder resourceList, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault
    {
        impl.getServiceDescription(registrationContext, desiredLocales, requiresRegistration, offeredPortlets, userCategoryDescriptions, customUserProfileItemDescriptions, customWindowStateDescriptions, customModeDescriptions, requiresInitCookie, registrationPropertyDescription, locales, resourceList, extensions);
    }

}
