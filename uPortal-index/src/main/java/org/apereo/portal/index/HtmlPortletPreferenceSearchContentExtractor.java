/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.index;

import java.util.stream.Stream;
import org.apache.commons.lang.StringEscapeUtils;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlPortletPreferenceSearchContentExtractor implements ISearchContentExtractor {

    private final String portletName;
    private final String webappName;
    private final String preferenceName;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /* package-private */ HtmlPortletPreferenceSearchContentExtractor(
            String portletName, String webappName, String preferenceName) {
        this.portletName = portletName;
        this.webappName = webappName;
        this.preferenceName = preferenceName;
    }

    @Override
    public boolean appliesTo(IPortletDefinition portlet) {
        final boolean portletMatch =
                portletName.equalsIgnoreCase(portlet.getPortletDescriptorKey().getPortletName());
        final boolean webappMatch =
                webappName.equalsIgnoreCase(portlet.getPortletDescriptorKey().getWebAppName());
        final boolean preferenceMatch =
                portlet.getPortletPreferences().stream()
                        .anyMatch(
                                preference ->
                                        preference.getName().equalsIgnoreCase(preferenceName));

        return portletMatch && webappMatch && preferenceMatch;
    }

    @Override
    public String extractContent(IPortletDefinition portlet) {

        // It's not ideal that we have to iterate the list to find the preference we want
        final IPortletPreference preference =
                portlet.getPortletPreferences().stream()
                        .filter(item -> item.getName().equalsIgnoreCase(preferenceName))
                        .findFirst()
                        .orElse(null);

        if (preference == null) {
            // Nothing we can index...
            return null;
        }

        final StringBuilder stringBuilder = new StringBuilder();
        Stream.of(preference.getValues())
                .forEach(
                        item ->
                                stringBuilder
                                        .append(StringEscapeUtils.unescapeHtml(item))
                                        .append(" "));

        // There must be a single root element
        stringBuilder.insert(0, "<html><body>").append("</body></html>");

        final String html = stringBuilder.toString();

        try {

            // Use JSoup to parse the HTML b/c it's often pretty sketchy
            final Document doc = Jsoup.parse(html);
            final String body = doc.body().text().trim();
            return body.length() > 0 ? body : null;
        } catch (Exception e) {
            logger.warn(
                    "Failed to index preference '{}' for portlet with fname='{}'",
                    preference.getName(),
                    portlet.getFName(),
                    e);
        }

        return null; // Indexing failed
    }
}
