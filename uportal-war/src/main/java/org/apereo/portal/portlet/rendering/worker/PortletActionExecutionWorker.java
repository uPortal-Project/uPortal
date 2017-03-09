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
package org.apereo.portal.portlet.rendering.worker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.rendering.IPortletRenderer;

class PortletActionExecutionWorker extends PortletExecutionWorker<Long>
    implements IPortletActionExecutionWorker {
  public PortletActionExecutionWorker(
      ExecutorService executorService,
      List<IPortletExecutionInterceptor> interceptors,
      IPortletRenderer portletRenderer,
      HttpServletRequest request,
      HttpServletResponse response,
      IPortletWindow portletWindow) {

    super(
        executorService,
        interceptors,
        portletRenderer,
        request,
        response,
        portletWindow,
        portletWindow.getPortletEntity().getPortletDefinition().getActionTimeout() != null
            ? portletWindow.getPortletEntity().getPortletDefinition().getActionTimeout()
            : portletWindow.getPortletEntity().getPortletDefinition().getTimeout());
  }

  @Override
  public ExecutionType getExecutionType() {
    return ExecutionType.ACTION;
  }

  @Override
  protected Long callInternal() throws Exception {
    return portletRenderer.doAction(portletWindowId, request, response);
  }
}
