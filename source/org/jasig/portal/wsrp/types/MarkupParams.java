/**
 * MarkupParams.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.types;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class MarkupParams  implements java.io.Serializable {
    private boolean secureClientCommunication;
    private java.lang.String[] locales;
    private java.lang.String[] mimeTypes;
    private java.lang.String mode;
    private java.lang.String windowState;
    private org.jasig.portal.wsrp.types.ClientData clientData;
    private java.lang.String navigationalState;
    private java.lang.String[] markupCharacterSets;
    private java.lang.String validateTag;
    private java.lang.String[] validNewModes;
    private java.lang.String[] validNewWindowStates;
    private org.jasig.portal.wsrp.types.Extension[] extensions;

    public MarkupParams() {
    }

    public boolean isSecureClientCommunication() {
        return secureClientCommunication;
    }

    public void setSecureClientCommunication(boolean secureClientCommunication) {
        this.secureClientCommunication = secureClientCommunication;
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

    public java.lang.String[] getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(java.lang.String[] mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public java.lang.String getMimeTypes(int i) {
        return mimeTypes[i];
    }

    public void setMimeTypes(int i, java.lang.String value) {
        this.mimeTypes[i] = value;
    }

    public java.lang.String getMode() {
        return mode;
    }

    public void setMode(java.lang.String mode) {
        this.mode = mode;
    }

    public java.lang.String getWindowState() {
        return windowState;
    }

    public void setWindowState(java.lang.String windowState) {
        this.windowState = windowState;
    }

    public org.jasig.portal.wsrp.types.ClientData getClientData() {
        return clientData;
    }

    public void setClientData(org.jasig.portal.wsrp.types.ClientData clientData) {
        this.clientData = clientData;
    }

    public java.lang.String getNavigationalState() {
        return navigationalState;
    }

    public void setNavigationalState(java.lang.String navigationalState) {
        this.navigationalState = navigationalState;
    }

    public java.lang.String[] getMarkupCharacterSets() {
        return markupCharacterSets;
    }

    public void setMarkupCharacterSets(java.lang.String[] markupCharacterSets) {
        this.markupCharacterSets = markupCharacterSets;
    }

    public java.lang.String getMarkupCharacterSets(int i) {
        return markupCharacterSets[i];
    }

    public void setMarkupCharacterSets(int i, java.lang.String value) {
        this.markupCharacterSets[i] = value;
    }

    public java.lang.String getValidateTag() {
        return validateTag;
    }

    public void setValidateTag(java.lang.String validateTag) {
        this.validateTag = validateTag;
    }

    public java.lang.String[] getValidNewModes() {
        return validNewModes;
    }

    public void setValidNewModes(java.lang.String[] validNewModes) {
        this.validNewModes = validNewModes;
    }

    public java.lang.String getValidNewModes(int i) {
        return validNewModes[i];
    }

    public void setValidNewModes(int i, java.lang.String value) {
        this.validNewModes[i] = value;
    }

    public java.lang.String[] getValidNewWindowStates() {
        return validNewWindowStates;
    }

    public void setValidNewWindowStates(java.lang.String[] validNewWindowStates) {
        this.validNewWindowStates = validNewWindowStates;
    }

    public java.lang.String getValidNewWindowStates(int i) {
        return validNewWindowStates[i];
    }

    public void setValidNewWindowStates(int i, java.lang.String value) {
        this.validNewWindowStates[i] = value;
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
        if (!(obj instanceof MarkupParams)) return false;
        MarkupParams other = (MarkupParams) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.secureClientCommunication == other.isSecureClientCommunication() &&
            ((this.locales==null && other.getLocales()==null) || 
             (this.locales!=null &&
              java.util.Arrays.equals(this.locales, other.getLocales()))) &&
            ((this.mimeTypes==null && other.getMimeTypes()==null) || 
             (this.mimeTypes!=null &&
              java.util.Arrays.equals(this.mimeTypes, other.getMimeTypes()))) &&
            ((this.mode==null && other.getMode()==null) || 
             (this.mode!=null &&
              this.mode.equals(other.getMode()))) &&
            ((this.windowState==null && other.getWindowState()==null) || 
             (this.windowState!=null &&
              this.windowState.equals(other.getWindowState()))) &&
            ((this.clientData==null && other.getClientData()==null) || 
             (this.clientData!=null &&
              this.clientData.equals(other.getClientData()))) &&
            ((this.navigationalState==null && other.getNavigationalState()==null) || 
             (this.navigationalState!=null &&
              this.navigationalState.equals(other.getNavigationalState()))) &&
            ((this.markupCharacterSets==null && other.getMarkupCharacterSets()==null) || 
             (this.markupCharacterSets!=null &&
              java.util.Arrays.equals(this.markupCharacterSets, other.getMarkupCharacterSets()))) &&
            ((this.validateTag==null && other.getValidateTag()==null) || 
             (this.validateTag!=null &&
              this.validateTag.equals(other.getValidateTag()))) &&
            ((this.validNewModes==null && other.getValidNewModes()==null) || 
             (this.validNewModes!=null &&
              java.util.Arrays.equals(this.validNewModes, other.getValidNewModes()))) &&
            ((this.validNewWindowStates==null && other.getValidNewWindowStates()==null) || 
             (this.validNewWindowStates!=null &&
              java.util.Arrays.equals(this.validNewWindowStates, other.getValidNewWindowStates()))) &&
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
        _hashCode += new Boolean(isSecureClientCommunication()).hashCode();
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
        if (getMimeTypes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMimeTypes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMimeTypes(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getMode() != null) {
            _hashCode += getMode().hashCode();
        }
        if (getWindowState() != null) {
            _hashCode += getWindowState().hashCode();
        }
        if (getClientData() != null) {
            _hashCode += getClientData().hashCode();
        }
        if (getNavigationalState() != null) {
            _hashCode += getNavigationalState().hashCode();
        }
        if (getMarkupCharacterSets() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMarkupCharacterSets());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMarkupCharacterSets(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getValidateTag() != null) {
            _hashCode += getValidateTag().hashCode();
        }
        if (getValidNewModes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getValidNewModes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getValidNewModes(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getValidNewWindowStates() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getValidNewWindowStates());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getValidNewWindowStates(), i);
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
        new org.apache.axis.description.TypeDesc(MarkupParams.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupParams"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("secureClientCommunication");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "secureClientCommunication"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("locales");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "locales"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mimeTypes");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "mimeTypes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mode");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "mode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("windowState");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "windowState"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("clientData");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "clientData"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "ClientData"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("navigationalState");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "navigationalState"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("markupCharacterSets");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "markupCharacterSets"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("validateTag");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "validateTag"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("validNewModes");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "validNewModes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("validNewWindowStates");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "validNewWindowStates"));
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
