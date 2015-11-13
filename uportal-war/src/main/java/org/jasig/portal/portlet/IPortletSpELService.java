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

import javax.portlet.PortletRequest;

import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;

/**
 * IPortletSpELService provides an interface for parsing strings using the Spring expression language.
 * Strings are assumed to be a potential mixture of SpEL expressions contained inside a ${ } and text string
 * content.  For example, a portal-relative URL path might be formatted as
 * ${request.contextPath}/my/path.
 *
 * Possible spring attribute values are:<ul>
 *     <li>request - portlet Request</li>
 *     <li>userInfo - map of User Info available to the portlet</li>
 *     <li>@beanName - bean in spring context</li>
 * </ul>
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 * @since 4.2
 */

public interface IPortletSpELService {

    /**
     * Parse the supplied string by replacing any ${ } blocks with the SpEL-evaluated value.
     *
     * @param expressionString  string to be evaluated
     * @param request portlet request
     * @return evaluated string
     */
    String parseString(String expressionString, PortletRequest request)  throws EvaluationException;

    /**
     * Parse the supplied string by replacing any ${ } blocks with the SpEL-evaluated value, returning the
     * desired type if possible.
     *
     * @param expressionString  string to be evaluated
     * @param request portlet request
     * @return evaluated object
     * @exception EvaluationException
     */
    <T> T getValue(String expressionString, PortletRequest request, Class<T> desiredResultType) throws EvaluationException;

    /**
     * Returns a SpEL Expression using the standard Parsing Context (uses ${ }).  Allows you to supply your own
     * {@link org.springframework.expression.spel.support.StandardEvaluationContext} to evaluate the expression
     * against any objects you want.  Refer to SpEL API or review the code in
     * {@link org.jasig.portal.portlet.PortletSpELServiceImpl}.
     * @param expressionString string to be evaluated
     * @return SpEL Expression
     * @throws ParseException Error in expression string format
     */
    Expression parseExpression(String expressionString) throws ParseException;
}
