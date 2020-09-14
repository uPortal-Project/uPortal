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

import com.google.common.hash.Hashing;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
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

    public static final String LUCENE_DOC_ID_FIELD = "id";

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
        if (isEnabled()) {
            updateIndex();
        }
    }

    /** Called by Quatrz. */
    public void updateIndex() {
        logger.debug("Updating Lucene index files ...");

        if (!isEnabled()) {
            return;
        }

        final IndexWriterConfig indexWriterConfig = new IndexWriterConfig(new StandardAnalyzer());
        indexWriterConfig
                .setCommitOnClose(true)
                .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        try (IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig)) {
            final List<IPortletDefinition> portlets = portletRegistry.getAllPortletDefinitions();
            portlets.forEach(portlet -> indexPortlet(portlet, indexWriter));
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

    private void indexPortlet(IPortletDefinition portlet, IndexWriter indexWriter) {
        // Unique identifier, hashed to eliminate special character concerns, such as hyphens
        final String fnameHash =
                Hashing.sha256().hashString(portlet.getFName(), StandardCharsets.UTF_8).toString();
        try {
            final Document doc = new Document();
            doc.add(new TextField(LUCENE_DOC_ID_FIELD, fnameHash, Field.Store.YES));
            doc.add(
                    new TextField(
                            SearchField.FNAME.getValue(), portlet.getFName(), Field.Store.YES));
            doc.add(new TextField(SearchField.NAME.getValue(), portlet.getName(), Field.Store.YES));
            doc.add(
                    new TextField(
                            SearchField.TITLE.getValue(), portlet.getTitle(), Field.Store.YES));
            final String description = portlet.getDescription();
            if (StringUtils.isNotBlank(description)) {
                doc.add(
                        new TextField(
                                SearchField.DESCRIPTION.getValue(), description, Field.Store.YES));
            }
            final IPortletDefinitionParameter keywords = portlet.getParameter("keywords");
            if (keywords != null && StringUtils.isNotBlank(keywords.getValue())) {
                doc.add(
                        new TextField(
                                SearchField.KEYWORDS.getValue(),
                                keywords.getValue(),
                                Field.Store.YES));
            }
            final String content = extractContent(portlet);
            if (StringUtils.isNotBlank(content)) {
                doc.add(new TextField(SearchField.CONTENT.getValue(), content, Field.Store.YES));
            }

            indexWriter.updateDocument(new Term(LUCENE_DOC_ID_FIELD, fnameHash), doc);
        } catch (IOException ioe) {
            logger.warn(
                    "Unable to index portlet with fname='{}' and hash='{}'",
                    portlet.getFName(),
                    fnameHash);
            return;
        }
        logger.debug("Indexed portlet '{}' (hash='{}')", portlet.getFName(), fnameHash);
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
