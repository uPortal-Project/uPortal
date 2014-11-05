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
package org.jasig.portal.properties;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.spring.context.support.QueryablePropertySourcesPlaceholderConfigurer;
import org.jasig.portal.utils.ContextPropertyPlaceholderUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.util.Properties;

import static org.jasig.portal.spring.context.support.QueryablePropertySourcesPlaceholderConfigurer.UnresolvablePlaceholderStrategy;

/**
 * Ensures that {@link PropertiesManager} is initialized before the Spring {@code BeanFactory} actually starts
 * instantiating "normal" singletons. This is important because some beans will attempt to access static methods on
 * that class during their own initialization process, but that class is not ever instantiated as a Spring bean,
 * nor do dependent classes declared their dependency on it via Spring config. So this was the least intrusive
 * way of ensuring a fully initialized {@link PropertiesManager} would be available when Spring started
 * instantiating singletons.
 *
 * <p>Using this class represents a significant departure from pre-SSP-1888 behavior in that for any given
 * {@code portal.properties} property, you get get the same value from either a {@link PropertiesManager} lookup, or
 * a property placeholder-style reference from a Spring Bean definition. Previously the latter would participate in
 * embedded property placeholder resolution but the former would not.</p>
 */
public class PropertiesManagerInitializer implements BeanFactoryPostProcessor {

    private static final Log log = LogFactory.getLog(PropertiesManagerInitializer.class);

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        QueryablePropertySourcesPlaceholderConfigurer propertyResolver =
                (QueryablePropertySourcesPlaceholderConfigurer)
                        beanFactory.getBean("primaryPropertyPlaceholderConfigurer");
        final Properties properties;
        try {
            properties = PropertiesManager.readDefaultProperties();
        } catch ( IOException e ) {
            log.error("Unable to read portal.properties file.", e); // same logging level and msg as equivalent operation in PropertiesManager.loadProps()
            return;
        }
        // IGNORE is consistent with historical PropertiesManager behavior (that's also why we trap the exception above)
        final Properties resolvedProperties =
                ContextPropertyPlaceholderUtils.resolve(properties, UnresolvablePlaceholderStrategy.IGNORE,
                        propertyResolver);
        PropertiesManager.setProperties(resolvedProperties);
    }

}
