/**
 * _destroyPortlets.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.types;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class _destroyPortlets  implements java.io.Serializable {
    private org.jasig.portal.wsrp.types.RegistrationContext registrationContext;
    private java.lang.String[] portletHandles;

    public _destroyPortlets() {
    }

    public org.jasig.portal.wsrp.types.RegistrationContext getRegistrationContext() {
        return registrationContext;
    }

    public void setRegistrationContext(org.jasig.portal.wsrp.types.RegistrationContext registrationContext) {
        this.registrationContext = registrationContext;
    }

    public java.lang.String[] getPortletHandles() {
        return portletHandles;
    }

    public void setPortletHandles(java.lang.String[] portletHandles) {
        this.portletHandles = portletHandles;
    }

    public java.lang.String getPortletHandles(int i) {
        return portletHandles[i];
    }

    public void setPortletHandles(int i, java.lang.String value) {
        this.portletHandles[i] = value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof _destroyPortlets)) return false;
        _destroyPortlets other = (_destroyPortlets) obj;
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
            ((this.portletHandles==null && other.getPortletHandles()==null) || 
             (this.portletHandles!=null &&
              java.util.Arrays.equals(this.portletHandles, other.getPortletHandles())));
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
        if (getPortletHandles() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPortletHandles());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPortletHandles(), i);
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
        new org.apache.axis.description.TypeDesc(_destroyPortlets.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", ">destroyPortlets"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("registrationContext");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationContext"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("portletHandles");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletHandles"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
