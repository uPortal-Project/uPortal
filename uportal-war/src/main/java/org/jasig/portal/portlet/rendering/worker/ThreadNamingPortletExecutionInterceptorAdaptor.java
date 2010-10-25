/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlet.rendering.worker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Adds the targeted portlet's fname to the current thread name.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class ThreadNamingPortletExecutionInterceptorAdaptor extends PortletExecutionInterceptorAdaptor {
    private static final String THREAD_NAME = ThreadNamingPortletExecutionInterceptorAdaptor.class.getName() + ".THREAD_NAME";
    
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }
    
    @Override
    public void preExecution(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context) {
        final IPortletWindowId portletWindowId = context.getPortletWindowId();
        final String fname = this.getFname(request, portletWindowId);
        
        final Thread currentThread = Thread.currentThread();
        final String threadName = currentThread.getName();
        context.setExecutionAttribute(THREAD_NAME, threadName);
        
        currentThread.setName(threadName + "-[" + fname + "]");
    }
    
    @Override
    public void postExecution(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context, Exception e) {
        final String threadName = (String)context.getExecutionAttribute(THREAD_NAME);
        final Thread currentThread = Thread.currentThread();
        currentThread.setName(threadName);
    }

    protected String getFname(HttpServletRequest request, IPortletWindowId portletWindowId) {
        final IPortletEntity parentPortletEntity = portletWindowRegistry.getParentPortletEntity(request, portletWindowId);
        final IPortletDefinition parentPortletDefinition = portletEntityRegistry.getParentPortletDefinition(parentPortletEntity.getPortletEntityId());
        final IChannelDefinition channelDefinition = parentPortletDefinition.getChannelDefinition();
        return channelDefinition.getFName();
      }
}
