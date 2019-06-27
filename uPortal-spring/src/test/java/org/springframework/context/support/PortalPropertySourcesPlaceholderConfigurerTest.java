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

import static org.junit.Assert.assertEquals;

import java.util.Properties;
import org.junit.Before;
import org.junit.Test;

public class PortalPropertySourcesPlaceholderConfigurerTest {

    PortalPropertySourcesPlaceholderConfigurer propConfig =
            new PortalPropertySourcesPlaceholderConfigurer();
    Properties props = new Properties();

    @Before
    public void init() {
        props.clear();
        props.setProperty("dev.url", "https://github.com/");
        props.setProperty("dev.user", "bjagg");
        props.setProperty("doc.url", "https://github.io/");
        props.setProperty("doc.user", "jhelwig");
        props.setProperty("user", "jwick");
    }

    @Test
    public void testNoEnvVar() {
        propConfig.honorClusterOverrides("", props);
        assertEquals("props has same size", 5, props.size());
        assertEquals(
                "dev.url is still https://github.com/",
                "https://github.com/",
                props.getProperty("dev.url"));
        assertEquals(
                "dev.user is still https://github.com/", "bjagg", props.getProperty("dev.user"));
        assertEquals(
                "doc.url is still https://github.com/",
                "https://github.io/",
                props.getProperty("doc.url"));
        assertEquals(
                "doc.user is still https://github.com/", "jhelwig", props.getProperty("doc.user"));
        assertEquals("user is still https://github.com/", "jwick", props.getProperty("user"));
    }

    @Test
    public void testDev() {
        propConfig.honorClusterOverrides("dev", props);
        assertEquals("props has same size", 6, props.size());
        assertEquals(
                "dev.url is still https://github.com/",
                "https://github.com/",
                props.getProperty("dev.url"));
        assertEquals(
                "dev.user is still https://github.com/", "bjagg", props.getProperty("dev.user"));
        assertEquals(
                "doc.url is still https://github.com/",
                "https://github.io/",
                props.getProperty("doc.url"));
        assertEquals(
                "doc.user is still https://github.com/", "jhelwig", props.getProperty("doc.user"));
        assertEquals(
                "url is now https://github.com/", "https://github.com/", props.getProperty("url"));
        assertEquals("user is now https://github.com/", "bjagg", props.getProperty("user"));
    }

    @Test
    public void testDoc() {
        propConfig.honorClusterOverrides("doc", props);
        assertEquals("props has same size", 6, props.size());
        assertEquals(
                "dev.url is still https://github.com/",
                "https://github.com/",
                props.getProperty("dev.url"));
        assertEquals(
                "dev.user is still https://github.com/", "bjagg", props.getProperty("dev.user"));
        assertEquals(
                "doc.url is still https://github.com/",
                "https://github.io/",
                props.getProperty("doc.url"));
        assertEquals(
                "doc.user is still https://github.com/", "jhelwig", props.getProperty("doc.user"));
        assertEquals(
                "url is now https://github.com/", "https://github.io/", props.getProperty("url"));
        assertEquals("user is now https://github.com/", "jhelwig", props.getProperty("user"));
    }
}
