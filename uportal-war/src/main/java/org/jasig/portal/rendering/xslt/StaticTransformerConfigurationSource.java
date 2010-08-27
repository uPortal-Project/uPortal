/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering.xslt;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.cache.CacheKey;
import org.jasig.portal.spring.spel.IPortalSpELService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StaticTransformerConfigurationSource implements TransformerConfigurationSource {
    private Properties outputProperties;
    private LinkedHashMap<String, Object> parameters;
    private IPortalSpELService portalSpELService;
    private Map<String, Expression> parameterExpressions;


    @Autowired
    public void setPortalSpELService(IPortalSpELService portalSpELService) {
        this.portalSpELService = portalSpELService;
    }
    
    public void setProperties(Properties transformerOutputProperties) {
        this.outputProperties = transformerOutputProperties;
    }

    public void setParameters(Map<String, Object> transformerParameters) {
        this.parameters = new LinkedHashMap<String, Object>(transformerParameters);
    }

    public void setParameterExpressions(Map<String, String> parameterExpressions) {
        final Map<String, Expression> parameterExpressionsBuilder = new LinkedHashMap<String, Expression>();
        
        for (final Map.Entry<String, String> expressionEntry : parameterExpressions.entrySet()) {
            final String string = expressionEntry.getValue();
            final Expression expression = this.portalSpELService.parseExpression(string);
            parameterExpressionsBuilder.put(expressionEntry.getKey(), expression);
        }
        
        this.parameterExpressions = parameterExpressionsBuilder;
    }

    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final LinkedHashMap<String, Object> transformerParameters = this.getParameters(request, response);
        return new CacheKey(this.outputProperties, transformerParameters);
    }

    @Override
    public Properties getOutputProperties(HttpServletRequest request, HttpServletResponse response) {
        return this.outputProperties;
    }

    @Override
    public LinkedHashMap<String, Object> getParameters(HttpServletRequest request, HttpServletResponse response) {
        final ServletWebRequest webRequest = new ServletWebRequest(request, response);
        
        final LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>(this.parameters);
        for (final Map.Entry<String, Expression> expressionEntry : this.parameterExpressions.entrySet()) {
            final Expression expression = expressionEntry.getValue();
            final Object value = this.portalSpELService.getValue(expression, webRequest);
            
            parameters.put(expressionEntry.getKey(), value);
        }
        
        return parameters;
    }
}
