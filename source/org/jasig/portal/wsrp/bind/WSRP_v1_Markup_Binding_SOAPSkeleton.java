/**
 * WSRP_v1_Markup_Binding_SOAPSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.bind;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class WSRP_v1_Markup_Binding_SOAPSkeleton implements org.jasig.portal.wsrp.intf.WSRP_v1_Markup_PortType, org.apache.axis.wsdl.Skeleton {
    private org.jasig.portal.wsrp.intf.WSRP_v1_Markup_PortType impl;
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
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "runtimeContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RuntimeContext"), org.jasig.portal.wsrp.types.RuntimeContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UserContext"), org.jasig.portal.wsrp.types.UserContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "markupParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupParams"), org.jasig.portal.wsrp.types.MarkupParams.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "markupContext"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupContext"), org.jasig.portal.wsrp.types.MarkupContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "sessionContext"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "SessionContext"), org.jasig.portal.wsrp.types.SessionContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getMarkup", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "getMarkup"));
        _oper.setSoapAction("urn:oasis:names:tc:wsrp:v1:getMarkup");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getMarkup") == null) {
            _myOperations.put("getMarkup", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getMarkup")).add(_oper);
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
        _fault.setName("MissingParameters");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParameters"));
        _fault.setClassName("org.jasig.portal.wsrp.types.MissingParametersFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParametersFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("OperationFailed");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailed"));
        _fault.setClassName("org.jasig.portal.wsrp.types.OperationFailedFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailedFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("UnsupportedMimeType");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMimeType"));
        _fault.setClassName("org.jasig.portal.wsrp.types.UnsupportedMimeTypeFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMimeTypeFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("UnsupportedMode");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMode"));
        _fault.setClassName("org.jasig.portal.wsrp.types.UnsupportedModeFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedModeFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("UnsupportedLocale");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedLocale"));
        _fault.setClassName("org.jasig.portal.wsrp.types.UnsupportedLocaleFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedLocaleFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidUserCategory");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategory"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidUserCategoryFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategoryFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidSession");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidSession"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidSessionFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidSessionFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidCookie");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidCookie"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidCookieFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidCookieFault"));
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
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("UnsupportedWindowState");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedWindowState"));
        _fault.setClassName("org.jasig.portal.wsrp.types.UnsupportedWindowStateFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedWindowStateFault"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletContext"), org.jasig.portal.wsrp.types.PortletContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "runtimeContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RuntimeContext"), org.jasig.portal.wsrp.types.RuntimeContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UserContext"), org.jasig.portal.wsrp.types.UserContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "markupParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupParams"), org.jasig.portal.wsrp.types.MarkupParams.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "interactionParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InteractionParams"), org.jasig.portal.wsrp.types.InteractionParams.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "updateResponse"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UpdateResponse"), org.jasig.portal.wsrp.types.UpdateResponse.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "redirectURL"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("performBlockingInteraction", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "performBlockingInteraction"));
        _oper.setSoapAction("urn:oasis:names:tc:wsrp:v1:performBlockingInteraction");
        _myOperationsList.add(_oper);
        if (_myOperations.get("performBlockingInteraction") == null) {
            _myOperations.put("performBlockingInteraction", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("performBlockingInteraction")).add(_oper);
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
        _fault.setName("MissingParameters");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParameters"));
        _fault.setClassName("org.jasig.portal.wsrp.types.MissingParametersFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParametersFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("OperationFailed");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailed"));
        _fault.setClassName("org.jasig.portal.wsrp.types.OperationFailedFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailedFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("UnsupportedMimeType");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMimeType"));
        _fault.setClassName("org.jasig.portal.wsrp.types.UnsupportedMimeTypeFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMimeTypeFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("UnsupportedMode");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMode"));
        _fault.setClassName("org.jasig.portal.wsrp.types.UnsupportedModeFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedModeFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("UnsupportedLocale");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedLocale"));
        _fault.setClassName("org.jasig.portal.wsrp.types.UnsupportedLocaleFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedLocaleFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidUserCategory");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategory"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidUserCategoryFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategoryFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidSession");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidSession"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidSessionFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidSessionFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidCookie");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidCookie"));
        _fault.setClassName("org.jasig.portal.wsrp.types.InvalidCookieFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidCookieFault"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("PortletStateChangeRequired");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletStateChangeRequired"));
        _fault.setClassName("org.jasig.portal.wsrp.types.PortletStateChangeRequiredFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletStateChangeRequiredFault"));
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
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("UnsupportedWindowState");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedWindowState"));
        _fault.setClassName("org.jasig.portal.wsrp.types.UnsupportedWindowStateFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedWindowStateFault"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "sessionIDs"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("releaseSessions", _params, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "releaseSessions"));
        _oper.setSoapAction("urn:oasis:names:tc:wsrp:v1:releaseSessions");
        _myOperationsList.add(_oper);
        if (_myOperations.get("releaseSessions") == null) {
            _myOperations.put("releaseSessions", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("releaseSessions")).add(_oper);
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
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("initCookie", _params, new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "initCookie"));
        _oper.setSoapAction("urn:oasis:names:tc:wsrp:v1:initCookie");
        _myOperationsList.add(_oper);
        if (_myOperations.get("initCookie") == null) {
            _myOperations.put("initCookie", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("initCookie")).add(_oper);
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
        _fault.setName("AccessDenied");
        _fault.setQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDenied"));
        _fault.setClassName("org.jasig.portal.wsrp.types.AccessDeniedFault");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDeniedFault"));
        _oper.addFault(_fault);
    }

    public WSRP_v1_Markup_Binding_SOAPSkeleton() {
        this.impl = new org.jasig.portal.wsrp.bind.WSRP_v1_Markup_Binding_SOAPImpl();
    }

    public WSRP_v1_Markup_Binding_SOAPSkeleton(org.jasig.portal.wsrp.intf.WSRP_v1_Markup_PortType impl) {
        this.impl = impl;
    }
    public void getMarkup(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.RuntimeContext runtimeContext, org.jasig.portal.wsrp.types.UserContext userContext, org.jasig.portal.wsrp.types.MarkupParams markupParams, org.jasig.portal.wsrp.types.holders.MarkupContextHolder markupContext, org.jasig.portal.wsrp.types.holders.SessionContextHolder sessionContext, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.UnsupportedMimeTypeFault, org.jasig.portal.wsrp.types.UnsupportedModeFault, org.jasig.portal.wsrp.types.UnsupportedLocaleFault, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InvalidSessionFault, org.jasig.portal.wsrp.types.InvalidCookieFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault, org.jasig.portal.wsrp.types.UnsupportedWindowStateFault
    {
        impl.getMarkup(registrationContext, portletContext, runtimeContext, userContext, markupParams, markupContext, sessionContext, extensions);
    }

    public void performBlockingInteraction(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.RuntimeContext runtimeContext, org.jasig.portal.wsrp.types.UserContext userContext, org.jasig.portal.wsrp.types.MarkupParams markupParams, org.jasig.portal.wsrp.types.InteractionParams interactionParams, org.jasig.portal.wsrp.types.holders.UpdateResponseHolder updateResponse, javax.xml.rpc.holders.StringHolder redirectURL, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.UnsupportedMimeTypeFault, org.jasig.portal.wsrp.types.UnsupportedModeFault, org.jasig.portal.wsrp.types.UnsupportedLocaleFault, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InvalidSessionFault, org.jasig.portal.wsrp.types.InvalidCookieFault, org.jasig.portal.wsrp.types.PortletStateChangeRequiredFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault, org.jasig.portal.wsrp.types.UnsupportedWindowStateFault
    {
        impl.performBlockingInteraction(registrationContext, portletContext, runtimeContext, userContext, markupParams, interactionParams, updateResponse, redirectURL, extensions);
    }

    public org.jasig.portal.wsrp.types.Extension[] releaseSessions(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, java.lang.String[] sessionIDs) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault
    {
        org.jasig.portal.wsrp.types.Extension[] ret = impl.releaseSessions(registrationContext, sessionIDs);
        return ret;
    }

    public org.jasig.portal.wsrp.types.Extension[] initCookie(org.jasig.portal.wsrp.types.RegistrationContext registrationContext) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.AccessDeniedFault
    {
        org.jasig.portal.wsrp.types.Extension[] ret = impl.initCookie(registrationContext);
        return ret;
    }

}
