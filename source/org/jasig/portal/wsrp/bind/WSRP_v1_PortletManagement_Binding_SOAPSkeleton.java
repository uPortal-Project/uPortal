/**
 * WSRP_v1_PortletManagement_Binding_SOAPSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.bind;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class WSRP_v1_PortletManagement_Binding_SOAPSkeleton implements org.jasig.portal.wsrp.intf.WSRP_v1_PortletManagement_PortType, org.apache.axis.wsdl.Skeleton {
    private org.jasig.portal.wsrp.intf.WSRP_v1_PortletManagement_PortType impl;
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
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletContext"), org.jasig.portal.wsrp.types.PortletContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UserContext"), org.jasig.portal.wsrp.types.UserContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "desiredLocales"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletDescription"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletDescription"), org.jasig.portal.wsrp.types.PortletDescription.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "resourceList"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ResourceList"), org.jasig.portal.wsrp.types.ResourceList.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getPortletDescription", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "getPortletDescription"));
        _oper.setSoapAction("urn:oasis:names:tc:wsrp:v1:getPortletDescription");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getPortletDescription") == null) {
            _myOperations.put("getPortletDescription", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getPortletDescription")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidUserCategory");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategory"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidUserCategoryFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategoryFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InconsistentParameters");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParameters"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InconsistentParametersFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParametersFault"));
        _oper.addFault(_fault);
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
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("AccessDenied");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDenied"));
        _fault.setClassName("org.jasig.portal.wsrp.types.AccessDeniedFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDeniedFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidHandle");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandle"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidHandleFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandleFault"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletContext"), org.jasig.portal.wsrp.types.PortletContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UserContext"), org.jasig.portal.wsrp.types.UserContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletHandle"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletState"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "base64"), byte[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("clonePortlet", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "clonePortlet"));
        _oper.setSoapAction("urn:oasis:names:tc:wsrp:v1:clonePortlet");
        _myOperationsList.add(_oper);
        if (_myOperations.get("clonePortlet") == null) {
            _myOperations.put("clonePortlet", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("clonePortlet")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidUserCategory");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategory"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidUserCategoryFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategoryFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InconsistentParameters");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParameters"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InconsistentParametersFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParametersFault"));
        _oper.addFault(_fault);
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
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("AccessDenied");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDenied"));
        _fault.setClassName("org.jasig.portal.wsrp.types.AccessDeniedFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDeniedFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidHandle");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandle"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidHandleFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandleFault"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletHandles"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "destroyFailed"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "DestroyFailed"), org.jasig.portal.wsrp.types.DestroyFailed[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("destroyPortlets", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "destroyPortlets"));
        _oper.setSoapAction("urn:oasis:names:tc:wsrp:v1:destroyPortlets");
        _myOperationsList.add(_oper);
        if (_myOperations.get("destroyPortlets") == null) {
            _myOperations.put("destroyPortlets", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("destroyPortlets")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InconsistentParameters");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParameters"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InconsistentParametersFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParametersFault"));
        _oper.addFault(_fault);
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
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletContext"), org.jasig.portal.wsrp.types.PortletContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UserContext"), org.jasig.portal.wsrp.types.UserContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "propertyList"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PropertyList"), org.jasig.portal.wsrp.types.PropertyList.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletHandle"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletState"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "base64"), byte[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("setPortletProperties", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "setPortletProperties"));
        _oper.setSoapAction("urn:oasis:names:tc:wsrp:v1:setPortletProperties");
        _myOperationsList.add(_oper);
        if (_myOperations.get("setPortletProperties") == null) {
            _myOperations.put("setPortletProperties", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("setPortletProperties")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidUserCategory");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategory"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidUserCategoryFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategoryFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InconsistentParameters");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParameters"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InconsistentParametersFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParametersFault"));
        _oper.addFault(_fault);
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
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("AccessDenied");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDenied"));
        _fault.setClassName("org.jasig.portal.wsrp.types.AccessDeniedFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDeniedFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidHandle");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandle"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidHandleFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandleFault"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletContext"), org.jasig.portal.wsrp.types.PortletContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UserContext"), org.jasig.portal.wsrp.types.UserContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "names"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "properties"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Property"), org.jasig.portal.wsrp.types.Property[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "resetProperties"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ResetProperty"), org.jasig.portal.wsrp.types.ResetProperty[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getPortletProperties", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "getPortletProperties"));
        _oper.setSoapAction("urn:oasis:names:tc:wsrp:v1:getPortletProperties");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getPortletProperties") == null) {
            _myOperations.put("getPortletProperties", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getPortletProperties")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidUserCategory");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategory"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidUserCategoryFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategoryFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InconsistentParameters");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParameters"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InconsistentParametersFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParametersFault"));
        _oper.addFault(_fault);
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
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("AccessDenied");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDenied"));
        _fault.setClassName("org.jasig.portal.wsrp.types.AccessDeniedFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDeniedFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidHandle");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandle"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidHandleFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandleFault"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletContext"), org.jasig.portal.wsrp.types.PortletContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UserContext"), org.jasig.portal.wsrp.types.UserContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "desiredLocales"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "modelDescription"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ModelDescription"), org.jasig.portal.wsrp.types.ModelDescription.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "resourceList"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ResourceList"), org.jasig.portal.wsrp.types.ResourceList.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getPortletPropertyDescription", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "getPortletPropertyDescription"));
        _oper.setSoapAction("urn:oasis:names:tc:wsrp:v1:getPortletPropertyDescription");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getPortletPropertyDescription") == null) {
            _myOperations.put("getPortletPropertyDescription", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getPortletPropertyDescription")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidUserCategory");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategory"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidUserCategoryFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategoryFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InconsistentParameters");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParameters"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InconsistentParametersFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParametersFault"));
        _oper.addFault(_fault);
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
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("AccessDenied");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDenied"));
        _fault.setClassName("org.jasig.portal.wsrp.types.AccessDeniedFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDeniedFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidHandle");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandle"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidHandleFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandleFault"));
        _oper.addFault(_fault);
    }

    public WSRP_v1_PortletManagement_Binding_SOAPSkeleton() {
        this.impl = new org.jasig.portal.wsrp.bind.WSRP_v1_PortletManagement_Binding_SOAPImpl();
    }

    public WSRP_v1_PortletManagement_Binding_SOAPSkeleton(org.jasig.portal.wsrp.intf.WSRP_v1_PortletManagement_PortType impl) {
        this.impl = impl;
    }
    public void getPortletDescription(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, java.lang.String[] desiredLocales, org.jasig.portal.wsrp.types.holders.PortletDescriptionHolder portletDescription, org.jasig.portal.wsrp.types.holders.ResourceListHolder resourceList, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault
    {
        impl.getPortletDescription(registrationContext, portletContext, userContext, desiredLocales, portletDescription, resourceList, extensions);
    }

    public void clonePortlet(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, javax.xml.rpc.holders.StringHolder portletHandle, javax.xml.rpc.holders.ByteArrayHolder portletState, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault
    {
        impl.clonePortlet(registrationContext, portletContext, userContext, portletHandle, portletState, extensions);
    }

    public void destroyPortlets(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, java.lang.String[] portletHandles, org.jasig.portal.wsrp.types.holders.DestroyFailedArrayHolder destroyFailed, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault
    {
        impl.destroyPortlets(registrationContext, portletHandles, destroyFailed, extensions);
    }

    public void setPortletProperties(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, org.jasig.portal.wsrp.types.PropertyList propertyList, javax.xml.rpc.holders.StringHolder portletHandle, javax.xml.rpc.holders.ByteArrayHolder portletState, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault
    {
        impl.setPortletProperties(registrationContext, portletContext, userContext, propertyList, portletHandle, portletState, extensions);
    }

    public void getPortletProperties(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, java.lang.String[] names, org.jasig.portal.wsrp.types.holders.PropertyArrayHolder properties, org.jasig.portal.wsrp.types.holders.ResetPropertyArrayHolder resetProperties, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault
    {
        impl.getPortletProperties(registrationContext, portletContext, userContext, names, properties, resetProperties, extensions);
    }

    public void getPortletPropertyDescription(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, java.lang.String[] desiredLocales, org.jasig.portal.wsrp.types.holders.ModelDescriptionHolder modelDescription, org.jasig.portal.wsrp.types.holders.ResourceListHolder resourceList, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault
    {
        impl.getPortletPropertyDescription(registrationContext, portletContext, userContext, desiredLocales, modelDescription, resourceList, extensions);
    }

}
