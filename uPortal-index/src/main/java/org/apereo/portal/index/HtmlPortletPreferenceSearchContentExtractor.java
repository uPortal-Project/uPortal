package org.apereo.portal.index;

import org.apache.commons.lang.StringEscapeUtils;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class HtmlPortletPreferenceSearchContentExtractor implements ISearchContentExtractor {

    private final String portletName;
    private final String webappName;
    private final String preferenceName;

    private final Logger logger = LoggerFactory.getLogger(getClass());

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

        // It's not ideal that we have to iterate the list to find the preference we want
        final IPortletPreference preference = portlet.getPortletPreferences().stream()
            .filter(item -> item.getName().equalsIgnoreCase(preferenceName))
            .findFirst()
            .orElse(null);

        if (preference == null) {
            // Nothing we can index...
            return null;
        }

        final StringBuilder stringBuilder = new StringBuilder();
        Stream.of(preference.getValues())
            .forEach(item -> stringBuilder.append(StringEscapeUtils.unescapeHtml(item)).append(" "));

        // There must be a single root element
        stringBuilder.insert(0, "<html><body>")
            .append("</body></html>");

        final String html = stringBuilder.toString();

        try {

            // Use JSoup to parse the HTML b/c it's often pretty sketchy
            final Document doc = Jsoup.parse(html);
            final String body = doc.body().text().trim();
            return body.length() > 0
                    ? body
                    : null;
        } catch (Exception e) {
            logger.warn("Failed to index preference '{}' for portlet with fname='{}'",
                preference.getName(), portlet.getFName(), e);
        }

        return null; // Indexing failed

    }

}
