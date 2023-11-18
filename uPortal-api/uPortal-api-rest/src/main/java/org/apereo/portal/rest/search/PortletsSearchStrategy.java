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
package org.apereo.portal.rest.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apereo.portal.UserPreferencesManager;
import org.apereo.portal.index.PortalSearchIndexer;
import org.apereo.portal.index.SearchField;
import org.apereo.portal.layout.IUserLayout;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portlets.favorites.FavoritesUtils;
import org.apereo.portal.portlets.search.PortletRegistryUtil;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provides search results to the {@link SearchRESTController} that are portlets.
 *
 * @since 5.0
 */
@Component
public class PortletsSearchStrategy implements ISearchStrategy {

    private static final String RESULT_TYPE_NAME = "portlets";

    private MultiFieldQueryParser queryParser;

    private boolean displayScore;

    private boolean displayFavoriteFlag;

    @Autowired private Directory directory;

    @Autowired private IPortletDefinitionRegistry portletDefinitionRegistry;

    @Autowired private PortletRegistryUtil portletRegistryUtil;

    @Autowired private IUserInstanceManager userInstanceManager;

    @Autowired private FavoritesUtils favoritesUtils;

    /** Set if the score should be provided in the search results */
    @Value("${org.apereo.portal.rest.search.PortletsSearchStrategy.displayScore:true}")
    public void setDisplayScore(boolean displayScore) {
        this.displayScore = displayScore;
    }

    /** Set if the favorite flag should be provided in the search results */
    @Value("${org.apereo.portal.rest.search.PortletsSearchStrategy.displayFavoriteFlag:true}")
    public void setDisplayFavoriteFlag(boolean b) {
        this.displayFavoriteFlag = b;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        final String[] fields =
                Arrays.stream(SearchField.values())
                        .map(SearchField::getValue)
                        .toArray(String[]::new);
        queryParser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
    }

    @Override
    public String getResultTypeName() {
        return RESULT_TYPE_NAME;
    }

    @Override
    public List<?> search(String query, HttpServletRequest request) {
        logger.debug("Entering search() with query={}", query);
        final List<Object> result = new ArrayList<>();
        final Set<IPortletDefinition> favorites = getFavorites(request);

        try (IndexReader indexReader = DirectoryReader.open(directory)) {
            final String queryString = query.endsWith(" ") ? query : query + "*";
            final Query q = queryParser.parse(queryString);
            final IndexSearcher searcher = new IndexSearcher(indexReader);
            final TopDocs topDocs = searcher.search(q, 50);
            Arrays.stream(topDocs.scoreDocs)
                    .forEach(
                            scoreDoc -> {
                                try {
                                    final Document document = searcher.doc(scoreDoc.doc);
                                    final IPortletDefinition portlet =
                                            portletDefinitionRegistry.getPortletDefinitionByFname(
                                                    document.get(SearchField.FNAME.getValue()));
                                    final String scoreStr = Float.toString(scoreDoc.score);
                                    final String hashKey =
                                            document.getField(
                                                            PortalSearchIndexer.LUCENE_DOC_ID_FIELD)
                                                    .stringValue();
                                    logger.debug(
                                            "Search query '{}' matches portlet: '{}', score='{}', hashId='{}'",
                                            query,
                                            portlet,
                                            scoreStr,
                                            hashKey);
                                    /* requester permissions checked in buildPortletUrl() */
                                    final String url =
                                            portletRegistryUtil.buildPortletUrl(request, portlet);
                                    if (url != null) {
                                        logger.debug(
                                                "Adding portlet with fname='{}', score='{}', hash='{}' to search results for query='{}'",
                                                portlet.getFName(),
                                                scoreStr,
                                                hashKey,
                                                query);
                                        result.add(
                                                getPortletAttrs(portlet, url, scoreStr, favorites));
                                    }
                                } catch (IOException e) {
                                    // Log a warning, but don't prevent other matches from
                                    // succeeding...
                                    logger.warn(
                                            "Failed to process the following search result for query='{}': {}",
                                            query,
                                            scoreDoc,
                                            e);
                                }
                            });

        } catch (Exception e) {
            // Log a warning, but don't prevent other search strategies from succeeding...
            logger.warn("Failed to search portal content for query='{}'", query, e);
        }

        return result;
    }

    private Map<String, Object> getPortletAttrs(
            IPortletDefinition portlet,
            String url,
            String score,
            Set<IPortletDefinition> favorites) {
        final Map<String, Object> result = new TreeMap<>();
        result.put("name", portlet.getName());
        result.put("fname", portlet.getFName());
        result.put("title", portlet.getTitle());
        result.put("description", portlet.getDescription());
        result.put("url", url);
        if (displayFavoriteFlag) {
            result.put("favorite", favorites.contains(portlet) ? true : false);
        }
        if (displayScore) {
            result.put("score", score);
        }
        return result;
    }

    private Set<IPortletDefinition> getFavorites(HttpServletRequest request) {
        if (displayFavoriteFlag) {
            final IUserInstance ui = userInstanceManager.getUserInstance(request);
            final UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
            final IUserLayoutManager ulm = upm.getUserLayoutManager();
            final IUserLayout layout = ulm.getUserLayout();
            final Set<IPortletDefinition> result =
                    displayFavoriteFlag
                            ? favoritesUtils.getFavoritePortletDefinitions(layout)
                            : Collections.emptySet();
            return result;
        } else {
            return Collections.emptySet();
        }
    }
}
