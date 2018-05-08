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
package org.apereo.portal.layout;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.om.IPortletPreference;

public class LayoutPortlet {
    private static final String CONTENT_PORTLET_PREFERENCE = "content";
    private static final String PITHY_CONTENT_PORTLET_PREFERENCE = "pithyContent";
    private static final String WIDGET_URL_PORTLET_PREFERENCE = "widgetURL";
    private static final String WIDGET_TYPE_PORTLET_PREFERENCE = "widgetType";
    private static final String WIDGET_CONFIG_PORTLET_PREFERENCE = "widgetConfig";
    private static final String WIDGET_TEMPLATE_PORTLET_PREFERENCE = "widgetTemplate";
    private static final String RENDER_ON_WEB_PORTLET_PREFERENCE = "renderOnWeb";
    private static final String STATIC_CONTENT_PORTLET_WEBAPP_NAME = "/SimpleContentPortlet";

    private String nodeId;
    private String title;
    private String description;
    private String url;
    private String iconUrl;
    private String faIcon;
    private String fname;
    private String target;
    private String widgetURL;
    private String widgetType;
    private String widgetTemplate;
    @JsonRawValue private Object widgetConfig;

    private boolean isAltMaxUrl = false;
    private boolean isRenderOnWeb;

    /** Fuller static content that you might display in a lightbox or so. */
    private String staticContent;

    /** Pithy static content that you might display on a dashboard mosaic view or so. */
    private String pithyStaticContent;

    public LayoutPortlet() {}

    public LayoutPortlet(IPortletDefinition portletDef) {
        if (portletDef != null) {

            nodeId = "-1";
            title = portletDef.getTitle();
            description = portletDef.getDescription();
            fname = portletDef.getFName();
            url = portletDef.getAlternativeMaximizedLink();
            target = portletDef.getTarget();
            isAltMaxUrl = StringUtils.isNotBlank(url);
            IPortletDefinitionParameter iconParam = portletDef.getParameter("iconUrl");
            if (iconParam != null) {
                this.setIconUrl(iconParam.getValue());
            }

            IPortletDefinitionParameter faIconParam = portletDef.getParameter("faIcon");
            if (faIconParam != null) {
                this.setFaIcon(faIconParam.getValue());
            }

            // flags tracking which portlet preferences have already been parsed are an attempt
            // to make more efficient the harvesting of portlet
            // preferences into JavaBean properties, since portlet preferences are available here
            // only as a list for iteration and not as a map for efficient lookup. This single-loop
            // solution traverses the list one time handling each
            // preference as it is encountered rather than looping through the preferences for each
            // preference sought. The flags potentially
            // further expedite this process by short-circuiting once all the relevant portlet
            // preferences have been parsed.

            boolean staticContentParsed = false;
            boolean pithyStaticContentParsed = false;
            boolean widgetUrlParsed = false;
            boolean widgetTypeParsed = false;
            boolean widgetConfigParsed = false;
            boolean widgetTemplateParsed = false;
            boolean renderOnWebParsed = false;

            // flag 0: true if staticContent JavaBean property setting is fulfilled
            // either by the portlet not being a static content portlet so there's nothing to do
            // or by the portlet being a static content portlet and the content portlet preference
            // having been copied into the JavaBean property
            staticContentParsed =
                    !(portletDef.getPortletDescriptorKey() != null
                            && STATIC_CONTENT_PORTLET_WEBAPP_NAME.equals(
                                    portletDef.getPortletDescriptorKey().getWebAppName()));
            for (IPortletPreference pref : portletDef.getPortletPreferences()) {
                if (!staticContentParsed
                        && CONTENT_PORTLET_PREFERENCE.equals(pref.getName())
                        && pref.getValues().length == 1) {
                    this.setStaticContent(pref.getValues()[0]);
                    staticContentParsed = true;
                } else if (!pithyStaticContentParsed
                        && PITHY_CONTENT_PORTLET_PREFERENCE.equals(pref.getName())
                        && 1 == pref.getValues().length) {
                    this.setPithyStaticContent(pref.getValues()[0]);
                    pithyStaticContentParsed = true;
                } else if (!widgetUrlParsed
                        && WIDGET_URL_PORTLET_PREFERENCE.equals(pref.getName())) {
                    this.setWidgetURL(pref.getValues()[0]);
                    widgetUrlParsed = true;
                } else if (!widgetTypeParsed
                        && WIDGET_TYPE_PORTLET_PREFERENCE.equals(pref.getName())) {
                    this.setWidgetType(pref.getValues()[0]);
                    widgetTypeParsed = true;
                } else if (!widgetConfigParsed
                        && WIDGET_CONFIG_PORTLET_PREFERENCE.equals(pref.getName())) {
                    if (isValidJSON(pref.getValues()[0])) {
                        this.setWidgetConfig(pref.getValues()[0]);
                    } else {
                        this.setWidgetConfig(
                                "{\"error\" : \"config JSON not valid, syntax error? Double quotes not escaped?\"}");
                    }
                    widgetConfigParsed = true;
                } else if (!widgetTemplateParsed
                        && WIDGET_TEMPLATE_PORTLET_PREFERENCE.equals(pref.getName())) {
                    this.setWidgetTemplate(pref.getValues()[0]);
                    widgetTemplateParsed = true;
                } else if (!renderOnWebParsed
                        && RENDER_ON_WEB_PORTLET_PREFERENCE.equals(pref.getName())) {
                    renderOnWebParsed = true;
                    this.setRenderOnWeb(Boolean.valueOf(pref.getValues()[0]));
                }

                if (staticContentParsed && pithyStaticContentParsed && widgetUrlParsed
                    && widgetTypeParsed && widgetConfigParsed && widgetTemplateParsed
                    && renderOnWebParsed) {
                    // if all the Portlet Preferences that might be harvested into JavaBean
                    // properties have been harvested, then there's nothing more to harvest so stop
                    // iterating through portlet preferences. However, since in particular
                    // pithyStaticContent is not widely adopted and many widgets will use a
                    // type rather than a custom template, this shortcut will almost never
                    // obtain
                    break;
                }
            }
        }
    }

    private boolean isValidJSON(final String json) {
        boolean valid = false;
        try {
            final JsonParser parser = new ObjectMapper().getFactory().createParser(json);
            while (parser.nextToken() != null) {}
            valid = true;
        } catch (Exception jpe) {
            // eat error
            valid = false;
        }

        return valid;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getFaIcon() {
        return faIcon;
    }

    public void setFaIcon(String faIcon) {
        this.faIcon = faIcon;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getStaticContent() {
        return staticContent;
    }

    public void setStaticContent(String staticContent) {
        this.staticContent = staticContent;
    }

    public String getPithyStaticContent() {
        return this.pithyStaticContent;
    }

    public void setPithyStaticContent(final String pithyStaticContent) {
        this.pithyStaticContent = pithyStaticContent;
    }

    public boolean isAltMaxUrl() {
        return isAltMaxUrl;
    }

    public void setAltMaxUrl(boolean isAltMaxUrl) {
        this.isAltMaxUrl = isAltMaxUrl;
    }

    public String getWidgetURL() {
        return widgetURL;
    }

    public void setWidgetURL(String widgetURL) {
        this.widgetURL = widgetURL;
    }

    public String getWidgetType() {
        return widgetType;
    }

    public void setWidgetType(String widgetType) {
        this.widgetType = widgetType;
    }

    public String getWidgetConfig() {
        return (String) widgetConfig;
    }

    public void setWidgetConfig(String widgetConfig) {
        this.widgetConfig = widgetConfig;
    }

    public String getWidgetTemplate() {
        return widgetTemplate;
    }

    public void setWidgetTemplate(String widgetTemplate) {
        this.widgetTemplate = widgetTemplate;
    }

    public boolean isRenderOnWeb() {
        return isRenderOnWeb;
    }

    public void setRenderOnWeb(boolean setter) {
        this.isRenderOnWeb = setter;
    }
}
