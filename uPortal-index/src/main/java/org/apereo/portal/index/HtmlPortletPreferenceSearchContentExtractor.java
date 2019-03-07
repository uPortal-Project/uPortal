package org.apereo.portal.index;

import org.apereo.portal.portlet.om.IPortletDefinition;

public class HtmlPortletPreferenceSearchContentExtractor implements ISearchContentExtractor {

    private final String portletName;
    private final String webappName;
    private final String preferenceName;

    /* package-private */ HtmlPortletPreferenceSearchContentExtractor(String portletName, String webappName, String preferenceName) {
        this.portletName = portletName;
        this.webappName = webappName;
        this.preferenceName = preferenceName;
    }


    @Override
    public boolean appliesTo(IPortletDefinition portlet) {
        final boolean portletMatch = portletName.equalsIgnoreCase(portlet.getPortletDescriptorKey().getPortletName());
        final boolean webappMatch = webappName.equalsIgnoreCase(portlet.getPortletDescriptorKey().getWebAppName());
        final boolean preferenceMatch = portlet.getPortletPreferences().stream()
            .anyMatch(preference -> preference.getName().equalsIgnoreCase(preferenceName));

        return portletMatch && webappMatch && preferenceMatch;
    }

    @Override
    public String extractContent(IPortletDefinition portlet) {
        return "WIP -- indexed content!";
    }
}
