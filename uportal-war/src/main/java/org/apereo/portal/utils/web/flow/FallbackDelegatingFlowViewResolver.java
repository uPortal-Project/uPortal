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
package org.apereo.portal.utils.web.flow;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.springframework.web.servlet.View;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.mvc.builder.DelegatingFlowViewResolver;
import org.springframework.webflow.mvc.view.FlowViewResolver;

/**
 * FlowViewResolver that can use a list of both ViewResolver and FlowViewResolvers
 *
 */
public class FallbackDelegatingFlowViewResolver extends DelegatingFlowViewResolver {
    private final List<FlowViewResolver> flowViewResolvers;

    public FallbackDelegatingFlowViewResolver(
            List<FlowViewResolver> flowViewResolvers, List viewResolvers) {
        super(viewResolvers);
        this.flowViewResolvers = ImmutableList.copyOf(flowViewResolvers);
    }

    /* (non-Javadoc)
     * @see org.springframework.webflow.mvc.builder.DelegatingFlowViewResolver#resolveView(java.lang.String, org.springframework.webflow.execution.RequestContext)
     */
    @Override
    public View resolveView(String viewId, RequestContext context) {
        View view = super.resolveView(viewId, context);
        if (view != null) {
            return view;
        }

        for (final FlowViewResolver flowViewResolver : this.flowViewResolvers) {
            try {
                final String viewIdByConvention = flowViewResolver.getViewIdByConvention(viewId);
                view = flowViewResolver.resolveView(viewIdByConvention, context);
                if (view != null) {
                    return view;
                }
            } catch (Exception e) {
                IllegalStateException ise =
                        new IllegalStateException(
                                "Exception resolving view with name '" + viewId + "'");
                ise.initCause(e);
                throw ise;
            }
        }

        return null;
    }
}
