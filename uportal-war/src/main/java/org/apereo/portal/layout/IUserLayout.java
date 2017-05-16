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
package org.apereo.portal.layout;

import java.util.Enumeration;
import java.util.Set;
import javax.xml.xpath.XPathExpression;
import org.apereo.portal.PortalException;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.apereo.portal.layout.om.IStylesheetUserPreferences;

/**
 * An interface representing the user layout.
 *
 */
public interface IUserLayout {
    /**
     * The name to use for the root node of the layout. This should be used with regard to rendering
     * position within the layout tree.
     */
    static final String ROOT_NODE_NAME = "root";

    /**
     * Obtain a description of a node (channel or a folder) in a given user layout.
     *
     * @param nodeId a <code>String</code> channel subscribe id or folder id.
     * @return an <code>UserLayoutNodeDescription</code> value
     * @exception PortalException if an error occurs
     */
    IUserLayoutNodeDescription getNodeDescription(String nodeId) throws PortalException;

    /**
     * Returns an Id of a parent user layout node. The user layout root node always has ID="root"
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> value
     * @exception PortalException if an error occurs
     */
    String getParentId(String nodeId) throws PortalException;

    /**
     * Returns a list of child node Ids for a given node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>Enumeration</code> of <code>String</code> child node Ids.
     * @exception PortalException if an error occurs
     */
    Enumeration getChildIds(String nodeId) throws PortalException;

    /**
     * Returns a layout Id associated with this manager/
     *
     * @return an <code>String</code> layout Id value;
     */
    String getId();

    /**
     * Returns a node id as resolved by the supplied {@link XPathExpression}
     *
     * @param xpathExpression The expression to execute against the layout DOM
     * @return The ID of the resolved node, null if there is no match
     */
    String findNodeId(XPathExpression xpathExpression);

    /**
     * This method is for the same use as {@link #findNodeId(XPathExpression)}, but since DOM
     * traversal is much faster than XPath evaluation, sometimes it is worth to implement manual
     * document traversing.
     *
     * @param finder {@link INodeIdResolver} to use against the layout DOM.
     * @return The ID of the resolved node, null if there is no match.
     */
    String findNodeId(INodeIdResolver finder);

    /**
     * Returns an id of the root node.
     *
     * @return a <code>String</code> value
     */
    String getRootId();

    /**
     * @return The composite {@link IStylesheetUserPreferences} based on the preferences from the
     *     incorporated fragments
     */
    IStylesheetUserPreferences getDistributedStructureStylesheetUserPreferences();

    /**
     * @return The composite {@link IStylesheetUserPreferences} based on the preferences from the
     *     incorporated fragments
     */
    IStylesheetUserPreferences getDistributedThemeStylesheetUserPreferences();

}
