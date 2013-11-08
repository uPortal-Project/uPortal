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
package org.jasig.portal.properties;

import org.hamcrest.Matcher;
import org.jasig.portal.spring.context.support.QueryablePropertySourcesPlaceholderConfigurer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import static org.jasig.portal.spring.context.support.QueryablePropertySourcesPlaceholderConfigurer.UnresolvablePlaceholderStrategy;

/**
 * Integration tests for Spring-configured instances of {@link QueryablePropertySourcesPlaceholderConfigurer}
 * and closely related classes, especially {@link PropertiesManager} and {@link PropertiesManagerInitializer}.
 */
public class QueryablePropertySourcesPlaceholderConfigurerTest {

    @Before
    public void setUp() {
        assertNull("These tests assume SSP_CONFIGDIR hasn't been set in the OS env.", System.getenv("SSP_CONFIGDIR"));
        System.clearProperty("SSP_CONFIGDIR");
    }

    @After
    public void tearDown() {
        System.clearProperty("SSP_CONFIGDIR");
    }

    @Test
    public void doesntRequireExternalConfigOverrideDirectory() {

        final ClassPathXmlApplicationContext context =
                contextFor(configContextBeansPlus("classpath:org/jasig/portal/properties/beansWithDefaultedPropertyPlaceholdersContext.xml"));

        // We're really just trying to make sure the app context init doesn't blow up, but we'll also
        // check to make sure property placeholders are left alone like we'd expect
        final Map configReferences = context.getBean("configReferences", Map.class);
        assertEquals("Default property value was not preserved", "testing.default.value.1",
                configReferences.get("exists.only.for.testing.1"));
    }

    @Test
    public void doesntRequireExternalConfigOverrideFile() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        assertTrue("Temp dir at [" + tempDir.getAbsolutePath() + "] doesn't exist", tempDir.exists());
        assertTrue("Temp dir at [" + tempDir.getAbsolutePath() + "] isn't a dir", tempDir.isDirectory());
        assertTrue("Temp dir at [" + tempDir.getAbsolutePath() + "] can't be read", tempDir.canRead());
        assertEquals("Temp dir at [" + tempDir.getAbsolutePath() + "] had a file named [ssp-platform-config.properties]",
                0, tempDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return "ssp-platform-config.properties".equals(name);
            }
        }).length);

        final ClassPathXmlApplicationContext context =
                contextFor(configContextBeansPlus("classpath:org/jasig/portal/properties/beansWithDefaultedPropertyPlaceholdersContext.xml"),
                        tempDir);

        // We're really just trying to make sure the app context init doesn't blow up, but we'll also
        // check to make sure property placeholders are left alone like we'd expect
        final Map configReferences = context.getBean("configReferences", Map.class);
        assertEquals("Default property value was not preserved", "testing.default.value.1",
                configReferences.get("exists.only.for.testing.1"));
    }

    @Test
    public void overridesDefaultsWithExternalConfig() throws URISyntaxException {
        final ClassPathXmlApplicationContext context =
                contextFor(configContextBeansPlus("classpath:org/jasig/portal/properties/beansWithAllDefinedPropertyPlaceholdersContext.xml"),
                    standardPropertiesOverrideDir());

        final Map configReferences = context.getBean("configReferences", Map.class);
        assertEquals("Default property value was not preserved", "testing.override.value.1",
                configReferences.get("exists.only.for.testing.1"));
    }

    @Test
    public void resolvesPlaceholdersRecursivelyAcrossFiles() throws URISyntaxException {
        final ClassPathXmlApplicationContext context =
                contextFor(configContextBeansPlus("classpath:org/jasig/portal/properties/beansWithAllDefinedPropertyPlaceholdersContext.xml"),
                        standardPropertiesOverrideDir());

        final Map configReferences = context.getBean("configReferences", Map.class);
        // 8 points to 3 points to 2 (and 2 is in a different file)
        assertEquals("Recursive property placeholders not resolved",
                "testing.override.testing.override.testing.default.value.2.value.3.value.8",
                configReferences.get("exists.only.for.testing.8"));
    }


    /** May seem silly but guards against a bug found during development. */
    @Test
    public void resolvesPrefixedPlaceholders() throws URISyntaxException {
        final ClassPathXmlApplicationContext context =
                contextFor(configContextBeansPlus("classpath:org/jasig/portal/properties/beansWithAllDefinedPropertyPlaceholdersContext.xml"),
                        standardPropertiesOverrideDir());

        final Map configReferences = context.getBean("configReferences", Map.class);
        // 8 points to 3 points to 2 (and 2 is in a different file)
        assertEquals("Recursive property placeholders not resolved",
                "testing.override.value.1.testing.override.value.4",
                configReferences.get("exists.only.for.testing.4"));
    }

    /** May seem silly but guards against a bug found during development. */
    @Test
    public void resolvesSuffixedPlaceholders() throws URISyntaxException {
        final ClassPathXmlApplicationContext context =
                contextFor(configContextBeansPlus("classpath:org/jasig/portal/properties/beansWithAllDefinedPropertyPlaceholdersContext.xml"),
                        standardPropertiesOverrideDir());

        final Map configReferences = context.getBean("configReferences", Map.class);
        // 8 points to 3 points to 2 (and 2 is in a different file)
        assertEquals("Recursive property placeholders not resolved",
                "testing.override.value.5.testing.override.value.1",
                configReferences.get("exists.only.for.testing.5"));
    }

    /**
     * The method that this tests -
     * {@link QueryablePropertySourcesPlaceholderConfigurer#resolve(String, org.jasig.portal.spring.context.support.QueryablePropertySourcesPlaceholderConfigurer.UnresolvablePlaceholderStrategy)}
     * - is actually the key difference between {@link QueryablePropertySourcesPlaceholderConfigurer}
     * and the OOTB {@link PropertySourcesPlaceholderConfigurer}.
     */
    @Test
    public void supportsArbitraryPropertyResolutionAfterProcessingBeanFactory() throws URISyntaxException {
        final ClassPathXmlApplicationContext context =
                contextFor(configContextBeansPlus("classpath:org/jasig/portal/properties/beansWithAllDefinedPropertyPlaceholdersContext.xml"),
                        standardPropertiesOverrideDir());

        final QueryablePropertySourcesPlaceholderConfigurer configurer =
                context.getBean("primaryPropertyPlaceholderConfigurer",
                        QueryablePropertySourcesPlaceholderConfigurer.class);

        // Note the ${...} wrapping. QueryablePropertySourcesPlaceholderConfigurer is a BeanFactoryPostProcessor,
        // so the getPropertyValue() we invented for it accepts the same sort of property placeholder strings
        // you'd expect to find in Spring Bean definitions. I.e. it's not some kind of drop-in replacement for
        // PropertyManager nor the 'configReferences' Map Bean used elsewhere in this test class. In fact,
        // if you look at the PropertyManager impl, you'll notice it's passing portal.properties *values*
        // not property names to getPropertyValue().
        assertEquals("Recursive property placeholders not resolved",
                "foo.testing.override.testing.override.testing.default.value.2.value.3.value.8",
                configurer.resolve("foo.${exists.only.for.testing.8}", UnresolvablePlaceholderStrategy.ERROR));
    }


    @Test
    public void canErrorOutOnArbitraryLookupOfUnresolvablePlaceholders() throws URISyntaxException {
        final ClassPathXmlApplicationContext context =
                contextFor(configContextBeansPlus("classpath:org/jasig/portal/properties/beansWithAllDefinedPropertyPlaceholdersContext.xml"),
                        standardPropertiesOverrideDir());

        final QueryablePropertySourcesPlaceholderConfigurer configurer =
                context.getBean("primaryPropertyPlaceholderConfigurer",
                        QueryablePropertySourcesPlaceholderConfigurer.class);

        try {
            configurer.resolve("${doesnt.exist}", UnresolvablePlaceholderStrategy.ERROR);
            fail("should have thrown an IllegalArgumentException");
        } catch ( IllegalArgumentException e ) {
            assertThat(e.getMessage(),
                    containsString("Could not resolve placeholder 'doesnt.exist' in string value [${doesnt.exist}]"));
        }
    }

    @Test
    public void canEchoOriginalValueOnArbitraryLookupOfUnresolvablePlaceholders() throws URISyntaxException {
        final ClassPathXmlApplicationContext context =
                contextFor(configContextBeansPlus("classpath:org/jasig/portal/properties/beansWithAllDefinedPropertyPlaceholdersContext.xml"),
                        standardPropertiesOverrideDir());

        final QueryablePropertySourcesPlaceholderConfigurer configurer =
                context.getBean("primaryPropertyPlaceholderConfigurer",
                        QueryablePropertySourcesPlaceholderConfigurer.class);

        assertEquals("${doesnt.exist}", configurer.resolve("${doesnt.exist}", UnresolvablePlaceholderStrategy.IGNORE));
    }

    @Test
    public void canDeferToConfigurerBehaviorOnArbitraryLookupOfUnresolvablePlaceholders() throws URISyntaxException {
        final ClassPathXmlApplicationContext context =
                contextFor(configContextBeansPlus("classpath:org/jasig/portal/properties/beansWithAllDefinedPropertyPlaceholdersContext.xml"),
                        standardPropertiesOverrideDir());

        final QueryablePropertySourcesPlaceholderConfigurer configurer =
                context.getBean("primaryPropertyPlaceholderConfigurer",
                        QueryablePropertySourcesPlaceholderConfigurer.class);


        try {
            // we happen to to know the configurer is set up to error out
            configurer.resolve("${doesnt.exist}", UnresolvablePlaceholderStrategy.DEFER);
            fail("should have thrown an IllegalArgumentException");
        } catch ( IllegalArgumentException e ) {
            assertThat(e.getMessage(),
                    containsString("Could not resolve placeholder 'doesnt.exist' in string value [${doesnt.exist}]"));
        }
    }

    @Test
    public void failsApplicationContextStartupWhenPlaceholdersDontResolve() throws URISyntaxException {
        try {
            ClassPathXmlApplicationContext context =
                contextFor(configContextBeansPlus("classpath:org/jasig/portal/properties/beansWithInvalidPropertyPlaceholdersContext.xml"),
                        standardPropertiesOverrideDir());
            fail("Should have thrown a BeanDefinitionStoreException");
        } catch ( BeanDefinitionStoreException e ) {
            assertThat(e.getMessage(), containsString("Could not resolve placeholder 'doesnt.exist' in string value [${doesnt.exist}]"));
        }
    }

    @Test
    public void propertyManagerRespectsConfigOverrides() throws URISyntaxException {
        final ClassPathXmlApplicationContext context =
                contextFor(configContextBeansPlus("classpath:org/jasig/portal/properties/propertiesManagerInitializerContext.xml"),
                        standardPropertiesOverrideDir());

        final String resolved = PropertiesManager.getProperty("org.jasig.portal.email.fromAddress");
        assertEquals("override.from@test.org", resolved);
    }

    /**
     * {@link #propertyManagerRespectsConfigOverrides()} would have also failed if this doesn't work, but
     * here we also want to verify that the unresolvable placeholders are reflected in subsequent property
     * lookups.
     */
    @Test
    public void propertyManagerInitializesSuccessfullyDespiteUnresolvedPlaceholders() throws URISyntaxException {
        final ClassPathXmlApplicationContext context =
                contextFor(configContextBeansPlus("classpath:org/jasig/portal/properties/propertiesManagerInitializerContext.xml"),
                        standardPropertiesOverrideDir());

        final String resolved = PropertiesManager.getProperty("org.jasig.portal.exists.for.testing.1");
        assertEquals("com.sun.net.ssl.internal.www.protocol.${does.not.exist}", resolved);
    }

    private ClassPathXmlApplicationContext contextFor() {
        return contextFor(null, null);
    }

    private ClassPathXmlApplicationContext contextFor(File propertiesDir) {
        return contextFor(null, propertiesDir);
    }

    private ClassPathXmlApplicationContext contextFor(String[] beanResources) {
        return contextFor(beanResources, null);
    }

    private ClassPathXmlApplicationContext contextFor(String[] beanResources, File propertiesDir) {
        setPropertiesDir(propertiesDir);
        return loadContext(beanResources);
    }

    private void setPropertiesDir(File overrideDir) {
        if ( overrideDir != null ) {
            System.setProperty("SSP_CONFIGDIR", overrideDir.getAbsolutePath());
        }
    }

    private ClassPathXmlApplicationContext loadContext(String[] beanResources) {
        return new ClassPathXmlApplicationContext(beanResources);
    }

    private File standardPropertiesOverrideDir() throws URISyntaxException {
        return parentFileFromResourcePath("org/jasig/portal/properties/ssp-platform-config.properties");
    }

    private String[] configContextBeansPlus(String... otherBeans) {
        String[] beanResources = new String[otherBeans == null ? 1 : (otherBeans.length + 1)];
        beanResources[0] = "classpath:properties/contexts/configContext.xml";
        System.arraycopy(otherBeans, 0, beanResources, 1, otherBeans.length);
        return beanResources;
    }

    private File parentFileFromResourcePath(String path) throws URISyntaxException {
        final URI fileUri = getClass().getClassLoader().getResource(path).toURI();
        return new File(fileUri).getParentFile();
    }

}
