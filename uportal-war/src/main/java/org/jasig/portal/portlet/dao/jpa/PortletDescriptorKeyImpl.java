package org.jasig.portal.portlet.dao.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.jasig.portal.portlet.om.IPortletDescriptorKey;

@Embeddable
class PortletDescriptorKeyImpl implements IPortletDescriptorKey, Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "PORTLET_WEBAPP_NAME", length = 255)
    private String webAppName;
    
    @Column(name = "PORTLET_FRAMEWORK", nullable = false)
    private boolean frameworkPortlet;
    
    @Column(name = "PORTLET_APPLICATION_NAME", length = 255, nullable = false)
    private String portletName;

    @Override
    public String getWebAppName() {
        return webAppName;
    }

    @Override
    public void setWebAppName(String webAppName) {
        this.webAppName = webAppName;
    }

    @Override
    public boolean isFrameworkPortlet() {
        return frameworkPortlet;
    }

    @Override
    public void setFrameworkPortlet(boolean frameworkPortlet) {
        this.frameworkPortlet = frameworkPortlet;
    }

    @Override
    public String getPortletName() {
        return portletName;
    }

    @Override
    public void setPortletName(String portletName) {
        this.portletName = portletName;
    }

}
