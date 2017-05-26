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
    private static final String WIDGET_URL_PORLTET_PREFERENCE = "widgetURL";
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
            url = portletDef.getAlternativeMaximizedLink(); //todo get normal URL if alt is missing
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

            boolean[] efficencyFlag = {false, false, false, false, false, false, false};
            efficencyFlag[0] =
                    !(portletDef.getPortletDescriptorKey() != null
                            && STATIC_CONTENT_PORTLET_WEBAPP_NAME.equals(
                                    portletDef.getPortletDescriptorKey().getWebAppName()));
            for (IPortletPreference pref : portletDef.getPortletPreferences()) {
                if (!efficencyFlag[0]
                        && CONTENT_PORTLET_PREFERENCE.equals(pref.getName())
                        && pref.getValues().length == 1) {
                    this.setStaticContent(pref.getValues()[0]);
                    efficencyFlag[0] = true;
                } else if (!efficencyFlag[1]
                        && PITHY_CONTENT_PORTLET_PREFERENCE.equals(pref.getName())
                        && 1 == pref.getValues().length) {
                    this.setPithyStaticContent(pref.getValues()[0]);
                    efficencyFlag[1] = true;
                } else if (!efficencyFlag[2]
                        && WIDGET_URL_PORLTET_PREFERENCE.equals(pref.getName())) {
                    this.setWidgetURL(pref.getValues()[0]);
                    efficencyFlag[2] = true;
                } else if (!efficencyFlag[3]
                        && WIDGET_TYPE_PORTLET_PREFERENCE.equals(pref.getName())) {
                    this.setWidgetType(pref.getValues()[0]);
                    efficencyFlag[3] = true;
                } else if (!efficencyFlag[4]
                        && WIDGET_CONFIG_PORTLET_PREFERENCE.equals(pref.getName())) {
                    if (isValidJSON(pref.getValues()[0])) {
                        this.setWidgetConfig(pref.getValues()[0]);
                    } else {
                        this.setWidgetConfig(
                                "{\"error\" : \"config JSON not valid, syntax error? Double quotes not escaped?\"}");
                    }
                    efficencyFlag[4] = true;
                } else if (!efficencyFlag[5]
                        && WIDGET_TEMPLATE_PORTLET_PREFERENCE.equals(pref.getName())) {
                    this.setWidgetTemplate(pref.getValues()[0]);
                    efficencyFlag[5] = true;
                } else if (!efficencyFlag[6]
                        && RENDER_ON_WEB_PORTLET_PREFERENCE.equals(pref.getName())) {
                    efficencyFlag[6] = true;
                    this.setRenderOnWeb(Boolean.valueOf(pref.getValues()[0]));
                }

                if (allTrue(efficencyFlag)) {
                    break;
                }
            }
        }
    }

    private boolean allTrue(boolean[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == false) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidJSON(final String json) {
        boolean valid = false;
        try {
            final JsonParser parser = new ObjectMapper().getFactory().createParser(json);
            while (parser.nextToken() != null) {}
            valid = true;
        } catch (Exception jpe) {
            //eat error
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
