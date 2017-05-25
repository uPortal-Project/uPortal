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
package org.springframework.web.portlet;

import java.util.Map;
import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewRendererServlet;

public class ForwardingDispatcherPortlet extends DispatcherPortlet {
    /** URL that points to the ViewRendererServlet */
    private String viewRendererUrl = DispatcherPortlet.DEFAULT_VIEW_RENDERER_URL;

    public void setViewRendererUrl(String viewRendererUrl) {
        this.viewRendererUrl = viewRendererUrl;
        super.setViewRendererUrl(viewRendererUrl);
    }

    protected void doRender(View view, Map model, PortletRequest request, MimeResponse response)
            throws Exception {
        // Expose Portlet ApplicationContext to view objects.
        request.setAttribute(
                ViewRendererServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                getPortletApplicationContext());

        // These attributes are required by the ViewRendererServlet.
        request.setAttribute(ViewRendererServlet.VIEW_ATTRIBUTE, view);
        request.setAttribute(ViewRendererServlet.MODEL_ATTRIBUTE, model);

        // Forward to the view in the resource response.
        if (PortletRequest.RESOURCE_PHASE.equals(
                request.getAttribute(PortletRequest.LIFECYCLE_PHASE))) {
            getPortletContext()
                    .getRequestDispatcher(this.viewRendererUrl)
                    .forward(request, response);
        } else {
            getPortletContext()
                    .getRequestDispatcher(this.viewRendererUrl)
                    .include(request, response);
        }
    }
}
