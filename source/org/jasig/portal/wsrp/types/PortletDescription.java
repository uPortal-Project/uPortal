/**
 * PortletDescription.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.types;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class PortletDescription  implements java.io.Serializable {
    private java.lang.String portletHandle;
    private org.jasig.portal.wsrp.types.MarkupType[] markupTypes;
    private java.lang.String groupID;
    private org.jasig.portal.wsrp.types.LocalizedString description;
    private org.jasig.portal.wsrp.types.LocalizedString shortTitle;
    private org.jasig.portal.wsrp.types.LocalizedString title;
    private org.jasig.portal.wsrp.types.LocalizedString displayName;
    private org.jasig.portal.wsrp.types.LocalizedString[] keywords;
    private java.lang.String[] userCategories;
    private java.lang.String[] userProfileItems;
    private java.lang.Boolean usesMethodGet;
    private java.lang.Boolean defaultMarkupSecure;
    private java.lang.Boolean onlySecure;
    private java.lang.Boolean userContextStoredInSession;
    private java.lang.Boolean templatesStoredInSession;
    private java.lang.Boolean hasUserSpecificState;
    private java.lang.Boolean doesUrlTemplateProcessing;
    private org.jasig.portal.wsrp.types.Extension[] extensions;

    public PortletDescription() {
    }

    public java.lang.String getPortletHandle() {
        return portletHandle;
    }

    public void setPortletHandle(java.lang.String portletHandle) {
        this.portletHandle = portletHandle;
    }

    public org.jasig.portal.wsrp.types.MarkupType[] getMarkupTypes() {
        return markupTypes;
    }

    public void setMarkupTypes(org.jasig.portal.wsrp.types.MarkupType[] markupTypes) {
        this.markupTypes = markupTypes;
    }

    public org.jasig.portal.wsrp.types.MarkupType getMarkupTypes(int i) {
        return markupTypes[i];
    }

    public void setMarkupTypes(int i, org.jasig.portal.wsrp.types.MarkupType value) {
        this.markupTypes[i] = value;
    }

    public java.lang.String getGroupID() {
        return groupID;
    }

    public void setGroupID(java.lang.String groupID) {
        this.groupID = groupID;
    }

    public org.jasig.portal.wsrp.types.LocalizedString getDescription() {
        return description;
    }

    public void setDescription(org.jasig.portal.wsrp.types.LocalizedString description) {
        this.description = description;
    }

    public org.jasig.portal.wsrp.types.LocalizedString getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(org.jasig.portal.wsrp.types.LocalizedString shortTitle) {
        this.shortTitle = shortTitle;
    }

    public org.jasig.portal.wsrp.types.LocalizedString getTitle() {
        return title;
    }

    public void setTitle(org.jasig.portal.wsrp.types.LocalizedString title) {
        this.title = title;
    }

    public org.jasig.portal.wsrp.types.LocalizedString getDisplayName() {
        return displayName;
    }

    public void setDisplayName(org.jasig.portal.wsrp.types.LocalizedString displayName) {
        this.displayName = displayName;
    }

    public org.jasig.portal.wsrp.types.LocalizedString[] getKeywords() {
        return keywords;
    }

    public void setKeywords(org.jasig.portal.wsrp.types.LocalizedString[] keywords) {
        this.keywords = keywords;
    }

    public org.jasig.portal.wsrp.types.LocalizedString getKeywords(int i) {
        return keywords[i];
    }

    public void setKeywords(int i, org.jasig.portal.wsrp.types.LocalizedString value) {
        this.keywords[i] = value;
    }

    public java.lang.String[] getUserCategories() {
        return userCategories;
    }

    public void setUserCategories(java.lang.String[] userCategories) {
        this.userCategories = userCategories;
    }

    public java.lang.String getUserCategories(int i) {
        return userCategories[i];
    }

    public void setUserCategories(int i, java.lang.String value) {
        this.userCategories[i] = value;
    }

    public java.lang.String[] getUserProfileItems() {
        return userProfileItems;
    }

    public void setUserProfileItems(java.lang.String[] userProfileItems) {
        this.userProfileItems = userProfileItems;
    }

    public java.lang.String getUserProfileItems(int i) {
        return userProfileItems[i];
    }

    public void setUserProfileItems(int i, java.lang.String value) {
        this.userProfileItems[i] = value;
    }

    public java.lang.Boolean getUsesMethodGet() {
        return usesMethodGet;
    }

    public void setUsesMethodGet(java.lang.Boolean usesMethodGet) {
        this.usesMethodGet = usesMethodGet;
    }

    public java.lang.Boolean getDefaultMarkupSecure() {
        return defaultMarkupSecure;
    }

    public void setDefaultMarkupSecure(java.lang.Boolean defaultMarkupSecure) {
        this.defaultMarkupSecure = defaultMarkupSecure;
    }

    public java.lang.Boolean getOnlySecure() {
        return onlySecure;
    }

    public void setOnlySecure(java.lang.Boolean onlySecure) {
        this.onlySecure = onlySecure;
    }

    public java.lang.Boolean getUserContextStoredInSession() {
        return userContextStoredInSession;
    }

    public void setUserContextStoredInSession(java.lang.Boolean userContextStoredInSession) {
        this.userContextStoredInSession = userContextStoredInSession;
    }

    public java.lang.Boolean getTemplatesStoredInSession() {
        return templatesStoredInSession;
    }

    public void setTemplatesStoredInSession(java.lang.Boolean templatesStoredInSession) {
        this.templatesStoredInSession = templatesStoredInSession;
    }

    public java.lang.Boolean getHasUserSpecificState() {
        return hasUserSpecificState;
    }

    public void setHasUserSpecificState(java.lang.Boolean hasUserSpecificState) {
        this.hasUserSpecificState = hasUserSpecificState;
    }

    public java.lang.Boolean getDoesUrlTemplateProcessing() {
        return doesUrlTemplateProcessing;
    }

    public void setDoesUrlTemplateProcessing(java.lang.Boolean doesUrlTemplateProcessing) {
        this.doesUrlTemplateProcessing = doesUrlTemplateProcessing;
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
        if (!(obj instanceof PortletDescription)) return false;
        PortletDescription other = (PortletDescription) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.portletHandle==null && other.getPortletHandle()==null) || 
             (this.portletHandle!=null &&
              this.portletHandle.equals(other.getPortletHandle()))) &&
            ((this.markupTypes==null && other.getMarkupTypes()==null) || 
             (this.markupTypes!=null &&
              java.util.Arrays.equals(this.markupTypes, other.getMarkupTypes()))) &&
            ((this.groupID==null && other.getGroupID()==null) || 
             (this.groupID!=null &&
              this.groupID.equals(other.getGroupID()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this.shortTitle==null && other.getShortTitle()==null) || 
             (this.shortTitle!=null &&
              this.shortTitle.equals(other.getShortTitle()))) &&
            ((this.title==null && other.getTitle()==null) || 
             (this.title!=null &&
              this.title.equals(other.getTitle()))) &&
            ((this.displayName==null && other.getDisplayName()==null) || 
             (this.displayName!=null &&
              this.displayName.equals(other.getDisplayName()))) &&
            ((this.keywords==null && other.getKeywords()==null) || 
             (this.keywords!=null &&
              java.util.Arrays.equals(this.keywords, other.getKeywords()))) &&
            ((this.userCategories==null && other.getUserCategories()==null) || 
             (this.userCategories!=null &&
              java.util.Arrays.equals(this.userCategories, other.getUserCategories()))) &&
            ((this.userProfileItems==null && other.getUserProfileItems()==null) || 
             (this.userProfileItems!=null &&
              java.util.Arrays.equals(this.userProfileItems, other.getUserProfileItems()))) &&
            ((this.usesMethodGet==null && other.getUsesMethodGet()==null) || 
             (this.usesMethodGet!=null &&
              this.usesMethodGet.equals(other.getUsesMethodGet()))) &&
            ((this.defaultMarkupSecure==null && other.getDefaultMarkupSecure()==null) || 
             (this.defaultMarkupSecure!=null &&
              this.defaultMarkupSecure.equals(other.getDefaultMarkupSecure()))) &&
            ((this.onlySecure==null && other.getOnlySecure()==null) || 
             (this.onlySecure!=null &&
              this.onlySecure.equals(other.getOnlySecure()))) &&
            ((this.userContextStoredInSession==null && other.getUserContextStoredInSession()==null) || 
             (this.userContextStoredInSession!=null &&
              this.userContextStoredInSession.equals(other.getUserContextStoredInSession()))) &&
            ((this.templatesStoredInSession==null && other.getTemplatesStoredInSession()==null) || 
             (this.templatesStoredInSession!=null &&
              this.templatesStoredInSession.equals(other.getTemplatesStoredInSession()))) &&
            ((this.hasUserSpecificState==null && other.getHasUserSpecificState()==null) || 
             (this.hasUserSpecificState!=null &&
              this.hasUserSpecificState.equals(other.getHasUserSpecificState()))) &&
            ((this.doesUrlTemplateProcessing==null && other.getDoesUrlTemplateProcessing()==null) || 
             (this.doesUrlTemplateProcessing!=null &&
              this.doesUrlTemplateProcessing.equals(other.getDoesUrlTemplateProcessing()))) &&
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
        if (getPortletHandle() != null) {
            _hashCode += getPortletHandle().hashCode();
        }
        if (getMarkupTypes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMarkupTypes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMarkupTypes(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getGroupID() != null) {
            _hashCode += getGroupID().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getShortTitle() != null) {
            _hashCode += getShortTitle().hashCode();
        }
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        if (getDisplayName() != null) {
            _hashCode += getDisplayName().hashCode();
        }
        if (getKeywords() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getKeywords());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getKeywords(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getUserCategories() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUserCategories());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUserCategories(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getUserProfileItems() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUserProfileItems());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUserProfileItems(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getUsesMethodGet() != null) {
            _hashCode += getUsesMethodGet().hashCode();
        }
        if (getDefaultMarkupSecure() != null) {
            _hashCode += getDefaultMarkupSecure().hashCode();
        }
        if (getOnlySecure() != null) {
            _hashCode += getOnlySecure().hashCode();
        }
        if (getUserContextStoredInSession() != null) {
            _hashCode += getUserContextStoredInSession().hashCode();
        }
        if (getTemplatesStoredInSession() != null) {
            _hashCode += getTemplatesStoredInSession().hashCode();
        }
        if (getHasUserSpecificState() != null) {
            _hashCode += getHasUserSpecificState().hashCode();
        }
        if (getDoesUrlTemplateProcessing() != null) {
            _hashCode += getDoesUrlTemplateProcessing().hashCode();
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
        new org.apache.axis.description.TypeDesc(PortletDescription.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "PortletDescription"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("portletHandle");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "portletHandle"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("markupTypes");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "markupTypes"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupType"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("groupID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "groupID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "description"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "LocalizedString"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("shortTitle");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "shortTitle"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "LocalizedString"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("title");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "title"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "LocalizedString"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("displayName");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "displayName"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "LocalizedString"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("keywords");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "keywords"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "LocalizedString"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userCategories");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userCategories"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userProfileItems");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userProfileItems"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("usesMethodGet");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "usesMethodGet"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("defaultMarkupSecure");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "defaultMarkupSecure"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("onlySecure");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "onlySecure"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userContextStoredInSession");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "userContextStoredInSession"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("templatesStoredInSession");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "templatesStoredInSession"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hasUserSpecificState");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "hasUserSpecificState"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("doesUrlTemplateProcessing");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:types", "doesUrlTemplateProcessing"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
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
