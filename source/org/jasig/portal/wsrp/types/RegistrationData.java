/**
 * RegistrationData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.types;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class RegistrationData  implements java.io.Serializable {
    private java.lang.String consumerName;
    private java.lang.String consumerAgent;
    private boolean methodGetSupported;
    private java.lang.String[] consumerModes;
    private java.lang.String[] consumerWindowStates;
    private java.lang.String[] consumerUserScopes;
    private java.lang.String[] customUserProfileData;
    private org.jasig.portal.wsrp.types.Property[] registrationProperties;
    private org.jasig.portal.wsrp.types.Extension[] extensions;

    public RegistrationData() {
    }

    public java.lang.String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(java.lang.String consumerName) {
        this.consumerName = consumerName;
    }

    public java.lang.String getConsumerAgent() {
        return consumerAgent;
    }

    public void setConsumerAgent(java.lang.String consumerAgent) {
        this.consumerAgent = consumerAgent;
    }

    public boolean isMethodGetSupported() {
        return methodGetSupported;
    }

    public void setMethodGetSupported(boolean methodGetSupported) {
        this.methodGetSupported = methodGetSupported;
    }

    public java.lang.String[] getConsumerModes() {
        return consumerModes;
    }

    public void setConsumerModes(java.lang.String[] consumerModes) {
        this.consumerModes = consumerModes;
    }

    public java.lang.String getConsumerModes(int i) {
        return consumerModes[i];
    }

    public void setConsumerModes(int i, java.lang.String value) {
        this.consumerModes[i] = value;
    }

    public java.lang.String[] getConsumerWindowStates() {
        return consumerWindowStates;
    }

    public void setConsumerWindowStates(java.lang.String[] consumerWindowStates) {
        this.consumerWindowStates = consumerWindowStates;
    }

    public java.lang.String getConsumerWindowStates(int i) {
        return consumerWindowStates[i];
    }

    public void setConsumerWindowStates(int i, java.lang.String value) {
        this.consumerWindowStates[i] = value;
    }

    public java.lang.String[] getConsumerUserScopes() {
        return consumerUserScopes;
    }

    public void setConsumerUserScopes(java.lang.String[] consumerUserScopes) {
        this.consumerUserScopes = consumerUserScopes;
    }

    public java.lang.String getConsumerUserScopes(int i) {
        return consumerUserScopes[i];
    }

    public void setConsumerUserScopes(int i, java.lang.String value) {
        this.consumerUserScopes[i] = value;
    }

    public java.lang.String[] getCustomUserProfileData() {
        return customUserProfileData;
    }

    public void setCustomUserProfileData(java.lang.String[] customUserProfileData) {
        this.customUserProfileData = customUserProfileData;
    }

    public java.lang.String getCustomUserProfileData(int i) {
        return customUserProfileData[i];
    }

    public void setCustomUserProfileData(int i, java.lang.String value) {
        this.customUserProfileData[i] = value;
    }

    public org.jasig.portal.wsrp.types.Property[] getRegistrationProperties() {
        return registrationProperties;
    }

    public void setRegistrationProperties(org.jasig.portal.wsrp.types.Property[] registrationProperties) {
        this.registrationProperties = registrationProperties;
    }

    public org.jasig.portal.wsrp.types.Property getRegistrationProperties(int i) {
        return registrationProperties[i];
    }

    public void setRegistrationProperties(int i, org.jasig.portal.wsrp.types.Property value) {
        this.registrationProperties[i] = value;
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
        if (!(obj instanceof RegistrationData)) return false;
        RegistrationData other = (RegistrationData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.consumerName==null && other.getConsumerName()==null) || 
             (this.consumerName!=null &&
              this.consumerName.equals(other.getConsumerName()))) &&
            ((this.consumerAgent==null && other.getConsumerAgent()==null) || 
             (this.consumerAgent!=null &&
              this.consumerAgent.equals(other.getConsumerAgent()))) &&
            this.methodGetSupported == other.isMethodGetSupported() &&
            ((this.consumerModes==null && other.getConsumerModes()==null) || 
             (this.consumerModes!=null &&
              java.util.Arrays.equals(this.consumerModes, other.getConsumerModes()))) &&
            ((this.consumerWindowStates==null && other.getConsumerWindowStates()==null) || 
             (this.consumerWindowStates!=null &&
              java.util.Arrays.equals(this.consumerWindowStates, other.getConsumerWindowStates()))) &&
            ((this.consumerUserScopes==null && other.getConsumerUserScopes()==null) || 
             (this.consumerUserScopes!=null &&
              java.util.Arrays.equals(this.consumerUserScopes, other.getConsumerUserScopes()))) &&
            ((this.customUserProfileData==null && other.getCustomUserProfileData()==null) || 
             (this.customUserProfileData!=null &&
              java.util.Arrays.equals(this.customUserProfileData, other.getCustomUserProfileData()))) &&
            ((this.registrationProperties==null && other.getRegistrationProperties()==null) || 
             (this.registrationProperties!=null &&
              java.util.Arrays.equals(this.registrationProperties, other.getRegistrationProperties()))) &&
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
        if (getConsumerName() != null) {
            _hashCode += getConsumerName().hashCode();
        }
        if (getConsumerAgent() != null) {
            _hashCode += getConsumerAgent().hashCode();
        }
        _hashCode += new Boolean(isMethodGetSupported()).hashCode();
        if (getConsumerModes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getConsumerModes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getConsumerModes(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getConsumerWindowStates() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getConsumerWindowStates());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getConsumerWindowStates(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getConsumerUserScopes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getConsumerUserScopes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getConsumerUserScopes(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getCustomUserProfileData() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCustomUserProfileData());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCustomUserProfileData(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRegistrationProperties() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRegistrationProperties());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRegistrationProperties(), i);
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
        new org.apache.axis.description.TypeDesc(RegistrationData.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("consumerName");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "consumerName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("consumerAgent");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "consumerAgent"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("methodGetSupported");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "methodGetSupported"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("consumerModes");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "consumerModes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("consumerWindowStates");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "consumerWindowStates"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("consumerUserScopes");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "consumerUserScopes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customUserProfileData");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "customUserProfileData"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("registrationProperties");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "registrationProperties"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "Property"));
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
