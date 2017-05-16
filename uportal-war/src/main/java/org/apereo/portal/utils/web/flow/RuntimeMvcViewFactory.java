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

import javax.portlet.PortletRequest;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.servlet.View;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.mvc.portlet.PortletMvcView;
import org.springframework.webflow.mvc.servlet.ServletMvcView;
import org.springframework.webflow.mvc.view.AbstractMvcView;
import org.springframework.webflow.mvc.view.AbstractMvcViewFactory;
import org.springframework.webflow.mvc.view.FlowViewResolver;

/**
 * Implementation that does reflection on the native request to determine if a {@link
 * PortletMvcView} or a {@link ServletMvcView} should be used.
 *
 */
public class RuntimeMvcViewFactory extends AbstractMvcViewFactory {

    public RuntimeMvcViewFactory(
            Expression viewId,
            FlowViewResolver viewResolver,
            ExpressionParser expressionParser,
            ConversionService conversionService,
            BinderConfiguration binderConfiguration,
            MessageCodesResolver messageCodesResolver) {
        super(
                viewId,
                viewResolver,
                expressionParser,
                conversionService,
                binderConfiguration,
                messageCodesResolver);
    }

    /* (non-Javadoc)
     * @see org.springframework.webflow.mvc.view.AbstractMvcViewFactory#createMvcView(org.springframework.web.servlet.View, org.springframework.webflow.execution.RequestContext)
     */
    @Override
    protected AbstractMvcView createMvcView(View view, RequestContext context) {
        final ExternalContext externalContext = context.getExternalContext();
        final Object nativeRequest = externalContext.getNativeRequest();
        if (nativeRequest instanceof PortletRequest) {
            return new PortletMvcView(view, context);
        }

        return new ServletMvcView(view, context);
    }
}
