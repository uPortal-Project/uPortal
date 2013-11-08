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
package org.jasig.portal.spring.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.util.StringValueResolver;

/**
 * Exports the property placeholder resolution portion of {@link PropertySourcesPlaceholderConfigurer}'s
 * {@link BeanFactory} post-processing behavior via a new API method:
 * {@link #resolve(String, org.jasig.portal.spring.context.support.QueryablePropertySourcesPlaceholderConfigurer.UnresolvablePlaceholderStrategy)}.
 * The {@link UnresolvablePlaceholderStrategy} argument allows for post-{@link BeanFactory} initialization property
 * value resolution to either follow the configurer's Spring-defined configuration or the caller's current
 * preference. This exists for backward compatibility with uPortal components such as {@link PropertiesManager}.
 */
public class QueryablePropertySourcesPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

    protected final Log logger = LogFactory.getLog(this.getClass());
    private StringValueResolver origStringValueResolver;
    private ConfigurablePropertyResolver origConfigurablePropertyResolver;

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                     final ConfigurablePropertyResolver propertyResolver) throws BeansException {
        this.origConfigurablePropertyResolver = propertyResolver;
        super.processProperties(beanFactoryToProcess, propertyResolver);
    }

    @Override
    protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                       StringValueResolver valueResolver) {

        this.origStringValueResolver = valueResolver;
        super.doProcessProperties(beanFactoryToProcess, valueResolver);
    }

    public String resolve(String propertyValue, UnresolvablePlaceholderStrategy unresolvablePlaceholderStrategy) {
        if ( unresolvablePlaceholderStrategy == null ) {
            return UnresolvablePlaceholderStrategy.DEFER.resolve(propertyValue, this);
        }
        return unresolvablePlaceholderStrategy.resolve(propertyValue, this);
    }

    public static enum UnresolvablePlaceholderStrategy {
        ERROR {
            @Override
            String resolve(String propertyValue, QueryablePropertySourcesPlaceholderConfigurer configurer) {
                final ConfigurablePropertyResolver origConfigurablePropertyResolver = configurer.getOrigConfigurablePropertyResolver();
                if ( origConfigurablePropertyResolver == null ) {
                    throw new IllegalStateException("No ConfigurablePropertyResolver configured. Cannot query for value of ["
                            + propertyValue + "]");
                }
                // See org.springframework.context.support.PropertySourcesPlaceholderConfigurer.processProperties()
                // for how this usually works. We could get clever here and check the current value of
                // QueryablePropertySourcesPlaceholderConfigurer.ignoreUnresolvablePlaceholders and thereby
                // potentially avoid creation of a new StringValueResolver, but that makes fragile implementtaion
                // assumptions. The approach below is bullet proof... 'ERROR' always means "error".
                return new StringValueResolver() {
                    public String resolveStringValue(String strVal) {
                        return origConfigurablePropertyResolver.resolveRequiredPlaceholders(strVal);
                    }
                }.resolveStringValue(propertyValue);
            }
        },
        IGNORE {
            @Override
            String resolve(String propertyValue, QueryablePropertySourcesPlaceholderConfigurer configurer) {
                final ConfigurablePropertyResolver origConfigurablePropertyResolver = configurer.getOrigConfigurablePropertyResolver();
                if ( origConfigurablePropertyResolver == null ) {
                    throw new IllegalStateException("No ConfigurablePropertyResolver configured. Cannot query for value of ["
                            + propertyValue + "]");
                }
                // See org.springframework.context.support.PropertySourcesPlaceholderConfigurer.processProperties()
                // for how this usually works. We could get clever here and check the current value of
                // QueryablePropertySourcesPlaceholderConfigurer.ignoreUnresolvablePlaceholders and thereby
                // potentially avoid creation of a new StringValueResolver, but that makes fragile implementtaion
                // assumptions. The approach below is bullet proof... 'IGNORE' always means "ignore".
                return new StringValueResolver() {
                    public String resolveStringValue(String strVal) {
                        return origConfigurablePropertyResolver.resolvePlaceholders(strVal);
                    }
                }.resolveStringValue(propertyValue);
            }
        },
        DEFER {
            @Override
            String resolve(String propertyValue, QueryablePropertySourcesPlaceholderConfigurer configurer) {
                final StringValueResolver origStringValueResolver = configurer.getOrigStringValueResolver();
                if ( origStringValueResolver == null ) {
                    throw new IllegalStateException("No StringValueResolver configured. Cannot query for value of ["
                            + propertyValue + "]");
                }
                return origStringValueResolver.resolveStringValue(propertyValue);
            }
        };

        abstract String resolve(String propertyValue, QueryablePropertySourcesPlaceholderConfigurer configurer);
    }


    // No setters b/c this class is really set up to run first as a BeanFactoryPostProcessor, at which time the below
    // fields will be populated. Letting clients set one or the other just isn't needed and it's very hard to explain
    // why you would actually need to set both (One is expected to wrap the other, and this normally occurs when run as
    // a BeanFactoryPostProcessor, but the two instances are exposed to subclasses at different times and do not
    // have navigable relationships to each other. But depending on the UnresolvablePlaceholderStrategy passed to
    // getPropertyValue(), we might need one or the other.)

    StringValueResolver getOrigStringValueResolver() {
        return origStringValueResolver;
    }

    ConfigurablePropertyResolver getOrigConfigurablePropertyResolver() {
        return origConfigurablePropertyResolver;
    }

}
