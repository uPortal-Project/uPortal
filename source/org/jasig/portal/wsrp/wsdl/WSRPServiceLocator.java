/**
 * WSRPServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.wsdl;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class WSRPServiceLocator extends org.apache.axis.client.Service implements org.jasig.portal.wsrp.wsdl.WSRPService {

    // Use to get a proxy class for WSRPPortletManagementService
    private final java.lang.String WSRPPortletManagementService_address = "http://my.service:8080/WSRPService";

    public java.lang.String getWSRPPortletManagementServiceAddress() {
        return WSRPPortletManagementService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WSRPPortletManagementServiceWSDDServiceName = "WSRPPortletManagementService";

    public java.lang.String getWSRPPortletManagementServiceWSDDServiceName() {
        return WSRPPortletManagementServiceWSDDServiceName;
    }

    public void setWSRPPortletManagementServiceWSDDServiceName(java.lang.String name) {
        WSRPPortletManagementServiceWSDDServiceName = name;
    }

    public org.jasig.portal.wsrp.intf.WSRP_v1_PortletManagement_PortType getWSRPPortletManagementService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WSRPPortletManagementService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getWSRPPortletManagementService(endpoint);
    }

    public org.jasig.portal.wsrp.intf.WSRP_v1_PortletManagement_PortType getWSRPPortletManagementService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.jasig.portal.wsrp.bind.WSRP_v1_PortletManagement_Binding_SOAPStub _stub = new org.jasig.portal.wsrp.bind.WSRP_v1_PortletManagement_Binding_SOAPStub(portAddress, this);
            _stub.setPortName(getWSRPPortletManagementServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }


    // Use to get a proxy class for WSRPRegistrationService
    private final java.lang.String WSRPRegistrationService_address = "http://my.service:8080/WSRPService";

    public java.lang.String getWSRPRegistrationServiceAddress() {
        return WSRPRegistrationService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WSRPRegistrationServiceWSDDServiceName = "WSRPRegistrationService";

    public java.lang.String getWSRPRegistrationServiceWSDDServiceName() {
        return WSRPRegistrationServiceWSDDServiceName;
    }

    public void setWSRPRegistrationServiceWSDDServiceName(java.lang.String name) {
        WSRPRegistrationServiceWSDDServiceName = name;
    }

    public org.jasig.portal.wsrp.intf.WSRP_v1_Registration_PortType getWSRPRegistrationService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WSRPRegistrationService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getWSRPRegistrationService(endpoint);
    }

    public org.jasig.portal.wsrp.intf.WSRP_v1_Registration_PortType getWSRPRegistrationService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.jasig.portal.wsrp.bind.WSRP_v1_Registration_Binding_SOAPStub _stub = new org.jasig.portal.wsrp.bind.WSRP_v1_Registration_Binding_SOAPStub(portAddress, this);
            _stub.setPortName(getWSRPRegistrationServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }


    // Use to get a proxy class for WSRPBaseService
    private final java.lang.String WSRPBaseService_address = "http://my.service:8080/WSRPService";

    public java.lang.String getWSRPBaseServiceAddress() {
        return WSRPBaseService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WSRPBaseServiceWSDDServiceName = "WSRPBaseService";

    public java.lang.String getWSRPBaseServiceWSDDServiceName() {
        return WSRPBaseServiceWSDDServiceName;
    }

    public void setWSRPBaseServiceWSDDServiceName(java.lang.String name) {
        WSRPBaseServiceWSDDServiceName = name;
    }

    public org.jasig.portal.wsrp.intf.WSRP_v1_Markup_PortType getWSRPBaseService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WSRPBaseService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getWSRPBaseService(endpoint);
    }

    public org.jasig.portal.wsrp.intf.WSRP_v1_Markup_PortType getWSRPBaseService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.jasig.portal.wsrp.bind.WSRP_v1_Markup_Binding_SOAPStub _stub = new org.jasig.portal.wsrp.bind.WSRP_v1_Markup_Binding_SOAPStub(portAddress, this);
            _stub.setPortName(getWSRPBaseServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }


    // Use to get a proxy class for WSRPServiceDescriptionService
    private final java.lang.String WSRPServiceDescriptionService_address = "http://my.service:8080/WSRPService";

    public java.lang.String getWSRPServiceDescriptionServiceAddress() {
        return WSRPServiceDescriptionService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WSRPServiceDescriptionServiceWSDDServiceName = "WSRPServiceDescriptionService";

    public java.lang.String getWSRPServiceDescriptionServiceWSDDServiceName() {
        return WSRPServiceDescriptionServiceWSDDServiceName;
    }

    public void setWSRPServiceDescriptionServiceWSDDServiceName(java.lang.String name) {
        WSRPServiceDescriptionServiceWSDDServiceName = name;
    }

    public org.jasig.portal.wsrp.intf.WSRP_v1_ServiceDescription_PortType getWSRPServiceDescriptionService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WSRPServiceDescriptionService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getWSRPServiceDescriptionService(endpoint);
    }

    public org.jasig.portal.wsrp.intf.WSRP_v1_ServiceDescription_PortType getWSRPServiceDescriptionService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.jasig.portal.wsrp.bind.WSRP_v1_ServiceDescription_Binding_SOAPStub _stub = new org.jasig.portal.wsrp.bind.WSRP_v1_ServiceDescription_Binding_SOAPStub(portAddress, this);
            _stub.setPortName(getWSRPServiceDescriptionServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.jasig.portal.wsrp.intf.WSRP_v1_PortletManagement_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                org.jasig.portal.wsrp.bind.WSRP_v1_PortletManagement_Binding_SOAPStub _stub = new org.jasig.portal.wsrp.bind.WSRP_v1_PortletManagement_Binding_SOAPStub(new java.net.URL(WSRPPortletManagementService_address), this);
                _stub.setPortName(getWSRPPortletManagementServiceWSDDServiceName());
                return _stub;
            }
            if (org.jasig.portal.wsrp.intf.WSRP_v1_Registration_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                org.jasig.portal.wsrp.bind.WSRP_v1_Registration_Binding_SOAPStub _stub = new org.jasig.portal.wsrp.bind.WSRP_v1_Registration_Binding_SOAPStub(new java.net.URL(WSRPRegistrationService_address), this);
                _stub.setPortName(getWSRPRegistrationServiceWSDDServiceName());
                return _stub;
            }
            if (org.jasig.portal.wsrp.intf.WSRP_v1_Markup_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                org.jasig.portal.wsrp.bind.WSRP_v1_Markup_Binding_SOAPStub _stub = new org.jasig.portal.wsrp.bind.WSRP_v1_Markup_Binding_SOAPStub(new java.net.URL(WSRPBaseService_address), this);
                _stub.setPortName(getWSRPBaseServiceWSDDServiceName());
                return _stub;
            }
            if (org.jasig.portal.wsrp.intf.WSRP_v1_ServiceDescription_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                org.jasig.portal.wsrp.bind.WSRP_v1_ServiceDescription_Binding_SOAPStub _stub = new org.jasig.portal.wsrp.bind.WSRP_v1_ServiceDescription_Binding_SOAPStub(new java.net.URL(WSRPServiceDescriptionService_address), this);
                _stub.setPortName(getWSRPServiceDescriptionServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("WSRPPortletManagementService".equals(inputPortName)) {
            return getWSRPPortletManagementService();
        }
        else if ("WSRPRegistrationService".equals(inputPortName)) {
            return getWSRPRegistrationService();
        }
        else if ("WSRPBaseService".equals(inputPortName)) {
            return getWSRPBaseService();
        }
        else if ("WSRPServiceDescriptionService".equals(inputPortName)) {
            return getWSRPServiceDescriptionService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:oasis:names:tc:wsrp:v1:wsdl", "WSRPService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("WSRPPortletManagementService"));
            ports.add(new javax.xml.namespace.QName("WSRPRegistrationService"));
            ports.add(new javax.xml.namespace.QName("WSRPBaseService"));
            ports.add(new javax.xml.namespace.QName("WSRPServiceDescriptionService"));
        }
        return ports.iterator();
    }

}
