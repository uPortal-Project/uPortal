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

package org.jasig.portal.rendering.xslt;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.spring.spel.IPortalSpELService;
import org.jasig.portal.utils.cache.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StaticTransformerConfigurationSource implements TransformerConfigurationSource {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private Properties outputProperties;
    private LinkedHashMap<String, Object> parameters;
    private IPortalSpELService portalSpELService;
    private Map<String, Expression> parameterExpressions;
    private Set<String> cacheKeyExcludedParameters = Collections.emptySet();


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
    
    /**
     * Parameter keys to exclude from the cache key.
     */
    public void setCacheKeyExcludedParameters(Set<String> cacheKeyExcludedParameters) {
        this.cacheKeyExcludedParameters = cacheKeyExcludedParameters;
    }

    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final LinkedHashMap<String, Object> transformerParameters = this.getParameters(request, response);
        transformerParameters.keySet().removeAll(this.cacheKeyExcludedParameters);
        return CacheKey.build(this.getClass().getName(), this.outputProperties, transformerParameters);
    }

    @Override
    public Properties getOutputProperties(HttpServletRequest request, HttpServletResponse response) {
        this.logger.debug("Returning output properties: {}", this.outputProperties);
        return this.outputProperties;
    }

    @Override
    public LinkedHashMap<String, Object> getParameters(HttpServletRequest request, HttpServletResponse response) {
        final ServletWebRequest webRequest = new ServletWebRequest(request, response);
        
        //Clone the static parameter map
        final LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>(this.parameters);
        
        //Add in any SpEL based parameters
        if (this.parameterExpressions != null) {
            for (final Map.Entry<String, Expression> expressionEntry : this.parameterExpressions.entrySet()) {
                final Expression expression = expressionEntry.getValue();
                final Object value = this.portalSpELService.getValue(expression, webRequest);
                
                if (value != null) {
                    parameters.put(expressionEntry.getKey(), value);
                }
            }
        }
        
        this.logger.debug("Returning transformer parameters: {}", parameters);
        
        return parameters;
    }
}
