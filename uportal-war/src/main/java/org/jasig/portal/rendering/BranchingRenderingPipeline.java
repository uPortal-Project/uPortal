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
package org.jasig.portal.rendering;

import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A Rendering Pipeline that proceeds down one of two branches depending upon a Predicate.
 *
 * If the Predicate returns true, proceed down the true branch.
 * If the Predicate returns false, proceed down the false branch.
 *
 * @since uPortal 4.2
 */
public class BranchingRenderingPipeline
    implements IPortalRenderingPipeline {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // dependency-injected
    private Predicate<HttpServletRequest> predicate;

    // dependency-injected
    private IPortalRenderingPipeline truePipe;

    // dependency-injected
    private IPortalRenderingPipeline falsePipe;


    @Override
    public void renderState(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        // render either the true or false pipe, depending on the trueness or falseness of the predicate

        if (predicate.apply(request)) {
            logger.trace("Branching to the true pipe [{}].", truePipe);
            truePipe.renderState(request, response);
        } else {
            logger.trace("Branching to the false pipe [{}].", falsePipe);
            falsePipe.renderState(request, response);
        }

    }

    @Required
    public void setPredicate(Predicate<HttpServletRequest> predicate) {
        this.predicate = predicate;
    }

    @Required
    public void setTruePipe(IPortalRenderingPipeline truePipe) {
        this.truePipe = truePipe;
    }

    @Required
    public void setFalsePipe(IPortalRenderingPipeline falsePipe) {
        this.falsePipe = falsePipe;
    }

    @Override
    public String toString() {
        return "BranchingRenderingPipeline which considering predicate [" + this.predicate +
                "] proceeds down pipe [" + this.truePipe + "] when the predicate is true and proceeds down pipe [" +
                this.falsePipe + "] when the predicate is false.";
    }
}
