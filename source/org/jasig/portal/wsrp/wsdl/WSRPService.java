/**
 * WSRPService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.wsdl;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public interface WSRPService extends javax.xml.rpc.Service {
    public java.lang.String getWSRPPortletManagementServiceAddress();

    public org.jasig.portal.wsrp.intf.WSRP_v1_PortletManagement_PortType getWSRPPortletManagementService() throws javax.xml.rpc.ServiceException;

    public org.jasig.portal.wsrp.intf.WSRP_v1_PortletManagement_PortType getWSRPPortletManagementService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
    public java.lang.String getWSRPRegistrationServiceAddress();

    public org.jasig.portal.wsrp.intf.WSRP_v1_Registration_PortType getWSRPRegistrationService() throws javax.xml.rpc.ServiceException;

    public org.jasig.portal.wsrp.intf.WSRP_v1_Registration_PortType getWSRPRegistrationService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
    public java.lang.String getWSRPBaseServiceAddress();

    public org.jasig.portal.wsrp.intf.WSRP_v1_Markup_PortType getWSRPBaseService() throws javax.xml.rpc.ServiceException;

    public org.jasig.portal.wsrp.intf.WSRP_v1_Markup_PortType getWSRPBaseService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
    public java.lang.String getWSRPServiceDescriptionServiceAddress();

    public org.jasig.portal.wsrp.intf.WSRP_v1_ServiceDescription_PortType getWSRPServiceDescriptionService() throws javax.xml.rpc.ServiceException;

    public org.jasig.portal.wsrp.intf.WSRP_v1_ServiceDescription_PortType getWSRPServiceDescriptionService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
