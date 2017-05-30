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
package org.apereo.portal.xml.xpath;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates new {@link XPathExpression} instances
 *
 */
class XPathExpressionFactory extends BaseKeyedPoolableObjectFactory {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final XPathFactory xPathFactory = XPathFactory.newInstance();
    private final NamespaceContext namespaceContext;
    private final XPathVariableResolver variableResolver;

    public XPathExpressionFactory(
            NamespaceContext namespaceContext, XPathVariableResolver variableResolver) {
        this.namespaceContext = namespaceContext;
        this.variableResolver = variableResolver;
    }

    @Override
    public synchronized Object makeObject(Object key) throws Exception {
        final String expression = (String) key;

        final XPath xPath = xPathFactory.newXPath();
        if (this.namespaceContext != null) {
            xPath.setNamespaceContext(this.namespaceContext);
        }
        if (this.variableResolver != null) {
            xPath.setXPathVariableResolver(this.variableResolver);
        }

        logger.debug("Compiling XPathExpression from: {}", expression);

        try {
            return xPath.compile(expression);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(
                    "Failed to compile XPath expression '" + expression + "'", e);
        }
    }

    @Override
    public void destroyObject(Object key, Object obj) throws Exception {
        final String expression = (String) key;
        logger.debug("Destroying XPathExpression: {}", expression);
    }
}
