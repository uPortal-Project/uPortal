/**
 * WSRP_v1_ServiceDescription_Binding_SOAPStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.bind;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class WSRP_v1_ServiceDescription_Binding_SOAPStub extends org.apache.axis.client.Stub implements org.jasig.portal.wsrp.intf.WSRP_v1_ServiceDescription_PortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[1];
        org.apache.axis.description.OperationDesc oper;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getServiceDescription");
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"), org.jasig.portal.wsrp.types.RegistrationContext.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "desiredLocales"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "requiresRegistration"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "offeredPortlets"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletDescription"), org.jasig.portal.wsrp.types.PortletDescription[].class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userCategoryDescriptions"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ItemDescription"), org.jasig.portal.wsrp.types.ItemDescription[].class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customUserProfileItemDescriptions"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ItemDescription"), org.jasig.portal.wsrp.types.ItemDescription[].class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customWindowStateDescriptions"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ItemDescription"), org.jasig.portal.wsrp.types.ItemDescription[].class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customModeDescriptions"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ItemDescription"), org.jasig.portal.wsrp.types.ItemDescription[].class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "requiresInitCookie"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "CookieProtocol"), org.jasig.portal.wsrp.types.CookieProtocol.class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationPropertyDescription"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ModelDescription"), org.jasig.portal.wsrp.types.ModelDescription.class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "locales"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "resourceList"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ResourceList"), org.jasig.portal.wsrp.types.ResourceList.class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"), new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"), org.jasig.portal.wsrp.types.Extension[].class, org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
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
        _operations[0] = oper;

    }

    public WSRP_v1_ServiceDescription_Binding_SOAPStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public WSRP_v1_ServiceDescription_Binding_SOAPStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public WSRP_v1_ServiceDescription_Binding_SOAPStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
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

    public void getServiceDescription(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, java.lang.String[] desiredLocales, javax.xml.rpc.holders.BooleanHolder requiresRegistration, org.jasig.portal.wsrp.types.holders.PortletDescriptionArrayHolder offeredPortlets, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder userCategoryDescriptions, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder customUserProfileItemDescriptions, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder customWindowStateDescriptions, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder customModeDescriptions, org.jasig.portal.wsrp.types.holders.CookieProtocolHolder requiresInitCookie, org.jasig.portal.wsrp.types.holders.ModelDescriptionHolder registrationPropertyDescription, org.jasig.portal.wsrp.types.holders.StringArrayHolder locales, org.jasig.portal.wsrp.types.holders.ResourceListHolder resourceList, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:oasis:names:tc:wsrp:v1:getServiceDescription");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "getServiceDescription"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {registrationContext, desiredLocales});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            java.util.Map _output;
            _output = _call.getOutputParams();
            try {
                requiresRegistration.value = ((java.lang.Boolean) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "requiresRegistration"))).booleanValue();
            } catch (java.lang.Exception _exception) {
                requiresRegistration.value = ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "requiresRegistration")), boolean.class)).booleanValue();
            }
            try {
                offeredPortlets.value = (org.jasig.portal.wsrp.types.PortletDescription[]) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "offeredPortlets"));
            } catch (java.lang.Exception _exception) {
                offeredPortlets.value = (org.jasig.portal.wsrp.types.PortletDescription[]) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "offeredPortlets")), org.jasig.portal.wsrp.types.PortletDescription[].class);
            }
            try {
                userCategoryDescriptions.value = (org.jasig.portal.wsrp.types.ItemDescription[]) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userCategoryDescriptions"));
            } catch (java.lang.Exception _exception) {
                userCategoryDescriptions.value = (org.jasig.portal.wsrp.types.ItemDescription[]) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userCategoryDescriptions")), org.jasig.portal.wsrp.types.ItemDescription[].class);
            }
            try {
                customUserProfileItemDescriptions.value = (org.jasig.portal.wsrp.types.ItemDescription[]) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customUserProfileItemDescriptions"));
            } catch (java.lang.Exception _exception) {
                customUserProfileItemDescriptions.value = (org.jasig.portal.wsrp.types.ItemDescription[]) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customUserProfileItemDescriptions")), org.jasig.portal.wsrp.types.ItemDescription[].class);
            }
            try {
                customWindowStateDescriptions.value = (org.jasig.portal.wsrp.types.ItemDescription[]) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customWindowStateDescriptions"));
            } catch (java.lang.Exception _exception) {
                customWindowStateDescriptions.value = (org.jasig.portal.wsrp.types.ItemDescription[]) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customWindowStateDescriptions")), org.jasig.portal.wsrp.types.ItemDescription[].class);
            }
            try {
                customModeDescriptions.value = (org.jasig.portal.wsrp.types.ItemDescription[]) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customModeDescriptions"));
            } catch (java.lang.Exception _exception) {
                customModeDescriptions.value = (org.jasig.portal.wsrp.types.ItemDescription[]) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customModeDescriptions")), org.jasig.portal.wsrp.types.ItemDescription[].class);
            }
            try {
                requiresInitCookie.value = (org.jasig.portal.wsrp.types.CookieProtocol) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "requiresInitCookie"));
            } catch (java.lang.Exception _exception) {
                requiresInitCookie.value = (org.jasig.portal.wsrp.types.CookieProtocol) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "requiresInitCookie")), org.jasig.portal.wsrp.types.CookieProtocol.class);
            }
            try {
                registrationPropertyDescription.value = (org.jasig.portal.wsrp.types.ModelDescription) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationPropertyDescription"));
            } catch (java.lang.Exception _exception) {
                registrationPropertyDescription.value = (org.jasig.portal.wsrp.types.ModelDescription) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationPropertyDescription")), org.jasig.portal.wsrp.types.ModelDescription.class);
            }
            try {
                locales.value = (java.lang.String[]) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "locales"));
            } catch (java.lang.Exception _exception) {
                locales.value = (java.lang.String[]) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "locales")), java.lang.String[].class);
            }
            try {
                resourceList.value = (org.jasig.portal.wsrp.types.ResourceList) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "resourceList"));
            } catch (java.lang.Exception _exception) {
                resourceList.value = (org.jasig.portal.wsrp.types.ResourceList) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "resourceList")), org.jasig.portal.wsrp.types.ResourceList.class);
            }
            try {
                extensions.value = (org.jasig.portal.wsrp.types.Extension[]) _output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"));
            } catch (java.lang.Exception _exception) {
                extensions.value = (org.jasig.portal.wsrp.types.Extension[]) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions")), org.jasig.portal.wsrp.types.Extension[].class);
            }
        }
    }

}
