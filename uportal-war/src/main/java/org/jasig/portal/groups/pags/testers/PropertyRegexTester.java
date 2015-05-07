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
package org.jasig.portal.groups.pags.testers;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.jasig.portal.properties.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tester for matching the possibly multiple values of an attribute against a regular expression obtained from
 * a property value specified in portal.properties.  The match function attempts to match the entire region against
 * the pattern specified.
 * <br/>
 * See {@link org.jasig.portal.groups.pags.testers.RegexTester} for regex examples.
 *
 * @author James Wennmacher jwennmacher@unicon.net
 * @see org.jasig.portal.groups.pags.testers.RegexTester for expression examples
 */
public class PropertyRegexTester extends RegexTester {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * @since 4.3
     */
    public PropertyRegexTester(IPersonAttributesGroupTestDefinition definition) {
        super(definition);
        setPattern(definition.getAttributeName(), definition.getTestValue());
    }

    /**
     *
     * @param attribute Person attribute to propertyName against
     * @param propertyName name of a property defined in portal.properties
     * @deprecated use {@link EntityPersonAttributesGroupStore}, which leverages
     * the single-argument constructor.
     */
    @Deprecated
    public PropertyRegexTester(String attribute, String propertyName) {
        super(attribute, propertyName);
        setPattern(attribute, propertyName);
    }

    @Override
    public boolean test(String att) {
        return pattern.matcher(att).matches();
    }

    private void setPattern(String attribute, String propertyName) {
        String regexExpression = PropertiesManager.getProperty
                (propertyName, "");
        if (StringUtils.isBlank(regexExpression)) {
            logger.error("Unable to find property name {} in portal.properties or has empty value."
                + " PAGS PropertyRegexTester will always return false for attribute {}",
                propertyName, attribute);
        }
        setPattern(regexExpression);
    }

}
