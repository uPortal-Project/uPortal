/**
 * MarkupType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.types;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class MarkupType  implements java.io.Serializable {
    private java.lang.String mimeType;
    private java.lang.String[] modes;
    private java.lang.String[] windowStates;
    private java.lang.String[] locales;
    private org.jasig.portal.wsrp.types.Extension[] extensions;

    public MarkupType() {
    }

    public java.lang.String getMimeType() {
        return mimeType;
    }

    public void setMimeType(java.lang.String mimeType) {
        this.mimeType = mimeType;
    }

    public java.lang.String[] getModes() {
        return modes;
    }

    public void setModes(java.lang.String[] modes) {
        this.modes = modes;
    }

    public java.lang.String getModes(int i) {
        return modes[i];
    }

    public void setModes(int i, java.lang.String value) {
        this.modes[i] = value;
    }

    public java.lang.String[] getWindowStates() {
        return windowStates;
    }

    public void setWindowStates(java.lang.String[] windowStates) {
        this.windowStates = windowStates;
    }

    public java.lang.String getWindowStates(int i) {
        return windowStates[i];
    }

    public void setWindowStates(int i, java.lang.String value) {
        this.windowStates[i] = value;
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
        if (!(obj instanceof MarkupType)) return false;
        MarkupType other = (MarkupType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.mimeType==null && other.getMimeType()==null) || 
             (this.mimeType!=null &&
              this.mimeType.equals(other.getMimeType()))) &&
            ((this.modes==null && other.getModes()==null) || 
             (this.modes!=null &&
              java.util.Arrays.equals(this.modes, other.getModes()))) &&
            ((this.windowStates==null && other.getWindowStates()==null) || 
             (this.windowStates!=null &&
              java.util.Arrays.equals(this.windowStates, other.getWindowStates()))) &&
            ((this.locales==null && other.getLocales()==null) || 
             (this.locales!=null &&
              java.util.Arrays.equals(this.locales, other.getLocales()))) &&
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
        if (getMimeType() != null) {
            _hashCode += getMimeType().hashCode();
        }
        if (getModes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getModes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getModes(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getWindowStates() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getWindowStates());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getWindowStates(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
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
        new org.apache.axis.description.TypeDesc(MarkupType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mimeType");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "mimeType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("modes");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "modes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("windowStates");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "windowStates"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("locales");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "locales"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
