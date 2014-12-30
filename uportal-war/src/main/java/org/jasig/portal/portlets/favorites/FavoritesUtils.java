/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlets.favorites;

import org.apache.commons.lang3.Validate;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import static org.jasig.portal.layout.node.IUserLayoutNodeDescription.LayoutNodeType.FOLDER;
import static org.jasig.portal.layout.node.IUserLayoutFolderDescription.FAVORITES_TYPE;
import static org.jasig.portal.layout.node.IUserLayoutFolderDescription.FAVORITE_COLLECTION_TYPE;

/**
 * Utility class supporting Favorites portlet.
 * @since uPortal 4.1
 */
public final class FavoritesUtils {

    protected static Logger logger = LoggerFactory.getLogger(FavoritesUtils.class);


    /**
     * Get the favorite collections of portlets (i.e. suitable folders ("tabs") in the user layout.)
     * Suitable layout nodes are of type folder with @type attribute favorite_collection.
     *
     * @param userLayout
     * @return non-null List of IUserLayoutDescriptions describing the tabs
     */
    public static List<IUserLayoutNodeDescription> getFavoriteCollections(IUserLayout userLayout) {

        if (null == userLayout) {
            throw new IllegalArgumentException("Cannot get favorites collections from a null userLayout");
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

                    IUserLayoutFolderDescription folderDescription = (IUserLayoutFolderDescription) nodeDescription;

                    String folderType = folderDescription.getFolderType();

                    if (FAVORITE_COLLECTION_TYPE.equals(folderType)) {

                        results.add(nodeDescription);

                        logger.trace("Selected node with id [{}] named [{}] with " +
                                "folderType [{}] and type [{}] as a collection of favorites.",
                                nodeId, nodeName, folderType, nodeType);

                    } else {

                        logger.trace("Rejected node with id [{}] named [{}] with " +
                                "folderType [{}] and type [{}] as not a collection of favorites.",
                                nodeId, nodeName, folderType, nodeType);

                    }


                } else {
                    logger.trace("Rejected non-folder node with id [{}] named [{}] " +
                            "with parentId [{}] and type [{}] as not a collection of favorites.",
                            nodeId, nodeName, parentId, nodeType);
                }

                // if something goes wrong in processing a node, exclude it
            } catch (Exception e) {
                logger.error("Error determining whether to include layout node [{}]" +
                        " as a collection of favorites.  Excluding.",
                        nodeId, e);
            }
        }

        logger.debug("Extracted favorites collections [{}] from [{}]", results, userLayout);

        return results;
    }
    
    public static String getFavoriteTabNodeId(IUserLayout userLayout) {

        @SuppressWarnings("unchecked")
        Enumeration<String> childrenOfRoot = userLayout.getChildIds(userLayout.getRootId());
        
        while (childrenOfRoot.hasMoreElements()) { //loop over folders that might be the favorites folder
            String nodeId = childrenOfRoot.nextElement();

            try {

                IUserLayoutNodeDescription nodeDescription = userLayout.getNodeDescription(nodeId);
                IUserLayoutNodeDescription.LayoutNodeType nodeType = nodeDescription.getType();

                if (FOLDER.equals(nodeType)
                        && nodeDescription instanceof IUserLayoutFolderDescription) {
                    IUserLayoutFolderDescription folderDescription = (IUserLayoutFolderDescription) nodeDescription;

                    if (FAVORITES_TYPE.equalsIgnoreCase(folderDescription.getFolderType())) {
                        return folderDescription.getId();
                    }
                }
            } catch (Exception e) {
                logger.error("Ignoring on error a node while examining for favorites: node ID is [{}]", nodeId, e);
            }
        }

        logger.warn("Favorite tab was searched for but not found");
        return null; //didn't find favorite tab
    }

    /**
     * Get the portlets that are in the folder(s) of type "favorites".
     * @param userLayout
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<IUserLayoutNodeDescription> getFavoritePortlets(IUserLayout userLayout) {

        logger.trace("Extracting favorite portlets from layout [{}]", userLayout);


        List<IUserLayoutNodeDescription> favorites = new LinkedList<IUserLayoutNodeDescription>();

        Enumeration<String> childrenOfRoot = userLayout.getChildIds(userLayout.getRootId());

        while (childrenOfRoot.hasMoreElements()) { //loop over folders that might be the favorites folder
            String nodeId = childrenOfRoot.nextElement();

            try {

                IUserLayoutNodeDescription nodeDescription = userLayout.getNodeDescription(nodeId);

                String parentId = userLayout.getParentId(nodeId);
                String nodeName = nodeDescription.getName();
                IUserLayoutNodeDescription.LayoutNodeType nodeType = nodeDescription.getType();

                if (FOLDER.equals(nodeDescription.getType())
                        && nodeDescription instanceof IUserLayoutFolderDescription) {

                    IUserLayoutFolderDescription folderDescription = (IUserLayoutFolderDescription) nodeDescription;

                    if (FAVORITES_TYPE.equalsIgnoreCase(folderDescription.getFolderType())) {
                        // TODO: assumes columns structure, but should traverse tree to collect all portlets regardless
                        Enumeration<String> columns = userLayout.getChildIds(nodeId);

                        //loop through columns to gather beloved portlets
                        while (columns.hasMoreElements()) {
                            String column = (String) columns.nextElement();
                            Enumeration<String> portlets = userLayout.getChildIds(column);
                            while (portlets.hasMoreElements()) {
                                String portlet = (String) portlets.nextElement();
                                IUserLayoutNodeDescription portletDescription = userLayout.getNodeDescription(portlet);
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
                logger.error("Ignoring on error a node while examining for favorites: node ID is [{}]", nodeId, e);
            }
        }

        logger.debug("Extracted favorite portlets [{}] from [{}]", favorites, userLayout);

        return favorites;
    }


    /**
     * True if the layout contains any favorited collections or favorited individual portlets, false otherwise.
     * @param layout non-null user layout that might contain favorite portlets and/or collections
     * @return true if the layout contains at least one favorited portlet or collection, false otherwise
     * @throws IllegalArgumentException if layout is null
     */
    public static boolean hasAnyFavorites(IUserLayout layout) {
        Validate.notNull(layout, "Cannot determine whether a null layout contains favorites.");

        // (premature) performance optimization: short circuit returns true if nonzero favorite portlets
        return (!getFavoritePortlets(layout).isEmpty() || !getFavoriteCollections(layout).isEmpty() );

    }
}
