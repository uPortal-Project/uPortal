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
package org.jasig.portal.portlet;

import java.util.Map;

import javax.portlet.PortletRequest;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.portlet.context.PortletWebRequest;

/**
 * PortletSpELServiceImpl provides the default implementation of IPortletSpELService.
 * 
 * @author James Wennmacher, jwennmacher@unicon.net (code based on PortalSpELServiceImpl by Jen Bourey)
 */
@Service
public class PortletSpELServiceImpl implements BeanFactoryAware, IPortletSpELService {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private ExpressionParser expressionParser = new SpelExpressionParser();
    private Ehcache expressionCache;
    private BeanResolver beanResolver;

    public void setExpressionParser(ExpressionParser expressionParser) {
        Validate.notNull(expressionParser);
        this.expressionParser = expressionParser;
    }

    @Autowired
    public void setExpressionCache(@Qualifier("portletSpELExpressionCache") Ehcache expressionCache) {
        this.expressionCache = expressionCache;
    }
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanResolver = new BeanFactoryResolver(beanFactory);
    }

    // Primary method used assuming all defaults.
    @Override
    public String parseString(String expressionString, PortletRequest request) {
        return getValue(expressionString, request, String.class);
    }

    @Override
    public <T> T getValue(String expressionString, PortletRequest request, Class<T> desiredResultType) {
        final Expression expression = parseExpression(expressionString);
        final EvaluationContext evaluationContext = getEvaluationContext(request);
        return expression.getValue(evaluationContext, desiredResultType);
    }

    @Override
    public Expression parseExpression(String expressionString) throws ParseException {
        if (this.expressionCache == null) {
            return parseExpression(expressionString, TemplateParserContext.INSTANCE);
        }

        Element element = this.expressionCache.get(expressionString);
        if (element != null) {
            return (Expression)element.getObjectValue();
        }

        final Expression expression = parseExpression(expressionString, TemplateParserContext.INSTANCE);

        element = new Element(expressionString, expression);
        this.expressionCache.put(element);

        return expression;
    }

    private Expression parseExpression(String expressionString, ParserContext parserContext) throws ParseException {
        if (parserContext == null) {
            return this.expressionParser.parseExpression(expressionString);
        }

        return this.expressionParser.parseExpression(expressionString, parserContext);
    }

    /**
     * Return a SpEL evaluation context for the supplied portlet request.
     *
     * @param request PortletRequest
     * @return SpEL evaluation context for the supplied portlet request
     */
    protected EvaluationContext getEvaluationContext(PortletRequest request) {
        Map<String, String> userInfo = (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
        final SpELEnvironmentRoot root = new SpELEnvironmentRoot(new PortletWebRequest(request), userInfo);
        final StandardEvaluationContext context = new StandardEvaluationContext(root);
        // Allows for @myBeanName replacements
        context.setBeanResolver(this.beanResolver);
        return context;
    }

    /**
     * Limited-use POJO representing the root of a SpEL environment.  At the current moment, we're only using
     * the request object and user info in the evaluation context.  Can add additional objects in the future.
     */
    private static class SpELEnvironmentRoot {

        private final WebRequest request;
        private final Map<String,String> userInfo;

        /**
         * Create a new SpEL environment root for use in a SpEL evaluation
         * context.
         *
         * @param request  web request
         */
        private SpELEnvironmentRoot(WebRequest request, Map<String,String>userInfo) {
            this.request = request;
            this.userInfo = userInfo;
        }

        /**
         * Get the request associated with this environment root.
         */
        public WebRequest getRequest() {
            return request;
        }

        /**
         * User attributes associated with the person in this request.
         * @return userInfo
         */
        public Map<String, String> getUserInfo() {
            return userInfo;
        }
    }

    public static class TemplateParserContext implements ParserContext {
        public static final TemplateParserContext INSTANCE = new TemplateParserContext();
    
        @Override
        public String getExpressionPrefix() {
            return "${";
        }

        @Override
        public String getExpressionSuffix() {
            return "}";
        }

        @Override
        public boolean isTemplate() {
            return true;
        }
    }
}
