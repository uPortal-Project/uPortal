/**
 * ServiceDescription.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.types;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class ServiceDescription  implements java.io.Serializable {
    private boolean requiresRegistration;
    private org.jasig.portal.wsrp.types.PortletDescription[] offeredPortlets;
    private org.jasig.portal.wsrp.types.ItemDescription[] userCategoryDescriptions;
    private org.jasig.portal.wsrp.types.ItemDescription[] customUserProfileItemDescriptions;
    private org.jasig.portal.wsrp.types.ItemDescription[] customWindowStateDescriptions;
    private org.jasig.portal.wsrp.types.ItemDescription[] customModeDescriptions;
    private org.jasig.portal.wsrp.types.CookieProtocol requiresInitCookie;
    private org.jasig.portal.wsrp.types.ModelDescription registrationPropertyDescription;
    private java.lang.String[] locales;
    private org.jasig.portal.wsrp.types.ResourceList resourceList;
    private org.jasig.portal.wsrp.types.Extension[] extensions;

    public ServiceDescription() {
    }

    public boolean isRequiresRegistration() {
        return requiresRegistration;
    }

    public void setRequiresRegistration(boolean requiresRegistration) {
        this.requiresRegistration = requiresRegistration;
    }

    public org.jasig.portal.wsrp.types.PortletDescription[] getOfferedPortlets() {
        return offeredPortlets;
    }

    public void setOfferedPortlets(org.jasig.portal.wsrp.types.PortletDescription[] offeredPortlets) {
        this.offeredPortlets = offeredPortlets;
    }

    public org.jasig.portal.wsrp.types.PortletDescription getOfferedPortlets(int i) {
        return offeredPortlets[i];
    }

    public void setOfferedPortlets(int i, org.jasig.portal.wsrp.types.PortletDescription value) {
        this.offeredPortlets[i] = value;
    }

    public org.jasig.portal.wsrp.types.ItemDescription[] getUserCategoryDescriptions() {
        return userCategoryDescriptions;
    }

    public void setUserCategoryDescriptions(org.jasig.portal.wsrp.types.ItemDescription[] userCategoryDescriptions) {
        this.userCategoryDescriptions = userCategoryDescriptions;
    }

    public org.jasig.portal.wsrp.types.ItemDescription getUserCategoryDescriptions(int i) {
        return userCategoryDescriptions[i];
    }

    public void setUserCategoryDescriptions(int i, org.jasig.portal.wsrp.types.ItemDescription value) {
        this.userCategoryDescriptions[i] = value;
    }

    public org.jasig.portal.wsrp.types.ItemDescription[] getCustomUserProfileItemDescriptions() {
        return customUserProfileItemDescriptions;
    }

    public void setCustomUserProfileItemDescriptions(org.jasig.portal.wsrp.types.ItemDescription[] customUserProfileItemDescriptions) {
        this.customUserProfileItemDescriptions = customUserProfileItemDescriptions;
    }

    public org.jasig.portal.wsrp.types.ItemDescription getCustomUserProfileItemDescriptions(int i) {
        return customUserProfileItemDescriptions[i];
    }

    public void setCustomUserProfileItemDescriptions(int i, org.jasig.portal.wsrp.types.ItemDescription value) {
        this.customUserProfileItemDescriptions[i] = value;
    }

    public org.jasig.portal.wsrp.types.ItemDescription[] getCustomWindowStateDescriptions() {
        return customWindowStateDescriptions;
    }

    public void setCustomWindowStateDescriptions(org.jasig.portal.wsrp.types.ItemDescription[] customWindowStateDescriptions) {
        this.customWindowStateDescriptions = customWindowStateDescriptions;
    }

    public org.jasig.portal.wsrp.types.ItemDescription getCustomWindowStateDescriptions(int i) {
        return customWindowStateDescriptions[i];
    }

    public void setCustomWindowStateDescriptions(int i, org.jasig.portal.wsrp.types.ItemDescription value) {
        this.customWindowStateDescriptions[i] = value;
    }

    public org.jasig.portal.wsrp.types.ItemDescription[] getCustomModeDescriptions() {
        return customModeDescriptions;
    }

    public void setCustomModeDescriptions(org.jasig.portal.wsrp.types.ItemDescription[] customModeDescriptions) {
        this.customModeDescriptions = customModeDescriptions;
    }

    public org.jasig.portal.wsrp.types.ItemDescription getCustomModeDescriptions(int i) {
        return customModeDescriptions[i];
    }

    public void setCustomModeDescriptions(int i, org.jasig.portal.wsrp.types.ItemDescription value) {
        this.customModeDescriptions[i] = value;
    }

    public org.jasig.portal.wsrp.types.CookieProtocol getRequiresInitCookie() {
        return requiresInitCookie;
    }

    public void setRequiresInitCookie(org.jasig.portal.wsrp.types.CookieProtocol requiresInitCookie) {
        this.requiresInitCookie = requiresInitCookie;
    }

    public org.jasig.portal.wsrp.types.ModelDescription getRegistrationPropertyDescription() {
        return registrationPropertyDescription;
    }

    public void setRegistrationPropertyDescription(org.jasig.portal.wsrp.types.ModelDescription registrationPropertyDescription) {
        this.registrationPropertyDescription = registrationPropertyDescription;
    }

    public java.lang.String[] getLocales() {
        return locales;
    }

    public void setLocales(java.lang.String[] locales) {
        this.locales = locales;
    }

    public java.lang.String getLocales(int i) {
        return locales[i];
    }

    public void setLocales(int i, java.lang.String value) {
        this.locales[i] = value;
    }

    public org.jasig.portal.wsrp.types.ResourceList getResourceList() {
        return resourceList;
    }

    public void setResourceList(org.jasig.portal.wsrp.types.ResourceList resourceList) {
        this.resourceList = resourceList;
    }

    public org.jasig.portal.wsrp.types.Extension[] getExtensions() {
        return extensions;
    }

    public void setExtensions(org.jasig.portal.wsrp.types.Extension[] extensions) {
        this.extensions = extensions;
    }

    public org.jasig.portal.wsrp.types.Extension getExtensions(int i) {
        return extensions[i];
    }

    public void setExtensions(int i, org.jasig.portal.wsrp.types.Extension value) {
        this.extensions[i] = value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ServiceDescription)) return false;
        ServiceDescription other = (ServiceDescription) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.requiresRegistration == other.isRequiresRegistration() &&
            ((this.offeredPortlets==null && other.getOfferedPortlets()==null) || 
             (this.offeredPortlets!=null &&
              java.util.Arrays.equals(this.offeredPortlets, other.getOfferedPortlets()))) &&
            ((this.userCategoryDescriptions==null && other.getUserCategoryDescriptions()==null) || 
             (this.userCategoryDescriptions!=null &&
              java.util.Arrays.equals(this.userCategoryDescriptions, other.getUserCategoryDescriptions()))) &&
            ((this.customUserProfileItemDescriptions==null && other.getCustomUserProfileItemDescriptions()==null) || 
             (this.customUserProfileItemDescriptions!=null &&
              java.util.Arrays.equals(this.customUserProfileItemDescriptions, other.getCustomUserProfileItemDescriptions()))) &&
            ((this.customWindowStateDescriptions==null && other.getCustomWindowStateDescriptions()==null) || 
             (this.customWindowStateDescriptions!=null &&
              java.util.Arrays.equals(this.customWindowStateDescriptions, other.getCustomWindowStateDescriptions()))) &&
            ((this.customModeDescriptions==null && other.getCustomModeDescriptions()==null) || 
             (this.customModeDescriptions!=null &&
              java.util.Arrays.equals(this.customModeDescriptions, other.getCustomModeDescriptions()))) &&
            ((this.requiresInitCookie==null && other.getRequiresInitCookie()==null) || 
             (this.requiresInitCookie!=null &&
              this.requiresInitCookie.equals(other.getRequiresInitCookie()))) &&
            ((this.registrationPropertyDescription==null && other.getRegistrationPropertyDescription()==null) || 
             (this.registrationPropertyDescription!=null &&
              this.registrationPropertyDescription.equals(other.getRegistrationPropertyDescription()))) &&
            ((this.locales==null && other.getLocales()==null) || 
             (this.locales!=null &&
              java.util.Arrays.equals(this.locales, other.getLocales()))) &&
            ((this.resourceList==null && other.getResourceList()==null) || 
             (this.resourceList!=null &&
              this.resourceList.equals(other.getResourceList()))) &&
            ((this.extensions==null && other.getExtensions()==null) || 
             (this.extensions!=null &&
              java.util.Arrays.equals(this.extensions, other.getExtensions())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += new Boolean(isRequiresRegistration()).hashCode();
        if (getOfferedPortlets() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getOfferedPortlets());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getOfferedPortlets(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getUserCategoryDescriptions() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUserCategoryDescriptions());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUserCategoryDescriptions(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getCustomUserProfileItemDescriptions() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCustomUserProfileItemDescriptions());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCustomUserProfileItemDescriptions(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getCustomWindowStateDescriptions() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCustomWindowStateDescriptions());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCustomWindowStateDescriptions(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getCustomModeDescriptions() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCustomModeDescriptions());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCustomModeDescriptions(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRequiresInitCookie() != null) {
            _hashCode += getRequiresInitCookie().hashCode();
        }
        if (getRegistrationPropertyDescription() != null) {
            _hashCode += getRegistrationPropertyDescription().hashCode();
        }
        if (getLocales() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getLocales());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getLocales(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getResourceList() != null) {
            _hashCode += getResourceList().hashCode();
        }
        if (getExtensions() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getExtensions());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getExtensions(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ServiceDescription.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ServiceDescription"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("requiresRegistration");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "requiresRegistration"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("offeredPortlets");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "offeredPortlets"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletDescription"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userCategoryDescriptions");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userCategoryDescriptions"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ItemDescription"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customUserProfileItemDescriptions");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customUserProfileItemDescriptions"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ItemDescription"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customWindowStateDescriptions");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customWindowStateDescriptions"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ItemDescription"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customModeDescriptions");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customModeDescriptions"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ItemDescription"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("requiresInitCookie");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "requiresInitCookie"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "CookieProtocol"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("registrationPropertyDescription");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationPropertyDescription"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ModelDescription"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("locales");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "locales"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resourceList");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "resourceList"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ResourceList"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("extensions");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "extensions"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Extension"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
