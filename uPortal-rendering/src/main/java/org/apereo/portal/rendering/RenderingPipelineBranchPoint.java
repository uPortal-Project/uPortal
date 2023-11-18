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
package org.apereo.portal.rendering;

import java.io.IOException;
import java.util.function.Predicate;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Offers an alternative to the "standard" rendering pipeline if certain conditions (encapsulated in
 * a <code>Predicate</code>) are satisfied. If the specified <code>Predicate</code> returns <code>
 * true</code>, the alternate path will be followed.
 *
 * <p>This class is an extension point for uPortal adopters. Beans of this type are automatically
 * detected and processed by the uPortal rendering infrastructure. Multiple <code>
 * RenderingPipelineBranchPoint</code> beans will be ordered based on their <code>order</code>
 * properties. Beans with a lower order score are processed before beans with a higher order.
 *
 * @since 5.0
 */
public class RenderingPipelineBranchPoint implements Comparable<RenderingPipelineBranchPoint> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private int order = Integer.MAX_VALUE; // default -- last position

    // dependency-injected
    private Predicate<HttpServletRequest> predicate;

    // dependency-injected
    private IPortalRenderingPipeline alternatePipe;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Required
    public void setPredicate(Predicate<HttpServletRequest> predicate) {
        this.predicate = predicate;
    }

    @Required
    public void setAlternatePipe(IPortalRenderingPipeline alternatePipe) {
        this.alternatePipe = alternatePipe;
    }

    /**
     * Checks this bean's <code>Predicate</code> to see if the alternate rendering path should be
     * followed. If so, it will follow the path and return <code>true</code>; if not, it will
     * immediately return <code>false</code>;
     *
     * @param request The current <code>HttpServletRequest</code>
     * @param response The current <code>HttpServletResponse</code>
     * @return <code>true</code> if the alternate path was followed, otherwise <code>false</code>
     *     indicating that the next {@link RenderingPipelineBranchPoint} should be attempted or the
     *     standard pipeline should be used
     */
    public boolean renderStateIfApplicable(
            final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final boolean result = predicate.test(request);
        if (result) {
            logger.debug(
                    "Using alternate pipe [{}] for branch point with order={}",
                    alternatePipe,
                    order);
            alternatePipe.renderState(request, response);
        } else {
            logger.debug(
                    "Bypassing alternate pipe [{}] for branch point with order={}",
                    alternatePipe,
                    order);
        }
        return result;
    }

    @Override
    public String toString() {
        return "RenderingPipelineBranchPoint with predicate ["
                + predicate
                + "] proceeds down pipe ["
                + alternatePipe
                + "] when the predicate is true.";
    }

    @Override
    public int compareTo(RenderingPipelineBranchPoint branchPoint) {
        return Integer.compare(order, branchPoint.order);
    }
}
