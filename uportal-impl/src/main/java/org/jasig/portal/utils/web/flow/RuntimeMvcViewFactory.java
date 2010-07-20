/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.utils.web.flow;

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
 * Implementation that does reflection on the native request to determine if a {@link PortletMvcView} or a
 * {@link ServletMvcView} should be used.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RuntimeMvcViewFactory extends AbstractMvcViewFactory {
    
    public RuntimeMvcViewFactory(Expression viewId, FlowViewResolver viewResolver, ExpressionParser expressionParser,
            ConversionService conversionService, BinderConfiguration binderConfiguration,
            MessageCodesResolver messageCodesResolver) {
        super(viewId, viewResolver, expressionParser, conversionService, binderConfiguration, messageCodesResolver);
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
