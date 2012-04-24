/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.spring.spel;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
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

/**
 * PortalSpELServiceImpl provides the default implementation of 
 * IPortalSpELService.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Service
public class PortalSpELServiceImpl implements IPortalSpELService, BeanFactoryAware {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private ExpressionParser expressionParser = new SpelExpressionParser();
    private Ehcache expressionCache;
    private BeanResolver beanResolver;
    private IPortalRequestUtils portalRequestUtils;
    private IUserInstanceManager userInstanceManager;

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }


    public void setExpressionParser(ExpressionParser expressionParser) {
        Validate.notNull(expressionParser);
        this.expressionParser = expressionParser;
    }

    @Autowired
    public void setExpressionCache(@Qualifier("SpELExpressionCache") Ehcache expressionCache) {
        this.expressionCache = expressionCache;
    }
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanResolver = new BeanFactoryResolver(beanFactory);
    }

    @Override
    public Expression parseExpression(String expressionString) throws ParseException {
        return this.parseCachedExpression(expressionString, null);
    }
    
    protected Expression parseCachedExpression(String expressionString, ParserContext parserContext) throws ParseException {
        if (this.expressionCache == null) {
            return parseExpression(expressionString, parserContext);
        }
        
        Element element = this.expressionCache.get(expressionString);
        if (element != null) {
            return (Expression)element.getObjectValue();
        }
        
        final Expression expression = parseExpression(expressionString, parserContext);
        
        element = new Element(expressionString, expression);
        this.expressionCache.put(element);
        
        return expression;
    }

    protected Expression parseExpression(String expressionString, ParserContext parserContext) {
        if (parserContext == null) {
            return this.expressionParser.parseExpression(expressionString);
        }

        return this.expressionParser.parseExpression(expressionString, parserContext);
    }

    @Override
    public String parseString(String expressionString, WebRequest request) {
        final Expression expression = this.parseCachedExpression(expressionString, TemplateParserContext.INSTANCE);
        return this.getValue(expression, request, String.class);
    }
    
    @Override
    public <T> T getValue(String expressionString, WebRequest request, Class<T> desiredResultType) {
        final Expression expression = this.parseExpression(expressionString);
        return this.getValue(expression, request, desiredResultType);
    }
    
    @Override
    public <T> T getValue(Expression expression, WebRequest request, Class<T> desiredResultType) {
        final EvaluationContext evaluationContext = this.getEvaluationContext(request);
        return expression.getValue(evaluationContext, desiredResultType);
    }

    @Override
    public Object getValue(String expressionString, WebRequest request) {
        final Expression expression = this.parseExpression(expressionString);
        return this.getValue(expression, request);
    }

    @Override
    public Object getValue(Expression expression, WebRequest request) {
        final EvaluationContext evaluationContext = this.getEvaluationContext(request);
        return expression.getValue(evaluationContext);
    }

    /**
     * Return a SpEL evaluation context for the supplied web request.
     * 
     * @param request
     * @return
     */
    protected EvaluationContext getEvaluationContext(WebRequest request) {
        final HttpServletRequest httpRequest = this.portalRequestUtils.getOriginalPortalRequest(request);
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(httpRequest);
        final IPerson person = userInstance.getPerson();
        
        final SpELEnvironmentRoot root = new SpELEnvironmentRoot(request, person);
        final StandardEvaluationContext context = new StandardEvaluationContext(root);
        context.setBeanResolver(this.beanResolver);
        return context;
    }

    /**
     * Limited-use POJO representing the root of a SpEL environment.  At the
     * current moment, we're only using the request object in the evaluation
     * context, but we'd like to be able to add additional objects in the 
     * future.
     */
    @SuppressWarnings("unused")
    private static class SpELEnvironmentRoot {
        
        private final WebRequest request;
        private final IPerson person;
        
        /**
         * Create a new SpEL environment root for use in a SpEL evaluation
         * context.
         * 
         * @param request  web request
         */
        private SpELEnvironmentRoot(WebRequest request, IPerson person) {
            this.request = request;
            this.person = person;
        }

        /**
         * Get the request associated with this environment root.
         */
        public WebRequest getRequest() {
            return request;
        }

        /**
         * The person associated with this environment root
         */
        public IPerson getPerson() {
            return this.person;
        }
    }

    static class TemplateParserContext implements ParserContext {
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
