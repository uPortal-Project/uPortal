/**
 * _setPortletProperties.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.types;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class _setPortletProperties  implements java.io.Serializable {
    private org.jasig.portal.wsrp.types.RegistrationContext registrationContext;
    private org.jasig.portal.wsrp.types.PortletContext portletContext;
    private org.jasig.portal.wsrp.types.UserContext userContext;
    private org.jasig.portal.wsrp.types.PropertyList propertyList;

    public _setPortletProperties() {
    }

    public org.jasig.portal.wsrp.types.RegistrationContext getRegistrationContext() {
        return registrationContext;
    }

    public void setRegistrationContext(org.jasig.portal.wsrp.types.RegistrationContext registrationContext) {
        this.registrationContext = registrationContext;
    }

    public org.jasig.portal.wsrp.types.PortletContext getPortletContext() {
        return portletContext;
    }

    public void setPortletContext(org.jasig.portal.wsrp.types.PortletContext portletContext) {
        this.portletContext = portletContext;
    }

    public org.jasig.portal.wsrp.types.UserContext getUserContext() {
        return userContext;
    }

    public void setUserContext(org.jasig.portal.wsrp.types.UserContext userContext) {
        this.userContext = userContext;
    }

    public org.jasig.portal.wsrp.types.PropertyList getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(org.jasig.portal.wsrp.types.PropertyList propertyList) {
        this.propertyList = propertyList;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof _setPortletProperties)) return false;
        _setPortletProperties other = (_setPortletProperties) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.registrationContext==null && other.getRegistrationContext()==null) || 
             (this.registrationContext!=null &&
              this.registrationContext.equals(other.getRegistrationContext()))) &&
            ((this.portletContext==null && other.getPortletContext()==null) || 
             (this.portletContext!=null &&
              this.portletContext.equals(other.getPortletContext()))) &&
            ((this.userContext==null && other.getUserContext()==null) || 
             (this.userContext!=null &&
              this.userContext.equals(other.getUserContext()))) &&
            ((this.propertyList==null && other.getPropertyList()==null) || 
             (this.propertyList!=null &&
              this.propertyList.equals(other.getPropertyList())));
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
        if (getRegistrationContext() != null) {
            _hashCode += getRegistrationContext().hashCode();
        }
        if (getPortletContext() != null) {
            _hashCode += getPortletContext().hashCode();
        }
        if (getUserContext() != null) {
            _hashCode += getUserContext().hashCode();
        }
        if (getPropertyList() != null) {
            _hashCode += getPropertyList().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(_setPortletProperties.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", ">setPortletProperties"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("registrationContext");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("portletContext");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletContext"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletContext"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userContext");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userContext"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UserContext"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("propertyList");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "propertyList"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PropertyList"));
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
