package org.jasig.portal.portlet.om;

public interface IPortletDescriptorKey {

    public String getWebAppName();
    
    public boolean isFrameworkPortlet();
    
    public String getPortletName();

    public void setWebAppName(String webAppName);
    
    public void setFrameworkPortlet(boolean isFrameworkPortlet);
    
    public void setPortletName(String portletName);

}
