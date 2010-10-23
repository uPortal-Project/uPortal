package org.jasig.portal.portlets.search;

public class SearchPortletConfigurationForm {

    private boolean gsaEnabled;
    private String gsaBaseUrl;
    private String gsaSite;

    private boolean directoryEnabled;
    private boolean portletRegistryEnabled;

    public boolean isGsaEnabled() {
        return gsaEnabled;
    }

    public void setGsaEnabled(boolean gsaEnabled) {
        this.gsaEnabled = gsaEnabled;
    }

    public String getGsaBaseUrl() {
        return gsaBaseUrl;
    }

    public void setGsaBaseUrl(String gsaBaseUrl) {
        this.gsaBaseUrl = gsaBaseUrl;
    }

    public String getGsaSite() {
        return gsaSite;
    }

    public void setGsaSite(String gsaSite) {
        this.gsaSite = gsaSite;
    }

    public boolean isDirectoryEnabled() {
        return directoryEnabled;
    }

    public void setDirectoryEnabled(boolean directoryEnabled) {
        this.directoryEnabled = directoryEnabled;
    }

    public boolean isPortletRegistryEnabled() {
        return portletRegistryEnabled;
    }

    public void setPortletRegistryEnabled(boolean portletRegistryEnabled) {
        this.portletRegistryEnabled = portletRegistryEnabled;
    }

}
