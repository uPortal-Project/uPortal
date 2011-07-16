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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link IPortletExecutionInterceptor} which does nothing.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletExecutionInterceptorAdaptor implements IPortletExecutionInterceptor {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionInterceptor#preSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.jasig.portal.portlet.rendering.worker.IPortletExecutionContext)
     */
    @Override
    public void preSubmit(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context) {
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionInterceptor#preExecution(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.jasig.portal.portlet.rendering.worker.IPortletExecutionContext)
     */
    @Override
    public void preExecution(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context) {
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionInterceptor#postExecution(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.jasig.portal.portlet.rendering.worker.IPortletExecutionContext, java.lang.Exception)
     */
    @Override
    public void postExecution(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context, Exception e) {
    }
}
