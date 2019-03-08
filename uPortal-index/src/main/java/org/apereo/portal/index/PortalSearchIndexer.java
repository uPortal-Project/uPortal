package org.apereo.portal.index;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PortalSearchIndexer {

    @Autowired private IPortletDefinitionRegistry portletRegistry;

    @Autowired private Directory directory;

    @Autowired(required = false)
    private Set<ISearchContentExtractor> searchContentExtractors = Collections.emptySet();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        logger.info(
                "Search indexing is {} based on presence or absence of a Directory",
                isEnabled() ? "ENABLED" : "DISABLED");
    }

    public void updateIndex() {

        if (!isEnabled()) {
            return;
        }

        final IndexWriterConfig indexWriterConfig = new IndexWriterConfig(new StandardAnalyzer());
        indexWriterConfig
                .setCommitOnClose(true)
                .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        try (IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig)) {
            final List<IPortletDefinition> portlets = portletRegistry.getAllPortletDefinitions();
            portlets.forEach(portlet -> index(portlet, indexWriter));
        } catch (Exception e) {
            logger.error("Unable to update index", e);
        }
    }

    private boolean isEnabled() {
        /*
         * Use of a Directory class that doesn't extend from FSDirectory is a signal to disable
         * indexing.
         */
        return directory instanceof FSDirectory;
    }

    private void index(IPortletDefinition portlet, IndexWriter indexWriter) {
        final String fname = portlet.getFName(); // Unique identifier
        try {
            final Document doc = new Document();
            doc.add(new TextField("fname", fname, Field.Store.YES));
            doc.add(new TextField("title", portlet.getTitle(), Field.Store.YES));
            final IPortletDefinitionParameter keywords = portlet.getParameter("keywords");
            if (keywords != null) {
                doc.add(new TextField("keywords", keywords.getValue(), Field.Store.YES));
            }
            final String content = extractContent(portlet);
            if (content != null) {
                doc.add(new TextField("content", content, Field.Store.YES));
            }
            indexWriter.updateDocument(new Term("fname", fname), doc);
        } catch (IOException ioe) {
            logger.warn("Unable to index portlet with fname='{}'", fname);
            return;
        }
        logger.debug("Indexed portlet '{}'", fname);
    }

    private String extractContent(IPortletDefinition portlet) {

        final ISearchContentExtractor contentExtractor =
                searchContentExtractors.stream()
                        .filter(extractor -> extractor.appliesTo(portlet))
                        .findFirst()
                        .orElse(null);

        if (contentExtractor == null) {
            logger.debug(
                    "No ISearchContentExtractor bean found for portlet with fname='{}';  "
                            + "content will not be indexed",
                    portlet.getFName());
            return null;
        }

        final String rslt = contentExtractor.extractContent(portlet);
        logger.debug(
                "Extracted the following content for portlet with fname='{}'\n{}",
                portlet.getFName(),
                rslt);

        return rslt;
    }
}
