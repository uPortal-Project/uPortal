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

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Copies {@link LocaleContextHolder} data from the portal request thread into the portlet execution
 * thread and cleans up afterwards.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("RequestContextThreadLocalPortletExecutionInterceptor")
public class RequestContextThreadLocalPortletExecutionInterceptor extends ThreadLocalPortletExecutionInterceptor<RequestAttributes> {

    @Override
    protected RequestAttributes getThreadLocalValue(HttpServletRequest request, HttpServletResponse response,
            IPortletExecutionContext context) {
        return RequestContextHolder.getRequestAttributes();
    }

    @Override
    protected void setThreadLocalValue(HttpServletRequest request, HttpServletResponse response,
            IPortletExecutionContext context, RequestAttributes value) {
        if (value == null) {
            RequestContextHolder.resetRequestAttributes();
        }
        else {
            RequestContextHolder.setRequestAttributes(value);
        }
    }
}
