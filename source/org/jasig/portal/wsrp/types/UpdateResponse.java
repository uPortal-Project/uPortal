/**
 * UpdateResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.types;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class UpdateResponse  implements java.io.Serializable {
    private org.jasig.portal.wsrp.types.SessionContext sessionContext;
    private org.jasig.portal.wsrp.types.PortletContext portletContext;
    private org.jasig.portal.wsrp.types.MarkupContext markupContext;
    private java.lang.String navigationalState;
    private java.lang.String newWindowState;
    private java.lang.String newMode;

    public UpdateResponse() {
    }

    public org.jasig.portal.wsrp.types.SessionContext getSessionContext() {
        return sessionContext;
    }

    public void setSessionContext(org.jasig.portal.wsrp.types.SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    public org.jasig.portal.wsrp.types.PortletContext getPortletContext() {
        return portletContext;
    }

    public void setPortletContext(org.jasig.portal.wsrp.types.PortletContext portletContext) {
        this.portletContext = portletContext;
    }

    public org.jasig.portal.wsrp.types.MarkupContext getMarkupContext() {
        return markupContext;
    }

    public void setMarkupContext(org.jasig.portal.wsrp.types.MarkupContext markupContext) {
        this.markupContext = markupContext;
    }

    public java.lang.String getNavigationalState() {
        return navigationalState;
    }

    public void setNavigationalState(java.lang.String navigationalState) {
        this.navigationalState = navigationalState;
    }

    public java.lang.String getNewWindowState() {
        return newWindowState;
    }

    public void setNewWindowState(java.lang.String newWindowState) {
        this.newWindowState = newWindowState;
    }

    public java.lang.String getNewMode() {
        return newMode;
    }

    public void setNewMode(java.lang.String newMode) {
        this.newMode = newMode;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UpdateResponse)) return false;
        UpdateResponse other = (UpdateResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.sessionContext==null && other.getSessionContext()==null) || 
             (this.sessionContext!=null &&
              this.sessionContext.equals(other.getSessionContext()))) &&
            ((this.portletContext==null && other.getPortletContext()==null) || 
             (this.portletContext!=null &&
              this.portletContext.equals(other.getPortletContext()))) &&
            ((this.markupContext==null && other.getMarkupContext()==null) || 
             (this.markupContext!=null &&
              this.markupContext.equals(other.getMarkupContext()))) &&
            ((this.navigationalState==null && other.getNavigationalState()==null) || 
             (this.navigationalState!=null &&
              this.navigationalState.equals(other.getNavigationalState()))) &&
            ((this.newWindowState==null && other.getNewWindowState()==null) || 
             (this.newWindowState!=null &&
              this.newWindowState.equals(other.getNewWindowState()))) &&
            ((this.newMode==null && other.getNewMode()==null) || 
             (this.newMode!=null &&
              this.newMode.equals(other.getNewMode())));
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
        if (getSessionContext() != null) {
            _hashCode += getSessionContext().hashCode();
        }
        if (getPortletContext() != null) {
            _hashCode += getPortletContext().hashCode();
        }
        if (getMarkupContext() != null) {
            _hashCode += getMarkupContext().hashCode();
        }
        if (getNavigationalState() != null) {
            _hashCode += getNavigationalState().hashCode();
        }
        if (getNewWindowState() != null) {
            _hashCode += getNewWindowState().hashCode();
        }
        if (getNewMode() != null) {
            _hashCode += getNewMode().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UpdateResponse.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "UpdateResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sessionContext");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "sessionContext"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "SessionContext"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("portletContext");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletContext"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletContext"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("markupContext");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "markupContext"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupContext"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("navigationalState");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "navigationalState"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("newWindowState");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "newWindowState"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("newMode");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "newMode"));
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
