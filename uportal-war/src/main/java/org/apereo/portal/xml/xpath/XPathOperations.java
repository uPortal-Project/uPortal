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
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpression;

/**
 */
public interface XPathOperations {
    /**
     * Call the specified Function with the compiled {@link XPathExpression} version of the
     * expression string
     */
    public <T> T doWithExpression(String expression, Function<XPathExpression, T> callback);

    /**
     * Call the specified Function with the compiled {@link XPathExpression} version of the
     * expression string The provided variables will be available for the expression at evaluation
     * time
     */
    public <T> T doWithExpression(
            String expression, Map<String, ?> variables, Function<XPathExpression, T> callback);

    /** @see XPathExpression#evaluate(Object, QName) */
    public <T> T evaluate(String expression, final Object item, final QName returnType);

    /** @see XPathExpression#evaluate(Object, QName) */
    public <T> T evaluate(
            String expression, Map<String, ?> variables, final Object item, final QName returnType);
}
