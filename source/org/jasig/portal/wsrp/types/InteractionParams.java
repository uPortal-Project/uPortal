/**
 * InteractionParams.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.types;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class InteractionParams  implements java.io.Serializable {
    private org.jasig.portal.wsrp.types.StateChange portletStateChange;
    private java.lang.String interactionState;
    private org.jasig.portal.wsrp.types.NamedString[] formParameters;
    private org.jasig.portal.wsrp.types.UploadContext[] uploadContexts;
    private org.jasig.portal.wsrp.types.Extension[] extensions;

    public InteractionParams() {
    }

    public org.jasig.portal.wsrp.types.StateChange getPortletStateChange() {
        return portletStateChange;
    }

    public void setPortletStateChange(org.jasig.portal.wsrp.types.StateChange portletStateChange) {
        this.portletStateChange = portletStateChange;
    }

    public java.lang.String getInteractionState() {
        return interactionState;
    }

    public void setInteractionState(java.lang.String interactionState) {
        this.interactionState = interactionState;
    }

    public org.jasig.portal.wsrp.types.NamedString[] getFormParameters() {
        return formParameters;
    }

    public void setFormParameters(org.jasig.portal.wsrp.types.NamedString[] formParameters) {
        this.formParameters = formParameters;
    }

    public org.jasig.portal.wsrp.types.NamedString getFormParameters(int i) {
        return formParameters[i];
    }

    public void setFormParameters(int i, org.jasig.portal.wsrp.types.NamedString value) {
        this.formParameters[i] = value;
    }

    public org.jasig.portal.wsrp.types.UploadContext[] getUploadContexts() {
        return uploadContexts;
    }

    public void setUploadContexts(org.jasig.portal.wsrp.types.UploadContext[] uploadContexts) {
        this.uploadContexts = uploadContexts;
    }

    public org.jasig.portal.wsrp.types.UploadContext getUploadContexts(int i) {
        return uploadContexts[i];
    }

    public void setUploadContexts(int i, org.jasig.portal.wsrp.types.UploadContext value) {
        this.uploadContexts[i] = value;
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
        if (!(obj instanceof InteractionParams)) return false;
        InteractionParams other = (InteractionParams) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.portletStateChange==null && other.getPortletStateChange()==null) || 
             (this.portletStateChange!=null &&
              this.portletStateChange.equals(other.getPortletStateChange()))) &&
            ((this.interactionState==null && other.getInteractionState()==null) || 
             (this.interactionState!=null &&
              this.interactionState.equals(other.getInteractionState()))) &&
            ((this.formParameters==null && other.getFormParameters()==null) || 
             (this.formParameters!=null &&
              java.util.Arrays.equals(this.formParameters, other.getFormParameters()))) &&
            ((this.uploadContexts==null && other.getUploadContexts()==null) || 
             (this.uploadContexts!=null &&
              java.util.Arrays.equals(this.uploadContexts, other.getUploadContexts()))) &&
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
        if (getPortletStateChange() != null) {
            _hashCode += getPortletStateChange().hashCode();
        }
        if (getInteractionState() != null) {
            _hashCode += getInteractionState().hashCode();
        }
        if (getFormParameters() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getFormParameters());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getFormParameters(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getUploadContexts() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUploadContexts());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUploadContexts(), i);
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
        new org.apache.axis.description.TypeDesc(InteractionParams.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "InteractionParams"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("portletStateChange");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletStateChange"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "StateChange"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("interactionState");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "interactionState"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("formParameters");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "formParameters"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "NamedString"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("uploadContexts");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "uploadContexts"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UploadContext"));
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
