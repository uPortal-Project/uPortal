/**
 * WSRP_v1_ServiceDescription_Binding_SOAPImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.bind;

public class WSRP_v1_ServiceDescription_Binding_SOAPImpl implements org.jasig.portal.wsrp.intf.WSRP_v1_ServiceDescription_PortType{
    public void getServiceDescription(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, java.lang.String[] desiredLocales, javax.xml.rpc.holders.BooleanHolder requiresRegistration, org.jasig.portal.wsrp.types.holders.PortletDescriptionArrayHolder offeredPortlets, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder userCategoryDescriptions, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder customUserProfileItemDescriptions, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder customWindowStateDescriptions, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder customModeDescriptions, org.jasig.portal.wsrp.types.holders.CookieProtocolHolder requiresInitCookie, org.jasig.portal.wsrp.types.holders.ModelDescriptionHolder registrationPropertyDescription, org.jasig.portal.wsrp.types.holders.StringArrayHolder locales, org.jasig.portal.wsrp.types.holders.ResourceListHolder resourceList, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault {
        requiresRegistration.value = true;
        offeredPortlets.value = new org.jasig.portal.wsrp.types.PortletDescription[0];
        userCategoryDescriptions.value = new org.jasig.portal.wsrp.types.ItemDescription[0];
        customUserProfileItemDescriptions.value = new org.jasig.portal.wsrp.types.ItemDescription[0];
        customWindowStateDescriptions.value = new org.jasig.portal.wsrp.types.ItemDescription[0];
        customModeDescriptions.value = new org.jasig.portal.wsrp.types.ItemDescription[0];
        requiresInitCookie.value = org.jasig.portal.wsrp.types.CookieProtocol.none;
        registrationPropertyDescription.value = new org.jasig.portal.wsrp.types.ModelDescription();
        locales.value = new java.lang.String[0];
        resourceList.value = new org.jasig.portal.wsrp.types.ResourceList();
        extensions.value = new org.jasig.portal.wsrp.types.Extension[0];
    }

}
