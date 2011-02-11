package org.jasig.portal.portlet.dao.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.jasig.portal.portlet.om.IPortletDescriptorKey;

@Embeddable
public class PortletDescriptorKeyImpl implements IPortletDescriptorKey, Serializable {
    
    @Column(name = "PORTLET_WEBAPP_NAME", length = 255)
    private String webAppName;
    
    @Column(name = "PORTLET_FRAMEWORK", nullable = false)
    private boolean frameworkPortlet;
    
    @Column(name = "PORTLET_APPLICATION_NAME", length = 255, nullable = false)
    private String portletName;

    public String getWebAppName() {
        return webAppName;
    }

    public void setWebAppName(String webAppName) {
        this.webAppName = webAppName;
    }

    public boolean isFrameworkPortlet() {
        return frameworkPortlet;
    }

    public void setFrameworkPortlet(boolean frameworkPortlet) {
        this.frameworkPortlet = frameworkPortlet;
    }

    public String getPortletName() {
        return portletName;
    }

    public void setPortletName(String portletName) {
        this.portletName = portletName;
    }

}
