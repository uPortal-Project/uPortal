/**
 * WSRP_v1_Registration_Binding_SOAPSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.bind;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class WSRP_v1_Registration_Binding_SOAPSkeleton implements org.jasig.portal.wsrp.intf.WSRP_v1_Registration_PortType, org.apache.axis.wsdl.Skeleton {
    private org.jasig.portal.wsrp.intf.WSRP_v1_Registration_PortType impl;
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
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "consumerName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "consumerAgent"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "methodGetSupported"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "consumerModes"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "consumerWindowStates"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "consumerUserScopes"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customUserProfileData"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationProperties"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Property"), org.jasig.portal.wsrp.types.Property[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), org.apache.axis.description.ParameterDesc.INOUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationHandle"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationState"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "base64"), byte[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("register", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "register"));
        _oper.setSoapAction("urn:oasis:names:tc:wsrp:v1:register");
        _myOperationsList.add(_oper);
        if (_myOperations.get("register") == null) {
            _myOperations.put("register", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("register")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("OperationFailed");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailed"));
        _fault.setClassName("org.jasig.portal.wsrp.types.OperationFailedFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailedFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("MissingParameters");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParameters"));
        _fault.setClassName("org.jasig.portal.wsrp.types.MissingParametersFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParametersFault"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationHandle"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationState"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "base64"), byte[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), org.apache.axis.description.ParameterDesc.INOUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("deregister", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "deregister"));
        _oper.setSoapAction("urn:oasis:names:tc:wsrp:v1:deregister");
        _myOperationsList.add(_oper);
        if (_myOperations.get("deregister") == null) {
            _myOperations.put("deregister", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("deregister")).add(_oper);
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
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationData"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationData"), org.jasig.portal.wsrp.types.RegistrationData.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationState"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "base64"), byte[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("modifyRegistration", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "modifyRegistration"));
        _oper.setSoapAction("urn:oasis:names:tc:wsrp:v1:modifyRegistration");
        _myOperationsList.add(_oper);
        if (_myOperations.get("modifyRegistration") == null) {
            _myOperations.put("modifyRegistration", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("modifyRegistration")).add(_oper);
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
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("MissingParameters");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParameters"));
        _fault.setClassName("org.jasig.portal.wsrp.types.MissingParametersFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParametersFault"));
        _oper.addFault(_fault);
    }

    public WSRP_v1_Registration_Binding_SOAPSkeleton() {
        this.impl = new org.jasig.portal.wsrp.bind.WSRP_v1_Registration_Binding_SOAPImpl();
    }

    public WSRP_v1_Registration_Binding_SOAPSkeleton(org.jasig.portal.wsrp.intf.WSRP_v1_Registration_PortType impl) {
        this.impl = impl;
    }
    public void register(java.lang.String consumerName, java.lang.String consumerAgent, boolean methodGetSupported, java.lang.String[] consumerModes, java.lang.String[] consumerWindowStates, java.lang.String[] consumerUserScopes, java.lang.String[] customUserProfileData, org.jasig.portal.wsrp.types.Property[] registrationProperties, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions, javax.xml.rpc.holders.StringHolder registrationHandle, javax.xml.rpc.holders.ByteArrayHolder registrationState) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault
    {
        impl.register(consumerName, consumerAgent, methodGetSupported, consumerModes, consumerWindowStates, consumerUserScopes, customUserProfileData, registrationProperties, extensions, registrationHandle, registrationState);
    }

    public void deregister(java.lang.String registrationHandle, byte[] registrationState, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault
    {
        impl.deregister(registrationHandle, registrationState, extensions);
    }

    public void modifyRegistration(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.RegistrationData registrationData, javax.xml.rpc.holders.ByteArrayHolder registrationState, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault
    {
        impl.modifyRegistration(registrationContext, registrationData, registrationState, extensions);
    }

}
