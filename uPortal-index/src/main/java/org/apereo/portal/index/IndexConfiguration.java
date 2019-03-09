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
package org.apereo.portal.index;

import java.io.IOException;
import java.nio.file.Paths;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexConfiguration {

    @Value("${org.apereo.portal.index.relativePath:/WEB-INF/index}")
    private String relativePath;

    @Autowired(required = false)
    private ServletContext servletContext;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        logger.info("Search indexing using relativePath='{}'", relativePath);
    }

    @Bean
    public Directory directory() throws IOException {
        final String realPath =
                servletContext != null ? servletContext.getRealPath(relativePath) : null;
        return realPath != null
                ? FSDirectory.open(Paths.get(realPath))
                : new ByteBuffersDirectory(); // Disables indexing
    }

    @Bean
    public ISearchContentExtractor simpleContentPortletSearchContentExtractor() {
        return new HtmlPortletPreferenceSearchContentExtractor(
                "cms", "/SimpleContentPortlet", "content");
    }
}
