package org.apereo.portal.index;

import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Paths;

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
        final String realPath = servletContext != null
                ? servletContext.getRealPath(relativePath)
                : null;
        return realPath != null
                ? FSDirectory.open(Paths.get(realPath))
                : new ByteBuffersDirectory(); // Disables indexing
    }

    @Bean
    public ISearchContentExtractor simpleContentPortletSearchContentExtractor() {
        return new HtmlPortletPreferenceSearchContentExtractor("cms", "/SimpleContentPortlet", "content");
    }

}
