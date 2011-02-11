package org.jasig.portal.io.portlet.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

public class ExternalPortletDescriptorKeyRepresentation implements Serializable {

    private String webAppName;
    private String portletName;
    private boolean framework;

    @XmlElement(name = "webAppName")
    public String getWebAppName() {
        return webAppName;
    }

    public void setWebAppName(String webAppName) {
        this.webAppName = webAppName;
    }

    @XmlElement(name = "portletName")
    public String getPortletName() {
        return portletName;
    }

    public void setPortletName(String portletName) {
        this.portletName = portletName;
    }

    @XmlElement(name = "isFramework")
    public boolean isFramework() {
        return framework;
    }

    public void setFramework(boolean framework) {
        this.framework = framework;
    }

}
