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
package org.apereo.portal.portlets.iframe;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apereo.portal.portlet.rendering.IPortletRenderer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

/**
 * This portlet renders content identified by a URL within an inline browser frame. See <a
 * href="http://www.htmlhelp.com/reference/html40/special/iframe.html">
 * http://www.htmlhelp.com/reference/html40/special/iframe.html</a> for more information on inline
 * frames.
 */
@Controller
@RequestMapping("VIEW")
public final class IFramePortletController {

    @SuppressWarnings("serial")
    protected static final Map<String, String> IFRAME_ATTRS =
            Collections.unmodifiableMap(
                    new LinkedHashMap<String, String>() {
                        {
                            /** document-wide unique id */
                            put("id", null);

                            /** space-separated list of classes */
                            put("cssClass", null);

                            /** associated style info */
                            put("style", null);

                            /** advisory title */
                            put("title", null);

                            /** link to long description (complements title) */
                            put("longDescription", null);

                            /** name of frame for targeting */
                            put("name", null);

                            /** source of frame content */
                            put("src", null);

                            /** request frame borders? */
                            put("frameBorder", "0");

                            /** margin widths in pixels */
                            put("marginWidth", null);

                            /** margin height in pixels */
                            put("marginHeight", null);

                            /** scrollbar or none */
                            put("scrolling", null);

                            /** vertical or horizontal alignment */
                            put("align", null);

                            /** frame height */
                            put("width", "100%");

                            /** frame width */
                            put("height", null);
                        }
                    });

    private static final String SCROLL_HEIGHT_ONLOAD_FIX =
            "this.style.height = parseInt(document.body.scrollHeight, 10) * 3 + 'px';";

    @RenderMapping
    protected ModelAndView display(RenderRequest req, RenderResponse res) throws Exception {

        Map<String, Object> model = new HashMap<String, Object>();

        // get the IFrame target URL and the configured height of the IFrame
        // window from the portlet preferences
        PortletPreferences prefs = req.getPreferences();

        for (final Map.Entry<String, String> attrEntry : IFRAME_ATTRS.entrySet()) {
            final String attr = attrEntry.getKey();
            final String defaultValue = attrEntry.getValue();
            model.put(attr, prefs.getValue(attr, defaultValue));
        }

        // Legacy support for url attribute
        if (model.get("src") == null) {
            model.put("src", prefs.getValue("url", IFRAME_ATTRS.get("src")));
        }

        // Fix for double scrollbars in specialized window states that control the whole window
        if (req.getWindowState().equals(IPortletRenderer.DETACHED)
                || req.getWindowState().equals(IPortletRenderer.EXCLUSIVE)) {
            model.put("onload", SCROLL_HEIGHT_ONLOAD_FIX);
        }

        return new ModelAndView("/jsp/IFrame/iframePortlet", "attrs", model);
    }
}
