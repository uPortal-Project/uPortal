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
package org.apereo.portal.rendering.predicates;

import com.google.common.base.Predicate;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.url.IPortalRequestInfo;
import org.apereo.portal.url.IUrlSyntaxProvider;
import org.apereo.portal.url.UrlState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Answers whether a given HttpServletRequest represents one that the rendering pipeline will focus
 * on rendering just one portlet (e.g., maximized, or exclusive).
 *
 * @since 4.2
 */
public class FocusedOnOnePortletPredicate implements Predicate<HttpServletRequest> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // auto-wired.
    private IUrlSyntaxProvider urlSyntaxProvider;

    @Override
    public boolean apply(final HttpServletRequest request) {

        final IPortalRequestInfo portalRequestInfo =
                this.urlSyntaxProvider.getPortalRequestInfo(request);

        if (null == portalRequestInfo) {
            logger.warn(
                    "Portal request info was not available for this request, "
                            + "so assuming that it does not represent focus on one portlet.");
            // False when portalRequestInfo is null because unknown portal state is not
            // focused-on-one-portlet portal state.
            return false;
        }

        final UrlState urlState = portalRequestInfo.getUrlState();

        // true when there is a portal request and that request is not for a portal in NORMAL state
        // i.e. portal is in some other state, like Maximized or Exclusive or Detached.
        // false otherwise.
        final boolean doesRequestFocusOnOnePortlet = (!(UrlState.NORMAL.equals(urlState)));

        logger.debug(
                "Determined request with UrlState {} {} focus on one portlet.",
                urlState,
                doesRequestFocusOnOnePortlet ? "does" : "does not");

        return doesRequestFocusOnOnePortlet;
    }

    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }
}
