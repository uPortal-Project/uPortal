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
package org.springframework.context.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

/**
 * Custom extension of {@link PropertySourcesPlaceholderConfigurer} that serves three purposes:
 *
 * <ul>
 *   <li>Override postProcessing to provide access to "local" properties before bean post-processing
 *       has completed
 *   <li>Force configuration setting ignoreResourceNotFound=true and (safely) ignore noisy WARNings
 *       in the log concerning missing properties files that are optional
 *   <li>Provide support for encrypted property values based on Jasypt
 * </ul>
 *
 */
public class PortalPropertySourcesPlaceholderConfigurer
        extends PropertySourcesPlaceholderConfigurer {

    public static final String EXTENDED_PROPERTIES_SOURCE = "extendedPropertiesSource";
    public static final String JAYSYPT_ENCRYPTION_KEY_VARIABLE = "UP_JASYPT_KEY";

    private PropertyResolver propertyResolver;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PortalPropertySourcesPlaceholderConfigurer() {
        /*
         * We rely on this config for our optional properties files
         */
        super.setIgnoreResourceNotFound(true);
    }

    @Override
    public void setIgnoreResourceNotFound(boolean value) {
        if (value == false) {
            final String msg =
                    "Instances of PortalPropertySourcesPlaceholderConfigurer "
                            + "are always ignoreResourceNotFound=true";
            throw new UnsupportedOperationException(msg);
        }
    }

    /**
     * uPortal defines some properties files in its primaryPropertyPlaceholderConfigurer bean that
     * are considered (and documented) optional. The parent class
     * (PropertySourcesPlaceholderConfigurer) will operate properly without them, but puts a
     * significant number of WARN messages into the log. These are noisy, and could lead a new
     * adopter to think that something's wrong. This method removes absent properties files from the
     * collection.
     */
    @Override
    public void setLocations(Resource[] locations) {
        final List<Resource> list = new ArrayList<>();
        for (Resource r : locations) {
            if (r.exists()) {
                list.add(r);
            } else {
                // In our case this event is worth a DEBUG note.
                logger.debug(
                        "The following Resource was not present (it may be "
                                + "optional, or it's absence may lead to issues):  ",
                        r);
            }
        }
        super.setLocations(list.toArray(new Resource[0]));
    }

    /**
     * Override the postProcessing. The default PropertySourcesPlaceholderConfigurer does not inject
     * local properties into the Environment object. It builds a local list of properties files and
     * then uses a transient Resolver to resolve the @Value annotations. That means that you are
     * unable to get to "local" properties (eg. portal.properties) after bean post-processing has
     * completed unless you are going to re-parse those file. This is similar to what
     * PropertiesManager does, but it uses all the property files configured, not just
     * portal.properties.
     *
     * <p>If we upgrade to spring 4, there are better/more efficient solutions available. I'm not
     * aware of better solutions for spring 3.x.
     *
     * @param beanFactory the bean factory
     * @throws BeansException if an error occurs while loading properties or wiring up beans
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        if (propertyResolver == null) {
            try {
                MutablePropertySources sources = new MutablePropertySources();
                PropertySource<?> localPropertySource =
                        new PropertiesPropertySource(EXTENDED_PROPERTIES_SOURCE, mergeProperties());
                sources.addLast(localPropertySource);

                propertyResolver = new PropertySourcesPropertyResolver(sources);

            } catch (IOException e) {
                throw new BeanInitializationException("Could not load properties", e);
            }
        }

        super.postProcessBeanFactory(beanFactory);
    }

    /**
     * Get a property resolver that can read local properties.
     *
     * @return a property resolver that can be used to dynamically read the merged property values
     *     configured in applicationContext.xml
     */
    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    /**
     * Override PropertiesLoaderSupport.mergeProprties in order to slip in a properly-configured
     * EncryptableProperties instance, allowing us to encrypt property values at rest.
     */
    @Override
    protected Properties mergeProperties() throws IOException {

        Properties rslt = null;

        /*
         * If properties file encryption is used in this deployment, the
         * encryption key will be made available to the application as an
         * environment variable called UP_JASYPT_KEY.
         */
        final String encryptionKey = System.getenv(JAYSYPT_ENCRYPTION_KEY_VARIABLE);
        if (encryptionKey != null) {

            logger.info("Jasypt support for encrypted property values ENABLED");

            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword(encryptionKey);
            rslt = new EncryptableProperties(encryptor);

            /*
             * BEGIN copied from PropertiesLoaderSupport.mergeProperties()
             */

            if (this.localOverride) {
                // Load properties from file upfront, to let local properties override.
                loadProperties(rslt);
            }

            if (this.localProperties != null) {
                for (int i = 0; i < this.localProperties.length; i++) {
                    CollectionUtils.mergePropertiesIntoMap(this.localProperties[i], rslt);
                }
            }

            if (!this.localOverride) {
                // Load properties from file afterwards, to let those properties override.
                loadProperties(rslt);
            }

            /*
             * END copied from PropertiesLoaderSupport.mergeProperties()
             */

        } else {

            logger.info(
                    "Jasypt support for encrypted property values DISABLED;  "
                            + "specify environment variable {} to use this feature",
                    JAYSYPT_ENCRYPTION_KEY_VARIABLE);

            /*
             * The feature is not in use;  defer to the Spring-provided
             * implementation of this method.
             */
            return super.mergeProperties();
        }

        return rslt;
    }
}
