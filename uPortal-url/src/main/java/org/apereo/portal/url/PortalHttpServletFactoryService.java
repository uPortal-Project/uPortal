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
package org.apereo.portal.url;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apereo.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This <code>Service</code> bean is responsible for creating and properly initializing instances of
 * {@link PortalHttpServletRequestWrapper} and {@link PortalHttpServletResponseWrapper}.
 *
 * @since 5.4
 */
@Service
public class PortalHttpServletFactoryService {

    @Autowired private IUrlSyntaxProvider urlSyntaxProvider;

    @Autowired private IUserInstanceManager userInstanceManager;

    public RequestAndResponseWrapper createRequestAndResponseWrapper(
            HttpServletRequest request, HttpServletResponse response) {

        final IPortalRequestInfo portalRequestInfo =
                urlSyntaxProvider.getPortalRequestInfo(request);

        final UrlType urlType = portalRequestInfo.getUrlType();
        final UrlState urlState = portalRequestInfo.getUrlState();

        final PortalHttpServletResponseWrapper responseWrapper =
                new PortalHttpServletResponseWrapper(response);
        final PortalHttpServletRequestWrapper requestWrapper =
                new PortalHttpServletRequestWrapper(request, responseWrapper, userInstanceManager);

        requestWrapper.setHeader(IPortalRequestInfo.URL_TYPE_HEADER, urlType.toString());
        requestWrapper.setHeader(IPortalRequestInfo.URL_STATE_HEADER, urlState.toString());

        // Hack to make PortalController work in light of
        // https://jira.springsource.org/secure/attachment/18283/SPR7346.patch
        requestWrapper.setHeader(
                IPortalRequestInfo.URL_TYPE_HEADER + "." + urlType, Boolean.TRUE.toString());
        requestWrapper.setHeader(
                IPortalRequestInfo.URL_STATE_HEADER + "." + urlState, Boolean.TRUE.toString());

        return new RequestAndResponseWrapper(requestWrapper, responseWrapper);
    }

    /*
     * Nested Types
     */

    public static final class RequestAndResponseWrapper {

        final PortalHttpServletRequestWrapper requestWrapper;
        final PortalHttpServletResponseWrapper responseWrapper;

        /* package-private */ RequestAndResponseWrapper(
                PortalHttpServletRequestWrapper requestWrapper,
                PortalHttpServletResponseWrapper responseWrapper) {
            this.requestWrapper = requestWrapper;
            this.responseWrapper = responseWrapper;
        }

        public PortalHttpServletRequestWrapper getRequest() {
            return requestWrapper;
        }

        public PortalHttpServletResponseWrapper getResponse() {
            return responseWrapper;
        }
    }
}
