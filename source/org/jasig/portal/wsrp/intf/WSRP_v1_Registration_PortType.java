/**
 * WSRP_v1_Registration_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.intf;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public interface WSRP_v1_Registration_PortType extends java.rmi.Remote {
    public void register(java.lang.String consumerName, java.lang.String consumerAgent, boolean methodGetSupported, java.lang.String[] consumerModes, java.lang.String[] consumerWindowStates, java.lang.String[] consumerUserScopes, java.lang.String[] customUserProfileData, org.jasig.portal.wsrp.types.Property[] registrationProperties, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions, javax.xml.rpc.holders.StringHolder registrationHandle, javax.xml.rpc.holders.ByteArrayHolder registrationState) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault;
    public void deregister(java.lang.String registrationHandle, byte[] registrationState, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault;
    public void modifyRegistration(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.RegistrationData registrationData, javax.xml.rpc.holders.ByteArrayHolder registrationState, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault;
}
