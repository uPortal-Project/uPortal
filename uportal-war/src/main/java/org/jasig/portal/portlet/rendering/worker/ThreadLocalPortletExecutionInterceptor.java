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

import org.springframework.beans.factory.BeanNameAware;

/**
 * Base class for copying data from a {@link ThreadLocal} in the calling thread into the worker thread. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class ThreadLocalPortletExecutionInterceptor<T> extends PortletExecutionInterceptorAdaptor implements BeanNameAware {
    private static final String CURRENT_THREAD = ThreadLocalPortletExecutionInterceptor.class.getName() + ".CURRENT_THREAD";
    
    private String contextAttributeName = this.getClass().getName();
    
    /**
     * Get the current value from the ThreadLocal store
     */
    protected abstract T getThreadLocalValue(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context);
    
    /**
     * Set the specified value into the ThreadLocal store. If the value is null the store may remove the ThreadLocal
     * reference
     */
    protected abstract void setThreadLocalValue(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context, T value);
    
    public void setContextAttributeName(String contextAttributeName) {
        this.contextAttributeName = contextAttributeName;
    }
    
    @Override
    public void setBeanName(String name) {
        if (this.contextAttributeName != null) {
            this.contextAttributeName = name;
        }
    }

    @Override
    public final void preSubmit(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context) {
        context.setExecutionAttribute(CURRENT_THREAD, Thread.currentThread());
        
        final T preSubmitData = this.getThreadLocalValue(request, response, context);
        context.setExecutionAttribute(this.contextAttributeName, preSubmitData);
    }

    @Override
    public final void preExecution(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context) {
        final Thread currentThread = (Thread)context.getExecutionAttribute(CURRENT_THREAD);
        
        if (Thread.currentThread() != currentThread) {
            final T preExecuteData = this.getThreadLocalValue(request, response, context);
            final T preSubmitData = (T)context.getExecutionAttribute(this.contextAttributeName);
            this.setThreadLocalValue(request, response, context, preSubmitData);
            context.setExecutionAttribute(this.contextAttributeName, preExecuteData);
        }
    }

    @Override
    public final void postExecution(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context, Exception e) {
        final Thread currentThread = (Thread)context.getExecutionAttribute(CURRENT_THREAD);
        
        if (Thread.currentThread() != currentThread) {
            final T preExecuteData = (T)context.getExecutionAttribute(this.contextAttributeName);
            this.setThreadLocalValue(request, response, context, preExecuteData);
        }
    }
}
