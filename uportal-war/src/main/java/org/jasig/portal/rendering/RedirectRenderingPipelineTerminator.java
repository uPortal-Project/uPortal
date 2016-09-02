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

import org.apache.commons.lang3.StringUtils;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * PortalRenderingPipeline that issues a redirect to a configured path.
 * Intended as a Pipeline implementation that might be plugged into a branch in the overall rendering pipeline and
 * conditionally actuated.
 * @since uPortal 4.2
 */
public class RedirectRenderingPipelineTerminator
    implements IPortalRenderingPipeline {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    public static String APPENDER_FNAME = "fname";

    private RequestRenderingPipelineUtils utils;

    @Autowired
    public void setUtils(RequestRenderingPipelineUtils u) {
      this.utils = u;
    }

    /**
     * Path to which terminator will redirect.
     */
    private String redirectTo;
    private String appender;

    @Override
    public void renderState(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        if (null == this.redirectTo) {
            throw new IllegalStateException("RedirectRenderingPipelineTerminator must be configured " +
                    "with path to which to redirect.");
        }

        if(StringUtils.isNotBlank(appender)) {
          String thingToAppend = "";
          if(APPENDER_FNAME.equalsIgnoreCase(appender)) {
            try {
              final IPortletDefinition portletDefinition = utils.getPortletDefinitionFromServletRequest(request);
              thingToAppend = portletDefinition.getFName();
            }
            catch(Exception e){
              logger.error("Appender was null but couldn't figure out fname.",e);
            }
          }
          logger.trace("Redirecting to {} .", this.redirectTo + thingToAppend);
          response.sendRedirect(this.redirectTo + thingToAppend);
        } else {
          logger.trace("Redirecting to {} .", this.redirectTo);
          response.sendRedirect(this.redirectTo);
        }
    }

    /**
     * Set the path to which the pipeline terminator should redirect.
     * Required.
     * @param targetPath non-null path String suitable for presentation to response.sendRedirect().
     * @throws IllegalArgumentException if targetPath is null.
     */
    @Required
    public void setRedirectTo(final String targetPath) {

        Assert.notNull(targetPath);

        this.redirectTo = targetPath;
    }

    /**
     * Setter for the appender which is used to add something to the end of the URL
     * @since 4.3
     * @param a appender to be set. Valid values: "fname"
     */
    public void setAppender(final String a) {
      this.appender = a;
    }

    @Override
    public String toString() {
        return "RedirectRenderingPipelineTerminator which redirects to " + this.redirectTo + " .";
    }
}
