/**
 * WSRP_v1_Markup_Binding_SOAPStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.bind;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class WSRP_v1_Markup_Binding_SOAPStub extends org.apache.axis.client.Stub implements org.jasig.portal.wsrp.intf.WSRP_v1_Markup_PortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[4];
        org.apache.axis.description.OperationDesc oper;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getMarkup");
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletContext"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletContext"), org.jasig.portal.wsrp.types.PortletContext.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "runtimeContext"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RuntimeContext"), org.jasig.portal.wsrp.types.RuntimeContext.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userContext"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UserContext"), org.jasig.portal.wsrp.types.UserContext.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "markupParams"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupParams"), org.jasig.portal.wsrp.types.MarkupParams.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "markupContext"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupContext"), org.jasig.portal.wsrp.types.MarkupContext.class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "sessionContext"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "SessionContext"), org.jasig.portal.wsrp.types.SessionContext.class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParameters"),
                      "org.jasig.portal.wsrp.types.InconsistentParametersFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParametersFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidRegistration"),
                      "org.jasig.portal.wsrp.types.InvalidRegistrationFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidRegistrationFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParameters"),
                      "org.jasig.portal.wsrp.types.MissingParametersFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParametersFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailed"),
                      "org.jasig.portal.wsrp.types.OperationFailedFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailedFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMimeType"),
                      "org.jasig.portal.wsrp.types.UnsupportedMimeTypeFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMimeTypeFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMode"),
                      "org.jasig.portal.wsrp.types.UnsupportedModeFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedModeFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedLocale"),
                      "org.jasig.portal.wsrp.types.UnsupportedLocaleFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedLocaleFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategory"),
                      "org.jasig.portal.wsrp.types.InvalidUserCategoryFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategoryFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidSession"),
                      "org.jasig.portal.wsrp.types.InvalidSessionFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidSessionFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidCookie"),
                      "org.jasig.portal.wsrp.types.InvalidCookieFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidCookieFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDenied"),
                      "org.jasig.portal.wsrp.types.AccessDeniedFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDeniedFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandle"),
                      "org.jasig.portal.wsrp.types.InvalidHandleFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandleFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedWindowState"),
                      "org.jasig.portal.wsrp.types.UnsupportedWindowStateFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedWindowStateFault"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("performBlockingInteraction");
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletContext"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletContext"), org.jasig.portal.wsrp.types.PortletContext.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "runtimeContext"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RuntimeContext"), org.jasig.portal.wsrp.types.RuntimeContext.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userContext"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UserContext"), org.jasig.portal.wsrp.types.UserContext.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "markupParams"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupParams"), org.jasig.portal.wsrp.types.MarkupParams.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "interactionParams"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InteractionParams"), org.jasig.portal.wsrp.types.InteractionParams.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "updateResponse"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UpdateResponse"), org.jasig.portal.wsrp.types.UpdateResponse.class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "redirectURL"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParameters"),
                      "org.jasig.portal.wsrp.types.InconsistentParametersFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParametersFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidRegistration"),
                      "org.jasig.portal.wsrp.types.InvalidRegistrationFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidRegistrationFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParameters"),
                      "org.jasig.portal.wsrp.types.MissingParametersFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParametersFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailed"),
                      "org.jasig.portal.wsrp.types.OperationFailedFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailedFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMimeType"),
                      "org.jasig.portal.wsrp.types.UnsupportedMimeTypeFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMimeTypeFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMode"),
                      "org.jasig.portal.wsrp.types.UnsupportedModeFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedModeFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedLocale"),
                      "org.jasig.portal.wsrp.types.UnsupportedLocaleFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedLocaleFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategory"),
                      "org.jasig.portal.wsrp.types.InvalidUserCategoryFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategoryFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidSession"),
                      "org.jasig.portal.wsrp.types.InvalidSessionFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidSessionFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidCookie"),
                      "org.jasig.portal.wsrp.types.InvalidCookieFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidCookieFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletStateChangeRequired"),
                      "org.jasig.portal.wsrp.types.PortletStateChangeRequiredFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletStateChangeRequiredFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDenied"),
                      "org.jasig.portal.wsrp.types.AccessDeniedFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDeniedFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandle"),
                      "org.jasig.portal.wsrp.types.InvalidHandleFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandleFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedWindowState"),
                      "org.jasig.portal.wsrp.types.UnsupportedWindowStateFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedWindowStateFault"), 
                      true
                     ));
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("releaseSessions");
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "sessionIDs"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"));
        oper.setReturnClass(org.jasig.portal.wsrp.types.Extension[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidRegistration"),
                      "org.jasig.portal.wsrp.types.InvalidRegistrationFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidRegistrationFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailed"),
                      "org.jasig.portal.wsrp.types.OperationFailedFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailedFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParameters"),
                      "org.jasig.portal.wsrp.types.MissingParametersFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParametersFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDenied"),
                      "org.jasig.portal.wsrp.types.AccessDeniedFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDeniedFault"), 
                      true
                     ));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("initCookie");
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"));
        oper.setReturnClass(org.jasig.portal.wsrp.types.Extension[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidRegistration"),
                      "org.jasig.portal.wsrp.types.InvalidRegistrationFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidRegistrationFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailed"),
                      "org.jasig.portal.wsrp.types.OperationFailedFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailedFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDenied"),
                      "org.jasig.portal.wsrp.types.AccessDeniedFault",
                      new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDeniedFault"), 
                      true
                     ));
        _operations[3] = oper;

    }

    public WSRP_v1_Markup_Binding_SOAPStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public WSRP_v1_Markup_Binding_SOAPStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public WSRP_v1_Markup_Binding_SOAPStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Property");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.Property.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ResourceList");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.ResourceList.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", ">clonePortlet");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types._clonePortlet.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PropertyDescription");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.PropertyDescription.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailedFault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.OperationFailedFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ModelTypes");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.ModelTypes.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", ">performBlockingInteraction");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types._performBlockingInteraction.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", ">getServiceDescription");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types._getServiceDescription.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PropertyList");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.PropertyList.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "CookieProtocol");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.CookieProtocol.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Templates");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.Templates.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RuntimeContext");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.RuntimeContext.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", ">getMarkup");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types._getMarkup.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", ">initCookie");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types._initCookie.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "CacheControl");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.CacheControl.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Online");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.Online.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "NamedStringArray");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.NamedStringArray.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategoryFault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.InvalidUserCategoryFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "NamedString");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.NamedString.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", ">releaseSessions");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types._releaseSessions.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ResourceValue");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.ResourceValue.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PersonName");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.PersonName.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ID");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.ID.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(simplesf);
            cachedDeserFactories.add(simpledf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParametersFault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.MissingParametersFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "TelephoneNum");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.TelephoneNum.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ItemDescription");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.ItemDescription.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ResetProperty");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.ResetProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "EmployerInfo");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.EmployerInfo.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedLocaleFault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.UnsupportedLocaleFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Handle");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.Handle.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(simplesf);
            cachedDeserFactories.add(simpledf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", ">destroyPortlets");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types._destroyPortlets.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidRegistrationFault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.InvalidRegistrationFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletDescriptionResponse");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.PortletDescriptionResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMimeTypeFault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.UnsupportedMimeTypeFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UserProfile");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.UserProfile.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UpdateResponse");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.UpdateResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "StringArray");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.StringArray.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Resource");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.Resource.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidSessionFault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.InvalidSessionFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Contact");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.Contact.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupResponse");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.MarkupResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UploadContext");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.UploadContext.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", ">getPortletPropertyDescription");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types._getPortletPropertyDescription.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Key");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.Key.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(simplesf);
            cachedDeserFactories.add(simpledf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "DestroyPortletsResponse");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.DestroyPortletsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UserContext");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.UserContext.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupContext");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.MarkupContext.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletDescription");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.PortletDescription.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", ">getPortletProperties");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types._getPortletProperties.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", ">modifyRegistration");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types._modifyRegistration.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParametersFault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.InconsistentParametersFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ReturnAny");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.ReturnAny.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Postal");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.Postal.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandleFault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.InvalidHandleFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletPropertyDescriptionResponse");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.PortletPropertyDescriptionResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletContext");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.PortletContext.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupParams");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.MarkupParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.Extension.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationState");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.RegistrationState.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDeniedFault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.AccessDeniedFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletStateChangeRequiredFault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.PortletStateChangeRequiredFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "BlockingInteractionResponse");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.BlockingInteractionResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "SessionContext");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.SessionContext.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.RegistrationContext.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ServiceDescription");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.ServiceDescription.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupType");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.MarkupType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ClientData");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.ClientData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InteractionParams");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.InteractionParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedModeFault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.UnsupportedModeFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "LocalizedString");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.LocalizedString.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "StateChange");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.StateChange.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", ">setPortletProperties");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types._setPortletProperties.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationData");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.RegistrationData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "DestroyFailed");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.DestroyFailed.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Telecom");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.Telecom.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Fault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.Fault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ModelDescription");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.ModelDescription.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedWindowStateFault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.UnsupportedWindowStateFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidCookieFault");
            cachedSerQNames.add(qName);
            cls = org.jasig.portal.wsrp.types.InvalidCookieFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    private org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call =
                    (org.apache.axis.client.Call) super.service.createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                        java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                        _call.registerTypeMapping(cls, qName, sf, df, false);
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", t);
        }
    }

    public void getMarkup(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.RuntimeContext runtimeContext, org.jasig.portal.wsrp.types.UserContext userContext, org.jasig.portal.wsrp.types.MarkupParams markupParams, org.jasig.portal.wsrp.types.holders.MarkupContextHolder markupContext, org.jasig.portal.wsrp.types.holders.SessionContextHolder sessionContext, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.UnsupportedMimeTypeFault, org.jasig.portal.wsrp.types.UnsupportedModeFault, org.jasig.portal.wsrp.types.UnsupportedLocaleFault, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InvalidSessionFault, org.jasig.portal.wsrp.types.InvalidCookieFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault, org.jasig.portal.wsrp.types.UnsupportedWindowStateFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:oasis:names:tc:wsrp:v1:getMarkup");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "getMarkup"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {registrationContext, portletContext, runtimeContext, userContext, markupParams});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            java.util.Map _output;
            _output = _call.getOutputParams();
            try {
                markupContext.value = (org.jasig.portal.wsrp.types.MarkupContext) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "markupContext"));
            } catch (java.lang.Exception _exception) {
                markupContext.value = (org.jasig.portal.wsrp.types.MarkupContext) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "markupContext")), org.jasig.portal.wsrp.types.MarkupContext.class);
            }
            try {
                sessionContext.value = (org.jasig.portal.wsrp.types.SessionContext) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "sessionContext"));
            } catch (java.lang.Exception _exception) {
                sessionContext.value = (org.jasig.portal.wsrp.types.SessionContext) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "sessionContext")), org.jasig.portal.wsrp.types.SessionContext.class);
            }
            try {
                extensions.value = (org.jasig.portal.wsrp.types.Extension[]) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"));
            } catch (java.lang.Exception _exception) {
                extensions.value = (org.jasig.portal.wsrp.types.Extension[]) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions")), org.jasig.portal.wsrp.types.Extension[].class);
            }
        }
    }

    public void performBlockingInteraction(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.RuntimeContext runtimeContext, org.jasig.portal.wsrp.types.UserContext userContext, org.jasig.portal.wsrp.types.MarkupParams markupParams, org.jasig.portal.wsrp.types.InteractionParams interactionParams, org.jasig.portal.wsrp.types.holders.UpdateResponseHolder updateResponse, javax.xml.rpc.holders.StringHolder redirectURL, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.UnsupportedMimeTypeFault, org.jasig.portal.wsrp.types.UnsupportedModeFault, org.jasig.portal.wsrp.types.UnsupportedLocaleFault, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InvalidSessionFault, org.jasig.portal.wsrp.types.InvalidCookieFault, org.jasig.portal.wsrp.types.PortletStateChangeRequiredFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault, org.jasig.portal.wsrp.types.UnsupportedWindowStateFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:oasis:names:tc:wsrp:v1:performBlockingInteraction");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "performBlockingInteraction"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {registrationContext, portletContext, runtimeContext, userContext, markupParams, interactionParams});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            java.util.Map _output;
            _output = _call.getOutputParams();
            try {
                updateResponse.value = (org.jasig.portal.wsrp.types.UpdateResponse) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "updateResponse"));
            } catch (java.lang.Exception _exception) {
                updateResponse.value = (org.jasig.portal.wsrp.types.UpdateResponse) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "updateResponse")), org.jasig.portal.wsrp.types.UpdateResponse.class);
            }
            try {
                redirectURL.value = (java.lang.String) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "redirectURL"));
            } catch (java.lang.Exception _exception) {
                redirectURL.value = (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "redirectURL")), java.lang.String.class);
            }
            try {
                extensions.value = (org.jasig.portal.wsrp.types.Extension[]) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"));
            } catch (java.lang.Exception _exception) {
                extensions.value = (org.jasig.portal.wsrp.types.Extension[]) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions")), org.jasig.portal.wsrp.types.Extension[].class);
            }
        }
    }

    public org.jasig.portal.wsrp.types.Extension[] releaseSessions(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, java.lang.String[] sessionIDs) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:oasis:names:tc:wsrp:v1:releaseSessions");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "releaseSessions"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {registrationContext, sessionIDs});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.jasig.portal.wsrp.types.Extension[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.jasig.portal.wsrp.types.Extension[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.jasig.portal.wsrp.types.Extension[].class);
            }
        }
    }

    public org.jasig.portal.wsrp.types.Extension[] initCookie(org.jasig.portal.wsrp.types.RegistrationContext registrationContext) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.AccessDeniedFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:oasis:names:tc:wsrp:v1:initCookie");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "initCookie"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {registrationContext});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.jasig.portal.wsrp.types.Extension[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.jasig.portal.wsrp.types.Extension[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.jasig.portal.wsrp.types.Extension[].class);
            }
        }
    }

}
