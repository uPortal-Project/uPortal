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
package org.apereo.portal.layout.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An interface describing a folder user layout node.
 *
 */
public interface IUserLayoutFolderDescription extends IUserLayoutNodeDescription {

    /**
     * Type attribute value of regular layout folders (i.e., tabs displayed normally, not headers,
     * footers, emergency fragments, tip fragments, favorites-containing meta-fragments, etc.
     *
     * @since 4.1
     */
    public static final String REGULAR_TYPE = "regular";

    /**
     * Type attribute value of folders containing user favorites (used by the optional Favorites
     * portlet).
     *
     * @since 4.1
     */
    public static final String FAVORITES_TYPE = "favorites";

    /**
     * Type attribute value of folders representing a named collection of user favorites (used by
     * the optional Favorites portlet).
     *
     * @since 4.1
     */
    public static final String FAVORITE_COLLECTION_TYPE = "favorite_collection";

    /**
     * Returns folder type. Type might be one of the values documented in this interface. Then
     * again, it might be any other String.
     *
     * @since 4.1
     * @return a non-null String representing folder type.
     */
    public String getFolderType();

    /**
     * Assign a type to a folder. Some useful types are documented in this interface.
     *
     * @since 4.1
     * @param folderType String corresponding to underlying 'type' attribute
     */
    public void setFolderType(String folderType);

    /**
     * Creates a <code>org.w3c.dom.Element</code> representation of the current node.
     *
     * @param root a <code>Document</code> for which the <code>Element</code> should be created.
     * @return a <code>Node</code> value
     */
    public Element getXML(Document root);
}
