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

import com.google.common.base.Function;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link XPathOperations} that uses a {@link GenericKeyedObjectPool} to pool
 * compiled {@link XPathExpression} instances.
 *
 */
@Service
public class XPathPoolImpl implements XPathOperations, DisposableBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ThreadLocalXPathVariableResolver variableResolver =
            new ThreadLocalXPathVariableResolver();
    private final XPathExpressionFactory xpathExpressionfactory;
    private final NamespaceContext namespaceContext;
    private final GenericKeyedObjectPool pool;

    public XPathPoolImpl() {
        this(null);
    }

    public XPathPoolImpl(NamespaceContext namespaceContext) {
        this.namespaceContext = namespaceContext;

        this.xpathExpressionfactory =
                new XPathExpressionFactory(this.namespaceContext, variableResolver);

        this.pool = new GenericKeyedObjectPool(xpathExpressionfactory);
        this.pool.setMaxActive(500);
        this.pool.setMaxIdle(500);
        this.pool.setTimeBetweenEvictionRunsMillis(TimeUnit.SECONDS.toMillis(60));
        this.pool.setMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(5));
        this.pool.setNumTestsPerEvictionRun(this.pool.getMaxIdle() / 9);
        this.pool.setWhenExhaustedAction(GenericKeyedObjectPool.WHEN_EXHAUSTED_FAIL);
    }

    @Override
    public void destroy() throws Exception {
        this.pool.close();
    }

    @Override
    public <T> T doWithExpression(String expression, Function<XPathExpression, T> callback) {
        return this.<T>doWithExpression(expression, null, callback);
    }

    @Override
    public <T> T doWithExpression(
            String expression, Map<String, ?> variables, Function<XPathExpression, T> callback) {
        try {
            final XPathExpression xPathExpression =
                    (XPathExpression) this.pool.borrowObject(expression);
            try {
                this.variableResolver.setVariables(variables);
                return callback.apply(xPathExpression);
            } finally {
                this.variableResolver.clearVariables();
                this.pool.returnObject(expression, xPathExpression);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Exception of type " + e.getClass().getName() + " is not expected", e);
        }
    }

    @Override
    public <T> T evaluate(String expression, final Object item, final QName returnType) {
        return this.<T>evaluate(expression, null, item, returnType);
    }

    @Override
    public <T> T evaluate(
            String expression,
            Map<String, ?> variables,
            final Object item,
            final QName returnType) {
        return this.doWithExpression(
                expression,
                variables,
                new Function<XPathExpression, T>() {
                    /* (non-Javadoc)
                     * @see com.google.common.base.Function#apply(java.lang.Object)
                     */
                    @SuppressWarnings("unchecked")
                    @Override
                    public T apply(XPathExpression xpathExpression) {
                        try {
                            return (T) xpathExpression.evaluate(item, returnType);
                        } catch (XPathExpressionException e) {
                            throw new RuntimeException(
                                    "Failed to execute XPathExpression '" + xpathExpression + "'",
                                    e);
                        }
                    }
                });
    }

    public void clear() {
        pool.clear();
    }

    public void clearOldest() {
        pool.clearOldest();
    }

    public void close() throws Exception {
        pool.close();
    }

    public void evict() throws Exception {
        pool.evict();
    }

    public boolean getLifo() {
        return pool.getLifo();
    }

    public int getMaxActive() {
        return pool.getMaxActive();
    }

    public int getMaxIdle() {
        return pool.getMaxIdle();
    }

    public int getMaxTotal() {
        return pool.getMaxTotal();
    }

    public long getMaxWait() {
        return pool.getMaxWait();
    }

    public long getMinEvictableIdleTimeMillis() {
        return pool.getMinEvictableIdleTimeMillis();
    }

    public int getMinIdle() {
        return pool.getMinIdle();
    }

    public int getNumActive() {
        return pool.getNumActive();
    }

    public int getNumIdle() {
        return pool.getNumIdle();
    }

    public int getNumTestsPerEvictionRun() {
        return pool.getNumTestsPerEvictionRun();
    }

    public boolean getTestOnBorrow() {
        return pool.getTestOnBorrow();
    }

    public boolean getTestOnReturn() {
        return pool.getTestOnReturn();
    }

    public boolean getTestWhileIdle() {
        return pool.getTestWhileIdle();
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return pool.getTimeBetweenEvictionRunsMillis();
    }

    public byte getWhenExhaustedAction() {
        return pool.getWhenExhaustedAction();
    }

    public void setLifo(boolean lifo) {
        pool.setLifo(lifo);
    }

    public void setMaxActive(int maxActive) {
        pool.setMaxActive(maxActive);
    }

    public void setMaxIdle(int maxIdle) {
        pool.setMaxIdle(maxIdle);
    }

    public void setMaxTotal(int maxTotal) {
        pool.setMaxTotal(maxTotal);
    }

    public void setMaxWait(long maxWait) {
        pool.setMaxWait(maxWait);
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        pool.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
    }

    public void setMinIdle(int poolSize) {
        pool.setMinIdle(poolSize);
    }

    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        pool.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        pool.setTestOnBorrow(testOnBorrow);
    }

    public void setTestOnReturn(boolean testOnReturn) {
        pool.setTestOnReturn(testOnReturn);
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        pool.setTestWhileIdle(testWhileIdle);
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        pool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
    }

    public void setWhenExhaustedAction(byte whenExhaustedAction) {
        pool.setWhenExhaustedAction(whenExhaustedAction);
    }
}
