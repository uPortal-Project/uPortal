/**
 * Templates.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.types;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class Templates  implements java.io.Serializable {
    private java.lang.String defaultTemplate;
    private java.lang.String blockingActionTemplate;
    private java.lang.String renderTemplate;
    private java.lang.String resourceTemplate;
    private java.lang.String secureDefaultTemplate;
    private java.lang.String secureBlockingActionTemplate;
    private java.lang.String secureRenderTemplate;
    private java.lang.String secureResourceTemplate;
    private org.jasig.portal.wsrp.types.Extension[] extensions;

    public Templates() {
    }

    public java.lang.String getDefaultTemplate() {
        return defaultTemplate;
    }

    public void setDefaultTemplate(java.lang.String defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
    }

    public java.lang.String getBlockingActionTemplate() {
        return blockingActionTemplate;
    }

    public void setBlockingActionTemplate(java.lang.String blockingActionTemplate) {
        this.blockingActionTemplate = blockingActionTemplate;
    }

    public java.lang.String getRenderTemplate() {
        return renderTemplate;
    }

    public void setRenderTemplate(java.lang.String renderTemplate) {
        this.renderTemplate = renderTemplate;
    }

    public java.lang.String getResourceTemplate() {
        return resourceTemplate;
    }

    public void setResourceTemplate(java.lang.String resourceTemplate) {
        this.resourceTemplate = resourceTemplate;
    }

    public java.lang.String getSecureDefaultTemplate() {
        return secureDefaultTemplate;
    }

    public void setSecureDefaultTemplate(java.lang.String secureDefaultTemplate) {
        this.secureDefaultTemplate = secureDefaultTemplate;
    }

    public java.lang.String getSecureBlockingActionTemplate() {
        return secureBlockingActionTemplate;
    }

    public void setSecureBlockingActionTemplate(java.lang.String secureBlockingActionTemplate) {
        this.secureBlockingActionTemplate = secureBlockingActionTemplate;
    }

    public java.lang.String getSecureRenderTemplate() {
        return secureRenderTemplate;
    }

    public void setSecureRenderTemplate(java.lang.String secureRenderTemplate) {
        this.secureRenderTemplate = secureRenderTemplate;
    }

    public java.lang.String getSecureResourceTemplate() {
        return secureResourceTemplate;
    }

    public void setSecureResourceTemplate(java.lang.String secureResourceTemplate) {
        this.secureResourceTemplate = secureResourceTemplate;
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
        if (!(obj instanceof Templates)) return false;
        Templates other = (Templates) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.defaultTemplate==null && other.getDefaultTemplate()==null) || 
             (this.defaultTemplate!=null &&
              this.defaultTemplate.equals(other.getDefaultTemplate()))) &&
            ((this.blockingActionTemplate==null && other.getBlockingActionTemplate()==null) || 
             (this.blockingActionTemplate!=null &&
              this.blockingActionTemplate.equals(other.getBlockingActionTemplate()))) &&
            ((this.renderTemplate==null && other.getRenderTemplate()==null) || 
             (this.renderTemplate!=null &&
              this.renderTemplate.equals(other.getRenderTemplate()))) &&
            ((this.resourceTemplate==null && other.getResourceTemplate()==null) || 
             (this.resourceTemplate!=null &&
              this.resourceTemplate.equals(other.getResourceTemplate()))) &&
            ((this.secureDefaultTemplate==null && other.getSecureDefaultTemplate()==null) || 
             (this.secureDefaultTemplate!=null &&
              this.secureDefaultTemplate.equals(other.getSecureDefaultTemplate()))) &&
            ((this.secureBlockingActionTemplate==null && other.getSecureBlockingActionTemplate()==null) || 
             (this.secureBlockingActionTemplate!=null &&
              this.secureBlockingActionTemplate.equals(other.getSecureBlockingActionTemplate()))) &&
            ((this.secureRenderTemplate==null && other.getSecureRenderTemplate()==null) || 
             (this.secureRenderTemplate!=null &&
              this.secureRenderTemplate.equals(other.getSecureRenderTemplate()))) &&
            ((this.secureResourceTemplate==null && other.getSecureResourceTemplate()==null) || 
             (this.secureResourceTemplate!=null &&
              this.secureResourceTemplate.equals(other.getSecureResourceTemplate()))) &&
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
        if (getDefaultTemplate() != null) {
            _hashCode += getDefaultTemplate().hashCode();
        }
        if (getBlockingActionTemplate() != null) {
            _hashCode += getBlockingActionTemplate().hashCode();
        }
        if (getRenderTemplate() != null) {
            _hashCode += getRenderTemplate().hashCode();
        }
        if (getResourceTemplate() != null) {
            _hashCode += getResourceTemplate().hashCode();
        }
        if (getSecureDefaultTemplate() != null) {
            _hashCode += getSecureDefaultTemplate().hashCode();
        }
        if (getSecureBlockingActionTemplate() != null) {
            _hashCode += getSecureBlockingActionTemplate().hashCode();
        }
        if (getSecureRenderTemplate() != null) {
            _hashCode += getSecureRenderTemplate().hashCode();
        }
        if (getSecureResourceTemplate() != null) {
            _hashCode += getSecureResourceTemplate().hashCode();
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
        new org.apache.axis.description.TypeDesc(Templates.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Templates"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("defaultTemplate");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "defaultTemplate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("blockingActionTemplate");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "blockingActionTemplate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("renderTemplate");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "renderTemplate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resourceTemplate");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "resourceTemplate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("secureDefaultTemplate");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "secureDefaultTemplate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("secureBlockingActionTemplate");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "secureBlockingActionTemplate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("secureRenderTemplate");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "secureRenderTemplate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("secureResourceTemplate");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "secureResourceTemplate"));
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
