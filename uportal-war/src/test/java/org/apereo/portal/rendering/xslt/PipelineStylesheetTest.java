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
package org.apereo.portal.rendering.xslt;

import org.apereo.portal.utils.cache.resource.TemplatesBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 */
public class PipelineStylesheetTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private ResourceLoader resourceLoader;
    private TemplatesBuilder templatesBuilder;

    @Before
    public void setup() throws Exception {
        this.resourceLoader = new ClassRelativeResourceLoader(getClass());

        templatesBuilder = new TemplatesBuilder();
        templatesBuilder.setResourceLoader(this.resourceLoader);
    }

    @Test
    public void testStructureColumnsCompile() throws Exception {
        this.testXslCompile("/layout/structure/columns/columns.xsl");
    }

    @Test
    public void testThemeRespondrCompile() throws Exception {
        this.testXslCompile("/layout/theme/respondr/respondr.xsl");
    }

    @Test
    public void testThemeJsonCompile() throws Exception {
        this.testXslCompile("/layout/theme/json/json.xsl");
    }

    @Test
    public void testThemeJson43Compile() throws Exception {
        this.testXslCompile("/layout/theme/json/json-v4-3.xsl");
    }

    private void testXslCompile(String file) throws Exception {
        final Resource resource = new ClassPathResource(file);
        templatesBuilder.loadResource(resource);
    }
}
