/**
 * WSRP_v1_ServiceDescription_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.intf;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public interface WSRP_v1_ServiceDescription_PortType extends java.rmi.Remote {
    public void getServiceDescription(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, java.lang.String[] desiredLocales, javax.xml.rpc.holders.BooleanHolder requiresRegistration, org.jasig.portal.wsrp.types.holders.PortletDescriptionArrayHolder offeredPortlets, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder userCategoryDescriptions, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder customUserProfileItemDescriptions, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder customWindowStateDescriptions, org.jasig.portal.wsrp.types.holders.ItemDescriptionArrayHolder customModeDescriptions, org.jasig.portal.wsrp.types.holders.CookieProtocolHolder requiresInitCookie, org.jasig.portal.wsrp.types.holders.ModelDescriptionHolder registrationPropertyDescription, org.jasig.portal.wsrp.types.holders.StringArrayHolder locales, org.jasig.portal.wsrp.types.holders.ResourceListHolder resourceList, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault;
}
