/**
 * _getServiceDescription.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.types;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class _getServiceDescription  implements java.io.Serializable {
    private org.jasig.portal.wsrp.types.RegistrationContext registrationContext;
    private java.lang.String[] desiredLocales;

    public _getServiceDescription() {
    }

    public org.jasig.portal.wsrp.types.RegistrationContext getRegistrationContext() {
        return registrationContext;
    }

    public void setRegistrationContext(org.jasig.portal.wsrp.types.RegistrationContext registrationContext) {
        this.registrationContext = registrationContext;
    }

    public java.lang.String[] getDesiredLocales() {
        return desiredLocales;
    }

    public void setDesiredLocales(java.lang.String[] desiredLocales) {
        this.desiredLocales = desiredLocales;
    }

    public java.lang.String getDesiredLocales(int i) {
        return desiredLocales[i];
    }

    public void setDesiredLocales(int i, java.lang.String value) {
        this.desiredLocales[i] = value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof _getServiceDescription)) return false;
        _getServiceDescription other = (_getServiceDescription) obj;
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
            ((this.desiredLocales==null && other.getDesiredLocales()==null) || 
             (this.desiredLocales!=null &&
              java.util.Arrays.equals(this.desiredLocales, other.getDesiredLocales())));
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
        if (getDesiredLocales() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDesiredLocales());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDesiredLocales(), i);
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
        new org.apache.axis.description.TypeDesc(_getServiceDescription.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", ">getServiceDescription"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("registrationContext");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("desiredLocales");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "desiredLocales"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
