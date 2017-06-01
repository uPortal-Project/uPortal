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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Adds the targeted portlet's fname to the current thread name.
 *
 */
@Service("ThreadNamingPortletExecutionInterceptorAdaptor")
public class ThreadNamingPortletExecutionInterceptorAdaptor
        extends PortletExecutionInterceptorAdaptor {
    private static final String THREAD_NAME =
            ThreadNamingPortletExecutionInterceptorAdaptor.class.getName() + ".THREAD_NAME";

    private IPortletWindowRegistry portletWindowRegistry;
    private IPersonManager personManager;

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Override
    public void preExecution(
            HttpServletRequest request,
            HttpServletResponse response,
            IPortletExecutionContext context) {
        final IPortletWindowId portletWindowId = context.getPortletWindowId();
        final String fname = this.getFname(request, portletWindowId);
        final String userName = this.getUserName(request);

        final Thread currentThread = Thread.currentThread();
        final String threadName = currentThread.getName();
        context.setExecutionAttribute(THREAD_NAME, threadName);

        final IPortletExecutionContext.ExecutionType executionType = context.getExecutionType();
        currentThread.setName(
                threadName + "-" + userName + "-" + executionType + "-[" + fname + "]");
    }

    @Override
    public void postExecution(
            HttpServletRequest request,
            HttpServletResponse response,
            IPortletExecutionContext context,
            Exception e) {
        final String threadName = (String) context.getExecutionAttribute(THREAD_NAME);
        final Thread currentThread = Thread.currentThread();
        currentThread.setName(threadName);
    }

    protected String getFname(HttpServletRequest request, IPortletWindowId portletWindowId) {
        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        if (portletWindow == null) {
            logger.warn(
                    "Failed to resolve IPortletWindow for id: "
                            + portletWindowId
                            + ", the id will be used instead of the fname");
            return portletWindowId.toString();
        }

        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        return portletDefinition.getFName();
    }

    protected String getUserName(HttpServletRequest request) {
        final IPerson person = this.personManager.getPerson(request);
        if (person == null) {
            return null;
        }

        return person.getUserName();
    }
}
