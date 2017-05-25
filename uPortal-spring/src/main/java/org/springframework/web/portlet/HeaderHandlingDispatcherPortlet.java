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

import java.io.IOException;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * This Dispatcher extends the capability of <code>org.springframework.web.portlet.DispatcherPortlet
 * </code> to add ability to process the <code>RENDER_HEADERS</code> portion of the two-phase render
 * process.
 *
 */
public class HeaderHandlingDispatcherPortlet extends DispatcherPortlet {

    /**
     * Used by the render method to set the response properties and headers.
     *
     * <p>The portlet should override this method and set its response header using this method in
     * order to ensure that they are set before anything is written to the output stream.
     *
     * <p>
     *
     * @param request the render request
     * @param response the render response
     */
    @Override
    protected void doHeaders(RenderRequest request, RenderResponse response) {
        try {
            doDispatch(request, response);
        } catch (IOException | PortletException ex) {
            logger.error(
                    "Exception rendering headers for portlet "
                            + getPortletName()
                            + ". Aborting doHeaders",
                    ex);
        }
    }

    /**
     * Processes the actual dispatching to the handler for render requests.
     *
     * <p>The handler will be obtained by applying the portlet's HandlerMappings in order. The
     * HandlerAdapter will be obtained by querying the portlet's installed HandlerAdapters to find
     * the first that supports the handler class.
     *
     * <p>For two-phase render processing
     *
     * <ol>
     *   <li>the interceptors and exception handlers should handle the two phases appropriately (if
     *       not idempotent skip processing during RENDER_HEADERS phase).
     *   <li>Though a streaming portlet likely will invoke RENDER_HEADERS before RENDER_MARKUP,
     *       there is no guarantee of order of execution; e.g. render_markup may be called before
     *       render_header. The only guarantee is that the resulting markup is inserted in the
     *       appropriate order.
     * </ol>
     *
     * <p>For single-phase render, this method executes following normal conventions.
     *
     * <p>For two-phase render, this method will delegate <code>RENDER_MARKUP</code> of a two-phase
     * render processing to the parent class for normal processing. Ideally for the <code>
     * RENDER_HEADERS</code> phase it would invoke the portlet if there were no exceptions from the
     * Action Phase (leave ACTION_EXCEPTION_SESSION_ATTRIBUTE in session and leave rethrowing the
     * exception for the RENDER_MARKUP phase) and probably render no output if there was an
     * exception. However triggerAfterRenderCompletion in the superclass is a private method so it
     * cannot be executed in a subclass. For now we can only have RENDER_HEADERS perform the normal
     * processing until Spring Framework Portlet MVC code incorporates this behavior.
     *
     * @param request current portlet render request
     * @param response current portlet render response
     * @throws Exception in case of any kind of processing failure
     */
    @Override
    protected void doRenderService(RenderRequest request, RenderResponse response)
            throws Exception {
        super.doRenderService(request, response);
    }
}
