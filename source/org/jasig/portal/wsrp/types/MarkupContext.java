/**
 * MarkupContext.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.types;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class MarkupContext  implements java.io.Serializable {
    private java.lang.Boolean useCachedMarkup;
    private java.lang.String mimeType;
    private java.lang.String markupString;
    private byte[] markupBinary;
    private java.lang.String locale;
    private java.lang.Boolean requiresUrlRewriting;
    private org.jasig.portal.wsrp.types.CacheControl cacheControl;
    private java.lang.String preferredTitle;
    private org.jasig.portal.wsrp.types.Extension[] extensions;

    public MarkupContext() {
    }

    public java.lang.Boolean getUseCachedMarkup() {
        return useCachedMarkup;
    }

    public void setUseCachedMarkup(java.lang.Boolean useCachedMarkup) {
        this.useCachedMarkup = useCachedMarkup;
    }

    public java.lang.String getMimeType() {
        return mimeType;
    }

    public void setMimeType(java.lang.String mimeType) {
        this.mimeType = mimeType;
    }

    public java.lang.String getMarkupString() {
        return markupString;
    }

    public void setMarkupString(java.lang.String markupString) {
        this.markupString = markupString;
    }

    public byte[] getMarkupBinary() {
        return markupBinary;
    }

    public void setMarkupBinary(byte[] markupBinary) {
        this.markupBinary = markupBinary;
    }

    public java.lang.String getLocale() {
        return locale;
    }

    public void setLocale(java.lang.String locale) {
        this.locale = locale;
    }

    public java.lang.Boolean getRequiresUrlRewriting() {
        return requiresUrlRewriting;
    }

    public void setRequiresUrlRewriting(java.lang.Boolean requiresUrlRewriting) {
        this.requiresUrlRewriting = requiresUrlRewriting;
    }

    public org.jasig.portal.wsrp.types.CacheControl getCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(org.jasig.portal.wsrp.types.CacheControl cacheControl) {
        this.cacheControl = cacheControl;
    }

    public java.lang.String getPreferredTitle() {
        return preferredTitle;
    }

    public void setPreferredTitle(java.lang.String preferredTitle) {
        this.preferredTitle = preferredTitle;
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
        if (!(obj instanceof MarkupContext)) return false;
        MarkupContext other = (MarkupContext) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.useCachedMarkup==null && other.getUseCachedMarkup()==null) || 
             (this.useCachedMarkup!=null &&
              this.useCachedMarkup.equals(other.getUseCachedMarkup()))) &&
            ((this.mimeType==null && other.getMimeType()==null) || 
             (this.mimeType!=null &&
              this.mimeType.equals(other.getMimeType()))) &&
            ((this.markupString==null && other.getMarkupString()==null) || 
             (this.markupString!=null &&
              this.markupString.equals(other.getMarkupString()))) &&
            ((this.markupBinary==null && other.getMarkupBinary()==null) || 
             (this.markupBinary!=null &&
              java.util.Arrays.equals(this.markupBinary, other.getMarkupBinary()))) &&
            ((this.locale==null && other.getLocale()==null) || 
             (this.locale!=null &&
              this.locale.equals(other.getLocale()))) &&
            ((this.requiresUrlRewriting==null && other.getRequiresUrlRewriting()==null) || 
             (this.requiresUrlRewriting!=null &&
              this.requiresUrlRewriting.equals(other.getRequiresUrlRewriting()))) &&
            ((this.cacheControl==null && other.getCacheControl()==null) || 
             (this.cacheControl!=null &&
              this.cacheControl.equals(other.getCacheControl()))) &&
            ((this.preferredTitle==null && other.getPreferredTitle()==null) || 
             (this.preferredTitle!=null &&
              this.preferredTitle.equals(other.getPreferredTitle()))) &&
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
        if (getUseCachedMarkup() != null) {
            _hashCode += getUseCachedMarkup().hashCode();
        }
        if (getMimeType() != null) {
            _hashCode += getMimeType().hashCode();
        }
        if (getMarkupString() != null) {
            _hashCode += getMarkupString().hashCode();
        }
        if (getMarkupBinary() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMarkupBinary());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMarkupBinary(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getLocale() != null) {
            _hashCode += getLocale().hashCode();
        }
        if (getRequiresUrlRewriting() != null) {
            _hashCode += getRequiresUrlRewriting().hashCode();
        }
        if (getCacheControl() != null) {
            _hashCode += getCacheControl().hashCode();
        }
        if (getPreferredTitle() != null) {
            _hashCode += getPreferredTitle().hashCode();
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
        new org.apache.axis.description.TypeDesc(MarkupContext.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupContext"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("useCachedMarkup");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "useCachedMarkup"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mimeType");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "mimeType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("markupString");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "markupString"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("markupBinary");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "markupBinary"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "base64"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("locale");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "locale"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("requiresUrlRewriting");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "requiresUrlRewriting"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("cacheControl");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "cacheControl"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "CacheControl"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("preferredTitle");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "preferredTitle"));
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
