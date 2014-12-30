/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlet.rendering.worker;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.portlet.rendering.PortletRenderResult;
import org.jasig.portal.portlet.rendering.RenderPortletOutputHandler;

/**
 * {@link PortletExecutionWorker} capable of rendering the head content
 * for a portlet.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
class PortletRenderHeaderExecutionWorker extends
		PortletExecutionWorker<PortletRenderResult> implements
		IPortletRenderExecutionWorker {

	private String output = null;
    
    public PortletRenderHeaderExecutionWorker(
            ExecutorService executorService, List<IPortletExecutionInterceptor> interceptors, IPortletRenderer portletRenderer, 
            HttpServletRequest request, HttpServletResponse response, IPortletWindow portletWindow) {
        
        super(executorService, interceptors, portletRenderer, request, response, portletWindow, 
                portletWindow.getPortletEntity().getPortletDefinition().getRenderTimeout() != null
                ? portletWindow.getPortletEntity().getPortletDefinition().getRenderTimeout()
                : portletWindow.getPortletEntity().getPortletDefinition().getTimeout());
    }

    @Override
    public ExecutionType getExecutionType() {
        return ExecutionType.RENDER_HEADER;
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.rendering.worker.IPortletRenderExecutionWorker#getOutput(long)
	 */
	@Override
	public String getOutput(long timeout) throws Exception {
		this.get(timeout);
        return this.output;
	}

    /**
     * Obtain the RENDER_HEADER output.  Note that uPortal supports the model of obtaining HTML markup from the
     * portlet, whether from a Spring view or servlet output and inserting it within the HTML HEAD section of
     * the page. uPortal does not support the model where the portlet creates DOM elements to be written to the
     * HEAD section and constructs the HTML output; e.g. the following does NOT work in uPortal:
     *
     * <p><pre>
     * Element linkElement = response.createElement(HTML.Tag.SCRIPT.toString());
     * linkElement.setAttribute(HTML.Attribute.TYPE.toString(), "text/javascript");
     * linkElement.setAttribute(HTML.Attribute.SRC.toString(), cssUrl);
     * linkElement.setTextContent(" ");
     * response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, linkElement);
     * </pre></p>
     *
	 * @see org.jasig.portal.portlet.rendering.worker.PortletExecutionWorker#callInternal()
	 */
	@Override
	protected PortletRenderResult callInternal() throws Exception {
	    final String characterEncoding = response.getCharacterEncoding();
	    final RenderPortletOutputHandler renderPortletOutputHandler = new RenderPortletOutputHandler(characterEncoding);
	    
        final PortletRenderResult result = portletRenderer.doRenderHeader(portletWindowId, request, response, renderPortletOutputHandler);
        
        this.output = renderPortletOutputHandler.getOutput();
        
        return result;
	}

}
