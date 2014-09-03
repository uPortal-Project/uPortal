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
package org.jasig.portal.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.spring.context.support.QueryablePropertySourcesPlaceholderConfigurer;
import org.jasig.portal.spring.locator.ApplicationContextLocator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.Enumeration;
import java.util.Properties;

import static org.jasig.portal.spring.context.support.QueryablePropertySourcesPlaceholderConfigurer.UnresolvablePlaceholderStrategy;

/**
 * Wrappers around {@link QueryablePropertySourcesPlaceholderConfigurer} mainly intended to support components which
 * manually look up {@link Properties}-based (name-value pair) configuration in bulk but where those same properties
 * may have been injected into SpringBeans, in which case they should be subject to the same property placeholder
 * resolution behaviors as the latter.
 */
public class ContextPropertyPlaceholderUtils {

    private static final Log LOG = LogFactory.getLog(ApplicationContextLocator.class);

    public static Properties resolve(Properties properties,
                                     UnresolvablePlaceholderStrategy unresolvablePlaceholderStrategy,
                                     ApplicationContext applicationContext,
                                     QueryablePropertySourcesPlaceholderConfigurer propertyResolver) {


        // some code dup. here, but trying to only look this thing up once when running in bulk resolution mode
        if ( propertyResolver == null ) {
            propertyResolver = currentPropertyResolver(applicationContext);
        }

        if ( propertyResolver == null ) {
            // diagnostics already logged by currentPropertyResolver()
            return properties;
        }

        final Properties processedProperties = new Properties();
        final Enumeration<String> propertyNames = (Enumeration<String>) properties.propertyNames();
        while ( propertyNames.hasMoreElements() ) {
            final String propertyName = propertyNames.nextElement();
            final String propertyValue = properties.getProperty(propertyName);
            final String processedPropertyValue = resolve(propertyValue, unresolvablePlaceholderStrategy,
                    applicationContext, propertyResolver);
            processedProperties.setProperty(propertyName, processedPropertyValue);
        }
        return processedProperties;
    }

    public static Properties resolve(Properties properties) {
        return resolve(properties, null, null, null);
    }

    public static Properties resolve(Properties properties,
                                      UnresolvablePlaceholderStrategy unresolvablePlaceholderStrategy) {
        return resolve(properties, unresolvablePlaceholderStrategy, null, null);
    }

    public static Properties resolve(Properties properties,
                                     UnresolvablePlaceholderStrategy unresolvablePlaceholderStrategy,
                                     ApplicationContext applicationContext) {
        return resolve(properties, unresolvablePlaceholderStrategy, applicationContext, null);
    }

    public static Properties resolve(Properties properties,
                                     UnresolvablePlaceholderStrategy unresolvablePlaceholderStrategy,
                                     QueryablePropertySourcesPlaceholderConfigurer propertyResolver) {
        return resolve(properties, unresolvablePlaceholderStrategy, null, propertyResolver);
    }

    private static String resolve(String propertyExpression, UnresolvablePlaceholderStrategy unresolvablePlaceholderStrategy,
                                  ApplicationContext applicationContext,
                                  QueryablePropertySourcesPlaceholderConfigurer propertyResolver) {
        if ( propertyResolver == null ) {
            propertyResolver = currentPropertyResolver(applicationContext);
        }
        if ( propertyResolver == null ) {
            return propertyExpression;
        }
        return propertyResolver.resolve(propertyExpression, unresolvablePlaceholderStrategy);
    }

    public static String resolve(String propertyExpression, UnresolvablePlaceholderStrategy unresolvablePlaceholderStrategy,
                                 ApplicationContext applicationContext) {
        return resolve(propertyExpression, unresolvablePlaceholderStrategy, applicationContext, null);
    }

    public static String resolve(String propertyExpression, UnresolvablePlaceholderStrategy unresolvablePlaceholderStrategy) {
        return resolve(propertyExpression, unresolvablePlaceholderStrategy, null);
    }

    public static String resolve(String propertyExpression) {
        return resolve(propertyExpression, null, null);
    }

    private static ApplicationContext currentApplicationContext() {
        return ApplicationContextLocator.getApplicationContext();
    }

    private static QueryablePropertySourcesPlaceholderConfigurer currentPropertyResolver(ApplicationContext applicationContext)
            throws BeansException {

        // yes, code duplication here, but in the name of an an opimization to avoid repeated AppContext lookups when
        // running in bulk properties handling mode
        if ( applicationContext == null ) {
            try {
                applicationContext = currentApplicationContext();
            } catch ( IllegalStateException e ) {
                LOG.error("Unable to load ApplicationContext. Proceeding as if an ApplicationContext simply does not exist", e);
            }
        }

        if ( applicationContext != null ) {
            try {
                return applicationContext.getBean("primaryPropertyPlaceholderConfigurer",
                        QueryablePropertySourcesPlaceholderConfigurer.class);
            } catch ( BeansException e ) {
                LOG.error("Unable to find a QueryablePropertySourcesPlaceholderConfigurer bean named"
                        + " [primaryPropertyPlaceholderConfigurer]. Proceeding as if properties do not need the"
                        + " placeholder resolution services of that bean.", e);
            }
        }

        return null;
    }

}
