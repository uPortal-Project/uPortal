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

import java.util.Collections;
import java.util.List;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.mvc.builder.FlowResourceFlowViewResolver;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;
import org.springframework.webflow.mvc.view.AbstractMvcViewFactory;
import org.springframework.webflow.mvc.view.FlowViewResolver;
import org.springframework.webflow.validation.WebFlowMessageCodesResolver;

/**
 * Clone of {@link MvcViewFactoryCreator} that returns our {@link RuntimeMvcViewFactory}.
 *
 */
public class RuntimeMvcViewFactoryCreator extends MvcViewFactoryCreator {
    private final FlowResourceFlowViewResolver flowResourceFlowViewResolver =
            new FlowResourceFlowViewResolver();

    private FlowViewResolver flowViewResolver = flowResourceFlowViewResolver;

    private MessageCodesResolver messageCodesResolver = new WebFlowMessageCodesResolver();

    /**
     * Sets the message codes resolver strategy to use to resolve bind and validation error message
     * codes. If not set, {@link WebFlowMessageCodesResolver} is the default. Plug in a {@link
     * DefaultMessageCodesResolver} to resolve message codes consistently between Spring MVC
     * Controllers and Web Flow.
     *
     * @param messageCodesResolver the message codes resolver
     */
    @Override
    public void setMessageCodesResolver(MessageCodesResolver messageCodesResolver) {
        super.setMessageCodesResolver(messageCodesResolver);
        this.messageCodesResolver = messageCodesResolver;
    }

    /**
     * Configure an {@link FlowResourceFlowViewResolver} capable of resolving view resources by
     * applying the specified default resource suffix. Default is .jsp.
     *
     * @param defaultViewSuffix the default view suffix
     */
    @Override
    public void setDefaultViewSuffix(String defaultViewSuffix) {
        super.setDefaultViewSuffix(defaultViewSuffix);

        FlowResourceFlowViewResolver internalResourceResolver = new FlowResourceFlowViewResolver();
        internalResourceResolver.setDefaultViewSuffix(defaultViewSuffix);
        this.flowViewResolver = internalResourceResolver;
    }

    /**
     * Set to fully customize how the flow system resolves Spring MVC {@link View} objects.
     *
     * @param flowViewResolver the flow view resolver
     */
    @Override
    public void setFlowViewResolver(FlowViewResolver flowViewResolver) {
        super.setFlowViewResolver(flowViewResolver);
        this.flowViewResolver = flowViewResolver;
    }

    /**
     * Sets the chain of Spring MVC {@link ViewResolver view resolvers} to delegate to resolve views
     * selected by flows. Allows for reuse of existing View Resolvers configured in a Spring
     * application context. If multiple resolvers are to be used, the resolvers should be ordered in
     * the manner they should be applied.
     *
     * @param viewResolvers the view resolver list
     */
    @Override
    public void setViewResolvers(List viewResolvers) {
        super.setViewResolvers(viewResolvers);
        this.flowViewResolver =
                new FallbackDelegatingFlowViewResolver(
                        Collections.<FlowViewResolver>singletonList(flowResourceFlowViewResolver),
                        viewResolvers);
    }

    /* (non-Javadoc)
     * @see org.springframework.webflow.mvc.builder.MvcViewFactoryCreator#createMvcViewFactory(org.springframework.binding.expression.Expression, org.springframework.binding.expression.ExpressionParser, org.springframework.binding.convert.ConversionService, org.springframework.webflow.engine.builder.BinderConfiguration)
     */
    @Override
    protected AbstractMvcViewFactory createMvcViewFactory(
            Expression viewId,
            ExpressionParser expressionParser,
            ConversionService conversionService,
            BinderConfiguration binderConfiguration) {

        return new RuntimeMvcViewFactory(
                viewId,
                flowViewResolver,
                expressionParser,
                conversionService,
                binderConfiguration,
                messageCodesResolver);
    }
}
