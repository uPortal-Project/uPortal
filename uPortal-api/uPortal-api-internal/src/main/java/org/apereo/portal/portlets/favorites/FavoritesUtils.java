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
package org.apereo.portal.portlets.favorites;

import static org.apereo.portal.layout.node.IUserLayoutFolderDescription.FAVORITES_TYPE;
import static org.apereo.portal.layout.node.IUserLayoutFolderDescription.FAVORITE_COLLECTION_TYPE;
import static org.apereo.portal.layout.node.IUserLayoutNodeDescription.LayoutNodeType.FOLDER;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.Validate;
import org.apereo.portal.layout.IUserLayout;
import org.apereo.portal.layout.node.IUserLayoutChannelDescription;
import org.apereo.portal.layout.node.IUserLayoutFolderDescription;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility class supporting Favorites portlet.
 *
 * @since 4.1
 */
@Component("favoritesUtils")
public class FavoritesUtils {

    @Autowired private IPortletDefinitionRegistry portletRegistry;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Get the favorite collections of portlets (i.e. suitable folders ("tabs") in the user layout.)
     * Suitable layout nodes are of type folder with @type attribute favorite_collection.
     *
     * @param userLayout
     * @return non-null List of IUserLayoutDescriptions describing the tabs
     */
    public List<IUserLayoutNodeDescription> getFavoriteCollections(IUserLayout userLayout) {

        if (null == userLayout) {
            throw new IllegalArgumentException(
                    "Cannot get favorites collections from a null userLayout");
        }

        logger.trace("Extracting favorites collections from layout [{}].", userLayout);

        Enumeration<String> nodeIds = userLayout.getChildIds(userLayout.getRootId());

        List<IUserLayoutNodeDescription> results = new LinkedList<IUserLayoutNodeDescription>();

        while (nodeIds.hasMoreElements()) {
            String nodeId = nodeIds.nextElement();

            try {
                IUserLayoutNodeDescription nodeDescription = userLayout.getNodeDescription(nodeId);

                String parentId = userLayout.getParentId(nodeId);
                String nodeName = nodeDescription.getName();
                IUserLayoutNodeDescription.LayoutNodeType nodeType = nodeDescription.getType();

                if (FOLDER.equals(nodeType)
                        && nodeDescription instanceof IUserLayoutFolderDescription) {

                    IUserLayoutFolderDescription folderDescription =
                            (IUserLayoutFolderDescription) nodeDescription;

                    String folderType = folderDescription.getFolderType();

                    if (FAVORITE_COLLECTION_TYPE.equals(folderType)) {

                        results.add(nodeDescription);

                        logger.trace(
                                "Selected node with id [{}] named [{}] with "
                                        + "folderType [{}] and type [{}] as a collection of favorites.",
                                nodeId,
                                nodeName,
                                folderType,
                                nodeType);

                    } else {

                        logger.trace(
                                "Rejected node with id [{}] named [{}] with "
                                        + "folderType [{}] and type [{}] as not a collection of favorites.",
                                nodeId,
                                nodeName,
                                folderType,
                                nodeType);
                    }

                } else {
                    logger.trace(
                            "Rejected non-folder node with id [{}] named [{}] "
                                    + "with parentId [{}] and type [{}] as not a collection of favorites.",
                            nodeId,
                            nodeName,
                            parentId,
                            nodeType);
                }

                // if something goes wrong in processing a node, exclude it
            } catch (Exception e) {
                logger.error(
                        "Error determining whether to include layout node [{}]"
                                + " as a collection of favorites.  Excluding.",
                        nodeId,
                        e);
            }
        }

        logger.debug("Extracted favorites collections [{}] from [{}]", results, userLayout);

        return results;
    }

    public String getFavoriteTabNodeId(IUserLayout userLayout) {

        @SuppressWarnings("unchecked")
        Enumeration<String> childrenOfRoot = userLayout.getChildIds(userLayout.getRootId());

        while (childrenOfRoot
                .hasMoreElements()) { // loop over folders that might be the favorites folder
            String nodeId = childrenOfRoot.nextElement();

            try {

                IUserLayoutNodeDescription nodeDescription = userLayout.getNodeDescription(nodeId);
                IUserLayoutNodeDescription.LayoutNodeType nodeType = nodeDescription.getType();

                if (FOLDER.equals(nodeType)
                        && nodeDescription instanceof IUserLayoutFolderDescription) {
                    IUserLayoutFolderDescription folderDescription =
                            (IUserLayoutFolderDescription) nodeDescription;

                    if (FAVORITES_TYPE.equalsIgnoreCase(folderDescription.getFolderType())) {
                        return folderDescription.getId();
                    }
                }
            } catch (Exception e) {
                logger.error(
                        "Ignoring on error a node while examining for favorites: node ID is [{}]",
                        nodeId,
                        e);
            }
        }

        logger.warn("Favorite tab was searched for but not found");
        return null; // didn't find favorite tab
    }

    /**
     * Get the portlets that are in the folder(s) of type "favorites".
     *
     * @param userLayout
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<IUserLayoutNodeDescription> getFavoritePortletLayoutNodes(IUserLayout userLayout) {

        logger.trace("Extracting favorite portlets from layout [{}]", userLayout);

        List<IUserLayoutNodeDescription> favorites = new LinkedList<>();

        Enumeration<String> childrenOfRoot = userLayout.getChildIds(userLayout.getRootId());

        // loop over folders that might be the favorites folder
        while (childrenOfRoot.hasMoreElements()) {
            String nodeId = childrenOfRoot.nextElement();

            try {

                IUserLayoutNodeDescription nodeDescription = userLayout.getNodeDescription(nodeId);

                if (FOLDER.equals(nodeDescription.getType())
                        && nodeDescription instanceof IUserLayoutFolderDescription) {

                    IUserLayoutFolderDescription folderDescription =
                            (IUserLayoutFolderDescription) nodeDescription;

                    if (FAVORITES_TYPE.equalsIgnoreCase(folderDescription.getFolderType())) {
                        // TODO: assumes columns structure, but should traverse tree to collect all
                        // portlets regardless
                        Enumeration<String> columns = userLayout.getChildIds(nodeId);

                        // loop through columns to gather beloved portlets
                        while (columns.hasMoreElements()) {
                            String column = columns.nextElement();
                            Enumeration<String> portlets = userLayout.getChildIds(column);
                            while (portlets.hasMoreElements()) {
                                String portlet = portlets.nextElement();
                                IUserLayoutNodeDescription portletDescription =
                                        userLayout.getNodeDescription(portlet);
                                favorites.add(portletDescription);
                            }
                        }
                    } else {
                        logger.trace("Ignoring non-favorites folder node [{}]", nodeDescription);
                    }

                } else {
                    logger.trace("Ignoring non-folder node [{}]", nodeDescription);
                }

            } catch (Exception e) {
                logger.error(
                        "Ignoring on error a node while examining for favorites: node ID is [{}]",
                        nodeId,
                        e);
            }
        }

        logger.debug("Extracted favorite portlets [{}] from [{}]", favorites, userLayout);

        return favorites;
    }

    public Set<IPortletDefinition> getFavoritePortletDefinitions(IUserLayout layout) {
        final Set<IPortletDefinition> rslt = new HashSet<>();
        final List<IUserLayoutNodeDescription> favoriteLayoutNodes =
                getFavoritePortletLayoutNodes(layout);
        favoriteLayoutNodes.stream()
                .forEach(
                        node -> {
                            if (IUserLayoutChannelDescription.class.isInstance(node)) {
                                final IUserLayoutChannelDescription chanDef =
                                        (IUserLayoutChannelDescription) node;
                                // Not the most usable API...
                                final IPortletDefinitionId pId =
                                        new IPortletDefinitionId() {
                                            @Override
                                            public long getLongId() {
                                                return Long.valueOf(chanDef.getChannelPublishId());
                                            }

                                            @Override
                                            public String getStringId() {
                                                return chanDef.getChannelPublishId();
                                            }
                                        };
                                final IPortletDefinition pDef =
                                        portletRegistry.getPortletDefinition(pId);
                                if (pDef != null) {
                                    rslt.add(pDef);
                                }
                            }
                        });
        return rslt;
    }

    /**
     * True if the layout contains any favorited collections or favorited individual portlets, false
     * otherwise.
     *
     * @param layout non-null user layout that might contain favorite portlets and/or collections
     * @return true if the layout contains at least one favorited portlet or collection, false
     *     otherwise
     * @throws IllegalArgumentException if layout is null
     */
    public boolean hasAnyFavorites(IUserLayout layout) {
        Validate.notNull(layout, "Cannot determine whether a null layout contains favorites.");

        // (premature) performance optimization: short circuit returns true if nonzero favorite
        // portlets
        return (!getFavoritePortletLayoutNodes(layout).isEmpty()
                || !getFavoriteCollections(layout).isEmpty());
    }

    /**
     * A helper method introduced to filter out duplicates by key
     *
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static List<IUserLayoutNodeDescription> filterFavoritesToUnique(
            List<IUserLayoutNodeDescription> originalFavoritesList) {
        List<IUserLayoutNodeDescription> uniqueFavorites = new ArrayList<>();
        Predicate<IUserLayoutNodeDescription> predicate;
        predicate = FavoritesUtils.distinctByKey(p -> p.getName());
        for (IUserLayoutNodeDescription favorite : originalFavoritesList) {
            if (predicate.test(favorite)) {
                uniqueFavorites.add(favorite);
            }
        }
        return uniqueFavorites;
    }
}
