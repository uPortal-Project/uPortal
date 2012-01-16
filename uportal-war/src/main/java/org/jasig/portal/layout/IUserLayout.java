/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.layout;

import java.util.Enumeration;
import java.util.Set;

import javax.xml.xpath.XPathExpression;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.w3c.dom.Document;

/**
 * An interface representing the user layout.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */
public interface IUserLayout {
    /**
     * The name to use for the root node of the layout. This should be used with
     * regard to rendering position within the layout tree.
     */
    public static final String ROOT_NODE_NAME = "root";


    /**
     * Writes user layout content (with appropriate markings) into
     * a <code>Document</code> object
     *
     * @param document a <code>Document</code> value
     * @exception PortalException if an error occurs
     */
    public void writeTo(Document document) throws PortalException;

    /**
     * Writes subtree of a user layout (with appropriate markings) defined by a particular node into
     * a <code>Document</code>
     *
     * @param nodeId a <code>String</code> a node determining a user layout subtree.
     * @param document a <code>Document</code> object
     * @exception PortalException if an error occurs
     */
    public void writeTo(String nodeId, Document document) throws PortalException;

    /**
     * Obtain a description of a node (channel or a folder) in a given user layout.
     *
     * @param nodeId a <code>String</code> channel subscribe id or folder id.
     * @return an <code>UserLayoutNodeDescription</code> value
     * @exception PortalException if an error occurs
     */
    public IUserLayoutNodeDescription getNodeDescription(String nodeId) throws PortalException;

    /**
     * Returns an Id of a parent user layout node.
     * The user layout root node always has ID="root"
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> value
     * @exception PortalException if an error occurs
     */
    public String getParentId(String nodeId) throws PortalException;

    /**
     * Returns a list of child node Ids for a given node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>Enumeration</code> of <code>String</code> child node Ids.
     * @exception PortalException if an error occurs
     */
    public Enumeration getChildIds(String nodeId) throws PortalException;

    /**
     * Determine an Id of a next sibling node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> Id value of a next sibling node, or <code>null</code> if this is the last sibling.
     * @exception PortalException if an error occurs
     */
    public String getNextSiblingId(String nodeId) throws PortalException;


    /**
     * Determine an Id of a previous sibling node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> Id value of a previous sibling node, or <code>null</code> if this is the first sibling.
     * @exception PortalException if an error occurs
     */
    public String getPreviousSiblingId(String nodeId) throws PortalException;

    /**
     * Return a cache key, uniqly corresponding to the composition and the structure of the user layout.
     *
     * @return a <code>String</code> value
     * @exception PortalException if an error occurs
     */
    public String getCacheKey() throws PortalException;

    /**
     * Returns a layout Id associated with this manager/
     *
     * @return an <code>String</code> layout Id value;
     */
    public String getId();

    /**
     * Returns a node id associated with the supplied functional name.
     *
     * @param fname  the functional name to lookup
     * @return a <code>String</code> subscription id
     * @exception PortalException if an error occurs
     */
    public String getNodeId(String fname) throws PortalException;
    
    /**
     * Returns a node id as resolved by the supplied {@link XPathExpression}
     * 
     * @param xpathExpression The expression to execute against the layout DOM
     * @return The ID of the resolved node, null if there is no match
     */
    public String findNodeId(XPathExpression xpathExpression);
    
    /**
     * This method is for the same use as {@link #findNodeId(XPathExpression)}, but since DOM traversal is much faster
     * than XPath evaluation, sometimes it is worth to implement manual document traversing.
     * 
     * @param finder {@link INodeIdResolver} to use against the layout DOM.
     * @return The ID of the resolved node, null if there is no match.
     */
    public String findNodeId(INodeIdResolver finder);

     /**
     * Returns a list of node Ids in the layout.
     *
     * @return a <code>Enumeration</code> of node Ids
     * @exception PortalException if an error occurs
     */
    public Enumeration getNodeIds() throws PortalException;

    /**
     * Returns an id of the root node.
     *
     * @return a <code>String</code> value
     */
    public String getRootId();

    /**
     * @return The names of all of the fragments incorporated into the layout
     */
    public Set<String> getFragmentNames();

    /**
     * @return The composite {@link IStylesheetUserPreferences} based on the preferences from the incorporated fragments
     */
    public IStylesheetUserPreferences getDistributedStructureStylesheetUserPreferences();

    /**
     * @return The composite {@link IStylesheetUserPreferences} based on the preferences from the incorporated fragments
     */
    public IStylesheetUserPreferences getDistributedThemeStylesheetUserPreferences();
}
